package com.sean.usercenter.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/8/4
 **/
@SpringBootTest
public class RedissonTest {

    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        RList<Object> list = redissonClient.getList("list");
        list.add("123");
    }

}
