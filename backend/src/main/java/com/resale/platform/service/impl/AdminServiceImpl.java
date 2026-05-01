package com.resale.platform.service.impl;

import com.resale.platform.common.BusinessException;
import com.resale.platform.common.ExceptionEnum;
import com.resale.platform.entity.Comment;
import com.resale.platform.entity.Goods;
import com.resale.platform.entity.Order;
import com.resale.platform.entity.User;
import com.resale.platform.mapper.*;
import com.resale.platform.service.AdminService;
import com.resale.platform.service.CacheService;
import com.resale.platform.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final CommentMapper commentMapper;
    private final OrderMapper orderMapper;
    private final MessageService messageService;
    private final CacheService cacheService;

    @Override
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("userCount", userMapper.selectCount(null));
        dashboard.put("goodsCount", goodsMapper.selectCount(null));
        dashboard.put("orderCount", orderMapper.selectCount(null));
        dashboard.put("commentCount", commentMapper.selectCount(null));
        return dashboard;
    }

    @Override
    public List<User> getUserList(String keyword, String role) {
        List<User> users;
        if (keyword != null && !keyword.isEmpty()) {
            User byName = userMapper.selectByUsername(keyword);
            User byMobile = userMapper.selectByMobile(keyword);
            users = new java.util.ArrayList<>();
            if (byName != null) users.add(byName);
            if (byMobile != null && !users.contains(byMobile)) users.add(byMobile);
        } else if (role != null && !role.isEmpty()) {
            users = userMapper.selectByRole(role);
        } else {
            users = userMapper.selectList(null);
        }
        users.forEach(u -> u.setPassword(null));
        return users;
    }

    @Override
    @Transactional
    public void updateUserStatus(Long id, Integer status) {
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ExceptionEnum.USER_NOT_FOUND);
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        cacheService.delete("user:" + id);
    }

    @Override
    @Transactional
    public void updateUserRole(Long id, String role) {
        if (!role.equals("USER") && !role.equals("ADMIN")) {
            throw new BusinessException(ExceptionEnum.BAD_REQUEST);
        }
        User user = userMapper.selectById(id);
        if (user == null) throw new BusinessException(ExceptionEnum.USER_NOT_FOUND);
        user.setRole(role);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        cacheService.delete("user:" + id);
    }

    @Override
    public List<Goods> getGoodsList(Integer status) {
        if (status != null) {
            return goodsMapper.findOnSale();
        }
        return goodsMapper.selectList(null);
    }

    @Override
    @Transactional
    public void updateGoodsStatus(Long id, Integer status) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) throw new BusinessException(ExceptionEnum.NOT_FOUND);
        goods.setStatus(status);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);
        cacheService.delete("product:" + id);
        cacheService.deleteByPattern("product:list:*");
    }

    @Override
    @Transactional
    public void deleteGoods(Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) throw new BusinessException(ExceptionEnum.NOT_FOUND);
        goods.setIsDeleted(1);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);
        cacheService.delete("product:" + id);
        cacheService.deleteByPattern("product:list:*");
    }

    @Override
    public List<Comment> getCommentList() {
        return commentMapper.selectList(null);
    }

    @Override
    @Transactional
    public void deleteComment(Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) throw new BusinessException(ExceptionEnum.NOT_FOUND);
        comment.setIsDeleted(1);
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.updateById(comment);
    }

    @Override
    public List<Order> getOrderList() {
        return orderMapper.selectList(null);
    }

    @Override
    public void sendNotification(Long receiverId, String title, String content, Integer type) {
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ExceptionEnum.BAD_REQUEST);
        }
        if (receiverId != null) {
            messageService.sendNotification(null, receiverId, title, content, type);
        } else {
            messageService.broadcastNotification(title, content, type);
        }
    }
}
