package com.resale.platform.event;

import com.resale.platform.service.CacheService;
import com.resale.platform.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventListener {

    private final MessageService messageService;
    private final CacheService cacheService;

    @Async
    @EventListener
    public void handleOrderEvent(OrderEvent event) {
        log.info("处理订单事件: orderId={}, userId={}, action={}", event.getOrderId(), event.getUserId(), event.getAction());
        try {
            String title = switch (event.getAction()) {
                case "created" -> "订单创建成功";
                case "paid" -> "订单已支付";
                case "shipped" -> "订单已发货";
                case "confirmed" -> "买家已确认收货";
                case "cancelled" -> "订单已取消";
                default -> "订单状态更新";
            };
            String content = "您的订单 #" + event.getOrderId() + " " + title;
            messageService.sendNotification(null, event.getUserId(), title, content, 1);
            cacheService.delete("order:" + event.getOrderId());
        } catch (Exception e) {
            log.error("处理订单事件失败: {}", e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void handleProductEvent(ProductEvent event) {
        log.info("处理商品事件: productId={}, userId={}, action={}", event.getProductId(), event.getUserId(), event.getAction());
        try {
            cacheService.delete("product:" + event.getProductId());
            cacheService.deleteByPattern("product:list:*");
        } catch (Exception e) {
            log.error("处理商品事件失败: {}", e.getMessage(), e);
        }
    }

    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("处理通知事件: receiverId={}, title={}", event.getReceiverId(), event.getTitle());
        try {
            if (event.getReceiverId() != null) {
                messageService.sendNotification(null, event.getReceiverId(), event.getTitle(), event.getContent(), event.getType());
            } else {
                messageService.broadcastNotification(event.getTitle(), event.getContent(), event.getType());
            }
        } catch (Exception e) {
            log.error("处理通知事件失败: {}", e.getMessage(), e);
        }
    }
}
