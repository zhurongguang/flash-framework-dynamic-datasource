package com.flash.framework.dynamic.datasource.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.flash.framework.mybatis.BaseModel;
import lombok.Data;

/**
 * @author zhurg
 * @date 2019/4/1 - 下午3:55
 */
@Data
@TableName("user")
public class User extends BaseModel {

    private static final long serialVersionUID = -1909870104740453178L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String extra;
}