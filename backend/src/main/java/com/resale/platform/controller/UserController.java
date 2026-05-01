package com.resale.platform.controller;

import com.resale.platform.common.AuditLog;
import com.resale.platform.common.Result;
import com.resale.platform.entity.UserAddress;
import com.resale.platform.security.SecurityUser;
import com.resale.platform.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
@Tag(name = "用户中心", description = "用户信息、地址、收藏、统计等接口")
public class UserController {

    private final UserService userService;

    @GetMapping("/info")
    @Operation(summary = "获取用户信息", description = "获取当前登录用户的详细信息")
    public Result<Map<String, Object>> getUserInfo() {
        Long userId = getCurrentUserId();
        Map<String, Object> info = userService.getUserInfo(userId);
        return Result.success(info);
    }

    @PutMapping("/info")
    @Operation(summary = "更新用户信息", description = "修改昵称、邮箱、头像等")
    @AuditLog(module = "用户", action = "更新信息", description = "用户更新个人信息")
    public Result<Void> updateUserInfo(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        userService.updateUserInfo(userId, body);
        return Result.success();
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码", description = "修改用户登录密码")
    @AuditLog(module = "用户", action = "修改密码", description = "用户修改密码")
    public Result<Void> updatePassword(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        userService.updatePassword(userId, body.get("oldPassword"), body.get("newPassword"));
        return Result.success();
    }

    @PostMapping("/avatar")
    @Operation(summary = "上传头像", description = "上传用户头像（占位接口）")
    public Result<Map<String, String>> updateAvatar() {
        return Result.success(Map.of("url", "/uploads/avatar-placeholder.jpg"));
    }

    @GetMapping("/address")
    @Operation(summary = "获取地址列表", description = "获取用户的所有收货地址")
    public Result<List<UserAddress>> getAddressList() {
        Long userId = getCurrentUserId();
        List<UserAddress> addresses = userService.getAddressList(userId);
        return Result.success(addresses);
    }

    @PostMapping("/address")
    @Operation(summary = "添加地址", description = "添加新的收货地址")
    @AuditLog(module = "用户", action = "添加地址", description = "用户添加收货地址")
    public Result<UserAddress> addAddress(@RequestBody UserAddress address) {
        Long userId = getCurrentUserId();
        UserAddress saved = userService.addAddress(userId, address);
        return Result.success(saved);
    }

    @PutMapping("/address/{id}")
    @Operation(summary = "更新地址", description = "修改收货地址")
    @AuditLog(module = "用户", action = "更新地址", description = "用户更新收货地址")
    public Result<Void> updateAddress(
            @Parameter(description = "地址ID") @PathVariable Long id,
            @RequestBody UserAddress address) {
        Long userId = getCurrentUserId();
        userService.updateAddress(userId, id, address);
        return Result.success();
    }

    @DeleteMapping("/address/{id}")
    @Operation(summary = "删除地址", description = "删除收货地址")
    @AuditLog(module = "用户", action = "删除地址", description = "用户删除收货地址")
    public Result<Void> deleteAddress(
            @Parameter(description = "地址ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        userService.deleteAddress(userId, id);
        return Result.success();
    }

    @PutMapping("/address/{id}/default")
    @Operation(summary = "设置默认地址", description = "将指定地址设为默认收货地址")
    public Result<Void> setDefaultAddress(
            @Parameter(description = "地址ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        userService.setDefaultAddress(userId, id);
        return Result.success();
    }

    @GetMapping("/favorites")
    @Operation(summary = "获取收藏列表", description = "获取用户收藏的所有商品")
    public Result<List<Map<String, Object>>> getFavorites() {
        Long userId = getCurrentUserId();
        List<Map<String, Object>> favorites = userService.getFavorites(userId);
        return Result.success(favorites);
    }

    @PostMapping("/favorites/{productId}")
    @Operation(summary = "添加收藏", description = "收藏指定商品")
    @AuditLog(module = "用户", action = "收藏", description = "用户收藏商品")
    public Result<Void> addFavorite(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Long userId = getCurrentUserId();
        userService.addFavorite(userId, productId);
        return Result.success();
    }

    @DeleteMapping("/favorites/{productId}")
    @Operation(summary = "取消收藏", description = "取消收藏指定商品")
    @AuditLog(module = "用户", action = "取消收藏", description = "用户取消收藏商品")
    public Result<Void> removeFavorite(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Long userId = getCurrentUserId();
        userService.removeFavorite(userId, productId);
        return Result.success();
    }

    @GetMapping("/stats")
    @Operation(summary = "获取用户统计", description = "获取用户的发布、交易、收藏等统计数据")
    public Result<Map<String, Object>> getUserStats() {
        Long userId = getCurrentUserId();
        Map<String, Object> stats = userService.getUserStats(userId);
        return Result.success(stats);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
