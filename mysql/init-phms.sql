create table if not exists biz_care_logs
(
    id         bigint unsigned auto_increment
        primary key,
    order_id   bigint unsigned                    not null comment '关联订单ID',
    hotel_id   bigint unsigned                    not null comment '关联门店ID',
    staff_id   bigint unsigned                    not null comment '操作员工ID',
    care_type  tinyint                            not null comment '1=喂食 2=遛弯 3=清洁 4=体检 5=其他',
    content    varchar(500)                       null comment '护理详情（如：喂食皇家狗粮100g）',
    images     text                               null comment '图片URL列表（逗号分隔）',
    created_at datetime default CURRENT_TIMESTAMP null,
    is_deleted tinyint  default 0                 null comment '逻辑删除'
)
    comment '宠物护理日志表' charset = utf8mb4;

create index idx_hotel_id
    on biz_care_logs (hotel_id);

create index idx_order_id
    on biz_care_logs (order_id);

create table if not exists biz_hotel_reviews
(
    id         bigint unsigned auto_increment
        primary key,
    order_id   bigint unsigned                    not null comment '关联订单ID（唯一）',
    hotel_id   bigint unsigned                    not null comment '酒店ID',
    user_id    bigint unsigned                    not null comment '用户ID',
    score      tinyint                            not null comment '满意度评分：1-5',
    content    varchar(500)                       null comment '评价内容',
    created_at datetime default CURRENT_TIMESTAMP null,
    updated_at datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted tinyint  default 0                 null comment '逻辑删除',
    constraint uk_order_id
        unique (order_id)
)
    comment '酒店评价表' charset = utf8mb4;

create index idx_hotel_id
    on biz_hotel_reviews (hotel_id);

create index idx_user_id
    on biz_hotel_reviews (user_id);

create table if not exists biz_order_items
(
    id           bigint auto_increment
        primary key,
    order_id     bigint                             not null comment '订单ID',
    product_id   bigint                             not null comment '商品ID',
    product_name varchar(100)                       not null comment '商品名称',
    price        decimal(10, 2)                     not null comment '单价',
    quantity     int                                not null comment '数量',
    subtotal     decimal(10, 2)                     not null comment '小计',
    created_at   datetime default CURRENT_TIMESTAMP null,
    updated_at   datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted   tinyint  default 0                 null
)
    comment '订单商品明细';

create table if not exists biz_orders
(
    id             bigint unsigned auto_increment
        primary key,
    order_no       varchar(32)                        not null comment '订单号（唯一）',
    hotel_id       bigint unsigned                    not null comment '门店ID',
    user_id        bigint unsigned                    not null comment '用户ID',
    pet_ids        json                               null comment '宠物ID列表（JSON数组）',
    room_id        bigint unsigned                    not null comment '房间ID',
    check_in_date  date                               not null comment '预计入住日期',
    check_out_date date                               not null comment '预计离店日期',
    total_amount   decimal(10, 2)                     not null comment '订单总金额',
    remark         varchar(500)                       null comment '订单备注',
    pay_time       datetime                           null comment '支付时间',
    status         tinyint  default 0                 null comment '0=待支付 1=待入住 2=入住中 3=已完成 4=已取消',
    created_at     datetime default CURRENT_TIMESTAMP null,
    updated_at     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted     tinyint  default 0                 null comment '逻辑删除',
    description    varchar(500)                       null comment '房间描述',
    constraint uk_order_no
        unique (order_no)
)
    comment '订单表' charset = utf8mb4;

create index idx_hotel_date
    on biz_orders (hotel_id, check_in_date);

create index idx_user_id
    on biz_orders (user_id);

create table if not exists biz_pets
(
    id                  bigint unsigned auto_increment
        primary key,
    user_id             bigint unsigned                    not null comment '主人ID',
    name                varchar(50)                        not null comment '宠物名字',
    type                tinyint                            not null comment '1=猫 2=狗 3=异宠',
    age                 int                                null comment '年龄',
    weight              decimal(5, 2)                      null comment '宠物体重（kg）',
    notes               varchar(500)                       null comment '性格/健康备注',
    photo_url           varchar(255)                       null comment '宠物照片URL',
    rabies_vaccine_date date                               null comment '狂犬疫苗接种日期',
    deworming_date      date                               null comment '驱虫日期',
    vaccine_notes       varchar(500)                       null comment '疫苗/驱虫备注',
    created_at          datetime default CURRENT_TIMESTAMP null,
    updated_at          datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted          tinyint  default 0                 null comment '逻辑删除'
)
    comment '宠物档案表' charset = utf8mb4;

create index idx_user_id
    on biz_pets (user_id);

create table if not exists biz_product_hotels
(
    id         bigint auto_increment
        primary key,
    product_id bigint                             not null comment '商品ID',
    hotel_id   bigint                             not null comment '门店ID',
    created_at datetime default CURRENT_TIMESTAMP null,
    updated_at datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted tinyint  default 0                 null,
    constraint uk_product_hotel
        unique (product_id, hotel_id)
)
    comment '商品-门店关联';

create table if not exists biz_products
(
    id          bigint auto_increment
        primary key,
    name        varchar(100)                       not null comment '商品名称',
    category    varchar(50)                        null comment '分类',
    price       decimal(10, 2)                     not null comment '单价',
    stock       int      default 0                 not null comment '库存',
    description varchar(500)                       null comment '描述',
    images      varchar(2000)                      null comment '图片JSON',
    status      tinyint  default 1                 not null comment '状态 1=上架 0=下架',
    created_at  datetime default CURRENT_TIMESTAMP null,
    updated_at  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted  tinyint  default 0                 null
)
    comment '酒店商品表';

create table if not exists biz_rooms
(
    id              bigint unsigned auto_increment
        primary key,
    hotel_id        bigint unsigned                    not null comment '所属门店ID',
    room_no         varchar(20)                        not null comment '房间号（如A-101）',
    type_name       varchar(50)                        not null comment '房型（豪华猫屋/标准狗舍）',
    price_per_night decimal(10, 2)                     not null comment '每晚价格',
    max_pet_num     tinyint  default 1                 null comment '最大容纳宠物数',
    features        json                               null comment '设施标签（监控、空调等）',
    description     varchar(4096)                      null,
    images          json                               null comment '房间图片URL列表（JSON数组）',
    status          tinyint  default 0                 null comment '0=空闲 1=已预订 2=入住中 3=待清洁 4=维修',
    created_at      datetime default CURRENT_TIMESTAMP null,
    updated_at      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted      tinyint  default 0                 null comment '逻辑删除',
    constraint uk_hotel_room_no
        unique (hotel_id, room_no)
)
    comment '房间表' charset = utf8mb4;

create index idx_hotel_status
    on biz_rooms (hotel_id, status);

create table if not exists support_chat_message
(
    id         bigint auto_increment comment '主键'
        primary key,
    user_id    bigint                             not null comment '用户ID',
    staff_id   bigint                             not null comment '客服ID',
    sender     varchar(16)                        not null comment '发送方 user/staff',
    content    text                               not null comment '消息内容',
    room_id    bigint                             null comment '房间ID',
    hotel_id   bigint                             null comment '酒店ID',
    created_at datetime default CURRENT_TIMESTAMP not null comment '创建时间'
)
    comment '客服聊天记录';

create index idx_created_at
    on support_chat_message (created_at);

create index idx_user_staff_time
    on support_chat_message (user_id, staff_id, created_at);

create table if not exists sys_hotels
(
    id             bigint unsigned auto_increment
        primary key,
    name           varchar(100)                       not null comment '门店名称',
    code           varchar(50)                        null comment '门店编码（唯一标识）',
    address        varchar(255)                       not null comment '详细地址',
    province_code  varchar(20)                        null comment '省编码',
    city_code      varchar(20)                        null comment '市编码',
    district_code  varchar(20)                        null comment '区/县编码',
    address_detail varchar(255)                       null comment '详细地址（门牌号/楼层等）',
    manager_name   varchar(50)                        null comment '店长姓名',
    phone          varchar(20)                        not null comment '联系电话',
    status         tinyint  default 1                 null comment '1=营业 0=停业',
    images         json                               null comment '酒店图片URL列表（JSON数组）',
    created_at     datetime default CURRENT_TIMESTAMP null,
    updated_at     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted     tinyint  default 0                 null comment '逻辑删除',
    constraint uk_code
        unique (code)
)
    comment '门店信息表' charset = utf8mb4;

create table if not exists sys_login_logs
(
    id           bigint unsigned auto_increment
        primary key,
    login_type   tinyint                            not null comment '登录主体类型：1=C端用户 2=B端员工',
    user_id      bigint unsigned                    null comment 'C端用户ID（login_type=1时必填）',
    staff_id     bigint unsigned                    null comment 'B端员工ID（login_type=2时必填）',
    login_way    tinyint                            not null comment '登录方式：1=手机号验证码 2=邮箱密码 3=第三方登录（预留）',
    login_ip     varchar(50)                        not null comment '登录IP地址',
    device_info  varchar(255)                       null comment '设备信息（如浏览器型号、手机品牌）',
    login_status tinyint                            not null comment '登录状态：1=成功 0=失败',
    fail_reason  varchar(255)                       null comment '失败原因（如验证码错误、密码不匹配）',
    created_at   datetime default CURRENT_TIMESTAMP null comment '登录时间',
    is_deleted   tinyint  default 0                 null comment '逻辑删除'
)
    comment '登录日志表' charset = utf8mb4;

create index idx_create_time
    on sys_login_logs (created_at);

create index idx_login_type
    on sys_login_logs (login_type);

create table if not exists sys_message_center
(
    id            bigint unsigned auto_increment
        primary key,
    hotel_id      bigint unsigned                    null comment '所属门店ID（平台消息为NULL）',
    receiver_type tinyint                            not null comment '接收方类型：1=C端用户 2=B端员工',
    receiver_id   bigint unsigned                    null comment '接收方ID（sys_users.id或sys_staff.id）',
    title         varchar(100)                       not null comment '消息标题',
    content       text                               not null comment '消息内容',
    is_read       tinyint  default 0                 null comment '是否已读：0=未读 1=已读',
    read_time     datetime                           null comment '已读时间',
    created_at    datetime default CURRENT_TIMESTAMP null,
    updated_at    datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted    tinyint  default 0                 null comment '逻辑删除'
)
    comment '消息中心表' charset = utf8mb4;

create index idx_created_at
    on sys_message_center (created_at);

create index idx_hotel_id
    on sys_message_center (hotel_id);

create index idx_hotel_read
    on sys_message_center (hotel_id, is_read);

create index idx_receiver
    on sys_message_center (receiver_type, receiver_id);

create index idx_receiver_read
    on sys_message_center (receiver_type, receiver_id, is_read);

create table if not exists sys_operation_logs
(
    id               bigint unsigned auto_increment
        primary key,
    operator_type    tinyint                            not null comment '操作人类型：1=C端用户 2=B端员工',
    operator_id      bigint unsigned                    not null comment '操作人ID（关联sys_users.id或sys_staff.id）',
    operator_name    varchar(50)                        not null comment '操作人姓名/昵称（冗余存储，便于查询）',
    operation_module varchar(50)                        not null comment '操作模块：如order=订单管理、pet=宠物档案、room=房态管理',
    operation_type   varchar(20)                        not null comment '操作类型：add=新增 update=修改 delete=删除 query=查询',
    operation_param  text                               null comment '操作入参（JSON格式字符串，完整保留请求参数）',
    operation_result tinyint                            not null comment '操作结果：1=成功 0=失败',
    fail_msg         varchar(255)                       null comment '失败信息（如异常堆栈摘要）',
    operation_ip     varchar(50)                        not null comment '操作IP地址',
    operation_url    varchar(255)                       null comment '请求接口地址',
    created_at       datetime default CURRENT_TIMESTAMP null comment '操作时间',
    updated_at       datetime default CURRENT_TIMESTAMP null comment '更新时间',
    is_deleted       tinyint  default 0                 null comment '逻辑删除'
)
    comment '操作日志表' charset = utf8mb4;

create index idx_module_time
    on sys_operation_logs (operation_module, created_at);

create index idx_operator
    on sys_operation_logs (operator_type, operator_id);

create table if not exists sys_permissions
(
    id         bigint unsigned auto_increment
        primary key,
    perm_code  varchar(50)                        not null comment '权限编码（如 hotel:list）',
    perm_name  varchar(100)                       not null comment '权限名称',
    module     varchar(50)                        not null comment '所属模块',
    sort_order int      default 0                 null comment '排序',
    created_at datetime default CURRENT_TIMESTAMP null,
    is_deleted tinyint  default 0                 null comment '逻辑删除',
    constraint uk_perm_code
        unique (perm_code)
)
    comment '权限表' charset = utf8mb4;

create table if not exists sys_role_permissions
(
    id         bigint unsigned auto_increment
        primary key,
    role_id    tinyint unsigned                   not null comment '角色ID',
    perm_id    bigint unsigned                    not null comment '权限ID',
    created_at datetime default CURRENT_TIMESTAMP null,
    constraint uk_role_perm
        unique (role_id, perm_id)
)
    comment '角色权限关联表' charset = utf8mb4;

create index idx_role_id
    on sys_role_permissions (role_id);

create table if not exists sys_roles
(
    id          tinyint unsigned                   not null comment '角色ID（与role_type一致）'
        primary key,
    role_name   varchar(50)                        not null comment '角色名称',
    role_code   varchar(50)                        not null comment '角色编码',
    description varchar(255)                       null comment '角色描述',
    sort_order  int      default 0                 null comment '排序',
    status      tinyint  default 1                 null comment '1=正常 0=禁用',
    created_at  datetime default CURRENT_TIMESTAMP null,
    updated_at  datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted  tinyint  default 0                 null comment '逻辑删除',
    constraint uk_role_code
        unique (role_code)
)
    comment '角色表' charset = utf8mb4;

create table if not exists sys_staff
(
    id              bigint unsigned auto_increment
        primary key,
    hotel_id        bigint unsigned                    null comment '归属门店ID（超管为NULL）',
    email           varchar(100)                       not null comment '登录邮箱',
    phone           varchar(128)                       null comment '手机号',
    password        varchar(100)                       not null comment 'BCrypt加密密码',
    real_name       varchar(50)                        not null comment '真实姓名',
    role_type       tinyint                            not null comment '1=普通员工 2=店长 9=平台超管',
    status          tinyint  default 1                 null comment '1=正常 0=禁用',
    last_login_time datetime                           null comment '最后登录时间',
    created_at      datetime default CURRENT_TIMESTAMP null,
    updated_at      datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted      tinyint  default 0                 null comment '逻辑删除',
    constraint uk_email
        unique (email)
)
    comment '内部员工表' charset = utf8mb4;

create index idx_hotel_id
    on sys_staff (hotel_id);

create table if not exists sys_staff_schedule
(
    id         bigint unsigned auto_increment
        primary key,
    staff_id   bigint unsigned                    not null comment '员工ID',
    hotel_id   bigint unsigned                    null comment '门店ID',
    work_date  date                               not null comment '排班日期',
    shift_type tinyint                            not null comment '班次：1=早班 2=中班 3=晚班 4=全天班 5=休息',
    start_time time                               null comment '开始时间',
    end_time   time                               null comment '结束时间',
    created_at datetime default CURRENT_TIMESTAMP null,
    updated_at datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted tinyint  default 0                 null comment '逻辑删除'
)
    comment '员工排班表' charset = utf8mb4;

create index idx_staff_date
    on sys_staff_schedule (staff_id, work_date);

create table if not exists sys_staff_status
(
    staff_id   bigint unsigned                    not null comment '员工ID'
        primary key,
    hotel_id   bigint unsigned                    null comment '门店ID',
    status     tinyint  default 4                 not null comment '状态：1=在岗 2=离岗 3=忙碌 4=离线',
    updated_at datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment '员工在岗状态表' charset = utf8mb4;

create table if not exists sys_users
(
    id              bigint unsigned auto_increment
        primary key,
    phone           varchar(20)                              not null comment '手机号（登录/验证码）',
    password        varchar(100)                             null comment 'BCrypt加密密码（选填）',
    nickname        varchar(50)                              null comment '用户昵称',
    avatar          varchar(255)                             null comment '头像URL',
    balance         decimal(10, 2) default 0.00              null comment '账户余额',
    status          tinyint        default 1                 null comment '1=正常 0=禁用',
    last_login_time datetime                                 null comment '最后登录时间',
    created_at      datetime       default CURRENT_TIMESTAMP null,
    updated_at      datetime       default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    is_deleted      tinyint        default 0                 null comment '逻辑删除',
    constraint uk_phone
        unique (phone)
)
    comment 'C端用户表' charset = utf8mb4;

create table if not exists sys_verification_codes
(
    id          bigint unsigned auto_increment
        primary key,
    target      varchar(20)                        not null comment '手机号',
    code        varchar(6)                         not null comment '6位验证码',
    type        tinyint                            not null comment '1=登录 2=注册',
    expire_time datetime                           not null comment '过期时间',
    is_used     tinyint  default 0                 null comment '0=未使用 1=已使用',
    created_at  datetime default CURRENT_TIMESTAMP null,
    updated_at  datetime default CURRENT_TIMESTAMP not null,
    is_deleted  tinyint  default 0                 null comment '逻辑删除'
)
    comment '验证码记录表' charset = utf8mb4;

create index idx_target_type
    on sys_verification_codes (target, type);


