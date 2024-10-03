package com.sean.usercenter.model.request.BlogRequest;

import lombok.Data;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/21
 * @description: 点赞用户评论的Request
 **/
@Data
public class ThumbsUpRemarkRequest {
    //创建评论的用户id
    private Long userId;
    private Long blogId;
    private String remark;
}
