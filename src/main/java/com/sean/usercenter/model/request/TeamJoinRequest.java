package com.sean.usercenter.model.request;


import lombok.Data;

import java.io.Serializable;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/8/5
 * 用户加入队伍请求体
 **/
@Data
public class TeamJoinRequest implements Serializable {

    private static final long serialVersionUID =  3191241716373120793L;

    /**
     * 队伍id
     */
    private Long teamId;

    /**
     * 密码
     */
    private String password;

}

