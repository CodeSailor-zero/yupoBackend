package com.sean.usercenter.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/8/4
 * redisson配置类
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
public class RedissonConfig {
    private String host;
    private String port;
    @Bean
    public RedissonClient redissonClient() {
        //1.创建redisson配置
        Config config = new Config();
        String redisAddress = String.format("redis://%s:%s", host, port);
        config.useSingleServer()
                .setAddress(redisAddress)
                .setDatabase(0)
                // 设置1秒钟ping一次来维持连接
                .setPingConnectionInterval(1000)
                .setPassword("123456");
        //2.创建实例
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
