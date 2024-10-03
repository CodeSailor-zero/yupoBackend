package com.sean.usercenter.model.request.BlogRequest;

import lombok.Data;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/12
 * @description: 更新博客请求体
 **/
@Data
public class UpdateBlogRequest {
        /**
         * 博客id
         */
        private Long blogId;

        /**
         * 标题
         */
        private String tittle;

        /**
         * 正文
         */
        private String text;

        /**
         * 话题标签 | json字符串
         */
        private String topicTags;

}
