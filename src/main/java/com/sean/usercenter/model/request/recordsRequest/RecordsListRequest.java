package com.sean.usercenter.model.request.recordsRequest;

import lombok.Data;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/10
 **/
@Data
public class RecordsListRequest {
    //发送好友申请id
    private Long userId;

    //接受者id
    private Long friendId;

}
