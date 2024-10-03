package com.sean.usercenter.model.DTO;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户博客关系表
 * @TableName user_blog
 */
@TableName(value ="user_blog")
@Data
public class UserBlog implements Serializable {

    /**
     * 对 blog 做出操作的用户 Id
     */
    private Long userId;

    /**
     * 博客id
     */
    private Long blogId;

    /**
     * 评论
     */
    private String remark;

    /**
     *  评论点赞用户id
     */
    private String remarkNum;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 逻辑删除 0 - 不删除，1 - 删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}