package com.sean.usercenter.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sean.usercenter.model.DTO.Team;
import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.model.TeamRequestParameter.TeamQuery;
import com.sean.usercenter.model.Vo.TeamUserVo;
import com.sean.usercenter.model.request.TeamJoinRequest;
import com.sean.usercenter.model.request.TeamQuitRequest;
import com.sean.usercenter.model.request.TeamUpdateRequest;

import java.util.List;


/**
* @author 24395
* @description 针对表【team(队伍)】的数据库操作Service
* @createDate 2024-08-05 14:53:09
*/
public interface TeamService extends IService<Team> {
    /**
     * 创建队伍
     * @param team
     * @param LoginUser
     * @return long
     */
    long addTeam(Team team, User LoginUser);

    /**
     * 搜索队伍
     * @param teamQuery
     * @param isAdmin
     * @return List
     */
    List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍
     * @param teamUpdateRequest
     * @param loginUser
     * @return boolean
     */
    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     * 加入队伍
     * @param teamJoinRequest
     * @param loginUser
     * @return boolean
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest,User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return boolean
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 移除/删除队伍
     * @param id
     * @param loginUser
     * @return boolean
     */
    boolean deleteTeam(long id,User loginUser);

    /**
     * 获取我加入的队伍
     * @param teamQuery
     * @return  List<TeamUserVo>
     */
    List<TeamUserVo> listMyJoinTeams(TeamQuery teamQuery);


    /**
     * 获取我创建的队伍
     * @param teamQuery
     * @return List<TeamUserVo>
     */
    List<TeamUserVo> listMyCreateTeams(TeamQuery teamQuery);
}
