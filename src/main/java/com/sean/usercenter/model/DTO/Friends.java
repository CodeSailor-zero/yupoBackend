package com.sean.usercenter.model.DTO;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户关系表
 * @TableName friends
 */
@TableName(value ="friends")
@Data
public class Friends implements Serializable {
    /**
     * id主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 被关注的 id
     */
    private Long friendId;
    /**
     * 申请状态 默认0 （0-未通过 1-已同意 2-已过期 3-已撤销）
     */
    private Integer status;
    /**
     * 好友申请备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}