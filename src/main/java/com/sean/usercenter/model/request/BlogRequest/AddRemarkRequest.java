package com.sean.usercenter.model.request.BlogRequest;

import lombok.Data;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/19
 **/
@Data
public class AddRemarkRequest {
    //操作的博客id
    private Long blogId;
    //评论内容
    private String remark;
}
