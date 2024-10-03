package com.sean.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sean.usercenter.common.BaseResponse;
import com.sean.usercenter.common.ErrorCode;
import com.sean.usercenter.common.ResultUtils;
import com.sean.usercenter.exception.BusinessException;
import com.sean.usercenter.model.DTO.Blog;
import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.model.DTO.UserBlog;
import com.sean.usercenter.model.Vo.blogVo.BlogVo;
import com.sean.usercenter.model.Vo.blogVo.RemarkVo;
import com.sean.usercenter.model.request.BlogRequest.AddBlogRequest;
import com.sean.usercenter.model.request.BlogRequest.AddRemarkRequest;
import com.sean.usercenter.model.request.BlogRequest.ThumbsUpRemarkRequest;
import com.sean.usercenter.model.request.BlogRequest.UpdateBlogRequest;
import com.sean.usercenter.service.BlogService;
import com.sean.usercenter.service.UserBlogService;
import com.sean.usercenter.service.UserService;
import com.sean.usercenter.utils.StringUtil;
import com.sun.org.apache.xpath.internal.operations.Bool;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/12
 **/
@RestController
@RequestMapping("/blog")
public class BlogController {

    @Resource
    private UserService userService;

    @Resource
    private BlogService blogService;

    @Resource
    private UserBlogService userBlogService;

    @PostMapping("/add")
    public BaseResponse<Boolean> addBlog(@RequestBody AddBlogRequest addBlogRequest, HttpServletRequest request) {
        if (addBlogRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = blogService.addBlog(addBlogRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/list")
    public BaseResponse<List<BlogVo>> listBlog() {
        List<BlogVo> blogList = blogService.listBlog();
        return ResultUtils.success(blogList);
    }

    @GetMapping("/get/{blogId}")
    public BaseResponse<BlogVo> getOneBlog(@PathVariable("blogId") Long blogId) {
        QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
        blogQueryWrapper.eq("blogId", blogId);
        Blog blog = blogService.getOne(blogQueryWrapper);
        BlogVo blogVo = new BlogVo();
        BeanUtils.copyProperties(blog, blogVo);
        User user = userService.getById(blog.getCreateUserId());
        blogVo.setCreateUser(user);
        return ResultUtils.success(blogVo);
    }

    @GetMapping("/search")
    public BaseResponse<List<BlogVo>> searchBlog(@RequestParam("searchText") String searchText,@RequestParam(required = false) Long userId) {
        if (StringUtils.isBlank(searchText)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR,"搜索内容不可以为空");
        }
        List<BlogVo> blogVoList = blogService.searchBlog(searchText,userId);
        return ResultUtils.success(blogVoList);
    }

    //点赞功能
    @PostMapping("/thumbsup/{blogId}")
    public BaseResponse<Long> ThumbsUpBlog(@PathVariable("blogId") Long blogId, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        long userId = loginUser.getId();
        if (blogId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "该博客不存在");
        }
        long startNum = blogService.ThumbsUpBlog(blogId, userId);
        return ResultUtils.success(startNum);
    }

    @PostMapping("/thumbsup/remark")
    public BaseResponse<Long> thumbsUpRemark(@RequestBody ThumbsUpRemarkRequest thumbsUpRemarkRequest, HttpServletRequest request) {
        if (thumbsUpRemarkRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long remarkNum = blogService.ThumbsUpRemark(thumbsUpRemarkRequest,loginUser);
        return ResultUtils.success(remarkNum);

    }

    @GetMapping("/get/remark/{blogId}")
    public BaseResponse<List<RemarkVo>> getRemarkList(@PathVariable("blogId") Long blogId) {
        if (blogId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        List<RemarkVo> remarkList = blogService.getRemarkList(blogId);
        return ResultUtils.success(remarkList);
    }

    @PostMapping("/remark")
    public BaseResponse<String> publishRemark(@RequestBody AddRemarkRequest addRemarkRequest, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (addRemarkRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR,"该博客不存在");
        }
        String remark = blogService.addRemark(addRemarkRequest,loginUser);
        return ResultUtils.success(remark);
    }

    @PostMapping("/get/my")
    public BaseResponse<List<BlogVo>> listMyBlog(HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        List<BlogVo> myBlogList = blogService.listMyBlog(loginUser);
        return ResultUtils.success(myBlogList);
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateBlog(@RequestBody UpdateBlogRequest updateBlogRequest) {
        if (updateBlogRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        boolean result = blogService.updateBlog(updateBlogRequest);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteBlog(@RequestParam(required = true) Long blogId)  {
        if (blogId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR,"博客不存在");
        }
        boolean result = blogService.removeById(blogId);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR,"删除失败");
        }
        QueryWrapper<UserBlog> userBlogQueryWrapper = new QueryWrapper<>();
        userBlogQueryWrapper.eq("blogId", blogId);
        userBlogService.remove(userBlogQueryWrapper);
        return ResultUtils.success(result);
    }


}
