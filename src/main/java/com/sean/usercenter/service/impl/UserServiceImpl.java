package com.sean.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;


import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sean.usercenter.common.BaseResponse;
import com.sean.usercenter.common.ErrorCode;
import com.sean.usercenter.common.ResultUtils;
import com.sean.usercenter.exception.BusinessException;
import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.model.Vo.UserVo;
import com.sean.usercenter.service.UserService;
import com.sean.usercenter.mapper.UserMapper;
import com.sean.usercenter.utils.AlgorithmUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;


import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sean.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.sean.usercenter.contant.UserConstant.USER_LOGIN_STATE;
import static com.sean.usercenter.utils.Alias.PREFIX;

/**
 * 用户服务实现类
 *
 * @author 24395
 * &#064;description  针对表【user(用户)】的数据库操作Service实现
 * &#064;createDate  2024-03-08 19:13:28
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    /**
     * 盐值：混淆密码，用来给密码加密
     */
    private static final String SALT = "yupi";





    @Resource
    private UserMapper userMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 检验密码
     * @param userName      用户名
     * @return 新用户的id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String userName) {
        //1.检验
        //StringUtils是导入commons-lang3的工具类
        //验证用户账号、密码和检验密码是否为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "参数为空");
        }
        //检验账号的长度不小于4
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "用户账号过短");
        }
        //检验密码不小于8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "用户密码过短");
        }
        //用户名不为空，并且不能为 ""
        if (StringUtils.isBlank(userName)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "用户名过短");
        }


        //账户不包含特殊字符密码
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return -1;
        }
        //密码和检验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "用户账号过短");
        }


        //账号不能重复
        //需要从数据库中取数据，匹配
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        //queryWrapper.eq 匹配的SQL语句
        //select * from user where  userAccount（前面的，表中的） = userAccount（传进来的）
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "账号重复");
        }

        //2.密码加密
        //防止从接口获取密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setUsername(userName);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }

        return user.getId();
    }

    /**
     * 用户登入
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后用户的信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //1.检验
        //StringUtils是导入commons-lang3的工具类
        //验证用户账号、密码和检验密码是否为空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "参数为空");
        }
        //检验账号的长度不小于4
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "用户账号过短");
        }
        //检验密码不小于8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "用户密码过短");
        }

        //账户不包含特殊字符密码
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            return null;
        }

        //2.校验密码是否输入正确，要和数据库中的密文密码去对比
//        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());

        //将 前端传进来的密码加密 ，与数据库中的匹配
        userPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", userPassword);
        User user = userMapper.selectOne(queryWrapper);
        //如果用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "用户账号或密码错误");
        }

        //3.用户脱敏，防止数据库中的字段泄露
        User SafetyUser = this.getSafetyUser(user);

        //4.记录用户的登入态
        request.getSession().setAttribute(USER_LOGIN_STATE, SafetyUser);


        return SafetyUser;
    }


    /**
     * 用户脱敏
     *
     * @param OriginalUser
     * @return
     */
    @Override
    public User getSafetyUser(User OriginalUser) {
        User SafetyUser = new User();
        if (SafetyUser == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "用户为空");
        }
        SafetyUser.setId(OriginalUser.getId());
        SafetyUser.setUsername(OriginalUser.getUsername());
        SafetyUser.setUserAccount(OriginalUser.getUserAccount());
        SafetyUser.setAvatarUrl(OriginalUser.getAvatarUrl());
        SafetyUser.setGender(OriginalUser.getGender());
        SafetyUser.setPhone(OriginalUser.getPhone());
        SafetyUser.setEmail(OriginalUser.getEmail());
        SafetyUser.setPlanetCode(OriginalUser.getPlanetCode());
        SafetyUser.setUserRole(OriginalUser.getUserRole());
        SafetyUser.setUserStatus(OriginalUser.getUserStatus());
        SafetyUser.setCreateTime(OriginalUser.getCreateTime());
        SafetyUser.setTags(OriginalUser.getTags());
        return SafetyUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        HttpSession session = request.getSession();
        Boolean result = redisTemplate.delete("spring:userSession:sessions:" + session.getId());
        return result;
    }

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        //内存查询
        //1.先查询所有用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);
        Gson gson = new Gson();
        //2.在内存中判断是否包含要求的标签

        return userList.stream().filter(user -> {
            String tagsStr = user.getTags();
            if (StringUtils.isBlank(tagsStr)) {
                return false;
            }
            Set<String> tempTagNameSet = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
            }.getType());

            tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());


            //目前只能搜索一个标签。搜索两个及以上，会有错位的可能
            for (String tagName : tagNameList) {
                if (!tempTagNameSet.contains(tagName)) {
                    return false;
                }
            }
            //搜索全部的
//            if(!tempTagNameSet.containsAll(tagNameList)){
//                return false;
//            }
            return true;
        }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    /**
     * 跟新用户信息
     *
     * @param user
     * @return
     */
    @Override
    public int updateUser(User user, User userLogin) {
        long id = user.getId();
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        //判断是否为管理员或自己本身
        //是管理员的情况，可以更新所有的用户
        //不是管理员的情况，只能更新自己的信息
        if (!isAdmin(userLogin) && id != userLogin.getId()) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User oldUser = userMapper.selectById(id);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }

    /**
     * 获取当前用户的信息
     *
     * @param request
     * @return User
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return loginUser;
    }

    /**
     * 根据标签搜索用户 (SQL查询版)
     *
     * @param tagNameList 用户要拥有的标签
     * @return
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        // SQL语句查询
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        //拼接 and 查询
//        //like '%Java%'  and  '%Python%'
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());
    }


    /**
     * 推荐用户
     *
     * @param pageSize
     * @param pageNum
     * @param request
     * @return BaseResponse
     */
    @Override
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        User loginUser = getLoginUser(request);
        long userId = loginUser.getId();
        String redisKey = PREFIX + ":recommend:" + userId;
        //如果有缓存直接读redis
        Page<User> userPage = (Page<User>) operations.get(redisKey);
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }
        //没有缓存，查数据库
        userPage = page(new Page<>(pageNum, pageSize), userQueryWrapper);
        //写入缓存
        try {
            operations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("redis set key error", e);
        }
        return ResultUtils.success(userPage);
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list();
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        ArrayList<Pair<User, Long>> list = new ArrayList<>();
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            //无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            if (CollectionUtils.isEmpty(userTagList) || CollectionUtils.isEmpty(tagList)) {
                return new ArrayList<>();
            }
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        //按编辑距离进行从小到大的排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(this::getSafetyUser)
                .collect(Collectors.groupingBy(User::getId));
        ArrayList<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }


    /**
     * 判断是否为管理员
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        Object userObject = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObject;
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    /**
     * 判断是否为管理员
     *
     * @param user
     * @return
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }


}




