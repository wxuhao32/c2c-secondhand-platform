package com.resale.platform.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class OrderEvent extends ApplicationEvent {

    private final Long orderId;
    private final Long userId;
    private final String action;

    public OrderEvent(Object source, Long orderId, Long userId, String action) {
        super(source);
        this.orderId = orderId;
        this.userId = userId;
        this.action = action;
    }
}
