package com.flash.framework.dynamic.datasource;

import com.alibaba.fastjson.JSON;
import com.flash.framework.dynamic.datasource.dao.OmDao;
import com.flash.framework.dynamic.datasource.dao.UserDao;
import com.flash.framework.dynamic.datasource.model.Om;
import com.flash.framework.dynamic.datasource.model.User;
import com.flash.framework.dynamic.datasource.service.DDSService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

/**
 * @author
 * @date 2019/4/1 - 下午3:59
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DynamicDataSourceApplication.class})
public class TestDDS {

    @Autowired
    private OmDao omDao;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DDSService ddsService;

    @Test
    public void testWriteMasterDefault() {
        Om om = new Om();
        om.setName(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 5));
        System.out.println(omDao.insert(om));
    }

    @Test
    public void testWriteMaster() {
        User user = new User();
        user.setName(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 5));
        user.setExtra(UUID.randomUUID().toString().replaceAll("-", "").substring(0, 5));
        System.out.println(userDao.insert(user));
    }

    @Test
    public void testReadSlave() {
        System.out.println(JSON.toJSONString(omDao.selectById(1L)));
    }

    @Test
    public void testReadSlave2() {
        System.out.println(userDao.selectById(1L));
    }

    @Test
    public void demo() {
        ddsService.demo();
    }

    @Test
    public void testMaster() {
        System.out.println(omDao.selectId(1L));
    }

    @Test
    public void testMaster2() {
        ddsService.demo2();
    }
}