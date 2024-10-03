package com.sean.usercenter.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.sean.usercenter.common.BaseResponse;
import com.sean.usercenter.model.DTO.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author Sean
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2024-03-08 19:13:28
*/
public interface UserService extends IService<User> {



    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 检验密码
     * @param userName 用户名
     * @return 用户ID
     */
    long userRegister(String userAccount,String userPassword,String checkPassword,String userName);

    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    User getSafetyUser(User OriginalUser);

    /**
     * 用户注销
     * @param request
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList 用户要拥有的标签
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 跟新用户信息
     * @return int
     */
    int updateUser(User user,User userLogin);

    /**
     * 获取当前登录用户
     * @return User
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 判断是否为管理员
     * @return boolean
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 判断是否为管理员
     * @return boolean
     */
    boolean isAdmin(User user);
    BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum , HttpServletRequest request);


    /**
     * 匹配用户
     *
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);

}
