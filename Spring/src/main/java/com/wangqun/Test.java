package com.wangqun;

import com.config.AppConfig;
import com.spring.MyApplicationContext;
import com.wangqun.service.UserService;

/**
 * @author wangqun03
 * @date 2021-09-27 18:28:06
 */
public class Test {
    public static void main(String[] args) {

        MyApplicationContext applicationContext = new MyApplicationContext(AppConfig.class);
        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.print();
    }
}
