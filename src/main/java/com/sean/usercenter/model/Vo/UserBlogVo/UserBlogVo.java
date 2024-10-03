package com.sean.usercenter.model.Vo.UserBlogVo;


import com.sean.usercenter.model.DTO.User;
import lombok.Data;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/14
 **/
@Data
public class UserBlogVo {


    /**
     * 对 blog 做出操作的用户
     */
    private User operateUser;

    /**
     * 博客id
     */
    private Long blogId;

    /**
     * 评论
     */
    private String remark;


    /**
     *  0 - 未点赞 ， 1 - 已点赞
     */
    private Integer remarkStatus;


    /**
     *  0 - 未点赞 ， 1 - 已点赞
     */
    private Integer blogStatus;

}
