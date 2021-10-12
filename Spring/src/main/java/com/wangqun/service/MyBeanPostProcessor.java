package com.wangqun.service;

import com.spring.BeanPostProcessor;
import com.spring.Component;

/**
 * @author wangqun03
 * @date 2021-10-12 13:05:19
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        System.out.println("before initialization...");

        // 对特定的bean进行一些处理
        if (beanName.equals("userService")){
            ((UserService)bean).setBeanName("userService1");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        System.out.println("after initialization...");
        return bean;
    }
}
