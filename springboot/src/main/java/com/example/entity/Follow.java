package com.example.entity;

/**
 * 关注关系实体
 */
public class Follow {
    private Integer id;
    /** 被关注人ID */
    private Integer userId;
    /** 关注人ID */
    private Integer followerId;
    /** 关注时间 */
    private String time;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getFollowerId() {
        return followerId;
    }

    public void setFollowerId(Integer followerId) {
        this.followerId = followerId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
