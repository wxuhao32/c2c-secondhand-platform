-- =============================================
-- C2C二手交易平台数据库初始化脚本
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS resale_platform
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE resale_platform;

-- =============================================
-- 用户表
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `user_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) DEFAULT NULL COMMENT '用户名',
  `mobile` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 1正常 2锁定 3禁用',
  `login_fail_cnt` INT NOT NULL DEFAULT 0 COMMENT '连续登录失败次数',
  `locked_until` DATETIME DEFAULT NULL COMMENT '锁定截止时间',
  `last_login_ip` VARCHAR(50) DEFAULT NULL COMMENT '最后登录IP',
  `last_login_time` DATETIME DEFAULT NULL COMMENT '最后登录时间',
  `wechat_openid` VARCHAR(100) DEFAULT NULL COMMENT '微信OpenID',
  `wechat_unionid` VARCHAR(100) DEFAULT NULL COMMENT '微信UnionID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_mobile` (`mobile`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_wechat_openid` (`wechat_openid`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 用户收货地址表
-- =============================================
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `address_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '地址ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
  `mobile` VARCHAR(20) NOT NULL COMMENT '联系电话',
  `province` VARCHAR(50) NOT NULL COMMENT '省份',
  `city` VARCHAR(50) NOT NULL COMMENT '城市',
  `district` VARCHAR(50) NOT NULL COMMENT '区县',
  `detail_address` VARCHAR(255) NOT NULL COMMENT '详细地址',
  `postal_code` VARCHAR(10) DEFAULT NULL COMMENT '邮政编码',
  `is_default` TINYINT NOT NULL DEFAULT 0 COMMENT '是否默认: 0否 1是',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`address_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户收货地址表';

-- =============================================
-- 商品分类表
-- =============================================
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
  `category_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` BIGINT UNSIGNED DEFAULT 0 COMMENT '父分类ID',
  `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `icon` VARCHAR(100) DEFAULT NULL COMMENT '分类图标',
  `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
  `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态: 0禁用 1启用',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`category_id`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品分类表';

-- =============================================
-- 商品表
-- =============================================
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
  `product_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '商品ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '卖家ID',
  `category_id` BIGINT UNSIGNED NOT NULL COMMENT '分类ID',
  `title` VARCHAR(200) NOT NULL COMMENT '商品标题',
  `description` TEXT COMMENT '商品描述',
  `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
  `price` DECIMAL(10,2) NOT NULL COMMENT '售价',
  `images` TEXT COMMENT '图片JSON数组',
  `condition` TINYINT NOT NULL DEFAULT 1 COMMENT '成色: 1全新 2几乎全新 3轻微使用 4明显使用 5破损',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0待审核 1在售 2已下架 3已售出',
  `view_count` INT NOT NULL DEFAULT 0 COMMENT '浏览次数',
  `favorite_count` INT NOT NULL DEFAULT 0 COMMENT '收藏次数',
  `province` VARCHAR(50) DEFAULT NULL COMMENT '省份',
  `city` VARCHAR(50) DEFAULT NULL COMMENT '城市',
  `district` VARCHAR(50) DEFAULT NULL COMMENT '区县',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_price` (`price`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- =============================================
-- 订单表
-- =============================================
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `order_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(32) NOT NULL COMMENT '订单号',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `seller_id` BIGINT UNSIGNED NOT NULL COMMENT '卖家ID',
  `buyer_id` BIGINT UNSIGNED NOT NULL COMMENT '买家ID',
  `price` DECIMAL(10,2) NOT NULL COMMENT '成交价',
  `address_id` BIGINT UNSIGNED NOT NULL COMMENT '收货地址ID',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '状态: 0待付款 1待发货 2待收货 3已完成 4已取消 5退款中 6已退款',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `ship_time` DATETIME DEFAULT NULL COMMENT '发货时间',
  `receive_time` DATETIME DEFAULT NULL COMMENT '收货时间',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` TINYINT NOT NULL DEFAULT 0 COMMENT '删除标记',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_seller_id` (`seller_id`),
  KEY `idx_buyer_id` (`buyer_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- =============================================
-- 商品收藏表
-- =============================================
DROP TABLE IF EXISTS `favorite`;
CREATE TABLE `favorite` (
  `favorite_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `product_id` BIGINT UNSIGNED NOT NULL COMMENT '商品ID',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`favorite_id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品收藏表';

-- =============================================
-- 商品评价表
-- =============================================
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
  `review_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '评价ID',
  `order_id` BIGINT UNSIGNED NOT NULL COMMENT '订单ID',
  `from_user_id` BIGINT UNSIGNED NOT NULL COMMENT '评价用户ID',
  `to_user_id` BIGINT UNSIGNED NOT NULL COMMENT '被评价用户ID',
  `rating` TINYINT NOT NULL COMMENT '评分 1-5',
  `content` VARCHAR(500) DEFAULT NULL COMMENT '评价内容',
  `images` TEXT COMMENT '评价图片JSON',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`review_id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_from_user_id` (`from_user_id`),
  KEY `idx_to_user_id` (`to_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品评价表';

-- =============================================
-- 操作日志表
-- =============================================
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
  `log_id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '操作用户ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `operation_desc` VARCHAR(500) DEFAULT NULL COMMENT '操作描述',
  `request_method` VARCHAR(10) NOT NULL COMMENT '请求方法',
  `request_url` VARCHAR(500) NOT NULL COMMENT '请求URL',
  `request_params` TEXT COMMENT '请求参数',
  `response_code` INT DEFAULT NULL COMMENT '响应码',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `user_agent` VARCHAR(500) DEFAULT NULL COMMENT 'User-Agent',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- =============================================
-- 插入测试数据
-- =============================================

-- 插入测试用户（密码为 BCrypt 加密的 "Test123456"）
INSERT INTO `user` (`username`, `mobile`, `email`, `password_hash`, `nickname`, `status`, `create_time`, `update_time`) VALUES
('admin', '13800138000', 'admin@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.tQfbEG6mu9mvGq', '管理员', 1, NOW(), NOW()),
('testuser', '13800138001', 'test@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.tQfbEG6mu9mvGq', '测试用户', 1, NOW(), NOW()),
('seller01', '13800138002', 'seller@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/X4.tQfbEG6mu9mvGq', '卖家一号', 1, NOW(), NOW());

-- 插入商品分类
INSERT INTO `category` (`category_name`, `icon`, `sort_order`, `status`) VALUES
('数码', 'laptop', 1, 1),
('手机', 'phone', 2, 1),
('电脑', 'monitor', 3, 1),
('相机', 'camera', 4, 1),
('手表', 'watch', 5, 1),
('服饰', 'tshirt', 6, 1),
('图书', 'book', 7, 1),
('家居', 'home', 8, 1),
('运动', 'basketball', 9, 1),
('其他', 'ellipsis', 10, 1);

-- 插入测试商品
INSERT INTO `product` (`user_id`, `category_id`, `title`, `description`, `original_price`, `price`, `images`, `condition`, `status`, `province`, `city`, `district`) VALUES
(3, 2, 'iPhone 13 Pro 256G 国行99新', '国行正品，无划痕，配件齐全，在保', 7999.00, 4599.00, '["https://via.placeholder.com/400x400?text=iPhone1","https://via.placeholder.com/400x400?text=iPhone2"]', 2, 1, '北京市', '北京市', '朝阳区'),
(3, 1, 'MacBook Pro 14寸 M2芯片 16+512G', '2023年购买，使用次数不多，性能完好', 15999.00, 11999.00, '["https://via.placeholder.com/400x400?text=MacBook1"]', 3, 1, '北京市', '北京市', '海淀区'),
(3, 4, '索尼A7M4微单相机 机身', '专业级全画幅微单，快门数3000+，包装配件齐全', 16999.00, 12500.00, '["https://via.placeholder.com/400x400?text=Sony1"]', 3, 1, '上海市', '上海市', '浦东新区'),
(3, 5, 'Apple Watch Series 8 45mm GPS版', 'GPS款，蜂窝版可加价换购', 3199.00, 2199.00, '["https://via.placeholder.com/400x400?text=Watch1"]', 2, 1, '广东省', '深圳市', '南山区');

-- =============================================
-- 创建存储过程
-- =============================================

-- 清理过期锁定用户存储过程
DELIMITER //
CREATE PROCEDURE `sp_cleanup_expired_locks`()
BEGIN
  UPDATE user 
  SET status = 1, locked_until = NULL, login_fail_cnt = 0
  WHERE status = 2 
    AND locked_until IS NOT NULL 
    AND locked_until < NOW();
END //
DELIMITER ;

-- 创建事件调度器（每天凌晨3点执行清理）
SET GLOBAL event_scheduler = ON;
CREATE EVENT IF NOT EXISTS `evt_cleanup_expired_locks`
ON SCHEDULE EVERY 1 DAY
STARTS CURRENT_TIMESTAMP + INTERVAL 3 HOUR
DO CALL sp_cleanup_expired_locks();

-- =============================================
-- 创建视图
-- =============================================

-- 用户登录统计视图
CREATE OR REPLACE VIEW `v_user_login_stats` AS
SELECT 
  DATE(last_login_time) as login_date,
  COUNT(*) as login_count,
  COUNT(DISTINCT user_id) as unique_users
FROM user
WHERE last_login_time IS NOT NULL
GROUP BY DATE(last_login_time)
ORDER BY login_date DESC;

-- 商品销售排行视图
CREATE OR REPLACE VIEW `v_product_sales_rank` AS
SELECT 
  p.product_id,
  p.title,
  p.price,
  p.user_id,
  u.username,
  COUNT(o.order_id) as order_count,
  SUM(o.price) as total_amount
FROM product p
LEFT JOIN `order` o ON p.product_id = o.product_id AND o.status IN (2, 3)
LEFT JOIN user u ON p.user_id = u.user_id
GROUP BY p.product_id, p.title, p.price, p.user_id, u.username
ORDER BY order_count DESC, total_amount DESC;

-- =============================================
-- 性能优化索引
-- =============================================

-- 为商品表添加全文索引（可选，用于高级搜索）
ALTER TABLE product ADD FULLTEXT INDEX ft_title_desc (title, description);

-- =============================================
-- 数据库配置完成
-- =============================================

SELECT 'Database initialization completed successfully!' AS message;
