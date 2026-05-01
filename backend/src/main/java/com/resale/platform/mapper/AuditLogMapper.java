package com.resale.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resale.platform.entity.AuditLogEntry;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLogEntry> {
}
