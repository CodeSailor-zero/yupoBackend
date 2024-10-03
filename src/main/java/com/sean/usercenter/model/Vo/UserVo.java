package com.sean.usercenter.model.Vo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户包装类（脱敏）
 *
 */
@TableName(value ="user")
@Data
public class UserVo implements Serializable {
    /**
     * id
     */
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
     * 性别
     */
    private Integer gender;


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
     * 好友申请的描述
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;



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


    private static final long serialVersionUID = 1L;

}