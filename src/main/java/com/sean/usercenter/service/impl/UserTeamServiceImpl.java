package com.sean.usercenter.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.sean.usercenter.model.DTO.UserTeam;
import com.sean.usercenter.mapper.UserTeamMapper;
import com.sean.usercenter.service.UserTeamService;
import org.springframework.stereotype.Service;

/**
* @author 24395
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-08-05 15:07:44
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService {

    @Override
    public boolean teamHasUser(long teamId, long userId) {
        return this.lambdaQuery().eq(UserTeam::getTeamId, teamId).eq(UserTeam::getUserId, userId).count() > 0;
    }

}




