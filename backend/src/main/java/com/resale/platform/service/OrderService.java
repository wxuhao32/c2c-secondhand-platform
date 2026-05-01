package com.resale.platform.service;

import com.resale.platform.entity.Order;

import java.util.List;
import java.util.Map;

public interface OrderService {

    List<Order> getOrderList(Long userId, Integer status);

    Order getOrderDetail(Long id);

    Order createOrder(Long userId, Map<String, Object> body);

    void cancelOrder(Long userId, Long id);

    void confirmReceipt(Long userId, Long id);

    void payOrder(Long userId, Long id);

    void shipOrder(Long userId, Long id);

    List<Order> getSoldOrders(Long userId);

    List<Order> getBoughtOrders(Long userId);
}
