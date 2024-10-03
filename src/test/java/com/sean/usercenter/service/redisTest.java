package com.sean.usercenter.service;

import com.sean.usercenter.model.DTO.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/8/2
 **/
@SpringBootTest
public class redisTest {

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Test
    void test(){
//        redisTemplate.opsForValue().set("name","sean");
        User user = new User();
        user.setUsername("Marry");
        redisTemplate.opsForValue().set("aaa",user);
    }
}
