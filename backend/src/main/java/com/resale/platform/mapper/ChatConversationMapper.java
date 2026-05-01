package com.resale.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resale.platform.entity.ChatConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface ChatConversationMapper extends BaseMapper<ChatConversation> {

    @Select("SELECT * FROM chat_conversation WHERE (user1_id = #{userId} OR user2_id = #{userId}) AND is_deleted = 0 ORDER BY last_message_time DESC")
    List<ChatConversation> findByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM chat_conversation WHERE conversation_key = #{conversationKey} AND is_deleted = 0")
    ChatConversation findByConversationKey(@Param("conversationKey") String conversationKey);

    @Update("UPDATE chat_conversation SET user1_unread_count = user1_unread_count + 1 WHERE id = #{conversationId} AND user1_id = #{receiverId}")
    void incrementUser1Unread(@Param("conversationId") Long conversationId, @Param("receiverId") Long receiverId);

    @Update("UPDATE chat_conversation SET user2_unread_count = user2_unread_count + 1 WHERE id = #{conversationId} AND user2_id = #{receiverId}")
    void incrementUser2Unread(@Param("conversationId") Long conversationId, @Param("receiverId") Long receiverId);

    @Update("UPDATE chat_conversation SET user1_unread_count = 0 WHERE id = #{conversationId} AND user1_id = #{userId}")
    void resetUser1Unread(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Update("UPDATE chat_conversation SET user2_unread_count = 0 WHERE id = #{conversationId} AND user2_id = #{userId}")
    void resetUser2Unread(@Param("conversationId") Long conversationId, @Param("userId") Long userId);
}
