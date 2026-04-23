-- ----------------------------
-- sm-console · commons schema (MySQL 8.4)
-- utf8mb4 / utf8mb4_0900_ai_ci
-- ----------------------------
SET NAMES utf8mb4;
SET SESSION sql_mode = 'STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ENGINE_SUBSTITUTION';

drop table if exists cnsl_rds_version;
create table cnsl_rds_version (
    version_id           bigint      not null auto_increment    comment '版本ID',
    software_name        varchar(200)    not null                   comment '软件名称',
    version_no           varchar(100)    not null                   comment '版本',
    default_group_id     bigint      default null               comment '默认模版组ID',
    default_version      tinyint      default 1                  comment '是否为默认版本',
    status               char(1)         not null                   comment '状态（1正常 0停用）',
    create_by            varchar(64)     default ''                 comment '创建者',
    create_time          datetime                                   comment '创建时间',
    update_by            varchar(64)     default ''                 comment '更新者',
    update_time          datetime                                   comment '更新时间',
    remark               varchar(500)    default null               comment '备注',
    primary key (version_id),
    unique key cnsl_rds_version_uniq (software_name,version_no)
) engine=innodb AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT = '版本信息';

drop table if exists cnsl_rds_version_pkg;
create table cnsl_rds_version_pkg (
    package_id           bigint      not null auto_increment    comment '安装包ID',
    version_id           bigint      not null                   comment '版本ID',
    pkg_name             varchar(200)    not null                   comment '包名称',
    pkg_type             varchar(30)     not null                   comment '包类类型: cnsl_package_type',
    file_name            varchar(200)    not null                   comment '文件名称（下载时使用不显示给用户）',
    file_size            int         not null                   comment '文件大小',
    create_by            varchar(64)     default ''                 comment '创建者',
    create_time          datetime                                   comment '创建时间',
    update_by            varchar(64)     default ''                 comment '更新者',
    update_time          datetime                                   comment '更新时间',
    remark               varchar(500)    default null               comment '备注',
    primary key (package_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT = '安装包信息';


drop table if exists cnsl_template_group;
CREATE TABLE cnsl_template_group (
    group_id           bigint        not null auto_increment     comment '模版组ID',
    group_name         varchar(200)      not null                    comment '模版组名称',
    create_by            varchar(64)     default ''                  comment '创建者',
    create_time          datetime                                    comment '创建时间',
    update_by            varchar(64)     default ''                  comment '更新者',
    update_time          datetime                                    comment '更新时间',
    remark               varchar(500)    default null                comment '备注',
    primary key (group_id),
    unique cnsl_template_group_uniq (group_name)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置模版组';

insert into cnsl_template_group values(1, '默认模版组', 'admin', CURRENT_TIMESTAMP(), '', null, '');

drop table if exists cnsl_template_group_version;
CREATE TABLE cnsl_template_group_version (
    group_id           bigint        not null     comment '模版组ID',
    version_id           bigint      not null     comment '版本ID',
    primary key (group_id, version_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='模版组适用版本';

insert into cnsl_template_group_version values(1, 1);

drop table if exists cnsl_template;
CREATE TABLE cnsl_template (
    template_id           bigint      not null auto_increment    comment '模版ID',
    group_id              bigint      not null                   comment '模版组ID',
    temp_name             varchar(200)    default null               comment '模版名称',
    temp_content          text            not null                   comment '模版内容',
    temp_type             varchar(30)     not null                   comment '对应模版类型: cnsl_template_type',
    create_by             varchar(64)     default ''                  comment '创建者',
    create_time           datetime                                    comment '创建时间',
    update_by             varchar(64)     default ''                  comment '更新者',
    update_time           datetime                                    comment '更新时间',
    remark                varchar(500)    default null                comment '备注',
    primary key (template_id),
    unique key cnsl_template_uniq (group_id, temp_type)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='配置模版';


-- 命令执行历史表中 --
drop table if exists cnsl_command_history;
CREATE TABLE cnsl_command_history (
    history_id          bigint      not null auto_increment    comment '历史ID',
    command_type        varchar(30)     not null                   comment '操作类型 cnsl_command_type',
    node_id             bigint      default null               comment '节点ID',
    manager_id          bigint      default null               comment '节点管理器ID',
    cmd                 varchar(500)     not null                   comment '操作指令',
    cmd_msg             text            default null               comment '操作指令内容',
    cmd_file            varchar(1000)   default ''                 comment '操作指令文件名',
    res_status          varchar(30)     not null                   comment '指令执行结果状态 cnsl_command_result',
    res_msg             text            default null               comment '指令执行结果消息内容',
    create_time         datetime                                   comment '创建时间',
    duration            bigint                                 comment '执行时长(毫秒)',
    del_flag            char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
    primary key (history_id),
    key command_history_idx1 (manager_id),
    key command_history_idx2 (node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='执行命令历史表';


-- ----------------------------
-- 用户信息表
-- ----------------------------
drop table if exists sys_user;
create table sys_user (
  user_id           bigint      not null auto_increment    comment '用户ID',
  user_name         varchar(30)     not null                   comment '用户账号',
  nick_name         varchar(30)     not null                   comment '用户昵称',
  user_type         varchar(2)      default '00'               comment '用户类型（00系统用户）',
  email             varchar(50)     default ''                 comment '用户邮箱',
  phonenumber       varchar(11)     default ''                 comment '手机号码',
  sex               char(1)         default '0'                comment '用户性别（0男 1女 2未知）',
  avatar            varchar(100)    default ''                 comment '头像地址',
  password          varchar(100)    default ''                 comment '密码',
  status            char(1)         default '0'                comment '帐号状态（0正常 1停用）',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  login_ip          varchar(128)    default ''                 comment '最后登录IP',
  login_date        datetime                                   comment '最后登录时间',
  login_retries     int          default 0                  comment '登录重试次数,登录成功后重置为0',
  login_locked      char(1)         default '0'                comment '登录锁定（0正常 1锁定）',
  password_expired  datetime                                   comment '密码过期时间',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (user_id),
  key user_name (user_name)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户信息表';

-- ----------------------------
-- 初始化-用户信息表数据
-- ----------------------------
insert into sys_user values(1, 'admin', '系统管理员', '00', 'admin@company.com', '16888888888', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', CURRENT_TIMESTAMP(), 0, '0', CURRENT_TIMESTAMP(), 'admin', CURRENT_TIMESTAMP(), '', null, '系统管理员');
insert into sys_user values(4, 'sysadmin', '系统管理员', '00', 'sysadmin@company.com', '16888888889', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', CURRENT_TIMESTAMP(), 0, '0', CURRENT_TIMESTAMP(), 'admin', CURRENT_TIMESTAMP(), '', null, '系统管理员');
insert into sys_user values(5, 'safeadmin', '安全管理员', '00', 'safeadmin@company.com', '16888888810', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', CURRENT_TIMESTAMP(), 0, '0', CURRENT_TIMESTAMP(), 'admin', CURRENT_TIMESTAMP(), '', null, '安全管理员');
insert into sys_user values(6, 'auditadmin', '审计管理员', '00', 'auditadmin@company.com', '16888888811', '1', '', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIrdAu2', '0', '0', '127.0.0.1', CURRENT_TIMESTAMP(), 0, '0', CURRENT_TIMESTAMP(), 'admin', CURRENT_TIMESTAMP(), '', null, '审计管理员');


-- ----------------------------
-- 角色信息表
-- ----------------------------
drop table if exists sys_role;
create table sys_role (
  role_id              bigint      not null auto_increment    comment '角色ID',
  role_name            varchar(30)     not null                   comment '角色名称',
  role_key             varchar(100)    not null                   comment '角色权限字符串',
  role_sort            int          not null                   comment '显示顺序',
  menu_check_strictly  tinyint      default 1                  comment '菜单树选择项是否关联显示',
  namespaces           varchar(3000)   default null               comment '角色可用的命名空间',
  status               char(1)         not null                   comment '角色状态（0正常 1停用）',
  del_flag             char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  create_by            varchar(64)     default ''                 comment '创建者',
  create_time          datetime                                   comment '创建时间',
  update_by            varchar(64)     default ''                 comment '更新者',
  update_time          datetime                                   comment '更新时间',
  remark               varchar(500)    default null               comment '备注',
  primary key (role_id)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色信息表';

insert into sys_role values(1, '超级管理员',  'admin',    1, 1, null, '0', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '超级管理员');
insert into sys_role values(4, '系统管理员',  'sysadmin', 4, 1, null, '0', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '系统管理员');
insert into sys_role values(5, '安全管理员',  'securityadmin', 5, 1, null, '0', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '安全管理员');
insert into sys_role values(6, '审计管理员',  'auditadmin', 6, 1, null, '0', '0', 'admin', CURRENT_TIMESTAMP(), '', null, '审计管理员');


-- ----------------------------
-- 用户和角色关联表  用户N-1角色
-- ----------------------------
drop table if exists sys_user_role;
create table sys_user_role (
  user_id   bigint not null comment '用户ID',
  role_id   bigint not null comment '角色ID',
  primary key(user_id, role_id)
) engine=innodb DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci comment = '用户和角色关联表';

insert into sys_user_role values ('1', '1');
insert into sys_user_role values ('4', '4');
insert into sys_user_role values ('5', '5');
insert into sys_user_role values ('6', '6');


-- ----------------------------
-- 角色和菜单关联表  角色1-N菜单
-- ----------------------------
drop table if exists sys_role_menu;
create table sys_role_menu (
  role_id     bigint    not null comment '角色ID',
  menu_code   varchar(100)  not null comment '菜单编码',
  primary key(role_id, menu_code)
) engine=innodb DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci comment = '角色和菜单关联表';

-- 插入系统管理员角色的菜单权限数据（menu_code）
-- 系统管理员(role_id=4): 无初始菜单权限绑定（admin用户自动拥有所有权限）

-- 安全管理员(role_id=5)
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:user');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:user:query');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:user:add');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:user:edit');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:user:remove');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:user:export');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:user:import');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:user:resetPwd');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:role');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:role:query');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:role:add');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:role:edit');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:role:remove');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(5, 'system:role:export');
-- 审计管理员(role_id=6)
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor:online');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor:online:query');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor:online:batchLogout');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor:online:forceLogout');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'log');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor:operlog');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor:operlog:query');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor:operlog:remove');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor:operlog:export');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor:logininfor');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor:logininfor:query');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor:logininfor:remove');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'monitor:logininfor:export');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'console:commandhistory');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'console:commandhistory:query');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'console:commandhistory:remove');
INSERT INTO sys_role_menu (role_id, menu_code) VALUES(6, 'console:commandhistory:export');

-- ----------------------------
-- 租户信息
-- ----------------------------
drop table if exists sys_user_tenant;
drop table if exists sys_tenant;
create table sys_tenant (
    tenant_id   bigint      not null auto_increment    comment '租户ID',
    tenant_name varchar(100)    not null                   comment '租户名称',
    status      char(1)         not null default '0'       comment '状态（0正常 1停用）',
    create_by   varchar(64)     default ''                 comment '创建者',
    create_time datetime                                   comment '创建时间',
    update_by   varchar(64)     default ''                 comment '更新者',
    update_time datetime                                   comment '更新时间',
    remark      varchar(500)    default null               comment '备注',
    primary key (tenant_id),
    unique key sys_tenant_name_uniq (tenant_name)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='租户信息';



-- ----------------------------
-- 用户-租户关联表
-- ----------------------------
create table sys_user_tenant (
    user_id     bigint  not null    comment '用户ID',
    tenant_id   bigint  not null    comment '租户ID',
    primary key (user_id, tenant_id),
    key sys_user_tenant_idx1 (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户-租户关联表';

-- 平台管理员（admin）用户无需关联，通过角色判断；此处为默认用户关联示例
-- 非管理员用户需要在 sys_user_tenant 中关联对应租户才能访问

-- ----------------------------
-- 操作日志记录
-- ----------------------------
drop table if exists sys_oper_log;
create table sys_oper_log (
  oper_id           bigint      not null auto_increment    comment '日志主键',
  title             varchar(50)     default ''                 comment '模块标题',
  business_type     varchar(50)     default ''                 comment '业务类型',
  method            varchar(100)    default ''                 comment '方法名称',
  request_method    varchar(10)     default ''                 comment '请求方式',
  operator_type     int          default 0                  comment '操作类别（0其它 1后台用户 2手机端用户）',
  oper_name         varchar(50)     default ''                 comment '操作人员',
  oper_url          varchar(255)    default ''                 comment '请求URL',
  oper_ip           varchar(128)    default ''                 comment '主机地址',
  oper_location     varchar(255)    default ''                 comment '操作地点',
  oper_param        varchar(2000)   default ''                 comment '请求参数',
  json_result       varchar(2000)   default ''                 comment '返回参数',
  status            int          default 0                  comment '操作状态（0正常 1异常）',
  error_msg         varchar(2000)   default ''                 comment '错误消息',
  oper_time         datetime                                   comment '操作时间',
  del_flag          char(1)         default '0'                comment '删除标志（0代表存在 2代表删除）',
  primary key (oper_id),
    key sys_oper_log_idx2 (oper_time)
) engine=innodb AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci comment = '操作日志记录';



-- ----------------------------
-- 参数配置表
-- 变更（20230831） config_value 改为 text类型， config_key  varchar长增到1000。
-- ----------------------------
drop table if exists sys_config;
create table sys_config (
  config_id         int          not null auto_increment    comment '参数主键',
  config_name       varchar(100)    default ''                 comment '参数名称',
  config_key        varchar(200)    default ''                 comment '参数键名',
  config_value      mediumtext      not null                   comment '参数键值',
  config_type       char(1)         default 'N'                comment '系统内置（Y是 N否）',
  create_by         varchar(64)     default ''                 comment '创建者',
  create_time       datetime                                   comment '创建时间',
  update_by         varchar(64)     default ''                 comment '更新者',
  update_time       datetime                                   comment '更新时间',
  remark            varchar(500)    default null               comment '备注',
  primary key (config_id),
  unique sys_config_uniq (config_key)
) engine=innodb AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci comment = '参数配置表';

insert into sys_config values(1, '主框架页-默认皮肤样式名称',     'sys.index.skinName',            'skin-blue',     'Y', 'admin', CURRENT_TIMESTAMP(), '', null, '蓝色 skin-blue、绿色 skin-green、紫色 skin-purple、红色 skin-red、黄色 skin-yellow' );
insert into sys_config values(3, '主框架页-侧边栏主题',           'sys.index.sideTheme',           'theme-dark',    'Y', 'admin', CURRENT_TIMESTAMP(), '', null, '深色主题theme-dark，浅色主题theme-light' );
insert into sys_config values(8, '系统是否完成初始化',           'sys.initialized',      'false',         'Y', 'admin', CURRENT_TIMESTAMP(), '', null, '系统是否完成初始化(true完成，false未完成)');
insert into sys_config values(9, '中心节点的认证Key',         'cnsl.rds.center.admin.authKey',  'abcef12376se790fowieaawew90qa8ew8e',     'N', 'admin', CURRENT_TIMESTAMP(), '', null, 'RDS中心节点admin端口的认证key');
insert into sys_config values(10, '节点管理器端口认证Key',      'cnsl.probe.authKey', 'x6wr6v6qyiqh7r2ha1sf240ad6dr7x670x', 'N', 'admin', CURRENT_TIMESTAMP(), '', null, '节点管理器端口认证Key');
insert into sys_config values(13, '预警发送失败默认失败次数',      'cnsl.alarm.sendFail', '3', 'Y', 'admin', CURRENT_TIMESTAMP(), '', null, '预警发送失败默认失败次数');
insert into sys_config values(16, '预警发送邮箱',      'cnsl.alarm.sendMail', '', 'Y', 'admin', CURRENT_TIMESTAMP(), '', null, '预警发送邮箱');
insert into sys_config values(18, '自定义密码强度',      'sys.user.password.strength', '{"level":[1,3,4],"maxLength":39,"minLength":6}', 'Y', 'admin', CURRENT_TIMESTAMP(), '', null, '用户自定义密码强度');


-- ----------------------------
-- 系统访问记录
-- ----------------------------
drop table if exists sys_logininfor;
create table sys_logininfor (
  info_id        bigint     not null auto_increment   comment '访问ID',
  user_name      varchar(50)    default ''                comment '用户账号',
  ipaddr         varchar(128)   default ''                comment '登录IP地址',
  login_location varchar(255)   default ''                comment '登录地点',
  browser        varchar(50)    default ''                comment '浏览器类型',
  os             varchar(50)    default ''                comment '操作系统',
  status         char(1)        default '0'               comment '登录状态（0成功 1失败）',
  msg            varchar(255)   default ''                comment '提示消息',
  login_time     datetime                                 comment '访问时间',
  primary key (info_id),
    key idx_user_name (user_name),
    key idx_ipaddr (login_time)
) engine=innodb AUTO_INCREMENT=100 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci comment = '系统访问记录';

