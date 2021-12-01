drop table if exists `user`;

create table if not exists `user` (
    id bigint auto_increment primary key comment 'id',
    username varchar(128) comment '用户名',
    password varchar(128) comment '密码',
    gender varchar(16) comment '性别',
    age int comment '年龄段标记',
    occupation varchar(128) comment '职业',
    zipCode varchar(32) comment ' zip code',
    isDel bool default 0 comment '是否被删除'
);