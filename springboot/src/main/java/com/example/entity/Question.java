package com.example.entity;

/**
 * 问答实体（一问一答）
 */
public class Question {
    private Integer id;
    /** 提问者ID */
    private Integer askerId;
    /** 被提问作者ID */
    private Integer authorId;
    /** 问题标题 */
    private String title;
    /** 问题描述 */
    private String content;
    /** 作者回答 */
    private String answer;
    /** 回答时间 */
    private String answerTime;
    /** 提问时间 */
    private String time;
    /** 状态：WAIT待回答/ANSWERED已回答 */
    private String status;

    /** 展示字段：提问者昵称（不落库） */
    private String askerName;
    /** 展示字段：提问者头像（不落库） */
    private String askerAvatar;
    /** 展示字段：被提问作者昵称（不落库） */
    private String authorName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAskerId() {
        return askerId;
    }

    public void setAskerId(Integer askerId) {
        this.askerId = askerId;
    }

    public Integer getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Integer authorId) {
        this.authorId = authorId;
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

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getAnswerTime() {
        return answerTime;
    }

    public void setAnswerTime(String answerTime) {
        this.answerTime = answerTime;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAskerName() {
        return askerName;
    }

    public void setAskerName(String askerName) {
        this.askerName = askerName;
    }

    public String getAskerAvatar() {
        return askerAvatar;
    }

    public void setAskerAvatar(String askerAvatar) {
        this.askerAvatar = askerAvatar;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
