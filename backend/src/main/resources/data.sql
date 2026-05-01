-- H2 内存数据库初始数据脚本

-- 插入测试用户（密码：Test123456）
-- 密码使用 BCrypt 加密
INSERT INTO users (id, username, password, mobile, email, nickname, avatar, status, role, last_login_time, last_login_ip) VALUES
(1, 'admin', '$2a$12$938AubesljVp7HCTTQZf3O.HLlwkZDqcmGjwvdVkQLhT7NpMtT2xC', '13800138000', 'admin@example.com', '管理员', 'https://api.dicebear.com/7.x/avataaars/svg?seed=admin', 1, 'ADMIN', NULL, NULL),
(2, 'user1', '$2a$12$938AubesljVp7HCTTQZf3O.HLlwkZDqcmGjwvdVkQLhT7NpMtT2xC', '13800138001', 'user1@example.com', '测试用户1', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user1', 1, 'USER', NULL, NULL),
(3, 'user2', '$2a$12$938AubesljVp7HCTTQZf3O.HLlwkZDqcmGjwvdVkQLhT7NpMtT2xC', '13800138002', 'user2@example.com', '测试用户2', 'https://api.dicebear.com/7.x/avataaars/svg?seed=user2', 1, 'USER', NULL, NULL);

-- 插入商品分类
INSERT INTO category (id, name, parent_id, icon, sort_order) VALUES
(1, '数码电子', 0, '💻', 1),
(2, '手机', 1, '📱', 1),
(3, '电脑', 1, '💻', 2),
(4, '相机', 1, '📷', 3),
(5, '服装鞋包', 0, '👔', 2),
(6, '男装', 5, '👔', 1),
(7, '女装', 5, '👗', 2),
(8, '鞋靴', 5, '👟', 3),
(9, '箱包', 5, '👜', 4),
(10, '家居生活', 0, '🏠', 3),
(11, '家具', 10, '🛋️', 1),
(12, '家纺', 10, '🛏️', 2),
(13, '厨具', 10, '🍳', 3),
(14, '图书音像', 0, '📚', 4),
(15, '图书', 14, '📖', 1),
(16, '音像', 14, '💿', 2),
(17, '运动户外', 0, '⚽', 5),
(18, '运动器材', 17, '🏋️', 1),
(19, '户外装备', 17, '⛺', 2);

-- 插入测试商品
INSERT INTO goods (id, seller_id, title, description, price, original_price, category_id, images, status, view_count, like_count) VALUES
(1, 2, 'iPhone 13 Pro Max 256G 远峰蓝', '自用iPhone 13 Pro Max，成色99新，无划痕，电池健康度92%，带原装盒子和配件。', 6500.00, 8999.00, 2, '["https://via.placeholder.com/400x400/4A90E2/FFFFFF?text=iPhone+13+Pro+Max"]', 1, 128, 15),
(2, 2, 'MacBook Pro 14寸 M1 Pro芯片', '2021款MacBook Pro，16G内存+512G固态，轻度使用，外观完美。', 8500.00, 14999.00, 3, '["https://via.placeholder.com/400x400/7B68EE/FFFFFF?text=MacBook+Pro"]', 1, 256, 32),
(3, 3, '索尼A7M3相机机身', '索尼A7M3全画幅微单，快门次数5000左右，成色很新，功能正常。', 8500.00, 12000.00, 4, '["https://via.placeholder.com/400x400/FF6B6B/FFFFFF?text=Sony+A7M3"]', 1, 89, 8),
(4, 2, 'Nike Air Jordan 1 芝加哥配色', 'AJ1芝加哥，42码，全新未穿，鞋盒完好，保真。', 2800.00, 3500.00, 8, '["https://via.placeholder.com/400x400/E74C3C/FFFFFF?text=AJ1+Chicago"]', 1, 67, 23),
(5, 3, '宜家布艺沙发三人位', '宜家三人位布艺沙发，使用2年，保养良好，无污渍，需要自提。', 800.00, 2999.00, 11, '["https://via.placeholder.com/400x400/95A5A6/FFFFFF?text=IKEA+Sofa"]', 1, 45, 5),
(6, 2, '《Java编程思想》第4版', '经典Java书籍，九成新，无笔记划线，适合学习使用。', 45.00, 108.00, 15, '["https://via.placeholder.com/400x400/3498DB/FFFFFF?text=Java+Book"]', 1, 34, 2),
(7, 3, '迪卡侬山地自行车', '迪卡侬ST100山地车，26寸，21速，骑行不到100公里，几乎全新。', 600.00, 1299.00, 18, '["https://via.placeholder.com/400x400/2ECC71/FFFFFF?text=Bike"]', 1, 78, 12),
(8, 2, 'ZARA冬季羽绒服', 'ZARA男士羽绒服，L码，黑色，仅试穿，吊牌还在。', 350.00, 899.00, 6, '["https://via.placeholder.com/400x400/34495E/FFFFFF?text=ZARA+Coat"]', 1, 56, 7);

-- 插入测试订单
INSERT INTO orders (id, order_no, buyer_id, seller_id, goods_id, price, status, address, remark) VALUES
(1, '202404280001', 3, 2, 1, 6500.00, 3, '北京市朝阳区xxx街道xxx号', '请包装好一点'),
(2, '202404280002', 2, 3, 5, 800.00, 1, '上海市浦东新区xxx路xxx号', '周末送货'),
(3, '202404280003', 3, 2, 4, 2800.00, 2, '广州市天河区xxx街xxx号', '');

-- 插入测试收藏
INSERT INTO favorite (id, user_id, goods_id) VALUES
(1, 3, 1),
(2, 3, 2),
(3, 2, 5),
(4, 2, 7);

-- 插入测试消息
INSERT INTO message (id, sender_id, receiver_id, type, title, content, is_read) VALUES
(1, NULL, 2, 0, '系统通知', '欢迎来到二手交易平台！', 0),
(2, 3, 2, 1, NULL, '您好，请问iPhone还在吗？', 0),
(3, NULL, 3, 0, '订单提醒', '您的订单202404280001已完成', 1),
(4, 2, 3, 1, NULL, '在的，可以小刀', 1);
