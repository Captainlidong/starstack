package com.example.mapper;

import com.example.entity.Notification;

import java.util.List;

public interface NotificationMapper {
    void insert(Notification notification);

    List<Notification> selectByUserId(Integer userId);

    int unreadCount(Integer userId);

    void readById(Integer id);

    void readAll(Integer userId);

    Notification selectById(Integer id);
}
