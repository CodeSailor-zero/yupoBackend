package com.sean.usercenter.service.impl;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.sean.usercenter.common.ErrorCode;
import com.sean.usercenter.exception.BusinessException;
import com.sean.usercenter.mapper.FriendsMapper;
import com.sean.usercenter.model.DTO.Friends;
import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.model.Vo.RecordsVo;
import com.sean.usercenter.model.request.friendRequest.FriendAddRequest;
import com.sean.usercenter.model.request.friendRequest.FriendRequest;
import com.sean.usercenter.service.FriendsService;
import com.sean.usercenter.service.UserService;
import com.sean.usercenter.utils.StringUtil;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author 24395
 * @description 针对表【friends(用户关系表)】的数据库操作Service实现
 * @createDate 2024-08-26 18:44:17
 */
@Service
public class FriendsServiceImpl extends ServiceImpl<FriendsMapper, Friends>
        implements FriendsService {

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public boolean addFriend(FriendAddRequest friendAddRequest, User loginUser) {
        if (friendAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        String remark = friendAddRequest.getRemark();
        if (StringUtils.isNotBlank(remark) && remark.length() > 30) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "申请备注不可以超过30个字");
        }

        Long userId = loginUser.getId();
        Long receivedId = friendAddRequest.getReceivedId();
        if (receivedId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "添加失败");
        }
        //自己不能添加自己为好友
        if (userId.equals(receivedId)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "不能添加自己为好友");
        }
        RLock lock = redissonClient.getLock("youqu:apply");
        try {
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                QueryWrapper<Friends> friendsQueryWrapper = new QueryWrapper<>();
                friendsQueryWrapper.eq("userId", userId);
                friendsQueryWrapper.eq("friendId", receivedId);
                List<Friends> friendsList = this.list(friendsQueryWrapper);
                friendsList.forEach(friends -> {
                    if (friendsList.size() > 1 && friends.getStatus() == 0) {
                        throw new BusinessException(ErrorCode.PARAMETER_ERROR, "不能重复申请");
                    }
                });
                Friends friends = new Friends();
                friends.setUserId(userId);
                friends.setFriendId(receivedId);
                if (StringUtils.isNotBlank(remark)) {
                    friends.setRemark(remark);
                } else {
                    friends.setRemark("我是" + userService.getById(loginUser.getId()).getUsername());
                }
                return this.save(friends);
            }
        } catch (InterruptedException e) {
            log.error("joinTeam error", e);
            return false;
        }finally {
            if (lock.isHeldByCurrentThread()) {
                log.error("redisson lock error");
                lock.unlock();
            }
        }
        return false;
    }

    @Override
    public boolean deleteFriends(FriendRequest friendRequest, User loginUser) {
        if (friendRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "好友Id错误");
        }
        long userId = loginUser.getId();
        Long friendId = friendRequest.getFriendId();
        QueryWrapper<Friends> friendsQueryWrapper = new QueryWrapper<>();
        friendsQueryWrapper.eq("userId", userId);
        friendsQueryWrapper.eq("friendId", friendId);
        boolean result = this.remove(friendsQueryWrapper);
        return result;
    }


    @Override
    public List<User> listFriends(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        User user = userService.getById(loginUser.getId());
        String friendIds = user.getUserIds();
        if (StringUtils.isBlank(friendIds)) {
            return new ArrayList<>();
        }
        Set<Long> FriendIdSet = StringUtil.stringJsonListToLongSet(friendIds);
        List<User> friendList = userService.listByIds(FriendIdSet);
        return friendList;
    }

    @Override
    public List<RecordsVo> getRecords(User loginUser) {
        QueryWrapper<Friends> friendsQueryWrapper = getFriendsQueryWrapper(loginUser);
        List<Friends> friendsList = this.list(friendsQueryWrapper);
        if (friendsList.isEmpty()) {
            return new ArrayList<>();
        }
        RecordsVo recordsVo = new RecordsVo();
        friendsList.forEach(friends -> {
            recordsVo.setRemark(friends.getRemark());
            recordsVo.setStatus(friends.getStatus());
        });
        List<Long> userIdList = friendsList
                .stream()
                .map(Friends::getUserId)
                .collect(Collectors.toList());
        if (userIdList.isEmpty()) {
            return new ArrayList<>();
        }
        List<User> userList = userService.listByIds(userIdList);

        ArrayList<RecordsVo> recordsVoArrayList = new ArrayList<>();
        userList.forEach(user -> {
            BeanUtils.copyProperties(user, recordsVo);
            recordsVoArrayList.add(recordsVo);
        });
        return recordsVoArrayList;
    }

    @Override
    public Long getRecordsNumber(User loginUser) {
        QueryWrapper<Friends> friendsQueryWrapper = getFriendsQueryWrapper(loginUser);
        return this.count(friendsQueryWrapper);
    }

    @Override
    public boolean agreeApply(Long fromId,User loginUser) {
        //发送申请的id
        if (fromId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //接受申请的id
        long receiveId = loginUser.getId();
        QueryWrapper<Friends> friendsQueryWrapper = new QueryWrapper<>();
        friendsQueryWrapper.eq("userId", fromId);
        friendsQueryWrapper.eq("friendId", receiveId);
        List<Friends> recordCount = this.list(friendsQueryWrapper);
        List<Friends> collect = recordCount.stream().filter(friend -> friend.getStatus() == 0).collect(Collectors.toList());
        // 条数小于1 就不能再同意
        if (collect.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "该申请不存在");
        }
        if (collect.size() > 1) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "操作有误,请重试");
        }
        AtomicBoolean flag = new AtomicBoolean(false);
        collect.forEach(friend -> {
            if (DateUtil.between(friend.getCreateTime(), new Date(), DateUnit.DAY) > 3 || friend.getStatus() == 2){
                throw new BusinessException(ErrorCode.PARAMETER_ERROR, "申请已过期");
            }
            // 1. 分别查询receiveId和fromId的用户，更改userIds中的数据
            User receiveUser = userService.getById(receiveId);
            User fromUser = userService.getById(fromId);
            Set<Long> receiveIds = StringUtil.stringJsonListToLongSet(receiveUser.getUserIds());
            Set<Long> fromIds = StringUtil.stringJsonListToLongSet(fromUser.getUserIds());

            receiveIds.add(receiveUser.getId());
            fromIds.add(fromUser.getId());

            Gson gson = new Gson();
            String receiveIdsString = gson.toJson(receiveIds);
            String fromIdsString = gson.toJson(fromIds);
            receiveUser.setUserIds(fromIdsString);
            fromUser.setUserIds(receiveIdsString);
            //2. 修改状态 0 -> 1
            friend.setStatus(1);
            flag.set(userService.updateById(receiveUser) && userService.updateById(fromUser) && this.updateById(friend));
        });
       return flag.get();
    }

    @Override
    public boolean cancelApply(Long fromId, User loginUser) {
        if (fromId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        long receiveId = loginUser.getId();
        QueryWrapper<Friends> friendsQueryWrapper = new QueryWrapper<>();
        friendsQueryWrapper.eq("userId", fromId);
        friendsQueryWrapper.eq("friendId", receiveId);
        Friends friends = new Friends();
        friends.setStatus(3);
        boolean result = this.update(friends, friendsQueryWrapper);
        return result;
    }

    /**
     * 获取我申请列表的条件
     * @param loginUser
     * @return QueryWrapper
     */
    private static QueryWrapper<Friends> getFriendsQueryWrapper(User loginUser) {
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //当前发送申请的id
        Long userId = loginUser.getId();
        QueryWrapper<Friends> friendsQueryWrapper = new QueryWrapper<>();
        friendsQueryWrapper.eq("friendId", userId);
        friendsQueryWrapper.eq("status", 0);
        return friendsQueryWrapper;
    }
}




