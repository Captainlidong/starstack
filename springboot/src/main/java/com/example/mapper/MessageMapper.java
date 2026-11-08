package com.example.mapper;

import com.example.entity.Message;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface MessageMapper {
    void insert(Message message);

    /** 聊天记录（最近 50 条倒序） */
    List<Message> selectHistory(@Param("me") Integer me, @Param("other") Integer other);

    /** 标记整段会话已读（对方发给我的未读消息） */
    void markRead(@Param("me") Integer me, @Param("other") Integer other);

    /** 私信未读总数 */
    int unreadCount(@Param("me") Integer me);

    /** 会话对聚合：对方ID、最后一条消息ID、未读数 */
    List<Map<String, Object>> selectConversationPairs(@Param("me") Integer me);

    Message selectById(Integer id);

    @org.apache.ibatis.annotations.Delete("delete from message where sender_id = #{userId} or receiver_id = #{userId}")
    void deleteByUser(Integer userId);
}
