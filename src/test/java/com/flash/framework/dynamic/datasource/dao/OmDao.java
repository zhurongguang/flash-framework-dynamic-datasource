package com.flash.framework.dynamic.datasource.dao;

import com.flash.framework.dynamic.datasource.annotation.Ds;
import com.flash.framework.dynamic.datasource.annotation.Master;
import com.flash.framework.dynamic.datasource.model.Om;
import com.flash.framework.mybatis.mapper.AbstractMapper;

/**
 * @author zhurg
 * @date 2019/4/1 - 下午3:55
 */
@Ds(name = "order")
public interface OmDao extends AbstractMapper<Om> {

    @Master
    Om selectId(Long id);
}