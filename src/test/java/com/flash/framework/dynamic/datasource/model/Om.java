package com.flash.framework.dynamic.datasource.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.flash.framework.mybatis.BaseModel;
import lombok.Data;

/**
 * @author zhurg
 * @date 2019/4/1 - 下午3:53
 */
@Data
@TableName("o_m")
public class Om extends BaseModel {

    private static final long serialVersionUID = 1451887293611437986L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;
}