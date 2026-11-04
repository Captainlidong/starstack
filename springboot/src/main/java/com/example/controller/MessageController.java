package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.service.MessageService;
import com.example.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 私信接口（需登录）
 */
@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    /** 发送私信 */
    @PostMapping("/send")
    public Result send(@RequestBody Map<String, Object> body) {
        Object receiverIdObj = body.get("receiverId");
        if (receiverIdObj == null) {
            return Result.error("400", "参数缺失");
        }
        Integer receiverId = ((Number) receiverIdObj).intValue();
        String content = (String) body.get("content");
        messageService.send(receiverId, content);
        return Result.success();
    }

    /** 会话列表（每个对话的最新消息+未读数+对方信息） */
    @GetMapping("/conversations")
    public Result conversations() {
        Account current = TokenUtils.getCurrentUser();
        return Result.success(messageService.conversations(current.getId()));
    }

    /** 与某人的聊天记录（最近 50 条） */
    @GetMapping("/history/{otherUserId}")
    public Result history(@PathVariable Integer otherUserId) {
        Account current = TokenUtils.getCurrentUser();
        return Result.success(messageService.history(current.getId(), otherUserId));
    }

    /** 进入会话时标记已读 */
    @PutMapping("/read/{otherUserId}")
    public Result read(@PathVariable Integer otherUserId) {
        Account current = TokenUtils.getCurrentUser();
        messageService.markRead(current.getId(), otherUserId);
        return Result.success();
    }

    /** 私信未读总数 */
    @GetMapping("/unreadCount")
    public Result unreadCount() {
        Account current = TokenUtils.getCurrentUser();
        return Result.success(messageService.unreadCount(current.getId()));
    }
}
