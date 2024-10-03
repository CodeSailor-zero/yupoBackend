package com.sean.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sean.usercenter.common.ErrorCode;
import com.sean.usercenter.exception.BusinessException;
import com.sean.usercenter.mapper.TeamMapper;
import com.sean.usercenter.model.DTO.Team;
import com.sean.usercenter.model.DTO.User;
import com.sean.usercenter.model.DTO.UserTeam;
import com.sean.usercenter.model.TeamRequestParameter.TeamQuery;
import com.sean.usercenter.model.Vo.TeamUserVo;
import com.sean.usercenter.model.Vo.UserVo;
import com.sean.usercenter.model.enums.TeamStatusEnum;
import com.sean.usercenter.model.request.TeamJoinRequest;
import com.sean.usercenter.model.request.TeamQuitRequest;
import com.sean.usercenter.model.request.TeamUpdateRequest;
import com.sean.usercenter.service.TeamService;
import com.sean.usercenter.service.UserService;
import com.sean.usercenter.service.UserTeamService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 24395
 * &#064;description  针对表【team(队伍)】的数据库操作Service实现
 * &#064;createDate  2024-08-05 14:53:09
 */
@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team>
        implements TeamService {

    @Resource
    private UserTeamService userTeamService;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public long addTeam(Team team, User LoginUser) {
        //1.请求参数为空
        if (team == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        //2.用户未登录
        if (LoginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        final long userId = LoginUser.getId();
        //3.检验信息
        // 1.队伍人数 > 1或者 <=20
        int maxNum = Optional.ofNullable(team.getMaxNum()).orElse(0);
        if (maxNum < 1 || maxNum > 20) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "队伍人数不满足要求");
        }
        //2.队伍标题 <= 20
        String name = team.getName();
        if (StringUtils.isBlank(name) && name.length() > 20) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "队伍标题不满足要求");
        }
        //3.描述 <= 512
        String description = team.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 512) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "描述字数过长");
        }
        //4.状态是否公开（int）不传默认为0（公开）
        int status = Optional.ofNullable(team.getStatus()).orElse(0);
        TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
        if (teamStatusEnum == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "队伍状态不满足要求");
        }
        //5.如果status是加密状态，一定要有密码，且密码<=32
        String password = team.getPassword();
        if (TeamStatusEnum.SECRET.equals(teamStatusEnum)) {
            if ((StringUtils.isBlank(password) || password.length() > 32)) {
                throw new BusinessException(ErrorCode.PARAMETER_ERROR, "密码不满足要求");
            }
        }
        //6.超时时间 > 当前时间
        //  10:00 > 9:00
        Date expireTime = team.getExpireTime();
        if (new Date().after(expireTime)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "超时时间 > 当前时间");
        }
        //7.检验用户最多创建 5 个队伍
        // todo 有bug,可能同时建100个队伍
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        long hasTeamNum = this.count(queryWrapper);
        if (hasTeamNum >= 5) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "用户最多创建 5 个队伍");
        }
        //8.插入队伍信息到队伍表
        team.setId(null);
        team.setUserId(userId);
        boolean result = this.save(team);
        Long teamId = team.getId();
        if (!result || team == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "创建队伍失败");
        }
        //9.插入用户 => 队伍关系到关系表
        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(userId);
        userTeam.setTeamId(teamId);
        userTeam.setJoinTime(new Date());
        result = userTeamService.save(userTeam);
        if (!result) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "创建队伍失败");
        }
        return teamId;
    }

    @Override
    public List<TeamUserVo> listTeams(TeamQuery teamQuery, boolean isAdmin) {
        //1.组合查询条件
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        if (teamQuery != null) {
            Long id = teamQuery.getId();
            if (id != null && id > 0) {
                queryWrapper.eq("id", id);
            }
            String searchText = teamQuery.getSearchText();
            if (StringUtils.isNotBlank(searchText)) {
                queryWrapper.and(qw -> qw.like("description", searchText).or().like("name", searchText));
            }
            String name = teamQuery.getName();
            if (StringUtils.isNotBlank(name)) {
                queryWrapper.like("name", name);
            }
            String description = teamQuery.getDescription();
            if (StringUtils.isNotBlank(description)) {
                queryWrapper.like("description", description);
            }
            //查询最大人数相等的
            Integer maxNum = teamQuery.getMaxNum();
            if (maxNum != null && maxNum > 0) {
                queryWrapper.eq("maxNum", maxNum);
            }
            //查询创建人信息
            Long userId = teamQuery.getUserId();
            if (userId != null && userId > 0) {
                queryWrapper.eq("userId", userId);
            }
            // 根据状态来查询
            Integer status = teamQuery.getStatus();
            TeamStatusEnum teamStatusEnum = TeamStatusEnum.getEnumByValue(status);
            if (teamStatusEnum == null) {
                //如果 teamStatusEnum 为空，默认当作查询公开的
                teamStatusEnum = TeamStatusEnum.PUBLIC;
            }
            if (!isAdmin && teamStatusEnum.equals(TeamStatusEnum.PRIVATE)) {
                //如果不是管理员，不能查询非私有队伍
                throw new BusinessException(ErrorCode.NOT_AUTH);
            }
            queryWrapper.eq("status", teamStatusEnum.getValue());
        }

        //不展示已过期的队伍
        // expireTime is null or expireTime >  now
        queryWrapper.and(qw -> qw.gt("expireTime", new Date()).or().isNull("expireTime"));
        List<Team> teamList = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(teamList)) {
            return new ArrayList<>();
        }
        ArrayList<TeamUserVo> teamUserVoList = new ArrayList<>();
        // 关联查询创建人的用户信息
        for (Team team : teamList) {
            Long createUserId = team.getUserId();
            if (createUserId == null) {
                continue;
            }
            User createUser = userService.getById(createUserId);
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            //用户脱敏
            UserVo userVo = new UserVo();
            if (createUser != null) {
                BeanUtils.copyProperties(createUser, userVo);
                teamUserVo.setCreateUser(userVo);
            }
            teamUserVoList.add(teamUserVo);
        }

        return teamUserVoList;
    }

    @Override
    public boolean updateTeam(TeamUpdateRequest teamUpdateRequest, User loginUser) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        Long id = teamUpdateRequest.getId();
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        Team oldTeam = this.getById(id);
        if (oldTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }

        //只有队伍创建者或管理员，可以修改
        if (oldTeam.getUserId() != loginUser.getId() && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }
        TeamStatusEnum statusEnum = TeamStatusEnum.getEnumByValue(teamUpdateRequest.getStatus());
        // 私密房间必须加密
        if (statusEnum.equals(TeamStatusEnum.SECRET)) {
            if (StringUtils.isBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMETER_ERROR, "加密房间必须要设置密码");
            }
        }
        // 公共房间无法加密
        if (statusEnum.equals(TeamStatusEnum.PUBLIC)) {
            if (StringUtils.isNotBlank(teamUpdateRequest.getPassword())) {
                throw new BusinessException(ErrorCode.PARAMETER_ERROR, "公共房间无法加密");
            }
        }
        Team updateTeam = new Team();
        BeanUtils.copyProperties(teamUpdateRequest, updateTeam);
        boolean result = this.updateById(updateTeam);
        if (result) {
            //同步user_team表的更新时间
            UserTeam userTeam = new UserTeam();
            Team team = this.getById(id);
            Date updateTime = team.getUpdateTime();
            userTeam.setUpdateTime(updateTime);
//          update user_team set updateTime = team.getUpdateTime()
            QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("teamId", id);
            userTeamService.update(userTeam, queryWrapper);
        }
        return result;

    }

    @Override
    public boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        // 2.队伍必须存在
        Long teamId = teamJoinRequest.getTeamId();
        Team currentTeam = getCurrentTeamById(teamId);
        //3.只能加入未满、未过期的队伍
        Date currentTeamExpireTime = currentTeam.getExpireTime();
        if (currentTeamExpireTime != null && currentTeamExpireTime.before(new Date())) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "队伍已过期");
        }
        Integer TeamStatus = currentTeam.getStatus();
        TeamStatusEnum currentTeamStatus = TeamStatusEnum.getEnumByValue(TeamStatus);
        if (TeamStatusEnum.PRIVATE.equals(currentTeamStatus)) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "禁止加入私有的队伍");
        }

        String currentTeamPassword = currentTeam.getPassword();
        String password = teamJoinRequest.getPassword();
        if (TeamStatusEnum.SECRET.equals(currentTeamStatus)) {
            if (StringUtils.isBlank(currentTeamPassword) || !password.equals(currentTeamPassword)) {
                throw new BusinessException(ErrorCode.PARAMETER_ERROR, "密码错误");
            }
        }

        //可以替换分布式锁
        //这里的锁不太好
        //要分锁用户，用户不可以一个时间内加入许多不同的队伍
        //要锁队伍，同一用户不可以一个时间内加入同一个队伍
        // 该用户加入队伍的数量
        long currentUserId = loginUser.getId();
        // 只有一个线程能获取到锁
        RLock lock = redissonClient.getLock("yupao:join_team");
        try {
            // 抢到锁并执行
            while (true) {
                if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                    QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", currentUserId);
                    long hasJoinNum = userTeamService.count(userTeamQueryWrapper);
                    // 1.每个用户最多创建和加入五个队伍
                    if (hasJoinNum > 5) {
                        throw new BusinessException(ErrorCode.PARAMETER_ERROR, "最多创建和加入五个队伍");
                    }

                    //不能重复加入队伍
                    userTeamQueryWrapper = new QueryWrapper<>();
                    userTeamQueryWrapper.eq("userId", currentUserId);
                    userTeamQueryWrapper.eq("teamId", teamId);
                    long hasUserJoinTeamNum = userTeamService.count(userTeamQueryWrapper);
                    //2.队伍人数超载
                    if (hasUserJoinTeamNum > 0) {
                        throw new BusinessException(ErrorCode.PARAMETER_ERROR, "该用户已加入该队伍");
                    }

                    // 已加入队伍的人数
                    long teamHasJoinNum = this.countTeamUserNumBYTeamId(teamId);
                    //2.队伍人数超载
                    if (teamHasJoinNum >= currentTeam.getMaxNum()) {
                        throw new BusinessException(ErrorCode.PARAMETER_ERROR, "队伍已满");
                    }

                    //修改队伍信息
                    UserTeam userTeam = new UserTeam();
                    userTeam.setUserId(currentUserId);
                    userTeam.setTeamId(teamId);
                    userTeam.setJoinTime(new Date());
                    return userTeamService.save(userTeam);
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheRecommendUser error", e);
            return false;
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        Long teamId = teamQuitRequest.getTeamId();
        Team currentTeam = getCurrentTeamById(teamId);
        long currentUserId = loginUser.getId();
        //判断是否加入队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", currentUserId);
        queryWrapper.eq("teamId", teamId);
        long hasJoinNum = userTeamService.count(queryWrapper);

        if (hasJoinNum <= 0) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR, "未加入队伍");
        }
        long teamHasJoinNum = this.countTeamUserNumBYTeamId(teamId);
        if (teamHasJoinNum == 1) {
            //队伍目前只有一个人，队伍直接解散
            this.removeById(teamId);
        } else {
            if (currentUserId == currentTeam.getUserId()) {
                // 如果队长退出，将队伍转移给队伍中最近加入的成员
                QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
                userTeamQueryWrapper.eq("teamId", teamId);
                userTeamQueryWrapper.last("order by id asc limit 2");
                List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
                if (CollectionUtils.isEmpty(userTeamList) || userTeamList.size() <= 1) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR);
                }
                UserTeam nextUserTeam = userTeamList.get(1);
                Long NextTeamLeaderId = nextUserTeam.getUserId();
                //更新当前队伍的队长
                Team updateTeam = new Team();
                updateTeam.setId(teamId);
                updateTeam.setUserId(NextTeamLeaderId);
                boolean result = updateById(updateTeam);
                if (!result) {
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍队长失败");
                }
            }
        }
        //如果不是队长，直接推出队伍
        return userTeamService.remove(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeam(@RequestBody long id, User loginUser) {
        // 2,校验队伍是否存在
        Team currentTeam = getCurrentTeamById(id);
        long teamId = currentTeam.getId();
        // 3.校验你是不是队伍的队长
        if (currentTeam.getUserId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NOT_AUTH, "无访问权限");
        }
        // 4.移除所有加入队伍的关联信息
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("teamId", teamId);
        boolean result = userTeamService.remove(queryWrapper);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "移除队伍关联信息失败");
        }
        // 5.删除队伍
        return removeById(teamId);
    }

    @Override
    public List<TeamUserVo> listMyJoinTeams(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        Long userId = teamQuery.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        //teamID -> userTeamList
        Map<Long, List<UserTeam>> listMap = userTeamService.list(queryWrapper)
                .stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));
        // teamIDList
        List<Long> idList = new ArrayList<>(listMap.keySet());
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        if (CollectionUtils.isNotEmpty(idList)) {
            teamQueryWrapper.in("id", idList);
        }else {
            return new ArrayList<>();
        }
        String searchText = teamQuery.getSearchText();
        if (StringUtils.isNotBlank(searchText)) {
            teamQueryWrapper = new QueryWrapper<>();
            teamQueryWrapper.and(qw -> qw.like("description", searchText).or().like("name", searchText));
        }
        List<Team> joinTeamList = this.list(teamQueryWrapper);
        UserVo userVo = new UserVo();
        //队伍创建人信息
        User user = userService.getById(userId);
        BeanUtils.copyProperties(user, userVo);
        ArrayList<TeamUserVo> hasJoinTeamList = new ArrayList<>();
        for (Team team : joinTeamList) {
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            teamUserVo.setCreateUser(userVo);
            hasJoinTeamList.add(teamUserVo);
        }
        return hasJoinTeamList;
    }

    @Override
    public List<TeamUserVo> listMyCreateTeams(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        Long userId = teamQuery.getUserId();
        if (userId == null) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        String searchText = teamQuery.getSearchText();
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper = new QueryWrapper<>();
            queryWrapper.and(qw -> qw.like("description", searchText).or().like("name", searchText));
        }
        List<Team> teamList = this.list(queryWrapper);
        ArrayList<TeamUserVo> createTeams = new ArrayList<>();
        for (Team team : teamList) {
            TeamUserVo teamUserVo = new TeamUserVo();
            BeanUtils.copyProperties(team, teamUserVo);
            createTeams.add(teamUserVo);
        }
        return createTeams;
    }

    /**
     * 查询某个队伍当前的用户数量
     *
     * @param teamId
     * @return long
     */
    private long countTeamUserNumBYTeamId(long teamId) {
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        userTeamQueryWrapper.eq("teamId", teamId);
        return userTeamService.count(userTeamQueryWrapper);
    }

    /**
     * 根据用户id获取当前队伍
     *
     * @param teamId
     * @return Team
     */
    private Team getCurrentTeamById(Long teamId) {
        if (teamId == null || teamId <= 0) {
            throw new BusinessException(ErrorCode.PARAMETER_ERROR);
        }
        Team currentTeam = getById(teamId);
        if (currentTeam == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return currentTeam;
    }
}




