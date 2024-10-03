package com.sean.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sean.usercenter.common.DeleteRequest;
import com.sean.usercenter.model.DTO.Team;
import com.sean.usercenter.model.DTO.UserTeam;
import com.sean.usercenter.model.TeamRequestParameter.TeamQuery;
import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.model.Vo.TeamUserVo;
import com.sean.usercenter.model.request.TeamAddRequest;
import com.sean.usercenter.common.BaseResponse;
import com.sean.usercenter.common.ErrorCode;
import com.sean.usercenter.common.ResultUtils;
import com.sean.usercenter.exception.BusinessException;
import com.sean.usercenter.model.request.TeamJoinRequest;
import com.sean.usercenter.model.request.TeamQuitRequest;
import com.sean.usercenter.model.request.TeamUpdateRequest;
import com.sean.usercenter.service.TeamService;
import com.sean.usercenter.service.UserService;
import com.sean.usercenter.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;


import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 队伍接口
 *
 * @author yupi
 */
@Slf4j
@RestController
@RequestMapping("/team")
//@CrossOrigin(origins = "http://localhost:3000",allowCredentials = "true")
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);
        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        long id = deleteRequest.getId();
        if (deleteRequest == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除失败");
        }
        return ResultUtils.success(true, "删除成功");
    }

    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "修改失败");
        }
        return ResultUtils.success(true, "修改成功");
    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeam(long id) {
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVo>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        //2.判断当前用户是否已经加入队伍
        boolean isAdmin = userService.isAdmin(request);
        //1.查询队伍列表，所有的用户列表
        List<TeamUserVo> teamList = teamService.listTeams(teamQuery, isAdmin);
        if (teamList.isEmpty()) {
            return ResultUtils.success(teamList);
        }
        List<Long> teamIdList = teamList
                .stream()
                .map(TeamUserVo::getId)
                .collect(Collectors.toList());
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            //已加入队伍的id集合
            Set<Long> hasJoinTeamIdSet = userTeamList.
                    stream()
                    .map(UserTeam::getTeamId)
                    .collect(Collectors.toSet());
            teamList.forEach(teamUserVo -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(teamUserVo.getId());
                teamUserVo.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //3.查询加入队伍的用户信息（人数）
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList
                .stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> {
            team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size());
        });
        return ResultUtils.success(teamList);
    }

    // todo 查询分页
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(teamQuery, team);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result, "加入成功");
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result, "退出成功");
    }


    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVo>> listMyCreateTeams(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        List<TeamUserVo> teamList = teamService.listMyCreateTeams(teamQuery);
        return ResultUtils.success(teamList);
    }


    //修改成可以根据用户不同查询的加入队伍
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVo>> listMyJoinTeams(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        List<TeamUserVo> teamList = teamService.listMyJoinTeams(teamQuery);
        return ResultUtils.success(teamList);
    }
}
