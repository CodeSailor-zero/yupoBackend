package com.sean.usercenter.model.Vo.blogVo;

import com.sean.usercenter.model.DTO.User;
import lombok.Data;

import java.util.Date;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/13
 **/
@Data
public class BlogVo {
    /**
     * 博客文章Id
     */
    private Long blogId;

    /**
     * 创建人
     */
    private User createUser;


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
     * 点赞数
     */
    private Long startNum;
    /**
     * 更新时间
     */
    private Date createTime;


}
