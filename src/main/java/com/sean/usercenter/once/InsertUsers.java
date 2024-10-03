package com.sean.usercenter.once;

import com.sean.usercenter.mapper.UserMapper;
import com.sean.usercenter.model.DTO.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/8/1
 **/
@Component
public class InsertUsers {

    @Resource
    private UserMapper userMapper;

//    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers() {
//        final int INSERT_NUM = 10000000;
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM = 1000;
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
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println("一共执行时间" + stopWatch.getTotalTimeMillis());
    }
}
