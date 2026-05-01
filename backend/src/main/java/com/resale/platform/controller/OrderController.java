package com.resale.platform.controller;

import com.resale.platform.common.AuditLog;
import com.resale.platform.common.Result;
import com.resale.platform.entity.Order;
import com.resale.platform.security.SecurityUser;
import com.resale.platform.service.OrderService;
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
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "订单管理", description = "订单创建、支付、发货、确认收货等接口")
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "获取订单列表", description = "获取当前用户的订单列表，可按状态筛选")
    public Result<List<Order>> getOrderList(
            @Parameter(description = "订单状态") @RequestParam(required = false) Integer status) {
        Long userId = getCurrentUserId();
        List<Order> orders = orderService.getOrderList(userId, status);
        return Result.success(orders);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情", description = "根据ID获取订单详细信息")
    public Result<Order> getOrderDetail(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        Order order = orderService.getOrderDetail(id);
        return Result.success(order);
    }

    @PostMapping
    @Operation(summary = "创建订单", description = "买家创建购买订单")
    @AuditLog(module = "订单", action = "创建", description = "买家创建购买订单")
    public Result<Order> createOrder(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        Order order = orderService.createOrder(userId, body);
        return Result.success(order);
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "取消订单", description = "买家或卖家取消订单")
    @AuditLog(module = "订单", action = "取消", description = "取消订单")
    public Result<Void> cancelOrder(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        orderService.cancelOrder(userId, id);
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    @Operation(summary = "确认收货", description = "买家确认收货")
    @AuditLog(module = "订单", action = "确认收货", description = "买家确认收货")
    public Result<Void> confirmReceipt(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        orderService.confirmReceipt(userId, id);
        return Result.success();
    }

    @PostMapping("/{id}/pay")
    @Operation(summary = "支付订单", description = "买家支付订单")
    @AuditLog(module = "订单", action = "支付", description = "买家支付订单")
    public Result<Void> payOrder(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        orderService.payOrder(userId, id);
        return Result.success();
    }

    @PutMapping("/{id}/ship")
    @Operation(summary = "发货", description = "卖家发货")
    @AuditLog(module = "订单", action = "发货", description = "卖家发货")
    public Result<Void> shipOrder(
            @Parameter(description = "订单ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        orderService.shipOrder(userId, id);
        return Result.success();
    }

    @GetMapping("/sold")
    @Operation(summary = "获取卖出订单", description = "获取卖家卖出的所有订单")
    public Result<List<Order>> getSoldOrders() {
        Long userId = getCurrentUserId();
        List<Order> orders = orderService.getSoldOrders(userId);
        return Result.success(orders);
    }

    @GetMapping("/bought")
    @Operation(summary = "获取买入订单", description = "获取买家买入的所有订单")
    public Result<List<Order>> getBoughtOrders() {
        Long userId = getCurrentUserId();
        List<Order> orders = orderService.getBoughtOrders(userId);
        return Result.success(orders);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
