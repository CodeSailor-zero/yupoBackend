package com.sean.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sean.usercenter.model.DTO.Friends;
import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.model.Vo.RecordsVo;
import com.sean.usercenter.model.Vo.UserVo;
import com.sean.usercenter.model.request.friendRequest.FriendAddRequest;
import com.sean.usercenter.model.request.friendRequest.FriendRequest;
import com.sean.usercenter.model.request.recordsRequest.RecordsListRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
* @author 24395
* @description 针对表【friends(用户关系表)】的数据库操作Service
* @createDate 2024-08-26 18:44:17
*/
public interface FriendsService extends IService<Friends> {

    /**
     * 添加好友
     * @param friendAddRequest
     * @param loginUser
     * @return boolean
     */
    boolean addFriend(FriendAddRequest friendAddRequest, User loginUser);

    /**
     * 删除好友
     * @param friendRequest
     * @param loginUser
     * @return boolean
     */
    boolean deleteFriends(FriendRequest friendRequest, User loginUser);

    /**
     * 获取好友列表
     * @param request
     * @return List<User>
     */
    List<User> listFriends(HttpServletRequest request);

    /**
     * 获取我申请记录列表
     * @param loginUser
     * @return
     */
    List<RecordsVo> getRecords(User loginUser);


    /**
     * 获取我申请记录的条数
     * @param loginUser
     * @return Long
     */
   Long getRecordsNumber(User loginUser);

    /**
     *  同意好友申请
     * @param fromId
     * @param loginUser
     * @return boolean
     */
    boolean agreeApply(Long fromId,User loginUser);

    /**
     * 拒绝好友申请
     * @param fromId
     * @param loginUser
     * @return
     */
    boolean cancelApply(Long fromId, User loginUser);
}
