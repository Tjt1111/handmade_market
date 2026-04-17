-- 手作趣集平台 - 完全按照概要设计生成
CREATE DATABASE IF NOT EXISTS handmade_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE handmade_platform;

-- 1. 管理员表 tb_admin
DROP TABLE IF EXISTS tb_admin;
CREATE TABLE tb_admin (
    admin_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID，自增',
    admin_account VARCHAR(50) NOT NULL UNIQUE COMMENT '管理员账号，唯一',
    admin_pwd VARCHAR(100) NOT NULL COMMENT '管理员密码，加密存储',
    admin_name VARCHAR(20) COMMENT '管理员真实姓名',
    permission_level INT COMMENT '1-超级管理员 2-普通管理员',
    create_time DATETIME COMMENT '创建时间',
    update_time DATETIME COMMENT '更新时间',
    status INT DEFAULT 1 COMMENT '0-禁用 1-正常'
) COMMENT='管理员表';

-- 2. 用户表 tb_user
DROP TABLE IF EXISTS tb_user;
CREATE TABLE tb_user (
    user_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID，自增',
    user_account VARCHAR(50) NOT NULL UNIQUE COMMENT '用户账号，唯一',
    user_pwd VARCHAR(100) NOT NULL COMMENT '用户密码，加密存储',
    user_name VARCHAR(20) COMMENT '用户昵称/真实姓名',
    phone VARCHAR(11) COMMENT '手机号',
    email VARCHAR(50) COMMENT '邮箱',
    avatar VARCHAR(255) COMMENT '头像链接',
    address VARCHAR(255) COMMENT '收货/发货地址',
    role INT COMMENT '1-消费者 2-创作者',
    specialty VARCHAR(100) COMMENT '创作者擅长领域',
    credit_score INT DEFAULT 80 COMMENT '信用分，初始80',
    register_time DATETIME COMMENT '注册时间',
    last_login_time DATETIME COMMENT '最后登录时间',
    status INT DEFAULT 1 COMMENT '0-禁用 1-正常 2-锁定'
) COMMENT='用户表';

-- 3. 商品表 tb_goods
DROP TABLE IF EXISTS tb_goods;
CREATE TABLE tb_goods (
    goods_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID，自增',
    creator_id INT COMMENT '创作者ID，外键',
    goods_name VARCHAR(50) COMMENT '商品名称',
    price DECIMAL(10,2) COMMENT '商品原价',
    reserve_price DECIMAL(10,2) COMMENT '底价',
    material VARCHAR(50) COMMENT '材质',
    size VARCHAR(50) COMMENT '尺寸',
    style VARCHAR(50) COMMENT '风格',
    delivery_cycle INT COMMENT '发货周期（天）',
    details VARCHAR(500) COMMENT '商品详情',
    images VARCHAR(500) COMMENT '商品图片，逗号分隔',
    category VARCHAR(50) COMMENT '商品分类',
    publish_time DATETIME COMMENT '发布时间',
    audit_time DATETIME COMMENT '审核时间',
    auditor_id INT COMMENT '审核管理员ID',
    status INT COMMENT '0-待审核 1-已上架 2-已拒绝 3-已下架',
    audit_remark VARCHAR(200) COMMENT '审核备注/拒绝原因'
) COMMENT='商品表';

-- 4. 定制需求表 tb_custom
DROP TABLE IF EXISTS tb_custom;
CREATE TABLE tb_custom (
    custom_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '需求ID，自增',
    consumer_id INT COMMENT '消费者ID',
    custom_desc VARCHAR(500) COMMENT '定制需求描述',
    reference_images VARCHAR(500) COMMENT '参考图片',
    budget DECIMAL(10,2) COMMENT '预算',
    cycle INT COMMENT '期望完成周期（天）',
    category VARCHAR(50) COMMENT '定制品类',
    style VARCHAR(50) COMMENT '定制风格',
    submit_time DATETIME COMMENT '提交时间',
    match_creators VARCHAR(200) COMMENT '匹配创作者ID，逗号分隔',
    status INT COMMENT '0-待匹配 1-沟通中 2-已接单 3-已完成 4-已取消'
) COMMENT='定制需求表';

-- 5. 订单表 tb_order
DROP TABLE IF EXISTS tb_order;
CREATE TABLE tb_order (
    order_id VARCHAR(50) PRIMARY KEY COMMENT '订单ID，组合生成',
    buyer_id INT COMMENT '买家ID',
    seller_id INT COMMENT '卖家ID',
    goods_id INT COMMENT '商品ID，普通订单非空',
    custom_id INT COMMENT '定制需求ID，定制订单非空',
    order_type INT COMMENT '1-普通订单 2-定制订单',
    amount DECIMAL(10,2) COMMENT '订单金额',
    deposit DECIMAL(10,2) COMMENT '定金',
    balance DECIMAL(10,2) COMMENT '尾款',
    delivery_address VARCHAR(255) COMMENT '收货地址',
    logistics_info VARCHAR(200) COMMENT '物流信息',
    create_time DATETIME COMMENT '创建时间',
    pay_time DATETIME COMMENT '支付时间',
    delivery_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '确认收货时间',
    pay_type VARCHAR(20) COMMENT '支付方式：模拟支付宝/微信',
    status INT COMMENT '0-待支付 1-已支付 2-待发货 3-已发货 4-已完成 5-已取消',
    remark VARCHAR(200) COMMENT '订单备注'
) COMMENT='订单表';

-- 6. 评价表 tb_evaluation
DROP TABLE IF EXISTS tb_evaluation;
CREATE TABLE tb_evaluation (
    eval_id VARCHAR(30) PRIMARY KEY COMMENT '评价ID，时间戳',
    order_id VARCHAR(50) COMMENT '订单ID',
    evaluator_id INT COMMENT '评价人ID',
    evaluated_id INT COMMENT '被评价人ID',
    score INT COMMENT '1-5星',
    content VARCHAR(200) COMMENT '评价内容',
    images VARCHAR(500) COMMENT '评价图片',
    create_time DATETIME COMMENT '评价时间',
    status INT DEFAULT 0 COMMENT '0-正常 1-违规 2-已删除'
) COMMENT='评价表';

-- 7. 信用记录表 tb_credit_record
DROP TABLE IF EXISTS tb_credit_record;
CREATE TABLE tb_credit_record (
    record_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID，自增',
    user_id INT COMMENT '用户ID',
    change_score INT COMMENT '信用分变化值',
    current_score INT COMMENT '变化后当前信用分',
    reason VARCHAR(200) COMMENT '变化原因',
    related_id VARCHAR(50) COMMENT '关联ID：订单/商品/评价',
    create_time DATETIME COMMENT '记录时间'
) COMMENT='信用记录表';

-- 8. 系统操作日志表（配套新增）
DROP TABLE IF EXISTS tb_sys_log;
CREATE TABLE tb_sys_log (
    log_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID，自增',
    operator_id INT COMMENT '操作人ID（用户ID/管理员ID）',
    operator_type INT COMMENT '操作人类型：1-用户 2-管理员',
    module VARCHAR(50) COMMENT '操作模块：用户/商品/订单/评价/信用/系统',
    operate_type VARCHAR(50) COMMENT '操作类型：新增/修改/删除/登录/审核/下架等',
    content VARCHAR(500) COMMENT '操作内容描述',
    ip VARCHAR(50) COMMENT '操作IP地址',
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
) COMMENT='系统操作日志表';

