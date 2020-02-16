package com.flash.framework.dynamic.datasource.service;

import com.flash.framework.dynamic.datasource.DynamicDataSourceContextHolder;
import com.flash.framework.dynamic.datasource.dao.OmDao;
import com.flash.framework.dynamic.datasource.dao.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author zhurg
 * @date 2019/4/2 - 上午10:10
 */
@Component
public class DDSService {

    @Autowired
    private OmDao omDao;

    @Autowired
    private UserDao userDao;

    public void demo() {
        System.out.println(omDao.selectById(1L));
        System.out.println(userDao.selectById(1L));
    }

    public void demo2() {
        System.out.println(userDao.selectById(1L));
        //切换主库
        DynamicDataSourceContextHolder.switchMaster();
        System.out.println(omDao.selectId(1L));
    }
}