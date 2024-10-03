package com.sean.usercenter.service;

import com.sean.usercenter.model.DTO.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/8/1
 **/
@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserService userService;

    /**
     * 批量插入数据
     */
    @Test
    public void doInsertUsers() {
//        final int INSERT_NUM = 10000000;
        final int INSERT_NUM = 700000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ArrayList<User> userArrayList = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUsername("假Sean");
            user.setUserAccount("fakeSean");
            user.setAvatarUrl("https://ts1.cn.mm.bing.net/th?id=OIP-C.vw7p083gv9r7uh_Bn2EEtwHaDt&w=174&h=104&c=7&bgcl=aeac8f&r=0&o=6&dpr=1.3&pid=13.1");
            user.setGender(0);
            user.setUserPassword("12345678");
            user.setPhone("15312340725");
            user.setEmail("mSean123qq.com");
            user.setTags("[]");
            user.setUserStatus(0);
            user.setUserRole(0);
            user.setPlanetCode("111111");
            userArrayList.add(user);
        }
        userService.saveBatch(userArrayList,100);
        stopWatch.stop();
        System.out.println("一共执行时间" + stopWatch.getTotalTimeMillis());
    }

    /**
     * 并发插入数据
     */
    @Test
    public void doConcurrencyInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        //分10组
        int batchSize = 5000;
        int j=0;
        List<CompletableFuture<Void>> futureList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            List<User> userList = new ArrayList<>();
            while (true){
                j++;
                User user = new User();
                user.setUsername("假数据");
                user.setUserAccount("fakeaccount");
                user.setAvatarUrl("https://img1.baidu.com/it/u=1645832847,2375824523&fm=253&fmt=auto&app=138&f=JPEG?w=480&h=480");
                user.setGender(0);
                user.setUserPassword("231313123");
                user.setPhone("1231312");
                user.setEmail("12331234@qq.com");
                user.setUserStatus(0);
                user.setUserRole(0);
                user.setPlanetCode("213123");
                user.setTags("[]");
                userList.add(user);
                if (j % batchSize == 0){
                    break;
                }
            }
            //异步执行
            CompletableFuture<Void> future = CompletableFuture.runAsync(() ->{
                System.out.println("threadName:"+Thread.currentThread().getName());
                userService.saveBatch(userList,batchSize);
            });
            futureList.add(future);
        }
        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
