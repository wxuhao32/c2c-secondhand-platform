package com.resale.platform.controller;

import com.resale.platform.common.Result;
import com.resale.platform.entity.User;
import com.resale.platform.entity.Goods;
import com.resale.platform.entity.Comment;
import com.resale.platform.entity.Order;
import com.resale.platform.mapper.UserMapper;
import com.resale.platform.mapper.GoodsMapper;
import com.resale.platform.mapper.CommentMapper;
import com.resale.platform.mapper.OrderMapper;
import com.resale.platform.mapper.MessageMapper;
import com.resale.platform.entity.Message;
import com.resale.platform.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserMapper userMapper;
    private final GoodsMapper goodsMapper;
    private final CommentMapper commentMapper;
    private final OrderMapper orderMapper;
    private final MessageMapper messageMapper;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        dashboard.put("userCount", userMapper.selectCount(null));
        dashboard.put("goodsCount", goodsMapper.selectCount(null));
        dashboard.put("orderCount", orderMapper.selectCount(null));
        dashboard.put("commentCount", commentMapper.selectCount(null));
        return Result.success(dashboard);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<User>> getUserList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
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
            users = userMapper.selectAll();
        }
        users.forEach(u -> u.setPassword(null));
        return Result.success(users);
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.error(404, "用户不存在");
        Integer status = body.get("status");
        if (status != null) {
            user.setStatus(status);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }
        return Result.success();
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = userMapper.selectById(id);
        if (user == null) return Result.error(404, "用户不存在");
        String role = body.get("role");
        if (role != null && (role.equals("USER") || role.equals("ADMIN"))) {
            user.setRole(role);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }
        return Result.success();
    }

    @GetMapping("/goods")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Goods>> getGoodsList(@RequestParam(required = false) Integer status) {
        List<Goods> goods;
        if (status != null) {
            goods = goodsMapper.findOnSale();
        } else {
            goods = goodsMapper.selectList(null);
        }
        return Result.success(goods);
    }

    @PutMapping("/goods/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> updateGoodsStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) return Result.error(404, "商品不存在");
        Integer status = body.get("status");
        if (status != null) {
            goods.setStatus(status);
            goods.setUpdatedAt(LocalDateTime.now());
            goodsMapper.updateById(goods);
        }
        return Result.success();
    }

    @DeleteMapping("/goods/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteGoods(@PathVariable Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) return Result.error(404, "商品不存在");
        goods.setIsDeleted(1);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);
        return Result.success();
    }

    @GetMapping("/comments")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Comment>> getCommentList() {
        List<Comment> comments = commentMapper.selectList(null);
        return Result.success(comments);
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) return Result.error(404, "评论不存在");
        comment.setIsDeleted(1);
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.updateById(comment);
        return Result.success();
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<Order>> getOrderList() {
        List<Order> orders = orderMapper.selectList(null);
        return Result.success(orders);
    }

    @PostMapping("/notify")
    public Result<Void> sendNotification(@RequestBody Map<String, Object> body) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymous".equals(auth.getPrincipal())) {
                return Result.error(401, "请先登录");
            }
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            if (!isAdmin) {
                return Result.error(403, "无管理员权限");
            }

            String content = (String) body.get("content");
            if (content == null || content.trim().isEmpty()) {
                return Result.error(400, "消息内容不能为空");
            }

            Long receiverId = body.get("receiverId") != null ? Long.valueOf(body.get("receiverId").toString()) : null;
            String title = (String) body.getOrDefault("title", "系统通知");
            Integer type = body.get("type") != null ? Integer.valueOf(body.get("type").toString()) : 0;

            if (receiverId != null) {
                Message message = new Message();
                message.setSenderId(null);
                message.setReceiverId(receiverId);
                message.setType(type);
                message.setTitle(title);
                message.setContent(content);
                message.setIsRead(0);
                message.setIsDeleted(0);
                message.setCreatedAt(LocalDateTime.now());
                messageMapper.insert(message);
            } else {
                List<User> allUsers = userMapper.selectAll();
                for (User user : allUsers) {
                    Message message = new Message();
                    message.setSenderId(null);
                    message.setReceiverId(user.getUserId());
                    message.setType(type);
                    message.setTitle(title);
                    message.setContent(content);
                    message.setIsRead(0);
                    message.setIsDeleted(0);
                    message.setCreatedAt(LocalDateTime.now());
                    messageMapper.insert(message);
                }
            }
            return Result.success();
        } catch (Exception e) {
            log.error("发送通知失败", e);
            return Result.error(500, "发送失败: " + e.getMessage());
        }
    }
}
