package com.resale.platform.controller;

import com.resale.platform.common.Result;
import com.resale.platform.entity.Order;
import com.resale.platform.entity.Goods;
import com.resale.platform.mapper.OrderMapper;
import com.resale.platform.mapper.GoodsMapper;
import com.resale.platform.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderMapper orderMapper;
    private final GoodsMapper goodsMapper;

    @GetMapping
    public Result<List<Order>> getOrderList(@RequestParam(required = false) Integer status) {
        Long userId = getCurrentUserId();
        List<Order> orders;
        if (status != null) {
            orders = orderMapper.findByBuyerIdAndStatus(userId, status);
        } else {
            orders = orderMapper.findByBuyerId(userId);
        }
        return Result.success(orders);
    }

    @GetMapping("/{id}")
    public Result<Order> getOrderDetail(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null || order.getIsDeleted() == 1) {
            return Result.error(404, "订单不存在");
        }
        return Result.success(order);
    }

    @PostMapping
    public Result<Order> createOrder(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        Long goodsId = Long.valueOf(body.get("goodsId").toString());
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null || goods.getIsDeleted() == 1 || goods.getStatus() != 1) {
            return Result.error(400, "商品不可购买");
        }
        if (goods.getSellerId().equals(userId)) {
            return Result.error(400, "不能购买自己的商品");
        }

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setBuyerId(userId);
        order.setSellerId(goods.getSellerId());
        order.setGoodsId(goodsId);
        order.setPrice(goods.getPrice());
        order.setStatus(0);
        order.setAddress(body.get("address") != null ? body.get("address").toString() : null);
        order.setRemark(body.get("remark") != null ? body.get("remark").toString() : null);
        order.setIsDeleted(0);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.insert(order);

        goods.setStatus(2);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);

        return Result.success(order);
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancelOrder(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Order order = orderMapper.selectById(id);
        if (order == null) return Result.error(404, "订单不存在");
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            return Result.error(403, "无权操作此订单");
        }
        order.setStatus(4);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            goods.setStatus(1);
            goods.setUpdatedAt(LocalDateTime.now());
            goodsMapper.updateById(goods);
        }
        return Result.success();
    }

    @PutMapping("/{id}/confirm")
    public Result<Void> confirmReceipt(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Order order = orderMapper.selectById(id);
        if (order == null) return Result.error(404, "订单不存在");
        if (!order.getBuyerId().equals(userId)) return Result.error(403, "无权操作此订单");
        order.setStatus(3);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success();
    }

    @PostMapping("/{id}/pay")
    public Result<Void> payOrder(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Order order = orderMapper.selectById(id);
        if (order == null) return Result.error(404, "订单不存在");
        if (!order.getBuyerId().equals(userId)) return Result.error(403, "无权操作此订单");
        if (order.getStatus() != 0) return Result.error(400, "订单状态不正确");
        order.setStatus(1);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success();
    }

    @PutMapping("/{id}/ship")
    public Result<Void> shipOrder(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Order order = orderMapper.selectById(id);
        if (order == null) return Result.error(404, "订单不存在");
        if (!order.getSellerId().equals(userId)) return Result.error(403, "无权操作此订单");
        if (order.getStatus() != 1) return Result.error(400, "订单状态不正确");
        order.setStatus(2);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        return Result.success();
    }

    @GetMapping("/sold")
    public Result<List<Order>> getSoldOrders() {
        Long userId = getCurrentUserId();
        List<Order> orders = orderMapper.findBySellerId(userId);
        return Result.success(orders);
    }

    @GetMapping("/bought")
    public Result<List<Order>> getBoughtOrders() {
        Long userId = getCurrentUserId();
        List<Order> orders = orderMapper.findByBuyerId(userId);
        return Result.success(orders);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return timestamp + random;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
