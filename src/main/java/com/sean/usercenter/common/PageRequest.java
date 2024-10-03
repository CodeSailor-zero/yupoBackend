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
public class PageRequest implements Serializable {

    private static final long serialVersionUID = 100L;

    /**
     * 当前页号(页面大小)
     */
    protected int pageSize = 10;
    /**
     * 当前页码(第几页)
     */
    protected int pageNum = 1;
}
