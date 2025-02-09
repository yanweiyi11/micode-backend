drop database if exists codedb;

create database codedb charset 'utf8';

use codedb;

create table if not exists user
(
    id          bigint auto_increment primary key,
    username    varchar(256)                                                      not null comment '用户名',
    password    varchar(256)                                                      not null comment '密码',
    avatarUrl   varchar(512)                                                      null comment '头像',
    userProfile varchar(512)                                                      null comment '用户简介',
    gender      tinyint                                                           null comment '性别（0-男，1-女）',
    email       varchar(64)                                                       null comment '邮箱',
    userRole    varchar(32) default 'user'                                        not null comment '用户角色（user / admin / ban）',
    createTime  datetime    default CURRENT_TIMESTAMP                             not null comment '创建时间',
    updateTime  datetime    default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete    tinyint     default 0                                             not null comment '是否删除（0-未删除，1-已删除）'
) comment '用户表' collate = utf8mb4_unicode_ci;;


create table if not exists question
(
    id          bigint auto_increment primary key,
    title       varchar(256)                                                   not null comment '标题',
    content     text                                                           null comment '内容',
    tags        varchar(512)                                                   null comment '标签（Json 数组）',
    answer      text                                                           null comment '答案',
    difficulty  varchar(32)                                                    null comment '难度（简单 / 中等 / 困难）',
    submitNum   INT      default 0                                             not null comment '题目提交数',
    acceptedNum INT      default 0                                             not null comment '提交通过数',
    judgeCase   text                                                           null comment '判题用例（Json 数组）',
    judgeConfig text                                                           null comment '判题配置（json 对象）',
    userId      bigint                                                         not null comment '上传者id',
    createTime  datetime default CURRENT_TIMESTAMP                             not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete    tinyint  default 0                                             not null comment '是否删除（0-未删除，1-已删除）'
) comment '题目表' collate = utf8mb4_unicode_ci;;

create table if not exists question_submit
(
    id         bigint auto_increment primary key,
    language   varchar(64)                                                    not null comment '编程语言',
    code       text                                                           not null comment '用户代码',
    status     INT                                                            not null comment '判题状态',
    judgeInfo  text                                                           null comment '判题信息（Json 对象）',
    questionId bigint                                                         not null comment '题目id',
    userId     bigint                                                         not null comment '提交者id',
    createTime datetime default CURRENT_TIMESTAMP                             not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete   tinyint  default 0                                             not null comment '是否删除（0-未删除，1-已删除）'
) comment '题目提交表' collate = utf8mb4_unicode_ci;;

create table if not exists question_tag
(
    id         bigint auto_increment primary key,
    tagName    varchar(256)                                                   null comment '标签名称',
    userId     bigint                                                         null comment '创建者id',
    parentId   bigint                                                         null comment '父标签id',
    isParent   tinyint                                                        null comment '是否为父标签（0-不是父标签，1-是父标签）',
    createTime datetime default CURRENT_TIMESTAMP                             not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete   tinyint  default 0                                             not null comment '是否删除（0-未删除，1-已删除）',
    constraint uniIdx_tagName
        unique (tagName)
) comment '题目标签表' collate = utf8mb4_unicode_ci;

create table if not exists post
(
    id         bigint auto_increment primary key,
    title      varchar(512)                                                   null comment '标题',
    content    mediumtext                                                     null comment '内容',
    thumbNum   int                                                            not null default '0' comment '点赞数',
    userId     bigint                                                         not null comment '创建用户 id',
    deleted    tinyint  default 0                                             not null comment '是否删除',
    createTime datetime default CURRENT_TIMESTAMP                             not null comment '创建时间',
    updateTime datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP not null comment '更新时间',
    isDelete   tinyint  default 0                                             not null comment '是否删除（0-未删除，1-已删除）'
) comment '题解表' collate = utf8mb4_unicode_ci