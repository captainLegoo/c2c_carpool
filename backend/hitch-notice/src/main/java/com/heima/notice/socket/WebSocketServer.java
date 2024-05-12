package com.heima.notice.socket;


import com.alibaba.fastjson.JSON;
import com.heima.commons.constant.HtichConstants;
import com.heima.commons.entity.SessionContext;
import com.heima.commons.helper.RedisSessionHelper;
import com.heima.commons.utils.SpringUtil;
import com.heima.modules.po.StrokePO;
import com.heima.modules.vo.NoticeVO;
import com.heima.notice.handler.NoticeHandler;
import com.heima.notice.service.StrokeAPIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ServerEndpoint(value = "/ws/socket")
public class WebSocketServer {

    //Websocket用户链接池
    //concurrent包的线程安全Map，用来存放每个客户端对应的WebSocketServer对象。
    //key是accountId，可以通过本类中的getAccountId方法获取到，value是session
    public final static Map<String, Session> sessionPools = new ConcurrentHashMap<>();

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);

    /*
        用户发送ws消息，message为json格式{'receiverId':'接收人','tripId':'行程id','message':'消息内容'}
    */
    @OnMessage
    public void onMessage(Session session, String message) {
        NoticeVO noticeVO = JSON.parseObject(message, NoticeVO.class);
        // 获取行程发布人的id
        StrokeAPIService strokeAPIService = SpringUtil.getBean(StrokeAPIService.class);
        StrokePO strokePO = strokeAPIService.selectByID(noticeVO.getTripId());
        // 消息接收者id = 行程发布者id
        noticeVO.setReceiverId(strokePO.getPublisherId());
        // 消息发送者id = 当前session中的Id
        noticeVO.setSenderId(getAccountId(session));
        //设置相关消息内容并存入mongodb：noticeHandler.saveNotice(noticeVO);
        NoticeHandler noticeHandler = SpringUtil.getBean(NoticeHandler.class);
        noticeHandler.saveNotice(noticeVO);
    }


    /**
     * 连接建立成功调用
     *
     * @param session 客户端与socket建立的会话
     * @param session 客户端的userId
     */
    @OnOpen
    public void onOpen(Session session) {
        logger.info("客户端连接成功 id:{}", getAccountId(session));
        sessionPools.put(getAccountId(session), session);
    }

    /**
     * 关闭连接时调用
     *
     * @param session 关闭连接的客户端的姓名
     */
    @OnClose
    public void onClose(Session session) {
        logger.info("客户端断开连接 id:{}", getAccountId(session));
        sessionPools.remove(getAccountId(session));
    }


    /**
     * 发生错误时候
     *
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable) {
        System.out.println("发生错误");
        throwable.printStackTrace();
    }



    /*
    * 在当前session中获取用户accoutId
    * */
    private String getAccountId(Session session) {
        String token = null;
        Map<String, List<String>> paramMap = session.getRequestParameterMap();
        List<String> paramList = paramMap.get(HtichConstants.SESSION_TOKEN_KEY);
        if (paramList!=null && paramList.size() != 0){
            token = paramList.get(0);
        }
        RedisSessionHelper redisSessionHelper = SpringUtil.getBean(RedisSessionHelper.class);
        if (null == redisSessionHelper) {
            return null;
        }
        SessionContext context = redisSessionHelper.getSession(token);
        boolean isisValid = redisSessionHelper.isValid(context);
        if (isisValid) {
            return context.getAccountID();
        }
        return null;
    }

}