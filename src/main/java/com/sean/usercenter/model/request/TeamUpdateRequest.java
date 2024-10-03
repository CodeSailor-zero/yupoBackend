package com.sean.usercenter.model.request;


import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/8/5
 * 更新队伍请求体
 **/
@Data
public class TeamUpdateRequest implements Serializable {

    private static final long serialVersionUID =  3191241716373120793L;

    /**
     * id
     */
    private Long id;

    /**
     * 队伍名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;


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

