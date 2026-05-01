package com.resale.platform.service;

import com.resale.platform.entity.Goods;
import com.resale.platform.entity.Order;
import com.resale.platform.mapper.GoodsMapper;
import com.resale.platform.mapper.OrderMapper;
import com.resale.platform.service.impl.LocalCacheServiceImpl;
import com.resale.platform.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单服务单元测试")
class OrderServiceTest {

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private GoodsMapper goodsMapper;

    private CacheService cacheService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Order testOrder;
    private Goods testGoods;

    @BeforeEach
    void setUp() {
        cacheService = new LocalCacheServiceImpl();
        orderService = new OrderServiceImpl(orderMapper, goodsMapper, cacheService);

        testGoods = new Goods();
        testGoods.setId(1L);
        testGoods.setSellerId(200L);
        testGoods.setTitle("测试商品");
        testGoods.setPrice(new BigDecimal("99.99"));
        testGoods.setStatus(1);
        testGoods.setIsDeleted(0);

        testOrder = new Order();
        testOrder.setId(1L);
        testOrder.setOrderNo("202401011200001234");
        testOrder.setBuyerId(100L);
        testOrder.setSellerId(200L);
        testOrder.setGoodsId(1L);
        testOrder.setPrice(new BigDecimal("99.99"));
        testOrder.setStatus(0);
        testOrder.setIsDeleted(0);
        testOrder.setCreatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("创建订单 - 正常情况")
    void createOrder_success() {
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(orderMapper.insert(any(Order.class))).thenReturn(1);
        when(goodsMapper.updateById(any(Goods.class))).thenReturn(1);

        Map<String, Object> body = Map.of("goodsId", "1");
        Order result = orderService.createOrder(100L, body);

        assertNotNull(result);
        assertEquals(100L, result.getBuyerId());
        assertEquals(200L, result.getSellerId());
        verify(orderMapper).insert(any(Order.class));
    }

    @Test
    @DisplayName("创建订单 - 不能购买自己的商品")
    void createOrder_cannotBuyOwnProduct() {
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        Map<String, Object> body = Map.of("goodsId", "1");
        assertThrows(com.resale.platform.common.BusinessException.class,
                () -> orderService.createOrder(200L, body));
    }

    @Test
    @DisplayName("支付订单 - 正常情况")
    void payOrder_success() {
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        orderService.payOrder(100L, 1L);

        verify(orderMapper).updateById(argThat(order -> order.getStatus() == 1));
    }

    @Test
    @DisplayName("支付订单 - 非买家不能支付")
    void payOrder_notBuyer() {
        when(orderMapper.selectById(1L)).thenReturn(testOrder);

        assertThrows(com.resale.platform.common.BusinessException.class,
                () -> orderService.payOrder(200L, 1L));
    }

    @Test
    @DisplayName("发货 - 正常情况")
    void shipOrder_success() {
        testOrder.setStatus(1);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        orderService.shipOrder(200L, 1L);

        verify(orderMapper).updateById(argThat(order -> order.getStatus() == 2));
    }

    @Test
    @DisplayName("确认收货 - 正常情况")
    void confirmReceipt_success() {
        testOrder.setStatus(2);
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);

        orderService.confirmReceipt(100L, 1L);

        verify(orderMapper).updateById(argThat(order -> order.getStatus() == 3));
    }

    @Test
    @DisplayName("取消订单 - 正常情况")
    void cancelOrder_success() {
        when(orderMapper.selectById(1L)).thenReturn(testOrder);
        when(orderMapper.updateById(any(Order.class))).thenReturn(1);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(goodsMapper.updateById(any(Goods.class))).thenReturn(1);

        orderService.cancelOrder(100L, 1L);

        verify(orderMapper).updateById(argThat(order -> order.getStatus() == 4));
    }

    @Test
    @DisplayName("获取买家订单列表")
    void getBoughtOrders_success() {
        when(orderMapper.findByBuyerId(100L)).thenReturn(List.of(testOrder));

        List<Order> result = orderService.getBoughtOrders(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
