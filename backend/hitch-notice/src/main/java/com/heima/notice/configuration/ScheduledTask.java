package com.heima.notice.configuration;


import com.alibaba.fastjson.JSON;
import com.heima.modules.po.NoticePO;
import com.heima.notice.service.NoticeService;
import com.heima.notice.socket.WebSocketServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 定时任务 推送暂存消息
 */
@Component
public class ScheduledTask {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTask.class);

    @Autowired
    private NoticeService noticeService;

    private static final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

    @PostConstruct
    public void init() {
        //定时调度，获取mongodb里的未读消息，推送给对应用户
        executorService.scheduleAtFixedRate(() -> {
            //获取所有在线的用户accountId，提示：WebSocketServer里有用户链接的池子
            List<String> accountIds = new ArrayList<>(WebSocketServer.sessionPools.keySet());

            //在MongoDB中获取需要推送的消息，noticeService里的方法研究一下，可以帮到你
            List<NoticePO> noticeByAccountIds = noticeService.getNoticeByAccountIds(accountIds);

            //遍历所有消息，逐个发送消息到浏览器
            for (NoticePO noticePO : noticeByAccountIds) {
                String json = JSON.toJSONString(noticePO);
                try {
                    String receiverId = noticePO.getReceiverId();
                    Session session = WebSocketServer.sessionPools.get(receiverId);
                    if (session != null) {
                        //方法：session.getBasicRemote().sendText(json);
                        session.getBasicRemote().sendText(json);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }, 0,1 , TimeUnit.SECONDS);
    }
}
