package com.sean.usercenter.model.request.friendRequest;

import lombok.Data;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/8
 **/
@Data
public class FriendAddRequest {
    // 接受申请的id
    private Long receivedId;

    //好友申请备注的信息
    private String remark;

}
