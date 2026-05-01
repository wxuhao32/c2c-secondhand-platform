package com.resale.platform.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("audit_log")
public class AuditLogEntry implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String module;

    private String action;

    private String description;

    private String method;

    private String path;

    private String ip;

    private Integer statusCode;

    private Long duration;

    private LocalDateTime createdAt;
}
