package com.flash.framework.dynamic.datasource;

/**
 * @author zhurg
 * @date 2020/1/28 - 下午6:27
 */
public interface DbConstants {

    /**
     * 数据源：主库
     */
    String MASTER = "_master";
    /**
     * 数据源：从库
     */
    String SLAVE = "_slave";

    /**
     * DRUID数据源类
     */
    String DRUID_DATASOURCE = "com.alibaba.druid.pool.DruidDataSource";
    /**
     * HikariCp数据源
     */
    String HIKARI_DATASOURCE = "com.zaxxer.hikari.HikariDataSource";
}