package com.sean.usercenter.utils;

import com.google.gson.Gson;
import com.sean.usercenter.model.Vo.ChatVo.MessageVo;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/29
 * @description 消息工具类
 **/
public class MessageUtils {

    public static String getMessage(boolean isSystemMessage, String fromName , Object message) {
        MessageVo messageVo = new MessageVo();
        messageVo.setSystem(isSystemMessage);
        if (fromName != null) {
            messageVo.setFromId(fromName);
        }
        messageVo.setMessage(message);
        Gson gson = new Gson();
        return gson.toJson(messageVo);
    }

}
