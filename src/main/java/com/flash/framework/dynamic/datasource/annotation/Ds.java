package com.flash.framework.dynamic.datasource.annotation;

import java.lang.annotation.*;

/**
 * 数据源注解
 *
 * @author zhurg
 * @date 2019/4/1 - 下午2:04
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Ds {

    /**
     * 数据源名称
     *
     * @return
     */
    String name();
}