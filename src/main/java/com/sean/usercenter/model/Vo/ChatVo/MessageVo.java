package com.sean.usercenter.model.Vo.ChatVo;

import com.sean.usercenter.model.DTO.User;
import lombok.Data;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/29
 * 发送到前端的消息封装
 **/
@Data
public class MessageVo {
    // 是否是系统消息
    private boolean isSystem;
    // 发送者id
    private String fromId;
    // 消息内容 如果是系统消息就是 数组
    private Object message;
}
