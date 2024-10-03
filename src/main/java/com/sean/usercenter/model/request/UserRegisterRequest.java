package com.sean.usercenter.model.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @author yupi
 * @version 1.0
 *  用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {
    public static final long serialVersionUID = 319124171637312073L ;
    private String userName;
    private String userAccount;
    private String userPassword;
    private String checkPassword;
}
