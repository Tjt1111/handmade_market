-- 手作趣集 - 完全按照文档要求生成的数据库表
CREATE DATABASE IF NOT EXISTS handmade_platform DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE handmade_platform;

-- ==============================================
-- 1. 管理员表 tb_admin
-- ==============================================
DROP TABLE IF EXISTS tb_admin;
CREATE TABLE tb_admin (
    admin_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员ID，自增',
    admin_account VARCHAR(50) NOT NULL UNIQUE COMMENT '管理员账号，唯一',
    admin_pwd VARCHAR(100) NOT NULL COMMENT '管理员密码，加密存储',
    admin_name VARCHAR(20) COMMENT '管理员真实姓名',
    permission_level INT COMMENT '1-超级管理员 2-普通管理员',
    create_time DATETIME COMMENT '账号创建时间',
    update_time DATETIME COMMENT '更新时间',
    status INT DEFAULT 1 COMMENT '0-禁用 1-正常'
) COMMENT='管理员表';

-- ==============================================
-- 2. 用户表 tb_user
-- ==============================================
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

-- ==============================================
-- 3. 商品表 tb_goods
-- ==============================================
DROP TABLE IF EXISTS tb_goods;
CREATE TABLE tb_goods (
    goods_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID，自增',
    creator_id INT COMMENT '创作者ID，外键',
    goods_name VARCHAR(50) COMMENT '商品名称',
    price DECIMAL(10,2) COMMENT '售价',
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
    stock INT NOT NULL DEFAULT 0 CHECK (stock >= 0) COMMENT '库存数量 ≥ 0',
    status INT COMMENT '0-待审核 1-已上架 2-已拒绝 3-已下架',
    audit_remark VARCHAR(200) COMMENT '审核备注/拒绝原因',
    FOREIGN KEY (creator_id) REFERENCES tb_user(user_id) -- 添加外键约束
) COMMENT='商品表';

-- ==============================================
-- 4. 定制需求表 tb_custom
-- ==============================================
DROP TABLE IF EXISTS tb_custom;
CREATE TABLE tb_custom (
    custom_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '定制需求ID',
    consumer_id INT NOT NULL COMMENT '消费者ID（发布人）',
    creator_id INT DEFAULT NULL COMMENT '接单创作者ID',
    
    custom_desc VARCHAR(500) NOT NULL COMMENT '需求描述',
    reference_images TEXT DEFAULT NULL COMMENT '参考图片，逗号分隔',
    
    quantity INT NOT NULL DEFAULT 1 COMMENT '定制/批发数量',
    is_wholesale BOOLEAN DEFAULT FALSE COMMENT '是否批发：FALSE-否 TRUE-是',
    budget DECIMAL(12,2) NOT NULL COMMENT '单件预算金额',
    final_unit_price DECIMAL(12,2) DEFAULT NULL COMMENT '最终成交单价',
    final_total_price DECIMAL(12,2) GENERATED ALWAYS AS (final_unit_price * quantity) STORED COMMENT '最终成交总价 = 单价 × 数量', 
    
    cycle INT NOT NULL COMMENT '期望周期（天）',
    category VARCHAR(50) NOT NULL COMMENT '定制品类',
    style VARCHAR(50) DEFAULT NULL COMMENT '定制风格',
    
    contact VARCHAR(50) DEFAULT NULL COMMENT '联系方式：电话/微信',
    deliver_content TEXT DEFAULT NULL COMMENT '交付内容/链接',
    
    submit_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    accept_time DATETIME DEFAULT NULL COMMENT '接单时间',
    finish_time DATETIME DEFAULT NULL COMMENT '完成时间',
    
    match_creators TEXT DEFAULT NULL COMMENT '匹配创作者ID，逗号分隔',
    status INT DEFAULT 0 COMMENT '0-待匹配 1-沟通中 2-已接单 3-已完成 4-已取消 5-已拒绝',
    remark TEXT DEFAULT NULL COMMENT '备注/拒绝原因/取消原因',
    
    FOREIGN KEY (consumer_id) REFERENCES tb_user(user_id),
    FOREIGN KEY (creator_id) REFERENCES tb_user(user_id),

    INDEX idx_consumer_id (consumer_id),
    INDEX idx_creator_id (creator_id),
    INDEX idx_status (status),
    INDEX idx_is_wholesale (is_wholesale)
) COMMENT='定制需求表';

-- ==============================================
-- 5. 订单表 tb_order（文档 5.3.5）
-- ==============================================
DROP TABLE IF EXISTS tb_order;
CREATE TABLE tb_order (
    order_id VARCHAR(50) PRIMARY KEY COMMENT '订单ID，组合生成',
    buyer_id INT COMMENT '买家ID',
    seller_id INT COMMENT '卖家ID',
    goods_id INT COMMENT '商品ID，普通订单使用',
    custom_id INT COMMENT '定制需求ID，定制订单使用',
    order_type INT COMMENT '1-普通订单 2-定制订单',
    amount DECIMAL(12,2) COMMENT '总金额',
    deposit DECIMAL(12,2) COMMENT '定金',
    balance DECIMAL(12,2) COMMENT '尾款',
    delivery_address VARCHAR(255) COMMENT '收货地址',
    logistics_info VARCHAR(200) COMMENT '物流信息',
    create_time DATETIME COMMENT '创建时间',
    pay_time DATETIME COMMENT '支付时间',
    delivery_time DATETIME COMMENT '发货时间',
    receive_time DATETIME COMMENT '确认收货时间',
    pay_type VARCHAR(20) COMMENT '支付方式：模拟支付宝/微信',
    status INT COMMENT '0-待支付 1-已支付 2-待发货 3-已发货 4-已完成 5-已取消',
    payment_status INT DEFAULT 0 COMMENT '支付状态：0-未支付 1-支付中 2-支付成功 3-支付失败',
    cancel_reason VARCHAR(200) COMMENT '订单取消原因',
    remark VARCHAR(200) COMMENT '订单备注',
    FOREIGN KEY (buyer_id) REFERENCES tb_user(user_id),
    FOREIGN KEY (seller_id) REFERENCES tb_user(user_id),
    FOREIGN KEY (goods_id) REFERENCES tb_goods(goods_id),
    FOREIGN KEY (custom_id) REFERENCES tb_custom_order(custom_order_id),
    CHECK ((goods_id IS NOT NULL AND custom_id IS NULL) OR (goods_id IS NULL AND custom_id IS NOT NULL))
) COMMENT='订单表';

-- ==============================================
-- 6. 评价表 tb_evaluation（文档 5.3.6）
-- ==============================================
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

-- ==============================================
-- 7. 信用记录表 tb_credit_record（文档 5.3.7）
-- ==============================================
DROP TABLE IF EXISTS tb_credit_record;
CREATE TABLE tb_credit_record (
    record_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    user_id INT COMMENT '用户ID',
    change_score INT COMMENT '变动分数',
    current_score INT COMMENT '变动后分数',
    reason VARCHAR(200) COMMENT '变动原因',
    related_id VARCHAR(50) COMMENT '关联ID：订单/评价',
    create_time DATETIME COMMENT '记录时间'
) COMMENT='信用记录表';

-- ==============================================
-- 8. 操作日志表 tb_operate_log（答辩表要求必须建）
-- ==============================================
DROP TABLE IF EXISTS tb_operate_log;
CREATE TABLE tb_operate_log (
    log_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    operator_id INT COMMENT '操作人ID',
    operator_type INT COMMENT '1-用户 2-管理员',
    module VARCHAR(50) COMMENT '操作模块：用户/商品/订单/评价',
    content VARCHAR(500) COMMENT '操作内容',
    ip VARCHAR(50) COMMENT '操作IP',
    operate_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间'
) COMMENT='系统操作日志表';

-- ==============================================
-- 9. 聊天消息表 tb_chat（需求分析+概要设计要求）
-- ==============================================
DROP TABLE IF EXISTS tb_chat;
CREATE TABLE tb_chat (
    msg_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    from_user_id INT COMMENT '发送方ID',
    to_user_id INT COMMENT '接收方ID',
    goods_id INT COMMENT '关联商品ID',
    custom_id INT COMMENT '关联定制ID',
    content VARCHAR(500) COMMENT '消息内容',
    image VARCHAR(255) COMMENT '图片',
    is_recall INT DEFAULT 0 COMMENT '0-未撤回 1-已撤回',
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间'
) COMMENT='聊天消息表';

-- ==============================================
-- 10. 订单商品关联表
-- ==============================================
DROP TABLE IF EXISTS tb_order_goods;
CREATE TABLE tb_order_goods (
    og_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    order_id VARCHAR(50) NOT NULL COMMENT '订单ID，关联tb_order',
    goods_id INT NOT NULL COMMENT '商品ID，关联tb_goods',
    goods_name VARCHAR(100) COMMENT '商品名称（快照）',
    price DECIMAL(10,2) COMMENT '下单时价格（快照）',
    num INT DEFAULT 1 COMMENT '购买数量，默认1',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT='订单商品关联表';

-- 定制订单表 tb_custom_order
DROP TABLE IF EXISTS tb_custom_order;
CREATE TABLE tb_custom_order (
    custom_order_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '定制订单ID，自增',
    user_id INT NOT NULL COMMENT '消费者ID，外键',
    goods_id INT NOT NULL COMMENT '商品ID，外键',
    customization_details TEXT COMMENT '定制内容',
    customization_price DECIMAL(10,2) NOT NULL COMMENT '定制价格',
    delivery_date DATE COMMENT '交付时间',
    status INT DEFAULT 0 COMMENT '订单状态：0-待支付，1-已支付，2-已完成',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '订单创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '订单更新时间',
    FOREIGN KEY (user_id) REFERENCES tb_user(user_id),
    FOREIGN KEY (goods_id) REFERENCES tb_goods(goods_id)
) COMMENT='定制订单表';
