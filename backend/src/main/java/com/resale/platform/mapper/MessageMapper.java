package com.resale.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resale.platform.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface MessageMapper extends BaseMapper<Message> {

    @Select("SELECT * FROM message WHERE receiver_id = #{receiverId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<Message> findByReceiverId(@Param("receiverId") Long receiverId);

    @Select("SELECT * FROM message WHERE receiver_id = #{receiverId} AND type = #{type} AND is_deleted = 0 ORDER BY created_at DESC")
    List<Message> findByReceiverIdAndType(@Param("receiverId") Long receiverId, @Param("type") Integer type);

    @Select("SELECT COUNT(*) FROM message WHERE receiver_id = #{receiverId} AND is_read = 0 AND is_deleted = 0")
    int countUnread(@Param("receiverId") Long receiverId);

    @Update("UPDATE message SET is_read = 1 WHERE id = #{id} AND receiver_id = #{receiverId}")
    void markAsRead(@Param("id") Long id, @Param("receiverId") Long receiverId);

    @Update("UPDATE message SET is_read = 1 WHERE receiver_id = #{receiverId} AND is_read = 0 AND is_deleted = 0")
    void markAllAsRead(@Param("receiverId") Long receiverId);
}
