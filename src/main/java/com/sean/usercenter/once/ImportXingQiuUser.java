package com.sean.usercenter.once;

import com.alibaba.excel.EasyExcel;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ImportXingQiuUser {
    public static void main(String[] args) {
        String fileName = "D:\\JAVALearning\\progarms\\BackendPrograms\\星球项目\\user-center\\src\\main\\resources\\testExcel.xlsx";
        List<XingQiuTableUserInfo> userinfolList = EasyExcel.read(fileName).head(XingQiuTableUserInfo.class).sheet().doReadSync();
        System.out.println("总人数 = " + userinfolList.size() );
        Map<String, List<XingQiuTableUserInfo>> userinfoMap = userinfolList.stream()
                .filter(userinfo -> StringUtils.isNotEmpty(userinfo.getUsername()))
                .collect(Collectors.groupingBy(XingQiuTableUserInfo::getUsername));
        System.out.println("不重复的名称 =" + userinfoMap.keySet().size());

        for (Map.Entry<String, List<XingQiuTableUserInfo>> entry : userinfoMap.entrySet()) {
            System.out.println("用户昵称 =" + entry.getKey());
            System.out.println("成员编号 =" + entry.getValue());
            System.out.println("========");
        }
    }
}
