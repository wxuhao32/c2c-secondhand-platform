package com.resale.platform.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class NotificationEvent extends ApplicationEvent {

    private final Long receiverId;
    private final String title;
    private final String content;
    private final Integer type;

    public NotificationEvent(Object source, Long receiverId, String title, String content, Integer type) {
        super(source);
        this.receiverId = receiverId;
        this.title = title;
        this.content = content;
        this.type = type;
    }
}
