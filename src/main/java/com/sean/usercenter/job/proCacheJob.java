package com.sean.usercenter.job;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.sean.usercenter.utils.Alias.PREFIX;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/8/3
 * 缓存预热
 **/
@Component
@Slf4j
public class proCacheJob {
    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private RedissonClient redissonClient;

    private List<Long> mainUserList = Arrays.asList(1L);


    //每天执行，预热推荐用户
    @Scheduled(cron = "0 30 15 * * *")
    public void doCacheRecommendUser() {
        RLock lock = redissonClient.getLock(PREFIX + "precachejob:docache:job");
        try {
            boolean res = lock.tryLock(0L, 30000L, TimeUnit.MILLISECONDS);
            if (res) {
                for (long userId : mainUserList) {
                    QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
                    String redisKey = PREFIX + "user:recommend:" + userId;
                    ValueOperations<String, Object> operations = redisTemplate.opsForValue();
                    IPage<User> userPage = userService.page(new Page<>(1, 20), userQueryWrapper);
                    //写入缓存
                    try {
                        operations.set(redisKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set key error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("get lock error", e);
        } finally {
            //只释放自己的锁
            //.isHeldByThread(Thread.currentThread().getId()) 获取线程的id作为判断的依据
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
