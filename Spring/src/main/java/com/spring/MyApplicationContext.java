package com.spring;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author wangqun03
 * @date 2021-09-27 18:40:26
 */
public class MyApplicationContext {

    private Class configClass;

    /**
     * 单例池
     */
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();

    /**
     * <beanName,BeanDefinition>信息
     */
    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();

    private List<BeanPostProcessor> beanPostProcessorList = new ArrayList<>();

    public MyApplicationContext(Class configClass) {
        this.configClass = configClass;

        // 解析配置类，并不是解析限定符、属性，而是解析类上的注解
        // ComponentScan ---> 扫描路径 ---> 扫描
        scan(configClass);

        // 创建bean对象
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            String beanName = entry.getKey();
            BeanDefinition beanDefinition = entry.getValue();
            if (beanDefinition.getScope().equals("singleton")) {
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }

    }

    private Object createBean(String beanName, BeanDefinition beanDefinition) {
        Class clazz = beanDefinition.getClazz();
        Object instance = null;
        try {
            instance = clazz.getDeclaredConstructor().newInstance();

            // 依赖注入
            // 1.找到属性
            for (Field declaredField : clazz.getDeclaredFields()) {
                // 2.判断属性有没有@Autowired注解
                if (declaredField.isAnnotationPresent(Autowired.class)) {
                    Autowired annotation = declaredField.getAnnotation(Autowired.class);
                    // 3.通过该属性的名字拿到bean
                    Object bean = getBean(declaredField.getName());
                    // 如果@Autowired属性required=true 并且获取的bean为空
                    if (annotation.require() && bean == null) {
                        throw new NullPointerException();
                    }
                    // 4.设置属性
                    declaredField.setAccessible(true);
                    declaredField.set(instance, bean);
                }
            }

            // Aware回调
            if (instance instanceof BeanNameAware){
                ((BeanNameAware)instance).setBeanName(beanName);
            }

            // BeanPostProcessor --> 初始化前
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            }

            // 初始化
            if (instance instanceof InitializingBean){
                ((InitializingBean)instance).afterPropertiesSet();
            }

            // BeanPostProcessor -->初始化后
            for (BeanPostProcessor beanPostProcessor : beanPostProcessorList) {
                instance = beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            }


        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }

        return instance;
    }

    private void scan(Class configClass) {
        ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
        String path = componentScanAnnotation.value(); // 扫描路径 com.wangqun.service

        // 扫描
        // 包下面有些不是我们需要加载的类如XxUtil，所以我们要先拿到包下面的所有类，判断类上面是否有@Component注解
        // 那么如何拿到所有类呢？？？
        // BootStrap -----> jre/lib
        // Ext -----> jre/ext/lib
        // App -----> classpath 例如：/Users/wangqun03/Projects/HandWritting/Spring/target/classes com.wangqun.Test
        ClassLoader classLoader = MyApplicationContext.class.getClassLoader(); // app
        path = path.replace(".", "/");
        URL resource = classLoader.getResource(path);// 相对路径，相对的是classpath
        File file = new File(resource.getFile());
        if (file.isDirectory()) {// 判断是否是目录
            File[] files = file.listFiles();// 获得目录下的所有文件
            for (File f : files) {
                String absolutePath = f.getAbsolutePath();// 获得文件的绝对路径 这里是编译后的target/classes下的绝对路径
                // 如果是类文件才处理
                if (absolutePath.endsWith(".class")) {
                    String name = toFullQualifiedName(absolutePath);// 需要将绝对路径变成全限定名
                    try {
                        Class<?> clazz = classLoader.loadClass(name);// 通过类全限定名加载类
                        if (clazz.isAnnotationPresent(Component.class)) {
                            // 表示当前这个类是一个Bean
                            // 解析类，判断单例还是原型 ----> BeanDefinition

                            //clazz对象是否实现了BeanPostProcessor接口
                            if (BeanPostProcessor.class.isAssignableFrom(clazz)) {
                                try {
                                    BeanPostProcessor instance = (BeanPostProcessor)clazz.getDeclaredConstructor().newInstance();
                                    beanPostProcessorList.add(instance);
                                } catch (InstantiationException e) {
                                    e.printStackTrace();
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                } catch (NoSuchMethodException e) {
                                    e.printStackTrace();
                                }
                            }

                            Component componentAnnotation = clazz.getAnnotation(Component.class);
                            String beanName = componentAnnotation.value();

                            // bean的定义
                            BeanDefinition beanDefinition = new BeanDefinition();
                            beanDefinition.setClazz(clazz);

                            if (clazz.isAnnotationPresent(Scope.class)) {
                                Scope scopeAnnotation = clazz.getDeclaredAnnotation(Scope.class);
                                beanDefinition.setScope(scopeAnnotation.value());
                            } else {
                                beanDefinition.setScope("singleton");
                            }
                            // 将bean的描述放入map
                            beanDefinitionMap.put(beanName, beanDefinition);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 绝对路径变为全限定名
     *
     * @param absolutePath 绝对路径
     * @return 全限定名
     */
    private String toFullQualifiedName(String absolutePath) {
        int com = absolutePath.indexOf("com");
        int point = absolutePath.indexOf(".");
        // 去掉.Class
        String substring = absolutePath.substring(0, point);
        // 去掉com之前
        substring = substring.substring(com);
        // 替换
        substring = substring.replace("/", ".");
        return substring;
    }

    public Object getBean(String beanName) {
        if (beanDefinitionMap.containsKey(beanName)) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if (beanDefinition.getScope().equals("singleton")) {
                Object o = singletonObjects.get(beanName);
                return o;
            } else {
                // 创建Bean对象
                return createBean(beanName, beanDefinition);
            }
        } else {
            throw new NullPointerException("Bean " + beanName + " not exists");
        }
    }

}
