package com.sean.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/8/5
 * 通用分页请求参数
 **/
@Data
public class DeleteRequest implements Serializable {

    private static final long serialVersionUID = 100L;
    /**
     * 队伍 id
     */
    private long id;
}
