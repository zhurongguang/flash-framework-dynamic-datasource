package com.flash.framework.dynamic.datasource.annotation;

import java.lang.annotation.*;

/**
 * 查询强制走主库
 *
 * @author zhurg
 * @date 2019/4/1 - 下午9:43
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Master {

}