package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Notification;
import com.example.mapper.NotificationMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 消息通知业务：send 内部吞异常，通知失败不影响主业务
 */
@Service
public class NotificationService {

    @Resource
    private NotificationMapper notificationMapper;

    /**
     * 发送一条通知
     * @param receiverId 接收人
     * @param type       LIKE/COLLECT/COMMENT/FOLLOW/AUDIT/REPORT/SYSTEM
     * @param title      标题
     * @param content    内容
     * @param targetId   关联对象ID（用于点击跳转）
     */
    public void send(Integer receiverId, String type, String title, String content, Integer targetId) {
        try {
            if (receiverId == null) {
                return;
            }
            Notification n = new Notification();
            n.setUserId(receiverId);
            n.setType(type);
            n.setTitle(title);
            n.setContent(content);
            n.setTargetId(targetId);
            n.setIsRead(0);
            n.setTime(DateUtil.now());
            notificationMapper.insert(n);
        } catch (Exception e) {
            // 通知发送失败不影响主业务
        }
    }

    public PageInfo<Notification> selectPage(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(notificationMapper.selectByUserId(userId));
    }

    public int unreadCount(Integer userId) {
        return notificationMapper.unreadCount(userId);
    }

    /** 标记单条已读（仅本人通知可操作） */
    public void read(Integer id, Integer userId) {
        Notification n = notificationMapper.selectById(id);
        if (n != null && userId.equals(n.getUserId())) {
            notificationMapper.readById(id);
        }
    }

    public void readAll(Integer userId) {
        notificationMapper.readAll(userId);
    }
}
