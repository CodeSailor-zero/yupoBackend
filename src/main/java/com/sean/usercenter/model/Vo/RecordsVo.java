package com.sean.usercenter.model.Vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/10
 **/
@Data
public class RecordsVo implements Serializable {
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
     * 0-未通过 1-已同意 2-已过期 3-已撤销
     */
    private Integer status;

    /**
     * 好友申请的描述
     */
    private String remark;


    /**
     * 用户标签
     */
    private String tags;


    private static final long serialVersionUID = 1L;
}
