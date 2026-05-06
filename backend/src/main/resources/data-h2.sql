-- 插入测试用户（不指定ID，由数据库自动分配）
INSERT INTO users (username, password, mobile, email, nickname, status, role)
SELECT 'admin', '$2a$12$938AubesljVp7HCTTQZf3O.HLlwkZDqcmGjwvdVkQLhT7NpMtT2xC', '13800138000', 'admin@example.com', '管理员', 1, 'ADMIN'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, mobile, email, nickname, status, role)
SELECT 'user1', '$2a$12$938AubesljVp7HCTTQZf3O.HLlwkZDqcmGjwvdVkQLhT7NpMtT2xC', '13800138001', 'user1@example.com', '测试用户1', 1, 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user1');

INSERT INTO users (username, password, mobile, email, nickname, status, role)
SELECT 'user2', '$2a$12$938AubesljVp7HCTTQZf3O.HLlwkZDqcmGjwvdVkQLhT7NpMtT2xC', '13800138002', 'user2@example.com', '测试用户2', 1, 'USER'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user2');

-- 插入分类数据
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '数码电子', 0, '💻', 1 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '数码电子');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '手机', 1, '📱', 1 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '手机' AND parent_id = 1);
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '电脑', 1, '💻', 2 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '电脑' AND parent_id = 1);
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '相机', 1, '📷', 3 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '相机' AND parent_id = 1);
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '服装鞋包', 0, '👔', 2 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '服装鞋包');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '男装', 5, '👔', 1 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '男装');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '女装', 5, '👗', 2 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '女装');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '鞋靴', 5, '👟', 3 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '鞋靴');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '箱包', 5, '👜', 4 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '箱包');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '家居生活', 0, '🏠', 3 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '家居生活');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '家具', 10, '🛋️', 1 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '家具');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '家纺', 10, '🛏️', 2 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '家纺');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '厨具', 10, '🍳', 3 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '厨具');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '图书音像', 0, '📚', 4 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '图书音像');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '图书', 14, '📖', 1 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '图书');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '音像', 14, '💿', 2 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '音像');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '运动户外', 0, '⚽', 5 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '运动户外');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '运动器材', 17, '🏋️', 1 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '运动器材');
INSERT INTO category (name, parent_id, icon, sort_order)
SELECT '户外装备', 17, '⛺', 2 WHERE NOT EXISTS (SELECT 1 FROM category WHERE name = '户外装备');

-- 插入商品数据（seller_id 使用子查询动态获取）
INSERT INTO goods (seller_id, title, description, price, original_price, category_id, status, view_count, like_count)
SELECT u.id, 'iPhone 13 Pro Max 256G 远峰蓝', '自用iPhone 13 Pro Max，成色99新，无划痕，电池健康度92%，带原装盒子和配件。', 6500.00, 8999.00, 2, 1, 128, 15
FROM users u WHERE u.username = 'user1'
AND NOT EXISTS (SELECT 1 FROM goods WHERE title = 'iPhone 13 Pro Max 256G 远峰蓝');

INSERT INTO goods (seller_id, title, description, price, original_price, category_id, status, view_count, like_count)
SELECT u.id, 'MacBook Pro 14寸 M1 Pro芯片', '2021款MacBook Pro，16G内存+512G固态，轻度使用，外观完美。', 8500.00, 14999.00, 3, 1, 256, 32
FROM users u WHERE u.username = 'user1'
AND NOT EXISTS (SELECT 1 FROM goods WHERE title = 'MacBook Pro 14寸 M1 Pro芯片');

INSERT INTO goods (seller_id, title, description, price, original_price, category_id, status, view_count, like_count)
SELECT u.id, '索尼A7M3相机机身', '索尼A7M3全画幅微单，快门次数5000左右，成色很新，功能正常。', 8500.00, 12000.00, 4, 1, 89, 8
FROM users u WHERE u.username = 'user2'
AND NOT EXISTS (SELECT 1 FROM goods WHERE title = '索尼A7M3相机机身');

INSERT INTO goods (seller_id, title, description, price, original_price, category_id, status, view_count, like_count)
SELECT u.id, 'Nike Air Jordan 1 芝加哥配色', 'AJ1芝加哥，42码，全新未穿，鞋盒完好，保真。', 2800.00, 3500.00, 8, 1, 67, 23
FROM users u WHERE u.username = 'user1'
AND NOT EXISTS (SELECT 1 FROM goods WHERE title = 'Nike Air Jordan 1 芝加哥配色');

INSERT INTO goods (seller_id, title, description, price, original_price, category_id, status, view_count, like_count)
SELECT u.id, '宜家布艺沙发三人位', '宜家三人位布艺沙发，使用2年，保养良好，无污渍，需要自提。', 800.00, 2999.00, 11, 1, 45, 5
FROM users u WHERE u.username = 'user2'
AND NOT EXISTS (SELECT 1 FROM goods WHERE title = '宜家布艺沙发三人位');

INSERT INTO goods (seller_id, title, description, price, original_price, category_id, status, view_count, like_count)
SELECT u.id, '《Java编程思想》第4版', '经典Java书籍，九成新，无笔记划线，适合学习使用。', 45.00, 108.00, 15, 1, 34, 2
FROM users u WHERE u.username = 'user1'
AND NOT EXISTS (SELECT 1 FROM goods WHERE title = '《Java编程思想》第4版');

INSERT INTO goods (seller_id, title, description, price, original_price, category_id, status, view_count, like_count)
SELECT u.id, '迪卡侬山地自行车', '迪卡侬ST100山地车，26寸，21速，骑行不到100公里，几乎全新。', 600.00, 1299.00, 18, 1, 78, 12
FROM users u WHERE u.username = 'user2'
AND NOT EXISTS (SELECT 1 FROM goods WHERE title = '迪卡侬山地自行车');

INSERT INTO goods (seller_id, title, description, price, original_price, category_id, status, view_count, like_count)
SELECT u.id, 'ZARA冬季羽绒服', 'ZARA男士羽绒服，L码，黑色，仅试穿，吊牌还在。', 350.00, 899.00, 6, 1, 56, 7
FROM users u WHERE u.username = 'user1'
AND NOT EXISTS (SELECT 1 FROM goods WHERE title = 'ZARA冬季羽绒服');

-- 插入订单数据（buyer_id/seller_id 使用子查询动态获取）
INSERT INTO orders (order_no, buyer_id, seller_id, goods_id, price, status, address, remark)
SELECT '202404280001', ub.id, us.id, g.id, 6500.00, 3, '北京市朝阳区xxx街道xxx号', '请包装好一点'
FROM users ub, users us, goods g
WHERE ub.username = 'user2' AND us.username = 'user1' AND g.title = 'iPhone 13 Pro Max 256G 远峰蓝'
AND NOT EXISTS (SELECT 1 FROM orders WHERE order_no = '202404280001');

INSERT INTO orders (order_no, buyer_id, seller_id, goods_id, price, status, address, remark)
SELECT '202404280002', ub.id, us.id, g.id, 800.00, 1, '上海市浦东新区xxx路xxx号', '周末送货'
FROM users ub, users us, goods g
WHERE ub.username = 'user1' AND us.username = 'user2' AND g.title = '宜家布艺沙发三人位'
AND NOT EXISTS (SELECT 1 FROM orders WHERE order_no = '202404280002');

INSERT INTO orders (order_no, buyer_id, seller_id, goods_id, price, status, address, remark)
SELECT '202404280003', ub.id, us.id, g.id, 2800.00, 2, '广州市天河区xxx街xxx号', ''
FROM users ub, users us, goods g
WHERE ub.username = 'user2' AND us.username = 'user1' AND g.title = 'Nike Air Jordan 1 芝加哥配色'
AND NOT EXISTS (SELECT 1 FROM orders WHERE order_no = '202404280003');

-- 插入收藏数据
INSERT INTO favorite (user_id, goods_id)
SELECT ub.id, g.id
FROM users ub, goods g
WHERE ub.username = 'user2' AND g.title = 'iPhone 13 Pro Max 256G 远峰蓝'
AND NOT EXISTS (SELECT 1 FROM favorite f, users u, goods gd WHERE f.user_id = u.id AND f.goods_id = gd.id AND u.username = 'user2' AND gd.title = 'iPhone 13 Pro Max 256G 远峰蓝');

INSERT INTO favorite (user_id, goods_id)
SELECT ub.id, g.id
FROM users ub, goods g
WHERE ub.username = 'user2' AND g.title = 'MacBook Pro 14寸 M1 Pro芯片'
AND NOT EXISTS (SELECT 1 FROM favorite f, users u, goods gd WHERE f.user_id = u.id AND f.goods_id = gd.id AND u.username = 'user2' AND gd.title = 'MacBook Pro 14寸 M1 Pro芯片');

INSERT INTO favorite (user_id, goods_id)
SELECT ub.id, g.id
FROM users ub, goods g
WHERE ub.username = 'user1' AND g.title = '宜家布艺沙发三人位'
AND NOT EXISTS (SELECT 1 FROM favorite f, users u, goods gd WHERE f.user_id = u.id AND f.goods_id = gd.id AND u.username = 'user1' AND gd.title = '宜家布艺沙发三人位');

INSERT INTO favorite (user_id, goods_id)
SELECT ub.id, g.id
FROM users ub, goods g
WHERE ub.username = 'user1' AND g.title = '迪卡侬山地自行车'
AND NOT EXISTS (SELECT 1 FROM favorite f, users u, goods gd WHERE f.user_id = u.id AND f.goods_id = gd.id AND u.username = 'user1' AND gd.title = '迪卡侬山地自行车');

-- 插入消息数据
INSERT INTO message (sender_id, receiver_id, type, title, content, is_read)
SELECT NULL, u.id, 0, '系统通知', '欢迎来到二手交易平台！', 0
FROM users u WHERE u.username = 'user1'
AND NOT EXISTS (SELECT 1 FROM message WHERE title = '系统通知' AND content = '欢迎来到二手交易平台！');

INSERT INTO message (sender_id, receiver_id, type, title, content, is_read)
SELECT us.id, ur.id, 1, NULL, '您好，请问iPhone还在吗？', 0
FROM users us, users ur
WHERE us.username = 'user2' AND ur.username = 'user1'
AND NOT EXISTS (SELECT 1 FROM message WHERE content = '您好，请问iPhone还在吗？');

INSERT INTO message (sender_id, receiver_id, type, title, content, is_read)
SELECT NULL, u.id, 0, '订单提醒', '您的订单202404280001已完成', 1
FROM users u WHERE u.username = 'user2'
AND NOT EXISTS (SELECT 1 FROM message WHERE title = '订单提醒' AND content = '您的订单202404280001已完成');

INSERT INTO message (sender_id, receiver_id, type, title, content, is_read)
SELECT us.id, ur.id, 1, NULL, '在的，可以小刀', 1
FROM users us, users ur
WHERE us.username = 'user1' AND ur.username = 'user2'
AND NOT EXISTS (SELECT 1 FROM message WHERE content = '在的，可以小刀');
