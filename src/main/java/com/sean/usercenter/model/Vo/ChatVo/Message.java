package com.sean.usercenter.model.Vo.ChatVo;

import lombok.Data;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/29
 * 接受前端的发送消息封装
 **/
@Data
public class Message {
    // 接受者名字
    private String toName;
    // 消息内容 如果是系统消息就是 数组
    private String message;
}
