package com.flash.framework.dynamic.datasource.autoconfigure;

import com.flash.framework.dynamic.datasource.DynamicGroupDataSource;
import com.flash.framework.dynamic.datasource.DynamicRoutingDataSource;
import com.flash.framework.dynamic.datasource.DynamicSqlSessionTemplate;
import com.flash.framework.dynamic.datasource.creator.DataSourceCreator;
import com.flash.framework.dynamic.datasource.support.seata.SeataConfiguration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * @author zhurg
 * @date 2019/4/1 - 上午10:41
 */
@EnableConfigurationProperties(DynamicDataSourceConfigure.class)
@AutoConfigureBefore({DataSourceAutoConfiguration.class})
@EnableAspectJAutoProxy(proxyTargetClass = true)
@Import(SeataConfiguration.class)
public class DynamicDataSourceAutoConfigure {

    @Bean
    @ConditionalOnMissingBean
    public DataSourceCreator dynamicDataSourceCreator() {
        return new DataSourceCreator();
    }

    @Bean
    @ConditionalOnMissingBean
    public DynamicGroupDataSource dynamicGroupDataSource(DynamicDataSourceConfigure dynamicDataSourceConfigure, DataSourceCreator dynamicDataSourceCreator) {
        return new DynamicGroupDataSource(dynamicDataSourceCreator, dynamicDataSourceConfigure);
    }

    @Bean
    @Primary
    public DataSource dataSource(DynamicGroupDataSource dynamicGroupDataSource) {
        return new DynamicRoutingDataSource(dynamicGroupDataSource);
    }

    @Bean
    public DynamicSqlSessionTemplate dynamicSqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new DynamicSqlSessionTemplate(sqlSessionFactory);
    }
}