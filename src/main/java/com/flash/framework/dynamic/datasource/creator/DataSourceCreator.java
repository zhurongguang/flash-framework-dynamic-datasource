/**
 * Copyright © 2018 organization baomidou
 * <pre>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <pre/>
 */
package com.flash.framework.dynamic.datasource.creator;

import com.flash.framework.dynamic.datasource.autoconfigure.DataSourceConfigure;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;

import static com.flash.framework.dynamic.datasource.DbConstants.DRUID_DATASOURCE;
import static com.flash.framework.dynamic.datasource.DbConstants.HIKARI_DATASOURCE;


/**
 * 数据源创建器
 *
 * @author
 */
@Slf4j
@Setter
public class DataSourceCreator {

    /**
     * 是否存在druid
     */
    private static Boolean druidExists = false;
    /**
     * 是否存在hikari
     */
    private static Boolean hikariExists = false;

    static {
        try {
            Class.forName(DRUID_DATASOURCE);
            druidExists = true;
        } catch (ClassNotFoundException ignored) {
        }
        try {
            Class.forName(HIKARI_DATASOURCE);
            hikariExists = true;
        } catch (ClassNotFoundException ignored) {
        }
    }

    private HikariDataSourceCreator hikariDataSourceCreator;
    private DruidDataSourceCreator druidDataSourceCreator;

    /**
     * 创建数据源
     *
     * @param dataSourceProperty 数据源信息
     * @return 数据源
     */
    public DataSource createDataSource(DataSourceConfigure dataSourceProperty) {
        DataSource dataSource;
        Class<? extends DataSource> type = dataSourceProperty.getType();
        if (type == null) {
            if (druidExists) {
                dataSource = createDruidDataSource(dataSourceProperty);
            } else if (hikariExists) {
                dataSource = createHikariDataSource(dataSourceProperty);
            } else {
                dataSource = createBasicDataSource(dataSourceProperty);
            }
        } else if (DRUID_DATASOURCE.equals(type.getName())) {
            dataSource = createDruidDataSource(dataSourceProperty);
        } else if (HIKARI_DATASOURCE.equals(type.getName())) {
            dataSource = createHikariDataSource(dataSourceProperty);
        } else {
            dataSource = createBasicDataSource(dataSourceProperty);
        }
        return dataSource;
    }

    /**
     * 创建基础数据源
     *
     * @param dataSourceProperty 数据源参数
     * @return 数据源
     */
    public DataSource createBasicDataSource(DataSourceConfigure dataSourceProperty) {
        return BasicDataSourceCreator.getInstance().createDataSource(dataSourceProperty);
    }

    /**
     * 创建Druid数据源
     *
     * @param dataSourceProperty 数据源参数
     * @return 数据源
     */
    public DataSource createDruidDataSource(DataSourceConfigure dataSourceProperty) {
        if (druidDataSourceCreator != null) {
            return druidDataSourceCreator.createDataSource(dataSourceProperty);
        }
        return createBasicDataSource(dataSourceProperty);
    }

    /**
     * 创建Hikari数据源
     *
     * @param dataSourceProperty 数据源参数
     * @return 数据源
     * @author
     */
    public DataSource createHikariDataSource(DataSourceConfigure dataSourceProperty) {
        if (hikariDataSourceCreator != null) {
            return hikariDataSourceCreator.createDataSource(dataSourceProperty);
        }
        return createBasicDataSource(dataSourceProperty);
    }
}
