INSERT INTO users (username, password, mobile, email, nickname, status, role) VALUES
('admin', '$2a$12$938AubesljVp7HCTTQZf3O.HLlwkZDqcmGjwvdVkQLhT7NpMtT2xC', '13800138000', 'admin@example.com', '管理员', 1, 'ADMIN'),
('user1', '$2a$12$938AubesljVp7HCTTQZf3O.HLlwkZDqcmGjwvdVkQLhT7NpMtT2xC', '13800138001', 'user1@example.com', '测试用户1', 1, 'USER'),
('user2', '$2a$12$938AubesljVp7HCTTQZf3O.HLlwkZDqcmGjwvdVkQLhT7NpMtT2xC', '13800138002', 'user2@example.com', '测试用户2', 1, 'USER');

INSERT INTO category (name, parent_id, icon, sort_order) VALUES
('数码电子', 0, '💻', 1),
('服装鞋包', 0, '�', 2),
('家居生活', 0, '🏠', 3),
('图书音像', 0, '�', 4),
('运动户外', 0, '⚽', 5);

INSERT INTO category (name, parent_id, icon, sort_order) VALUES
('手机', (SELECT id FROM category WHERE name = '数码电子'), '�', 1),
('电脑', (SELECT id FROM category WHERE name = '数码电子'), '�', 2),
('相机', (SELECT id FROM category WHERE name = '数码电子'), '📷', 3),
('男装', (SELECT id FROM category WHERE name = '服装鞋包'), '👔', 1),
('女装', (SELECT id FROM category WHERE name = '服装鞋包'), '👗', 2),
('鞋靴', (SELECT id FROM category WHERE name = '服装鞋包'), '👟', 3),
('箱包', (SELECT id FROM category WHERE name = '服装鞋包'), '👜', 4),
('家具', (SELECT id FROM category WHERE name = '家居生活'), '🛋️', 1),
('家纺', (SELECT id FROM category WHERE name = '家居生活'), '🛏️', 2),
('厨具', (SELECT id FROM category WHERE name = '家居生活'), '🍳', 3),
('图书', (SELECT id FROM category WHERE name = '图书音像'), '📖', 1),
('音像', (SELECT id FROM category WHERE name = '图书音像'), '💿', 2),
('运动器材', (SELECT id FROM category WHERE name = '运动户外'), '🏋️', 1),
('户外装备', (SELECT id FROM category WHERE name = '运动户外'), '⛺', 2);

INSERT INTO goods (seller_id, title, description, price, original_price, category_id, status, view_count, like_count) VALUES
((SELECT id FROM users WHERE username = 'user1'), 'iPhone 13 Pro Max 256G 远峰蓝', '自用iPhone 13 Pro Max，成色99新，无划痕，电池健康度92%，带原装盒子和配件。', 6500.00, 8999.00, (SELECT id FROM category WHERE name = '手机'), 1, 128, 15),
((SELECT id FROM users WHERE username = 'user1'), 'MacBook Pro 14寸 M1 Pro芯片', '2021款MacBook Pro，16G内存+512G固态，轻度使用，外观完美。', 8500.00, 14999.00, (SELECT id FROM category WHERE name = '电脑'), 1, 256, 32),
((SELECT id FROM users WHERE username = 'user2'), '索尼A7M3相机机身', '索尼A7M3全画幅微单，快门次数5000左右，成色很新，功能正常。', 8500.00, 12000.00, (SELECT id FROM category WHERE name = '相机'), 1, 89, 8),
((SELECT id FROM users WHERE username = 'user1'), 'Nike Air Jordan 1 芝加哥配色', 'AJ1芝加哥，42码，全新未穿，鞋盒完好，保真。', 2800.00, 3500.00, (SELECT id FROM category WHERE name = '鞋靴'), 1, 67, 23),
((SELECT id FROM users WHERE username = 'user2'), '宜家布艺沙发三人位', '宜家三人位布艺沙发，使用2年，保养良好，无污渍，需要自提。', 800.00, 2999.00, (SELECT id FROM category WHERE name = '家具'), 1, 45, 5),
((SELECT id FROM users WHERE username = 'user1'), '《Java编程思想》第4版', '经典Java书籍，九成新，无笔记划线，适合学习使用。', 45.00, 108.00, (SELECT id FROM category WHERE name = '图书'), 1, 34, 2),
((SELECT id FROM users WHERE username = 'user2'), '迪卡侬山地自行车', '迪卡侬ST100山地车，26寸，21速，骑行不到100公里，几乎全新。', 600.00, 1299.00, (SELECT id FROM category WHERE name = '运动器材'), 1, 78, 12),
((SELECT id FROM users WHERE username = 'user1'), 'ZARA冬季羽绒服', 'ZARA男士羽绒服，L码，黑色，仅试穿，吊牌还在。', 350.00, 899.00, (SELECT id FROM category WHERE name = '男装'), 1, 56, 7);

INSERT INTO orders (order_no, buyer_id, seller_id, goods_id, price, status, address, remark) VALUES
('202404280001', (SELECT id FROM users WHERE username = 'user2'), (SELECT id FROM users WHERE username = 'user1'), (SELECT id FROM goods WHERE title LIKE 'iPhone 13%'), 6500.00, 3, '北京市朝阳区xxx街道xxx号', '请包装好一点'),
('202404280002', (SELECT id FROM users WHERE username = 'user1'), (SELECT id FROM users WHERE username = 'user2'), (SELECT id FROM goods WHERE title LIKE '宜家%'), 800.00, 1, '上海市浦东新区xxx路xxx号', '周末送货'),
('202404280003', (SELECT id FROM users WHERE username = 'user2'), (SELECT id FROM users WHERE username = 'user1'), (SELECT id FROM goods WHERE title LIKE 'Nike%'), 2800.00, 2, '广州市天河区xxx街xxx号', '');

INSERT INTO favorite (user_id, goods_id) VALUES
((SELECT id FROM users WHERE username = 'user2'), (SELECT id FROM goods WHERE title LIKE 'iPhone 13%')),
((SELECT id FROM users WHERE username = 'user2'), (SELECT id FROM goods WHERE title LIKE 'MacBook%')),
((SELECT id FROM users WHERE username = 'user1'), (SELECT id FROM goods WHERE title LIKE '宜家%')),
((SELECT id FROM users WHERE username = 'user1'), (SELECT id FROM goods WHERE title LIKE '迪卡侬%'));

INSERT INTO message (sender_id, receiver_id, type, title, content, is_read) VALUES
(NULL, (SELECT id FROM users WHERE username = 'user1'), 0, '系统通知', '欢迎来到二手交易平台！', 0),
((SELECT id FROM users WHERE username = 'user2'), (SELECT id FROM users WHERE username = 'user1'), 1, NULL, '您好，请问iPhone还在吗？', 0),
(NULL, (SELECT id FROM users WHERE username = 'user2'), 0, '订单提醒', '您的订单202404280001已完成', 1),
((SELECT id FROM users WHERE username = 'user1'), (SELECT id FROM users WHERE username = 'user2'), 1, NULL, '在的，可以小刀', 1);
