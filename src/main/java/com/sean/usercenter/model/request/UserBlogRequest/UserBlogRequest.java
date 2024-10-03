package com.sean.usercenter.model.request.UserBlogRequest;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.util.Date;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/14
 **/
@Data
public class UserBlogRequest {

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
     *  0 - 未点赞 ， 1 - 已点赞
     */
    private Long remarkStatus;

    /**
     *  0 - 未点赞 ， 1 - 已点赞
     */
    private Long blogStatus;

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
