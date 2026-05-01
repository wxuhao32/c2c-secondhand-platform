package com.resale.platform.service;

import com.resale.platform.entity.Comment;
import com.resale.platform.entity.Goods;
import com.resale.platform.entity.Order;
import com.resale.platform.entity.User;

import java.util.List;
import java.util.Map;

public interface AdminService {

    Map<String, Object> getDashboard();

    List<User> getUserList(String keyword, String role);

    void updateUserStatus(Long id, Integer status);

    void updateUserRole(Long id, String role);

    List<Goods> getGoodsList(Integer status);

    void updateGoodsStatus(Long id, Integer status);

    void deleteGoods(Long id);

    List<Comment> getCommentList();

    void deleteComment(Long id);

    List<Order> getOrderList();

    void sendNotification(Long receiverId, String title, String content, Integer type);
}
