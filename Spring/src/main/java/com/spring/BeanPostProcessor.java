package com.spring;

/**
 * @author wangqun03
 * @date 2021-10-12 13:04:04
 */
public interface BeanPostProcessor {

    Object postProcessBeforeInitialization(Object bean, String beanName);

    Object postProcessAfterInitialization(Object bean, String beanName);
}
