package com.sean.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.usercenter.common.BaseResponse;
import com.sean.usercenter.common.ErrorCode;
import com.sean.usercenter.common.PageRequest;
import com.sean.usercenter.common.ResultUtils;
import com.sean.usercenter.exception.BusinessException;
import com.sean.usercenter.model.DTO.Friends;
import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.model.Vo.RecordsVo;
import com.sean.usercenter.model.Vo.UserVo;
import com.sean.usercenter.model.request.friendRequest.FriendAddRequest;
import com.sean.usercenter.model.request.friendRequest.FriendRequest;
import com.sean.usercenter.model.request.recordsRequest.RecordsListRequest;
import com.sean.usercenter.service.FriendsService;
import com.sean.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/8/26
 **/
@RestController
@Slf4j
@RequestMapping("/friends")
public class FriendsController {

    @Resource
    private FriendsService friendsService;

    @Resource
    private UserService userService;


    //这边改成 根据发送id 和 接收id 还有这个status 来判断是否为好友
    @PostMapping("/add")
    public BaseResponse<Boolean> addFriend(@RequestBody FriendAddRequest friendAddRequest, HttpServletRequest request) {
        if (friendAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = friendsService.addFriend(friendAddRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteFriends(@RequestBody FriendRequest friendRequest, HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = friendsService.deleteFriends(friendRequest, loginUser);
        return ResultUtils.success(result);
    }


    @GetMapping("/current")
    public BaseResponse<User> getCurrentFriend(@RequestParam(value = "friendId") Long friendId) {
        if (friendId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.eq("id", friendId);
        User user = userService.getOne(userQueryWrapper);
        return ResultUtils.success(user);
    }


    //变成根据前端传进进来的id，进行对应用户的查询
    @PostMapping("/list")
    public BaseResponse<List<User>> listFriends(HttpServletRequest request) {
        List<User> userList = friendsService.listFriends(request);
        return ResultUtils.success(userList);
    }

    // todo 还没有完成，有问题
    @PostMapping("/list/page")
    public Page<Friends> listFriendsByPage(@RequestBody PageRequest page, HttpServletRequest request) {
        if (page == null || request == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        //这里我不知道是否要限制 pageSize,pageNum
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        QueryWrapper<Friends> friendsQueryWrapper = new QueryWrapper<>();
        friendsQueryWrapper.eq("userId", loginUser.getId());
        Page<Friends> friendsPage = new Page<>();
        friendsPage.setCurrent(page.getPageNum());
        friendsPage.setSize(page.getPageSize());
        Page<Friends> resultPage = friendsService.page(friendsPage, friendsQueryWrapper);
        return resultPage;
    }

    @PostMapping("/get/records")
    public BaseResponse<List<RecordsVo>> getFriendsRecords(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<RecordsVo> userVoList = friendsService.getRecords(loginUser);
        return ResultUtils.success(userVoList);
    }

    @PostMapping("/agree/{fromId}")
    public BaseResponse<Boolean> agreeApply(@PathVariable(value = "fromId") Long fromId,HttpServletRequest request) {
        if (fromId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = friendsService.agreeApply(fromId,loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/get/records/number")
    public BaseResponse<Long> getFriendsRecordsNumber(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long number = friendsService.getRecordsNumber(loginUser);
        return ResultUtils.success(number);
    }

    @PostMapping("/cancel/apply/{fromId}")
    public BaseResponse<Boolean> cancelApply(@PathVariable("fromId") Long fromId , HttpServletRequest request) {
        if (fromId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = friendsService.cancelApply(fromId,loginUser);
        return ResultUtils.success(result);
    }

    //获取一个好友
    @GetMapping("/get/{friendId}")
    public BaseResponse<User> getOneFriend(@PathVariable Long friendId) {
        Friends friend = friendsService.getById(friendId);
        Long userId = friend.getUserId();
        User user = userService.getById(userId);
        return ResultUtils.success(user);
    }
}