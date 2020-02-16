package com.flash.framework.dynamic.datasource.strategy;

import javax.sql.DataSource;
import java.util.List;

/**
 * 数据源选择策略
 *
 * @author zhurg
 * @date 2019/4/1 - 上午10:22
 */
public interface DataSourceSelectedStrategy {

    /**
     * 决定当前数据源
     *
     * @return dataSource 所选择的数据源
     */
    Integer determineDataSource(List<DataSource> dataSources);
}
