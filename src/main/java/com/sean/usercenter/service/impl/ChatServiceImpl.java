package com.sean.usercenter.service.impl;


import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sean.usercenter.mapper.ChatMapper;
import com.sean.usercenter.model.DTO.Chat;
//import com.sean.usercenter.model.Vo.ChatVo.WebSocketVo;
import com.sean.usercenter.service.ChatService;

import org.springframework.stereotype.Service;


/**
 * @author qimu
 * @description 针对表【chat(聊天消息表)】的数据库操作Service实现
 * @createDate 2023-04-11 11:19:33
 */
@Service
public class ChatServiceImpl extends ServiceImpl<ChatMapper, Chat>
        implements ChatService {


}




