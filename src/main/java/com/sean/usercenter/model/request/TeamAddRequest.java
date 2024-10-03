package com.sean.usercenter.model.request;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/8/5
 * 添加队伍请求体
 **/
@Data
public class TeamAddRequest implements Serializable {
    private static final long serialVersionUID =  3191241716373120793L;


    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 最大人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 队伍状态
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 密码
     */
    private String password;

}

