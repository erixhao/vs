package com.vs.service.wechat.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by erix-mac on 2017/1/23.
 */
@RestController
//@EnableAutoConfiguration
public class WeChatBoots {

    @RequestMapping("/")
    public String boot(){
        return "WeChat Boots";
    }

    public static void main(String[] args){
        //SpringApplication.run(WeChatBoots.class, args);
    }
}
