# 创建加油记录表
create table sew.warn_record
(
    id            int auto_increment
        primary key,
    warn_type    bigint       null comment '告警类型',
    warn_msg varchar(255) null comment '告警内容',
    create_time   bigint       null comment '创建时间'
)
    comment '告警记录表';



alter table fuel_record add column oil_type varchar(255) null;
