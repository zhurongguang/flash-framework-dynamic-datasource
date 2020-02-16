package com.flash.framework.dynamic.datasource.dao;

import com.flash.framework.dynamic.datasource.annotation.Ds;
import com.flash.framework.dynamic.datasource.model.User;
import com.flash.framework.mybatis.mapper.AbstractMapper;

/**
 * @author zhurg
 * @date 2019/4/1 - 下午3:57
 */
@Ds(name = "user")
public interface UserDao extends AbstractMapper<User> {


}