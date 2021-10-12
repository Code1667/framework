package com.spring;

/**
 * @author wangqun03
 * @date 2021-09-27 20:06:28
 */
public class BeanDefinition {
    private Class clazz;
    private String scope;

    public BeanDefinition(){

    }

    public BeanDefinition(Class clazz, String scope) {
        this.clazz = clazz;
        this.scope = scope;
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }
}
