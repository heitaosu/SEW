# 创建加油记录表
create table sew.fuel_record
(
    id            int auto_increment
        primary key,
    work_order    bigint       null comment '工单号',
    fuel_end      bigint       null comment '加油结束时间',
    fuel_start    bigint       null comment '加油开始时间',
    sequence_code varchar(128) null comment '序列号',
    model_type    varchar(64)  null comment '型号',
    install_type  varchar(32)  null comment '安装方式',
    fuel_set_val  double       null comment '注油量设定值',
    fuel_real_val double       null comment '注油量实际值',
    tag_real_val  double       null comment '上传到铭牌的注油量',
    oper_id       int          null comment '操作员用户id',
    create_time   bigint       null comment '创建时间'
)
    comment '加油记录表';


# 创建用户表
create table sew.user
(
    id            int auto_increment
        primary key,
    username      varchar(80)   null,
    password      varchar(80)   null,
    real_name     varchar(80)   null comment '用户真实姓名',
    user_level    int default 1 null comment '用户等级 0:管理员 1:普通用户',
    register_date mediumtext    null comment '注册时间',
    state         int default 1 null comment '状态 0:无效(已删除)  1:有效(未删除)'
);

# 添加索引
create index idx_create_time
    on sew.fuel_record (create_time);

delete from sew.user where 1=1;
delete  from sew.fuel_record where 1=1;

# 添加管理员账号
insert into sew.user (id, username, password, real_name, user_level, register_date, state)
values  (1, 'sewadmin001', 'sew1qaz2wsx', 'sewadmin001', 0, '1652089474839', 1);
insert into sew.user (id, username, password, real_name, user_level, register_date, state)
values  (1, 'root', 'sew1qaz2wsx', '系统管理员', 0, '1652089474839', 1);



