package com.resale.platform.service.impl;

import com.resale.platform.common.BusinessException;
import com.resale.platform.common.ExceptionEnum;
import com.resale.platform.entity.Goods;
import com.resale.platform.entity.Order;
import com.resale.platform.mapper.GoodsMapper;
import com.resale.platform.mapper.OrderMapper;
import com.resale.platform.service.CacheService;
import com.resale.platform.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final GoodsMapper goodsMapper;
    private final CacheService cacheService;

    private static final String ORDER_CACHE_KEY = "order:";
    private static final String ORDER_LOCK_KEY = "order:lock:";

    @Override
    public List<Order> getOrderList(Long userId, Integer status) {
        if (status != null) {
            return orderMapper.findByBuyerIdAndStatus(userId, status);
        }
        return orderMapper.findByBuyerId(userId);
    }

    @Override
    public Order getOrderDetail(Long id) {
        String cacheKey = ORDER_CACHE_KEY + id;
        Order cached = cacheService.get(cacheKey, Order.class);
        if (cached != null) return cached;

        Order order = orderMapper.selectById(id);
        if (order == null || order.getIsDeleted() == 1) {
            throw new BusinessException(ExceptionEnum.NOT_FOUND);
        }
        cacheService.set(cacheKey, order, 5, TimeUnit.MINUTES);
        return order;
    }

    @Override
    @Transactional
    public Order createOrder(Long userId, Map<String, Object> body) {
        Long goodsId = Long.valueOf(body.get("goodsId").toString());
        String lockKey = ORDER_LOCK_KEY + goodsId;
        boolean locked = cacheService.setIfAbsent(lockKey, userId, 30, TimeUnit.SECONDS);
        if (!locked) {
            throw new BusinessException(ExceptionEnum.OPERATION_TOO_FREQUENT);
        }

        try {
            Goods goods = goodsMapper.selectById(goodsId);
            if (goods == null || goods.getIsDeleted() == 1 || goods.getStatus() != 1) {
                throw new BusinessException(400, "商品不可购买");
            }
            if (goods.getSellerId().equals(userId)) {
                throw new BusinessException(400, "不能购买自己的商品");
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

            cacheService.delete("product:" + goodsId);
            cacheService.deleteByPattern("product:list:*");
            return order;
        } finally {
            cacheService.delete(lockKey);
        }
    }

    @Override
    @Transactional
    public void cancelOrder(Long userId, Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(ExceptionEnum.NOT_FOUND);
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BusinessException(ExceptionEnum.FORBIDDEN);
        }
        order.setStatus(4);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);

        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            goods.setStatus(1);
            goods.setUpdatedAt(LocalDateTime.now());
            goodsMapper.updateById(goods);
            cacheService.delete("product:" + order.getGoodsId());
        }
        cacheService.delete(ORDER_CACHE_KEY + id);
    }

    @Override
    @Transactional
    public void confirmReceipt(Long userId, Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(ExceptionEnum.NOT_FOUND);
        if (!order.getBuyerId().equals(userId)) throw new BusinessException(ExceptionEnum.FORBIDDEN);
        order.setStatus(3);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        cacheService.delete(ORDER_CACHE_KEY + id);
    }

    @Override
    @Transactional
    public void payOrder(Long userId, Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(ExceptionEnum.NOT_FOUND);
        if (!order.getBuyerId().equals(userId)) throw new BusinessException(ExceptionEnum.FORBIDDEN);
        if (order.getStatus() != 0) throw new BusinessException(400, "订单状态不正确");
        order.setStatus(1);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        cacheService.delete(ORDER_CACHE_KEY + id);
    }

    @Override
    @Transactional
    public void shipOrder(Long userId, Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) throw new BusinessException(ExceptionEnum.NOT_FOUND);
        if (!order.getSellerId().equals(userId)) throw new BusinessException(ExceptionEnum.FORBIDDEN);
        if (order.getStatus() != 1) throw new BusinessException(400, "订单状态不正确");
        order.setStatus(2);
        order.setUpdatedAt(LocalDateTime.now());
        orderMapper.updateById(order);
        cacheService.delete(ORDER_CACHE_KEY + id);
    }

    @Override
    public List<Order> getSoldOrders(Long userId) {
        return orderMapper.findBySellerId(userId);
    }

    @Override
    public List<Order> getBoughtOrders(Long userId) {
        return orderMapper.findByBuyerId(userId);
    }

    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int random = ThreadLocalRandom.current().nextInt(1000, 9999);
        return timestamp + random;
    }
}
