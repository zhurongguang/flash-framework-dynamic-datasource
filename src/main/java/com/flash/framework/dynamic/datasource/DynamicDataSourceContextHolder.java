package com.flash.framework.dynamic.datasource;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

/**
 * 数据源切换上下文
 *
 * @author zhurg
 * @date 2019/4/1 - 上午11:34
 */
@Slf4j
public final class DynamicDataSourceContextHolder {

    private static final ThreadLocal<DataSourceContext> DS_CONTEXT = new ThreadLocal<>();

    protected static void setDs(DataSourceContext context) {
        DS_CONTEXT.set(context);
    }

    protected static DataSourceContext getDs() {
        if (log.isDebugEnabled()) {
            log.debug("[Dynamic DataSource] DynamicDataSourceContextHolder current {}", JSON.toJSONString(DS_CONTEXT.get()));
        }
        return DS_CONTEXT.get();
    }

    public static void clear() {
        DS_CONTEXT.remove();
    }

    /**
     * 切换到主库
     */
    public static void switchMaster() {
        DataSourceContext context = DS_CONTEXT.get();
        if (null != context) {
            context.setType(DsType.Wirte);
            setDs(context);
        }
    }

    @Data
    @Builder
    public static class DataSourceContext implements Serializable {

        private static final long serialVersionUID = -5302481162610784603L;

        private String dataSource;

        private DsType type;
    }
}