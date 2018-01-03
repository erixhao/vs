package com.vs.service.wechat.domain;

import com.google.common.collect.Maps;
import com.vs.market.DownloadExecutor;
import com.vs.service.wechat.domain.vo.MessageType;
import com.vs.service.wechat.service.WeChatService;
import lombok.Cleanup;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by erix-mac on 16/7/9.
 */
@Data
@Slf4j
@Component
public class WeChats {

    private final static String MKT_SYNC_UP = "venus:market@admin";


    @Autowired
    private WeChatService wechatService;


    public void processWeChatMessage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        String resMessage;
        try {
            resMessage = processResponseMessage(parsePostXml(request));

            if (resMessage.equals("")) {
                resMessage = "ERROR Response";
            }
        } catch (Exception e) {
            log.error("ERROR:" + e.getMessage());
            resMessage = "ERROR!";
        }
        log.info(">>>>>>>>>>>>>>>>>> Return Message: " + resMessage);
        response.getWriter().println(new String(resMessage.getBytes("utf-8"), "utf-8"));
    }


    private String processResponseMessage(Map map) {
        String resMessage = "";
        String type = map.get("MsgType").toString();

        MessageType msgType = MessageType.valueOf(MessageType.class, type.toUpperCase());
        switch (msgType) {
            case TEXT:
                resMessage = handleTextMessage(map);
                break;
            case EVENT:
                break;
            default:
                break;
        }

        return resMessage;
    }

    private String handleTextMessage(Map<String, String> map) {
        String content = map.get("Content");
        log.info("content : " + content);

        if (MKT_SYNC_UP.equals(content)) {
            DownloadExecutor.downloadAll();

            return "sync-up market successfully.";
        } else {
            String resMessage = this.wechatService.processUserAction(content);

            //log.info("WeChat Response XML: " + resMessage);
            return buildTextMessage(map, resMessage);
        }
    }


    private static String buildTextMessage(Map<String, String> map, String content) {
        String fromUserName = map.get("FromUserName");
        String toUserName = map.get("ToUserName");
        return String.format(
                "<xml>" +
                        "<ToUserName><![CDATA[%s]]></ToUserName>" +
                        "<FromUserName><![CDATA[%s]]></FromUserName>" +
                        "<CreateTime>%s</CreateTime>" +
                        "<MsgType><![CDATA[text]]></MsgType>" +
                        "<Content><![CDATA[%s]]></Content>" +
                        "</xml>",
                fromUserName, toUserName, new Date().getTime(), content);
    }


    private static Map<String, String> parsePostXml(HttpServletRequest request) throws Exception {
        log.info(">>>>>>>>>>>>>>>>parsePostXml");

        Map<String, String> map = Maps.newHashMap();
        @Cleanup
        InputStream inputStream = request.getInputStream();
        SAXReader reader = new SAXReader();
        Document document = reader.read(inputStream);
        Element root = document.getRootElement();
        List<Element> elementList = root.elements();

        for (Element e : elementList) {
            System.out.println(e.getName() + "|" + e.getText());
            map.put(e.getName(), e.getText());
        }

        log.info("WeChat Post Map: " + map.toString());

        return map;
    }
}
