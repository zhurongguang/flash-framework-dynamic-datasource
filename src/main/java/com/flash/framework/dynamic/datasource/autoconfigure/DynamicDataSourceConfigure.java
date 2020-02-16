package com.flash.framework.dynamic.datasource.autoconfigure;

import com.flash.framework.dynamic.datasource.autoconfigure.druid.DruidConfig;
import com.flash.framework.dynamic.datasource.autoconfigure.hikari.HikariCpConfig;
import com.flash.framework.dynamic.datasource.strategy.DataSourceSelectedStrategy;
import com.flash.framework.dynamic.datasource.strategy.RandomDataSourceSelectedStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhurg
 * @date 2019/4/1 - 上午10:07
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource.dynamic")
public class DynamicDataSourceConfigure {

    /**
     * 每一个数据源
     */
    private Map<String, List<DataSourceConfigure>> datasources = new LinkedHashMap<>();

    /**
     * 多数据源选择算法clazz，默认负载均衡算法
     */
    private Class<? extends DataSourceSelectedStrategy> strategy = RandomDataSourceSelectedStrategy.class;

    /**
     * Druid全局参数配置
     */
    @NestedConfigurationProperty
    private DruidConfig druid = new DruidConfig();

    /**
     * HikariCp全局参数配置
     */
    @NestedConfigurationProperty
    private HikariCpConfig hikari = new HikariCpConfig();

    /**
     * 是否启用分布式事物
     */
    private boolean enableDistributedTransaction;

    /**
     * 启用Fescar分布式事物 applicationId
     */
    private String fescarApplicationId;

    /**
     * 启用Fescar分布式事物 txServiceGroup
     */
    private String fescarTxServiceGroup;
}