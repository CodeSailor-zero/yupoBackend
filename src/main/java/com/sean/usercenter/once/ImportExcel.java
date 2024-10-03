package com.sean.usercenter.once;

import com.alibaba.excel.EasyExcel;
;import java.util.List;


public class ImportExcel {
    /**
     * 最简单的读
     * <p>
     * 1. 创建excel对应的实体对象 参照{@link XingQiuTableUserInfo}
     * <p>
     * 2. 由于默认一行行的读取excel，所以需要创建excel一行一行的回调监听器，参照{@link TableListener}
     * <p>
     * 3. 直接读即可
     */
   static String fileName = "D:\\JAVALearning\\progarms\\BackendPrograms\\星球项目\\user-center\\src\\main\\resources\\testExcel.xlsx";

    public static void main(String[] args) {
       ImportExcel.simpleRead();
    }

    public static void simpleRead() {
//        readByListener();
        synchronousRead();
    }

    /**
     * 监听器读取
     */
    public static void readByListener() {
        // 写法1：JDK8+ ,不用额外写一个XingQiuTableUserInfoListener
        // since: 3.0.0-beta1
        // 这里默认每次会读取100条数据 然后返回过来 直接调用使用数据就行
        // 具体需要返回多少行可以在`PageReadListener`的构造函数设置
        EasyExcel.read(fileName, XingQiuTableUserInfo.class, new TableListener()).sheet().doRead();
    }

    /**
     * 同步读
     * 同步的返回，不推荐使用，如果数据量大会把数据放到内存里面
     */
    public static void synchronousRead() {
        List<XingQiuTableUserInfo> totalList = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        for (XingQiuTableUserInfo xingQiuUserInfo : totalList) {
            System.out.println(xingQiuUserInfo);
        }
    }
}
