package com.sean.usercenter.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sean.usercenter.model.DTO.Chat;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author 24395
* @description 针对表【chat(聊天消息表)】的数据库操作Mapper
* @createDate 2024-09-23 19:48:00
* @Entity generator.domain.Chat
*/
public interface ChatMapper extends BaseMapper<Chat> {
    /**
     * 获取和好友最后一条的聊天消息
     * @param userId
     * @param friendIdList
     * @return
     */
    List<Chat> getLastPrivateChatMessages(@Param("userId") Long userId, @Param("friendIdList") List<Long> friendIdList);
}




