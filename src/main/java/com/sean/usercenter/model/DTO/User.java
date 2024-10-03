package com.sean.usercenter.model.DTO;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;


import java.io.Serializable;
import java.util.Date;

/**
 * 用户
 * @TableName user
 */
@TableName(value ="user")
@Data
public class User implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private long id;

    /**
     * 用户昵称
     */
    private String username;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 用户头像
     */
    private String avatarUrl;

    /**
     * 性别  0 -》 女  1 -》 男
     */
    private Integer gender;

    /**
     * 
     */
    private String userPassword;

    /**
     * 电话
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 用户状态, 0 -> 正常
     */
    private Integer userStatus;

    /**
     * 添加的好友
     */
    private String userIds;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    /**
     * 用户角色 0 - 普通用户 1 - 管理员
     */
    private Integer userRole;


    /**
     * 星球编号
     */
    private String planetCode;

    /**
     * 用户标签
     */
    private String tags;


    @TableField(exist = false)
    public static final long serialVersionUID = 1L;

}