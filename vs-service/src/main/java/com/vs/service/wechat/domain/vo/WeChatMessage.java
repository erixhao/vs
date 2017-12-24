package com.vs.service.wechat.domain.vo;

import lombok.Data;

/**
 * Created by erix-mac on 16/7/9.
 */
@Data
public class WeChatMessage {

    private String toUserName;// DEV
    private String fromUserName;// Wechat User
    private long createTime;
    private String msgType;
    private long msgId;
}
