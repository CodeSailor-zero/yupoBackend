package com.sean.usercenter.model.Vo.blogVo;

import com.sean.usercenter.model.DTO.User;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/9/16
 **/
@Data
public class RemarkVo {
    private User remarkUser;
    private String remark;
    public Long remarkNum;

}
