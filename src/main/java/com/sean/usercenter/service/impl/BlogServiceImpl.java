package com.sean.usercenter.service.impl;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.google.gson.Gson;
import com.sean.usercenter.common.ErrorCode;
import com.sean.usercenter.common.ResultUtils;
import com.sean.usercenter.exception.BusinessException;
import com.sean.usercenter.mapper.BlogMapper;
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
import com.sun.org.apache.xerces.internal.xs.StringList;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 24395
 * @description 针对表【blog(博客表)】的数据库操作Service实现
 * @createDate 2024-09-12 16:47:58
 */
@Service
public class BlogServiceImpl extends ServiceImpl<BlogMapper, Blog>
        implements BlogService {

    @Resource
    private UserService userService;
    @Resource
    private UserBlogService userBlogService;


    @Override
    public boolean addBlog(AddBlogRequest addBlogRequest, User loginUser) {
        //标题 ，标题不可以超过 31个字 但是要大于 5 个字
        String tittle = addBlogRequest.getTittle();
        if (StringUtils.isBlank(tittle) || tittle.length() > 31 || tittle.length() < 5) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "标题太长");
        }
        //正文，标题不可以超过 1000个字
        String text = addBlogRequest.getText();
        if (StringUtils.isBlank(text) || text.length() > 1000) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "正文太长");
        }
        long currentUserId = loginUser.getId();
        Blog blog = new Blog();
        blog.setCreateUserId(currentUserId);
        blog.setTittle(tittle);
        blog.setText(text);
        blog.setTopicTags(addBlogRequest.getTopicTags());
        boolean result = this.save(blog);
        return result;
    }

    @Override
    public List<BlogVo> listBlog() {
        List<Blog> blogList = this.list();
        List<BlogVo> blogs = new ArrayList<>();
        if (blogList == null) {
            return blogs;
        } else {
            blogs = this.getBlogVoList(blogList);
        }
        return blogs;
    }

    @Override
    public List<BlogVo> searchBlog(String searchText,Long userId) {
        if (StringUtils.isBlank(searchText)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "搜索内容不可以为空");
        }
        QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
        if (userId != null) {
            blogQueryWrapper.eq("createUserId", userId);
        }

        blogQueryWrapper.like("tittle", searchText);
        List<Blog> blogList = this.list(blogQueryWrapper);
        if (blogList.isEmpty()) {
            return new ArrayList<>();
        }
        List<BlogVo> blogVoList = this.getBlogVoList(blogList);
        if (blogVoList == null) {
            return new ArrayList<>();
        }
        return blogVoList;
    }

    @Override
    public long ThumbsUpBlog(Long blogId, long userId) {
        if (blogId == null) {
            throw new RuntimeException("该博客不存在");
        }
        //判断该用户是否已经点过赞
        QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
        blogQueryWrapper.eq("blogId", blogId);
        Blog blog = this.getOne(blogQueryWrapper);
        String startIds = blog.getStartIds();
        Set<Long> startIdsSet = StringUtil.stringJsonListToLongSet(startIds);
        if (startIdsSet == null) {
            startIdsSet = new HashSet<>();
        }
        if (startIdsSet.contains(userId)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "您已经点赞过了");
        }

        //将这个 用户id 加入到点赞列表中
        startIdsSet.add(userId);
        long startNum = (long) startIdsSet.size();

        //更新到数据库
        Gson gson = new Gson();
        String startIdsJson = gson.toJson(startIdsSet);
        Blog updateBlog = new Blog();
        updateBlog.setStartIds(startIdsJson);
        boolean result = this.update(updateBlog,blogQueryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "点赞失败");
        }
        return startNum;
    }

    @Override
    public List<RemarkVo> getRemarkList(Long blogId) {
        if (blogId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "该博客不存在");
        }
        QueryWrapper<UserBlog> userBlogQueryWrapper = new QueryWrapper<>();
        userBlogQueryWrapper.eq("blogId", blogId);
        List<UserBlog> userBlogList = userBlogService.list(userBlogQueryWrapper);
        if (userBlogList.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<RemarkVo> remarkVos = new ArrayList<>();
        userBlogList.forEach(userBlog -> {
            RemarkVo remarkVo = new RemarkVo();
            String remark = userBlog.getRemark();
            Long id = userBlog.getUserId();
            User user = userService.getById(id);
            remarkVo.setRemarkUser(user);
            remarkVo.setRemark(remark);
            String remarkNumStr = userBlog.getRemarkNum();
            Set<Long> remarkNumSet = StringUtil.stringJsonListToLongSet(remarkNumStr);
            int remarkNum = remarkNumSet.size();
            remarkVo.setRemarkNum((long) remarkNum);
            remarkVos.add(remarkVo);
        });
        //跟新评论数量
        QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
        blogQueryWrapper.eq("blogId", blogId);
        Blog blog = new Blog();
        blog.setRemarkNum((long) remarkVos.size());
        this.update(blog, blogQueryWrapper);
        return remarkVos;
    }

    @Override
    public String addRemark(AddRemarkRequest addRemarkRequest, User loginUser) {
        if (addRemarkRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "博客不存在");
        }
        Long blogId = addRemarkRequest.getBlogId();
        String remark = addRemarkRequest.getRemark();
        if (StringUtils.isBlank(remark) || blogId == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "评论不可为空");
        }
        // 评论不可以超过 100个字
        if (remark.length() > 100) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评论字数过长");
        }
        // 限制不可以进行评论重复
        QueryWrapper<UserBlog> userBlogQueryWrapper = new QueryWrapper<>();
        userBlogQueryWrapper.eq("blogId", blogId);
        userBlogQueryWrapper.eq("userId", loginUser.getId());
        userBlogQueryWrapper.eq("remark", remark);
        long number = userBlogService.count(userBlogQueryWrapper);
        if (number > 0) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "评论重复");
        }
        // 添加评论 到 数据库
        UserBlog userBlog = new UserBlog();
        userBlog.setBlogId(blogId);
        userBlog.setUserId(loginUser.getId());
        userBlog.setRemark(remark);
        boolean result = userBlogService.save(userBlog);

        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "评论失败");
        }
        return remark;
    }

    @Override
    public long ThumbsUpRemark(ThumbsUpRemarkRequest thumbsUpRemarkRequest, User loginUser) {
        if (thumbsUpRemarkRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "评论不存在");
        }
        //当前用户id
        long userId = loginUser.getId();
        //创建评论用户id
        // 这里的 问题;
        Long createRemarkUserId = thumbsUpRemarkRequest.getUserId();
        Long blogId = thumbsUpRemarkRequest.getBlogId();
        String remark = thumbsUpRemarkRequest.getRemark();
        if (createRemarkUserId == null && blogId == null && StringUtils.isBlank(remark)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "评论不存在");
        }
        // 判断该用户是否已经点过赞
        QueryWrapper<UserBlog> userBlogQueryWrapper = new QueryWrapper<>();
        userBlogQueryWrapper.eq("userId", createRemarkUserId);
        userBlogQueryWrapper.eq("blogId", blogId);
        userBlogQueryWrapper.eq("remark", remark);
        List<String> remarkNumStringList = userBlogService.list(userBlogQueryWrapper)
                .stream()
                .filter(Objects::nonNull)  // 过滤掉 UserBlog 中的 null 值
                .map(UserBlog::getRemarkNum)
                .filter(Objects::nonNull)  // 过滤掉 remarkNum 中的 null 值
                .filter(str -> !str.isEmpty())  // 这里添加了过滤条件[过滤到为空的字符串]
                .map(str -> str.replaceAll("^\\[|\\]$", ""))  // 移除字符串两端的方括号
                .collect(Collectors.toList());

        List<Long> remarkNumList = remarkNumStringList
                .stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());


        if (remarkNumList.contains(userId)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "您已经点赞过该评论了");
        }
        // 更新点赞数
        remarkNumList.add(userId);
        long remarkNum = (long) remarkNumList.size();
        String remarkNumJsonStr = JSONUtil.toJsonStr(remarkNumList);
        UserBlog userBlog = new UserBlog();
        userBlog.setRemarkNum(remarkNumJsonStr);
        userBlogService.update(userBlog,userBlogQueryWrapper);
        userBlogService.update();
        return remarkNum;
    }

    @Override
    public List<BlogVo> listMyBlog(User loginUser) {
        long userId = loginUser.getId();
        QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
        blogQueryWrapper.eq("createUserId", userId);
        List<Blog> blogList = this.list(blogQueryWrapper);
        List<BlogVo> myBlogVoList = getBlogVoList(blogList);
        return myBlogVoList;
    }

    @Override
    public boolean updateBlog(UpdateBlogRequest updateBlogRequest) {
        if (updateBlogRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        Long blogId = updateBlogRequest.getBlogId();
        // 不可 null
        String tittle = updateBlogRequest.getTittle();
        // 不可 null
        String text = updateBlogRequest.getText();
        String topicTags = updateBlogRequest.getTopicTags();
        if (blogId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
        blogQueryWrapper.eq("blogId", blogId);
        Blog blog = new Blog();
        blog.setTittle(tittle);
        blog.setText(text);
        blog.setTopicTags(topicTags);
        boolean result = this.update(blog, blogQueryWrapper);
        return result;
    }

    /**
     * 封装blogVoList
     * @param blogList
     * @return
     */
    public List<BlogVo> getBlogVoList(List<Blog> blogList) {
        if (blogList != null) {
            List<BlogVo> blogVoList = new ArrayList<>();

            blogList.forEach(blog -> {
                String startIds = blog.getStartIds();
                int startNum = StringUtil.stringJsonListToLongSet(startIds).size();
                BlogVo blogVo = new BlogVo();
                BeanUtils.copyProperties(blog, blogVo);
                User user = userService.getById(blog.getCreateUserId());
                blogVo.setCreateUser(user);
                blogVo.setStartNum((long) startNum);
                blogVoList.add(blogVo);
            });
            return blogVoList;
        }
        return new ArrayList<>();
    }
}




