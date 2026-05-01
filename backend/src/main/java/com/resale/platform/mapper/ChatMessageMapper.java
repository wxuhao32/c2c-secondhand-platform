package com.resale.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resale.platform.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    @Select("SELECT * FROM chat_message WHERE conversation_id = #{conversationId} AND is_deleted = 0 ORDER BY created_at ASC LIMIT #{limit} OFFSET #{offset}")
    List<ChatMessage> findByConversationId(@Param("conversationId") Long conversationId, @Param("limit") int limit, @Param("offset") int offset);

    @Select("SELECT COUNT(*) FROM chat_message WHERE conversation_id = #{conversationId} AND receiver_id = #{userId} AND status = 0 AND is_deleted = 0")
    int countUnreadByConversation(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Update("UPDATE chat_message SET status = 1 WHERE conversation_id = #{conversationId} AND receiver_id = #{userId} AND status = 0")
    void markAsReadByConversation(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM chat_message WHERE receiver_id = #{userId} AND status = 0 AND is_deleted = 0")
    int countTotalUnread(@Param("userId") Long userId);
}
