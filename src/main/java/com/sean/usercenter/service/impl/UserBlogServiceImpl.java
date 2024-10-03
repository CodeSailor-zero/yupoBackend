package com.sean.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sean.usercenter.common.BaseResponse;
import com.sean.usercenter.common.ErrorCode;
import com.sean.usercenter.exception.BusinessException;
import com.sean.usercenter.mapper.BlogMapper;
import com.sean.usercenter.mapper.UserBlogMapper;
import com.sean.usercenter.model.DTO.Blog;
import com.sean.usercenter.model.DTO.UserBlog;
import com.sean.usercenter.model.Vo.blogVo.BlogVo;
import com.sean.usercenter.service.BlogService;
import com.sean.usercenter.service.UserBlogService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 24395
 * @description 针对表【user_blog(用户博客关系表)】的数据库操作Service实现
 * @createDate 2024-09-14 14:43:26
 */
@Service
public class UserBlogServiceImpl extends ServiceImpl<UserBlogMapper, UserBlog>
        implements UserBlogService {
//
//    @Resource
//    private BlogService blogService;
//
//
//
//    @Override
//    public Long ThumbsUpBlog(Long blogId, long userId) {
//        if (blogId == null) {
//            throw new RuntimeException("该博客不存在");
//        }
//        long count = 0;
//        // 点赞过后的用户无法再次点赞
//        QueryWrapper<UserBlog> userBlogQueryWrapper = new QueryWrapper<>();
//        userBlogQueryWrapper.eq("blogId", blogId);
//        userBlogQueryWrapper.eq("userId", userId);
//        userBlogQueryWrapper.eq("blogStatus", 1);
//        long number = this.count(userBlogQueryWrapper);
//        if (number > 0) {
//            return 0L;
//        }
//
//        UserBlog userBlog = new UserBlog();
//        userBlog.setBlogId(blogId);
//        userBlog.setUserId(userId);
//        userBlog.setBlogStatus(1);
//        boolean result = this.save(userBlog);
//        if (!result) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "点赞失败");
//        }
//        // 更新博客点赞数
//        userBlogQueryWrapper = new QueryWrapper<>();
//        userBlogQueryWrapper.eq("blogId", blogId);
//        userBlogQueryWrapper.eq("blogStatus", 1);
//        count = this.count(userBlogQueryWrapper);
//
//        QueryWrapper<Blog> blogQueryWrapper = new QueryWrapper<>();
//        blogQueryWrapper.eq("blogId", blogId);
//        Blog blog = new Blog();
//        blog.setStartNum(count);
//        blogService.update(blog, blogQueryWrapper);
//        return count;
//    }


}




