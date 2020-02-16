package com.flash.framework.dynamic.datasource.exception;

/**
 * @author zhurg
 * @date 2020/1/28 - 下午7:29
 */
public class DynamicDataSourceException extends RuntimeException {

    private static final long serialVersionUID = -4657889588409505061L;

    public DynamicDataSourceException() {
    }

    public DynamicDataSourceException(String message) {
        super(message);
    }

    public DynamicDataSourceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DynamicDataSourceException(Throwable cause) {
        super(cause);
    }

    public DynamicDataSourceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}