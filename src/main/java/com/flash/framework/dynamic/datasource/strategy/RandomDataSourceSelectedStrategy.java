package com.flash.framework.dynamic.datasource.strategy;

import org.springframework.util.CollectionUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 司机选择策略
 *
 * @author zhurg
 * @date 2019/4/1 - 下午3:14
 */
public class RandomDataSourceSelectedStrategy implements DataSourceSelectedStrategy {

    @Override
    public Integer determineDataSource(List<DataSource> dataSources) {
        if (!CollectionUtils.isEmpty(dataSources)) {
            return ThreadLocalRandom.current().nextInt(dataSources.size());
        }
        return null;
    }
}