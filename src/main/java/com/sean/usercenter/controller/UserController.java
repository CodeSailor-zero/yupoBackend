package com.sean.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.usercenter.common.BaseResponse;
import com.sean.usercenter.common.ErrorCode;
import com.sean.usercenter.common.ResultUtils;
import com.sean.usercenter.exception.BusinessException;
import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.model.request.UserLoginRequest;
import com.sean.usercenter.model.request.UserRegisterRequest;
import com.sean.usercenter.service.FriendsService;
import com.sean.usercenter.service.UserService;
import com.sean.usercenter.service.UserTeamService;
import com.sean.usercenter.utils.OSSutils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.stream.Collectors;


import static com.sean.usercenter.contant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author yupi
 */
@Slf4j
@RestController
@RequestMapping("/user")
//@CrossOrigin(origins = "http://localhost:3000",allowCredentials = "true")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private OSSutils ossutils;

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private FriendsService friendsService;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String userName = userRegisterRequest.getUserName();
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, userName)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        long result = userService.userRegister(userAccount, userPassword, checkPassword,userName);
        return ResultUtils.success(result);
    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }


    //72FB06D7CF95A2D0D93668392AFF2EBB
    @GetMapping("/current")
    public BaseResponse<User> getUserCurrent(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        Long userId = currentUser.getId();
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }


    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        boolean result = userService.userLogout(request);

        return ResultUtils.success(result);
    }


    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        //校验用户权限
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            userQueryWrapper.like("username", username);
        }
        List<User> list = userService.list(userQueryWrapper);
        List<User> result = list.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    // todo 待完善,博客也需要删除 【不打算做】
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody Integer id, HttpServletRequest request) {
        //校验用户权限
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        boolean result = userService.removeById(id);
        if (result) {
            //删除 用户关系表中数据
            friendsService.removeById(id);
            //删除 用户队伍关系表中数据
            userTeamService.removeById(id);
        }
        return ResultUtils.success(result);
    }

    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        //1.检验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        // todo 朴充校验，如果用户没有传任何要更新的值，就直接报镐，不用执行任何更新语句

        User loginUser = userService.getLoginUser(request);

        //2.检验是否有权限
        //3.更新用户信息
        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);
    }

    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        return userService.recommendUsers(pageSize, pageNum, request);
    }

    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num < 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num, loginUser));
    }

    /**
     * 上传头像功能
     * @param file
     * @param request
     * @return
     */
    @PostMapping("/uploadAvatar")
    public BaseResponse<User> updateUserAvatarById(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        // 调用封装的OSS工具类将文件上传到OSS
        String url = null;
        try {
            url = ossutils.uploadFileToOSS(file);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "上传头像失败");
        }
        // 获取当前用户
        User loginUser = userService.getLoginUser(request);
        // 更新用户头像
        loginUser.setAvatarUrl(url);
        boolean result = userService.updateById(loginUser);
        // 将更新完的用户信息返回
        User user = userService.getById(loginUser.getId());
        // 对用户信息进行脱敏然后返回
        return ResultUtils.success(userService.getSafetyUser(user));
    }


}
