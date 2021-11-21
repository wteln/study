drop table `user`;

create table if not exists `user` (
    id bigint auto_increment primary key comment 'id',
    username varchar(128) not null comment '用户名',
    password varchar(128) not null comment '密码',
    isAdmin bool default 0 comment '是否管理员',
    isDel bool default 0 comment '是否被删除'
);