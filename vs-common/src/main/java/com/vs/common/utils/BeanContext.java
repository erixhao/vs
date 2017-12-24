package com.vs.common.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Created by erix-mac on 15/8/5.
 */
@Component
public class BeanContext {

    private final static String SPRING_APP_XML = "/spring-app.xml";


    public static <T> T getBean(Class<T> clazz){
        ApplicationContext appContext = new ClassPathXmlApplicationContext(SPRING_APP_XML);

        return appContext.getBean(clazz);
    }

    public static <T> T getBean(Class<T> clazz, String name){
        ApplicationContext appContext = new ClassPathXmlApplicationContext(SPRING_APP_XML);

        return appContext.getBean(name, clazz);
    }
}
