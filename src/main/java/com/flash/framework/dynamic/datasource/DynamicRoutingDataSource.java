package com.flash.framework.dynamic.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import javax.annotation.PostConstruct;

/**
 * @author zhurg
 * @date 2019/4/1 - 上午11:31
 */
public class DynamicRoutingDataSource extends AbstractRoutingDataSource {

    private final DynamicGroupDataSource dynamicGroupDataSource;

    public DynamicRoutingDataSource(DynamicGroupDataSource dynamicGroupDataSource) {
        this.dynamicGroupDataSource = dynamicGroupDataSource;
    }

    @PostConstruct
    public void init() {
        setDefaultTargetDataSource(dynamicGroupDataSource.getDefaultDataSource());
        setTargetDataSources(dynamicGroupDataSource.getDataSources());
    }

    @Override
    protected Object determineCurrentLookupKey() {
        return dynamicGroupDataSource.selectDataSource(DynamicDataSourceContextHolder.getDs());
    }
}