package com.sean.usercenter.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.sean.usercenter.model.DTO.UserTeam;

public interface UserTeamService extends IService<UserTeam> {

    boolean teamHasUser(long teamId, long userId);

}
