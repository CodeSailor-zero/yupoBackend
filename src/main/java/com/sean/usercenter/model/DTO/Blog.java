package com.sean.usercenter.model.DTO;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 博客表
 * @TableName blog
 */
@TableName(value ="blog")
@Data
public class Blog implements Serializable {
    /**
     * 博客文章Id
     */
    @TableId(type = IdType.AUTO)
    private Long blogId;

    /**
     * 创建人Id
     */
    private Long createUserId;

    /**
     * 标题
     */
    private String tittle;

    /**
     * 正文
     */
    private String text;

    /**
     * 话题标签 | json字符串
     */
    private String topicTags;

    /**
     * 评论数
     */
    private Long remarkNum;

    /**
     * 点赞用户id
     */
    private String startIds;

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