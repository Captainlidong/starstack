package com.example.entity;

/**
 * 功能：活动报名实体类
 * 作者：captain_dong
 * 日期：2024/6/26 12:53
 */
public class ActivitySign {
    private Integer id; // ID
    private  Integer activityId; // 活动ID
    private  Integer userId;  // 用户ID
    private String time; // 报名时间
    private String activityName;// 活动名称
    private String userName;// 用户名称

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}