package com.sean.usercenter.config;


import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

@Component
public class GetHttpSessionConfig extends ServerEndpointConfig.Configurator {

    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        // 获取 HttpSession 对象
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        // 将 httpSession 对象保存起来，存到 ServerEndpointConfig 对象中
        // 在 ChatEndpoint 类的 onOpen 方法就能通过 EndpointConfig 对象获取在这里存入的数据
        sec.getUserProperties().put(HttpSession.class.getName(), httpSession);
    }
    
}
