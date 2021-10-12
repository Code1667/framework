package com.wangqun.service;

import com.spring.Autowired;
import com.spring.BeanNameAware;
import com.spring.Component;
import com.spring.InitializingBean;

/**
 * @author wangqun03
 * @date 2021-09-27 18:53:02
 */
@Component("userService")
//@Scope("prototype")
public class UserServiceImpl implements BeanNameAware , InitializingBean, UserService {

    @Autowired
    private OrderService orderService;

    private String beanName;

    @Override
    public void setBeanName(String name) {
        beanName = name;
    }

    @Override
    public void afterPropertiesSet() {
        // 可以验证属性是否为空/属性赋值等...
        System.out.println("After properties set, initialization!");
    }

    @Override
    public void print(){
        System.out.println(orderService);
        System.out.println(beanName);
    }
}
