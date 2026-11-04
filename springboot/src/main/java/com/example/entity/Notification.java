package com.example.entity;

/**
 * 消息通知实体
 */
public class Notification {
    private Integer id;
    /** 接收人ID */
    private Integer userId;
    /** 类型：LIKE/COLLECT/COMMENT/FOLLOW/AUDIT/REPORT/SYSTEM */
    private String type;
    /** 标题 */
    private String title;
    /** 内容 */
    private String content;
    /** 关联对象ID（博客/活动/用户） */
    private Integer targetId;
    /** 0未读 1已读 */
    private Integer isRead;
    /** 时间 */
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public Integer getIsRead() {
        return isRead;
    }

    public void setIsRead(Integer isRead) {
        this.isRead = isRead;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
