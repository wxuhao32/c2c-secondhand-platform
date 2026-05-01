package com.resale.platform.controller;

import com.resale.platform.common.AuditLog;
import com.resale.platform.common.Result;
import com.resale.platform.entity.Comment;
import com.resale.platform.entity.Goods;
import com.resale.platform.entity.Order;
import com.resale.platform.entity.User;
import com.resale.platform.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "管理员", description = "管理员后台管理接口")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取仪表盘数据", description = "获取系统概览统计数据")
    public Result<Map<String, Object>> getDashboard() {
        return Result.success(adminService.getDashboard());
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取用户列表", description = "管理员获取用户列表，支持搜索和角色筛选")
    public Result<List<User>> getUserList(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色筛选") @RequestParam(required = false) String role) {
        return Result.success(adminService.getUserList(keyword, role));
    }

    @PutMapping("/users/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新用户状态", description = "启用/禁用用户")
    @AuditLog(module = "管理员", action = "更新用户状态", description = "管理员更新用户状态")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        adminService.updateUserStatus(id, body.get("status"));
        return Result.success();
    }

    @PutMapping("/users/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新用户角色", description = "修改用户角色（USER/ADMIN）")
    @AuditLog(module = "管理员", action = "更新用户角色", description = "管理员更新用户角色")
    public Result<Void> updateUserRole(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        adminService.updateUserRole(id, body.get("role"));
        return Result.success();
    }

    @GetMapping("/goods")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取商品列表", description = "管理员获取所有商品")
    public Result<List<Goods>> getGoodsList(
            @Parameter(description = "商品状态") @RequestParam(required = false) Integer status) {
        return Result.success(adminService.getGoodsList(status));
    }

    @PutMapping("/goods/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "更新商品状态", description = "管理员上架/下架商品")
    @AuditLog(module = "管理员", action = "更新商品状态", description = "管理员更新商品状态")
    public Result<Void> updateGoodsStatus(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        adminService.updateGoodsStatus(id, body.get("status"));
        return Result.success();
    }

    @DeleteMapping("/goods/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除商品", description = "管理员删除商品（软删除）")
    @AuditLog(module = "管理员", action = "删除商品", description = "管理员删除商品")
    public Result<Void> deleteGoods(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        adminService.deleteGoods(id);
        return Result.success();
    }

    @GetMapping("/comments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取评论列表", description = "管理员获取所有评论")
    public Result<List<Comment>> getCommentList() {
        return Result.success(adminService.getCommentList());
    }

    @DeleteMapping("/comments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "删除评论", description = "管理员删除评论")
    @AuditLog(module = "管理员", action = "删除评论", description = "管理员删除评论")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID") @PathVariable Long id) {
        adminService.deleteComment(id);
        return Result.success();
    }

    @GetMapping("/orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "获取订单列表", description = "管理员获取所有订单")
    public Result<List<Order>> getOrderList() {
        return Result.success(adminService.getOrderList());
    }

    @PostMapping("/notify")
    @Operation(summary = "发送通知", description = "管理员发送站内通知（可群发）")
    @AuditLog(module = "管理员", action = "发送通知", description = "管理员发送站内通知")
    public Result<Void> sendNotification(@RequestBody Map<String, Object> body) {
        try {
            String content = (String) body.get("content");
            String title = (String) body.getOrDefault("title", "系统通知");
            Long receiverId = body.get("receiverId") != null ? Long.valueOf(body.get("receiverId").toString()) : null;
            Integer type = body.get("type") != null ? Integer.valueOf(body.get("type").toString()) : 0;
            adminService.sendNotification(receiverId, title, content, type);
            return Result.success();
        } catch (Exception e) {
            log.error("发送通知失败", e);
            return Result.error(500, "发送失败: " + e.getMessage());
        }
    }
}
