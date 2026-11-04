package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.service.NotificationService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 消息通知接口（需登录）
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Resource
    private NotificationService notificationService;

    /** 我的通知分页 */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Account current = TokenUtils.getCurrentUser();
        PageInfo<?> page = notificationService.selectPage(current.getId(), pageNum, pageSize);
        return Result.success(page);
    }

    /** 未读数量（铃铛角标） */
    @GetMapping("/unreadCount")
    public Result unreadCount() {
        Account current = TokenUtils.getCurrentUser();
        return Result.success(notificationService.unreadCount(current.getId()));
    }

    /** 标记单条已读（仅本人通知） */
    @PutMapping("/read/{id}")
    public Result read(@PathVariable Integer id) {
        Account current = TokenUtils.getCurrentUser();
        notificationService.read(id, current.getId());
        return Result.success();
    }

    /** 全部已读 */
    @PutMapping("/readAll")
    public Result readAll() {
        Account current = TokenUtils.getCurrentUser();
        notificationService.readAll(current.getId());
        return Result.success();
    }
}
