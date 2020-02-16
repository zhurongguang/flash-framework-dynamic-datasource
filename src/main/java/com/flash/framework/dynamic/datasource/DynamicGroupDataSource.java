package com.flash.framework.dynamic.datasource;

import com.alibaba.druid.pool.DruidDataSource;
import com.flash.framework.dynamic.datasource.autoconfigure.DataSourceConfigure;
import com.flash.framework.dynamic.datasource.autoconfigure.DataSourceMode;
import com.flash.framework.dynamic.datasource.autoconfigure.DynamicDataSourceConfigure;
import com.flash.framework.dynamic.datasource.creator.DataSourceCreator;
import com.flash.framework.dynamic.datasource.exception.DynamicDataSourceException;
import com.flash.framework.dynamic.datasource.strategy.DataSourceSelectedStrategy;
import com.flash.framework.dynamic.datasource.strategy.RandomDataSourceSelectedStrategy;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.zaxxer.hikari.HikariDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.sql.DataSource;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zhurg
 * @date 2019/4/1 - 上午9:54
 */
@Slf4j
public class DynamicGroupDataSource {

    private final DataSourceCreator dynamicDataSourceCreator;

    private final DynamicDataSourceConfigure dynamicDataSourceConfigure;

    private DataSourceSelectedStrategy dataSourceSelectedStrategy;

    public DynamicGroupDataSource(DataSourceCreator dynamicDataSourceCreator, DynamicDataSourceConfigure dynamicDataSourceConfigure) {
        this.dynamicDataSourceCreator = dynamicDataSourceCreator;
        this.dynamicDataSourceConfigure = dynamicDataSourceConfigure;
        if (null == dynamicDataSourceConfigure.getStrategy()) {
            dynamicDataSourceConfigure.setStrategy(RandomDataSourceSelectedStrategy.class);
        }
        try {
            this.dataSourceSelectedStrategy = dynamicDataSourceConfigure.getStrategy().newInstance();
        } catch (Exception e) {
            log.error("[Dynamic DataSource] DataSourceSelectedStrategy property strategy {} can not created,use default strategy RandomDataSourceSelectedStrategy", dynamicDataSourceConfigure.getStrategy());
            this.dataSourceSelectedStrategy = new RandomDataSourceSelectedStrategy();
        }
    }


    /**
     * 主库Map 存放所有主库数据源 支持多个库
     */
    private Map<String, DataSource> masterDataSources = Maps.newConcurrentMap();
    /**
     * 从库Map 存放所有主库对应从库数据源
     */
    private Map<String, List<DataSource>> slaveDataSources = Maps.newConcurrentMap();

    /**
     * 默认数据源
     */
    @Getter
    private DataSource defaultDataSource;

    @Getter
    private String defaultDataSoureName;

    @PostConstruct
    public void loadDataSources() {
        Map<String, List<DataSourceConfigure>> dataSourceConfigureMap = dynamicDataSourceConfigure.getDatasources();
        if (CollectionUtils.isEmpty(dataSourceConfigureMap)) {
            throw new DynamicDataSourceException("[Dynamic DataSource] DynamicDataSourceConfigure property datasource can not be null");
        }
        dataSourceConfigureMap.forEach((name, configures) -> {
            if (!CollectionUtils.isEmpty(configures)) {
                configures.forEach(configure -> {
                    //初始化主库
                    if (DataSourceMode.Master.equals(configure.getMode())) {
                        initMasters(name, configure, defaultDataSource);
                    }
                    //初始化从库
                    if (DataSourceMode.Slave.equals(configure.getMode())) {
                        initSlaves(name, configure);
                    }
                });
            }
        });
        //设置默认数据源

        if (null == defaultDataSource) {
            throw new DynamicDataSourceException("[Dynamic DataSource] DataSourceConfigure property isDefault must have one is true");
        }
    }

    /**
     * 设置上下文
     *
     * @param ds
     * @return
     */
    public void selectDataSource(String ds, DsType type) {
        if (StringUtils.isEmpty(ds)) {
            ds = defaultDataSoureName;
            DynamicDataSourceContextHolder.setDs(DynamicDataSourceContextHolder.DataSourceContext.
                    builder()
                    .dataSource(ds)
                    .type(DsType.Unknown)
                    .build());
            return;
        }
        if (!masterDataSources.containsKey(ds)) {
            throw new DynamicDataSourceException("[Dynamic DataSource] dataSource " + ds + " can not be fund,please check your properties");
        }
        DynamicDataSourceContextHolder.setDs(DynamicDataSourceContextHolder.DataSourceContext
                .builder()
                .type(type)
                .dataSource(ds)
                .build());
    }

    /**
     * 获取数据源
     *
     * @return
     */
    public Map<Object, Object> getDataSources() {
        Map<Object, Object> dataSources = Maps.newHashMap();
        masterDataSources.forEach((name, ds) -> dataSources.put(name + DbConstants.MASTER, ds));
        slaveDataSources.forEach((name, dss) -> {
            for (int i = 0; i < dss.size(); i++) {
                dataSources.put(name + DbConstants.SLAVE + (i + 1), dss.get(i));
            }
        });
        return dataSources;
    }

    /**
     * 获取数据源
     *
     * @param context
     * @return
     */
    public Object selectDataSource(DynamicDataSourceContextHolder.DataSourceContext context) {
        if (null == context || StringUtils.isEmpty(context.getDataSource()) || DsType.Unknown.equals(context.getType())) {
            return defaultDataSource;
        }
        if (DsType.Wirte.equals(context.getType())) {
            return context.getDataSource() + DbConstants.MASTER;
        } else {
            Integer index = dataSourceSelectedStrategy.determineDataSource(slaveDataSources.get(context.getDataSource()));
            return null == index ? context.getDataSource() + DbConstants.MASTER : context.getDataSource() + DbConstants.SLAVE + (index + 1);
        }
    }

    private void initMasters(String name, DataSourceConfigure configure, DataSource defaultDataSource) {
        DataSource dataSource = dynamicDataSourceCreator.createDataSource(configure);
        if (dynamicDataSourceConfigure.isEnableDistributedTransaction()) {
            masterDataSources.put(name, new DataSourceProxy(dataSource));
        } else {
            masterDataSources.put(name, dataSource);
        }
        if (configure.isDefault() && null == defaultDataSource) {
            this.defaultDataSource = masterDataSources.get(name);
            this.defaultDataSoureName = name;
        }

    }

    private void initSlaves(String name, DataSourceConfigure configure) {
        if (slaveDataSources.containsKey(name)) {
            slaveDataSources.get(name).add(dynamicDataSourceCreator.createDataSource(configure));
        } else {
            slaveDataSources.put(name, Lists.newArrayList(dynamicDataSourceCreator.createDataSource(configure)));
        }
    }

    @PreDestroy
    public void close() {
        if (!CollectionUtils.isEmpty(masterDataSources)) {
            masterDataSources.values().stream()
                    .filter(Objects::nonNull)
                    .forEach(ds -> close(ds));
        }
        if (!CollectionUtils.isEmpty(slaveDataSources)) {
            slaveDataSources.values().stream().filter(slaves -> !CollectionUtils.isEmpty(slaves))
                    .forEach(slaves -> slaves.stream().filter(Objects::nonNull).forEach(ds -> close(ds)));
        }
    }

    private void close(DataSource ds) {
        if (null != ds) {
            if (ds instanceof DruidDataSource) {
                ((DruidDataSource) ds).close();
            } else if (ds instanceof HikariDataSource) {
                ((HikariDataSource) ds).close();
            }
        }
    }
}