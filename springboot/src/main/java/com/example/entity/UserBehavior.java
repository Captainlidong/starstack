package com.example.entity;

/**
 * 功能：
 * 作者：captain_dong
 * 日期：2025/6/8 12:13
 */
import java.util.Date;

public class UserBehavior {
    // 主键，自增
    private Integer id;
    // 用户 ID
    private Integer userId;
    // 博客 ID
    private Integer blogId;
    // 行为类型，1:浏览 2:点赞 3:收藏
    private Byte behaviorType;
    // 行为权重，默认值为 1.0
    private Float weight;
    // 行为发生的时间戳
    private Date timestamp;

    // 无参构造函数
    public UserBehavior() {
    }

    // 有参构造函数
    public UserBehavior(Integer id, Integer userId, Integer blogId, Byte behaviorType, Float weight, Date timestamp) {
        this.id = id;
        this.userId = userId;
        this.blogId = blogId;
        this.behaviorType = behaviorType;
        this.weight = weight;
        this.timestamp = timestamp;
    }

    // Getter 和 Setter 方法
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

    public Integer getBlogId() {
        return blogId;
    }

    public void setBlogId(Integer blogId) {
        this.blogId = blogId;
    }

    public Byte getBehaviorType() {
        return behaviorType;
    }

    public void setBehaviorType(Byte behaviorType) {
        this.behaviorType = behaviorType;
    }

    public Float getWeight() {
        return weight;
    }

    public void setWeight(Float weight) {
        this.weight = weight;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserBehavior{" +
                "id=" + id +
                ", userId=" + userId +
                ", blogId=" + blogId +
                ", behaviorType=" + behaviorType +
                ", weight=" + weight +
                ", timestamp=" + timestamp +
                '}';
    }
}