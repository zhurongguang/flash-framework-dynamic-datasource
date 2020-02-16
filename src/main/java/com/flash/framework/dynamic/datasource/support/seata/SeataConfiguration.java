package com.flash.framework.dynamic.datasource.support.seata;

import com.flash.framework.dynamic.datasource.autoconfigure.DynamicDataSourceConfigure;
import io.seata.spring.annotation.GlobalTransactionScanner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Seata分布式事务配置
 *
 * @author zhurg
 * @date 2019/4/19 - 上午11:30
 */
@Configuration
@EnableConfigurationProperties(DynamicDataSourceConfigure.class)
@ConditionalOnProperty(prefix = "spring.datasource.dynamic", name = "enable-distributed-transaction", havingValue = "true")
public class SeataConfiguration {

    @Bean
    public GlobalTransactionScanner globalTransactionScanner(DynamicDataSourceConfigure dynamicDataSourceConfigure) {
        return new GlobalTransactionScanner(dynamicDataSourceConfigure.getFescarApplicationId(), dynamicDataSourceConfigure.getFescarTxServiceGroup());
    }
}