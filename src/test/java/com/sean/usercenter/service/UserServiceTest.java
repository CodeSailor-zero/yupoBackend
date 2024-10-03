package com.sean.usercenter.service;


import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.service.impl.UserServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


/**
 * 用户服务测试
 *
 * @author yupi
 */
@SpringBootTest
public class UserServiceTest {
    //ctrl + shift + - 全部都折叠

    @Resource
    UserService userService;

    @Resource
    UserServiceImpl userServiceImpl;

    @Test
    public void testAddUser() {
        User user = new User();
        user.setUsername("admin");
        user.setUserAccount("123");
        user.setAvatarUrl("https://www.baidu.com/link?url=m9NLKQOBV95NCuYd0N2CADp8gzrCF_TH-rVE5yhjPKzPhHV4iGWMO4x992k_-h9m_cucXNftB0oIlUa6grJrL25vdFFlpWMpEuYn4VojJrxFmGlOP-0x0n2qFioEw3KXa3C1k6oQhDTCV9mxF5BhEfWSYKKDky96upq4wJZXi3LVaDTfHCS-6Z1pwbd5f_Zrt7ljiLbNL7Agpr0VbPCKwTEH9jFDsFtd_DSBf-NXdk4Tdk340N7W4DNxGtQmEveSVsnF8lWrlWsmwBEwfPobraKmTa4fbgL8DDQYulH_aK3_urG9l9F49OT53RVmzHStio6mnLhugab-2UDpBonbG91IrEG1nt-Xl_H3SlR260tKRo83mjSzEbo1dsniTnagLfgOXuxraPHE-h3cbt1NNuGN-LDLNWh0QEqcqZzWGFAfL25ap98KPtH3B_Peaf4gp7E2DePv7UhYvaTUo-XRX_7lVSZ725AArWnIAUY09pdQD4lbL0Wc1_a1me7NzNTk5qTeIDaxmoEpdR9uDxiZljTwLF5moajZSGz426MVbfQFgE3m6EUGkdy9tfY5h1QjmuvXsCUpemJiAfsdwHa6OsKOdBM1qd-0nguaXYl8v79dBBaOnUirorqhIQbvZ_7TkAyUlWqoMl91u0vrWN6Uk1AfVhbw6nrW2zKjqZtSrS_Z9oazZnmw45lMlrjUxGIkRsPMwo4jlJM9Uwqj20J17nR_UQBYoEUHVHF_CkrUMObLJhS5n1tW_vnzl3Tpt6oQ&wd=&eqid=89f269e90015532a0000000265eaf84c");
        user.setGender(0);
        user.setUserPassword("456");
        user.setPhone("123");
        user.setEmail("789");
        boolean result = userService.save(user);
        System.out.println(user.getId());
        //断言，断言结果是否如自己所预料的
        Assertions.assertTrue(result);
    }
}