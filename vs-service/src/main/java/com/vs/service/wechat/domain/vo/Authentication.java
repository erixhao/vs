package com.vs.service.wechat.domain.vo;

import lombok.Data;

/**
 * Created by erix-mac on 16/7/9.
 */
@Data
public class Authentication {
    private String signature;
    private String timestamp;
    private String nonce;
    private String echostr;
}
