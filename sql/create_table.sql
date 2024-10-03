create table user_team
(
    id         bigint auto_increment comment 'id' primary key,
    userId     bigint comment '用户id',
    teamId     bigint comment '队伍id',
    joinTime   datetime                           null comment '加入时间',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除'
) comment '用户队伍关系';


-- auto-generated definition
create table user
(
    username     varchar(256)                       null comment '用户呢称',
    id           bigint auto_increment comment 'id'
        primary key,
    userAccount  varchar(256)                       null comment '账号',
    avatarUrl    varchar(1024)                      null comment '用户头像',
    gender       tinyint                            null comment '性别 0 - > 女 1 - >男',
    profile      varchar(512)                       null comment '个人介绍',
    userPassword varchar(512)                       not null comment '密码',
    phone        varchar(128)                       null comment '电话',
    email        varchar(512)                       null comment '邮箱',
    userStatus   int      default 0                 not null comment '状态0 - 正常',
    userIds      varchar(512)                       null comment '好友id列表',
    createTime   datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete     tinyint  default 0                 not null comment '是否删除',
    userRole     int      default 0                 not null comment '用户角色 0 - 普通用户 1 - 管理员',
    planetCode   varchar(512)                       null comment '星球编号',
    tags         varchar(1024)                      null comment '标签列表 json'
)
    comment '用户';

-- auto-generated definition
create table team
(
    id          bigint auto_increment comment 'id'
        primary key,
    name        varchar(256)                       not null comment '队伍名称',
    description varchar(1024)                      null comment '描述',
    maxNum      int      default 1                 not null comment '最大人数',
    expireTime  datetime                           null comment '过期时间',
    userId      bigint                             null comment '用户id',
    status      int      default 0                 not null comment '0 - 公开，1 - 私有，2 - 加密',
    password    varchar(512)                       null comment '密码',
    createTime  datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete    tinyint  default 0                 not null comment '是否删除'
) comment '队伍';


-- auto-generated definition
create table friends
(
    id         bigint auto_increment comment 'id主键'
        primary key,
    userId     bigint                             not null comment '发送好友申请的id',
    friendId   bigint                             not null comment '接受好友信息的id',
    remark     varchar(200)                       null comment '申请好友的描述',
    status     tinyint  default 0                 not null comment '0-未通过 1-已同意 2-已过期 3-已撤销',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    isDelete   tinyint  default 0                 not null comment '是否删除'
)
    comment '好友申请关系表';

-- auto-generated definition
create table blog
(
    blogId       tinyint auto_increment comment '博客文章Id'
        primary key,
    createUserId tinyint                            not null comment '创建人Id',
    tittle       varchar(50)                        not null comment '标题',
    text         varchar(1024)                      not null comment '正文',
    topicTags    varchar(512)                       null comment '话题标签 | json字符串',
    remarkNum    tinyint  default 0                 null comment '评论数量',
    startIds     varchar(125)                       null comment '点赞博客用户Id',
    createTime   datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    isDelete     tinyint  default 0                 null comment '逻辑删除 0 - 不删除，1 - 删除'
)
    comment '博客表';



-- auto-generated definition
create table user_blog
(
    userId     tinyint                            not null comment '对 blog 做出操作的用户 Id',
    blogId     tinyint                            not null comment '博客id',
    remark     varchar(512)                       null comment '评论',
    remarkNum  varchar(512)                       null comment '评论点赞用户id',
    createTime datetime default CURRENT_TIMESTAMP null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP null comment '更新时间',
    isDelete   tinyint  default 0                 null comment '逻辑删除 0 - 不删除，1 - 删除'
)
    comment '用户博客关系表';

DROP TABLE IF EXISTS `chat`;
create table chat
(
    id         bigint auto_increment comment '聊天记录id'
        primary key,
    fromId     bigint                                  not null comment '发送消息id',
    toId       bigint                                  null comment '接收消息id',
    teamId     bigint                                  null comment '群聊id',
    text       varchar(512) collate utf8mb4_unicode_ci null comment '消息内容',
    chatType   tinyint                                 not null comment '聊天类型 1-私聊 2-群聊/team聊天',
    createTime datetime default CURRENT_TIMESTAMP      null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP      null comment '更新时间',
    isDelete   tinyint  default 0                      not null comment '是否删除'
)
    comment '聊天消息表';




