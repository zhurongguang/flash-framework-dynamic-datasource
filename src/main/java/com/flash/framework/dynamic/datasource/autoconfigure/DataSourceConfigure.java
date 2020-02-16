package com.flash.framework.dynamic.datasource.autoconfigure;

import com.flash.framework.dynamic.datasource.autoconfigure.druid.DruidConfig;
import com.flash.framework.dynamic.datasource.autoconfigure.hikari.HikariCpConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.Data;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.sql.DataSource;

/**
 * @author zhurg
 * @date 2019/4/1 - 上午10:16
 */
@Data
public class DataSourceConfigure {

    /**
     * 主/从
     */
    private DataSourceMode mode = DataSourceMode.Master;

    /**
     * 是否为默认数据源
     */
    private boolean isDefault;

    /**
     * 连接池类型，默认Hikari
     */
    private Class<? extends DataSource> type = HikariDataSource.class;
    /**
     * JDBC driver
     */
    private String driverClassName;
    /**
     * JDBC url 地址
     */
    private String url;
    /**
     * JDBC 用户名
     */
    private String username;
    /**
     * JDBC 密码
     */
    private String password;
    /**
     * 连接池名称
     */
    private String pollName;
    /**
     * Druid参数配置
     */
    @NestedConfigurationProperty
    private DruidConfig druid = new DruidConfig();
    /**
     * HikariCp参数配置
     */
    @NestedConfigurationProperty
    private HikariCpConfig hikari = new HikariCpConfig();
}