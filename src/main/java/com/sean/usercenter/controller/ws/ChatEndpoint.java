package com.sean.usercenter.controller.ws;

import com.google.gson.Gson;
import com.sean.usercenter.common.ErrorCode;
import com.sean.usercenter.config.GetHttpSessionConfig;
import com.sean.usercenter.exception.BusinessException;
import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.model.Vo.ChatVo.Message;
import com.sean.usercenter.utils.MessageUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.sean.usercenter.contant.UserConstant.USER_LOGIN_STATE;

@ServerEndpoint(value = "/chat", configurator = GetHttpSessionConfig.class) // 声明访问路径
@Component
@Slf4j
public class ChatEndpoint {

    // 保存在线的用户，key为用户名，value为 Session 对象
    private static final Map<String, Session> onlineUsers = new ConcurrentHashMap<>();

    private HttpSession httpSession;


    /**
     * 建立websocket连接后调用该方法
     *
     * @param session
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        this.httpSession = (HttpSession) endpointConfig.getUserProperties().get(HttpSession.class.getName());
        User user = (User) httpSession.getAttribute(USER_LOGIN_STATE);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
        }
        String username = user.getUsername();
        onlineUsers.put(username, session);
        // 通知所有用户，当前用户上线了
        String message = MessageUtils.getMessage(true, null, getFriends());
        broadcastAllUsers(message);
    }

    private Set<String> getFriends() {
        // 获取所有在线用户的用户名字
        return onlineUsers.keySet();
    }

    private void broadcastAllUsers(String message) {
        try {
            Set<Map.Entry<String, Session>> entries = onlineUsers.entrySet();
            for (Map.Entry<String, Session> entry : entries) {
                // 获取到所有用户对应的 session 对象
                Session session = entry.getValue();

                // 检查 session 是否仍然打开
                if (session.isOpen()) {
                    // 使用 getBasicRemote() 方法发送同步消息
                    session.getBasicRemote().sendText(message);
                }
            }
        } catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }


    /**
     * 断开 websocket 时调用
     *
     * @param session
     */
    @OnClose
    public void onOClose(Session session) throws IOException{
        User user = (User) this.httpSession.getAttribute(USER_LOGIN_STATE);
        if (user != null) {
            Session remove = onlineUsers.remove(user.getUsername());
            if (remove != null) {
                log.info("用户{}下线了", user.getUsername());
                remove.close();
            }
            session.close();
        }
        // 通知所有用户，当前用户下线了
        // 注意：不是发送类似于 xxx 已下线的消息，而是向在线用户重新发送一次当前在线的所有用户
        String message = MessageUtils.getMessage(true, null, getFriends());
        broadcastAllUsers(message);
    }

    /**
     * 浏览器发送消息到服务端调用该方法【接受前端发送的消息】
     *
     * @param message
     */
    @OnMessage
    public void onMessage( String message) {
        try {
            // 解析消息
            Gson gson = new Gson();
            Message msg = gson.fromJson(message, Message.class);

            //这里就是好友的名字
            String toName = msg.getToName();
            String TrueMessage = msg.getMessage();
            // 获取好友用户的 session
            Session session = onlineUsers.get(toName);
            User currentUser = (User) this.httpSession.getAttribute(USER_LOGIN_STATE);
            if (currentUser == null) {
                throw new BusinessException(ErrorCode.NOT_LOGIN, "未登录");
            }
            String username = currentUser.getUsername();
            String messageVo = MessageUtils.getMessage(false, username, TrueMessage);
            //发送消息
            session.getBasicRemote().sendText(messageVo);
        }catch (IOException e) {
            log.error("发送消息失败", e);
        }
    }


}
