package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Account;
import com.example.entity.Message;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.MessageMapper;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 私信业务：点对点消息，会话列表/聊天记录/已读
 */
@Service
public class MessageService {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private SensitiveWordService sensitiveWordService;

    /** 发送私信（内容敏感词拦截，不能发给自己） */
    public void send(Integer receiverId, String content) {
        Account current = TokenUtils.getCurrentUser();
        if (current.getId() == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
        }
        if (current.getId().equals(receiverId)) {
            throw new CustomException("400", "不能给自己发私信");
        }
        if (StrUtil.isBlank(content)) {
            throw new CustomException("400", "私信内容不能为空");
        }
        sensitiveWordService.checkNoSensitiveWord(content);
        Message m = new Message();
        m.setSenderId(current.getId());
        m.setReceiverId(receiverId);
        m.setContent(content.trim());
        m.setTime(DateUtil.now());
        messageMapper.insert(m);
    }

    /**
     * 会话列表：每个对话的最新一条消息 + 未读数 + 对方信息
     */
    public List<Map<String, Object>> conversations(Integer me) {
        List<Map<String, Object>> pairs = messageMapper.selectConversationPairs(me);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> pair : pairs) {
            // MySQL 聚合函数返回 Long，统一经 Number 转换避免强转异常
            Integer otherId = ((Number) pair.get("otherId")).intValue();
            Integer lastId = ((Number) pair.get("lastId")).intValue();
            Map<String, Object> item = new HashMap<>();
            item.put("otherId", otherId);
            item.put("unreadCount", ((Number) pair.get("unreadCount")).intValue());

            Message last = messageMapper.selectById(lastId);
            if (last != null) {
                item.put("lastContent", last.getContent());
                item.put("lastTime", last.getTime());
            }
            User other = userMapper.selectById(otherId);
            if (other != null) {
                item.put("otherName", StrUtil.isBlank(other.getName()) ? other.getUsername() : other.getName());
                item.put("otherAvatar", other.getAvatar());
            }
            result.add(item);
        }
        return result;
    }

    /** 聊天记录（最近 50 条，按时间正序返回） */
    public List<Message> history(Integer me, Integer other) {
        List<Message> list = messageMapper.selectHistory(me, other);
        List<Message> ordered = new ArrayList<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            ordered.add(list.get(i));
        }
        return ordered;
    }

    /** 进入会话时将对方发来的消息标记已读 */
    public void markRead(Integer me, Integer other) {
        messageMapper.markRead(me, other);
    }

    public int unreadCount(Integer me) {
        return messageMapper.unreadCount(me);
    }
}
