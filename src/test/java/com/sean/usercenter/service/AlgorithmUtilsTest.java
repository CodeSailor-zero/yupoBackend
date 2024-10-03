package com.sean.usercenter.service;

import com.sean.usercenter.utils.AlgorithmUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/8/15
 * 算法工具类测试
 **/
public class AlgorithmUtilsTest {
    @Test
    void test(){
        String str1 = "鱼皮是狗";
        String str2 = "鱼皮不是狗";
        String str3 = "鱼皮是鱼不是狗";
        // 1
//        int score1 = AlgorithmUtils.minDistance(str1, str2);
        // 3
//        int score2 = AlgorithmUtils.minDistance(str1, str3);
//        System.out.println("score1 = " + score1);
//        System.out.println("score2 = " + score2);

    }

    @Test
    void test2(){
        List<String> stringList1 = Arrays.asList("Java", "大一", "女");
        List<String> stringList2 = Arrays.asList("Java", "大一", "男");
        List<String> stringList3 = Arrays.asList("C++", "大二", "男");
        // 1
        int score1 = AlgorithmUtils.minDistance(stringList1, stringList2);
        // 3
        int score2 = AlgorithmUtils.minDistance(stringList1, stringList3);
        System.out.println("score1 = " + score1);
        System.out.println("score2 = " + score2);
    }
}
