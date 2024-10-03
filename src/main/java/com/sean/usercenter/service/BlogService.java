package com.sean.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sean.usercenter.model.DTO.Blog;
import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.model.Vo.blogVo.BlogVo;
import com.sean.usercenter.model.Vo.blogVo.RemarkVo;
import com.sean.usercenter.model.request.BlogRequest.AddBlogRequest;
import com.sean.usercenter.model.request.BlogRequest.AddRemarkRequest;
import com.sean.usercenter.model.request.BlogRequest.ThumbsUpRemarkRequest;
import com.sean.usercenter.model.request.BlogRequest.UpdateBlogRequest;

import java.util.List;


/**
 * @author 24395
 * @description 针对表【blog(博客表)】的数据库操作Service
 * @createDate 2024-09-12 16:47:58
 */
public interface BlogService extends IService<Blog> {

    /**
     * 添加博客
     *
     * @param addBlogRequest
     * @param loginUser
     * @return
     */
    boolean addBlog(AddBlogRequest addBlogRequest, User loginUser);


    /**
     * 获取博客列表
     * @return  List<BlogVo>
     */
    List<BlogVo> listBlog();

    /**
     * 根据标题搜索blog功能
     * @param searchText
     * @param userId
     * @return
     */
    List<BlogVo> searchBlog(String searchText,Long userId);

    /**
     * 点赞博客功能
     * @param blogId
     * @param userId
     * @return long
     */
    long ThumbsUpBlog(Long blogId, long userId);

    /**
     * 获取当前博客的评论 和 评论人的信息
     * @param blogId
     */
    List<RemarkVo> getRemarkList(Long blogId);

    /**
     * 发布评论
     * @param addRemarkRequest
     * @param loginUser
     * @return
     */
    String addRemark(AddRemarkRequest addRemarkRequest, User loginUser);

    /**
     * 点赞评论
     * @param thumbsUpRemarkRequest
     * @param loginUser
     * @return long [点赞评论数]
     */
    long ThumbsUpRemark(ThumbsUpRemarkRequest thumbsUpRemarkRequest, User loginUser);

    /**
     * 获取当前用户的博客列表
     * @param loginUser
     * @return List<BlogVo>
     */
    List<BlogVo> listMyBlog(User loginUser);

    /**
     * 更新当前用户自己的博客
     * @param updateBlogRequest
     * @return
     */
    boolean updateBlog(UpdateBlogRequest updateBlogRequest);
}
