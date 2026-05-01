package com.resale.platform.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class ProductEvent extends ApplicationEvent {

    private final Long productId;
    private final Long userId;
    private final String action;

    public ProductEvent(Object source, Long productId, Long userId, String action) {
        super(source);
        this.productId = productId;
        this.userId = userId;
        this.action = action;
    }
}
