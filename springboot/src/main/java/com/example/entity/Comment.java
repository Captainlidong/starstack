package com.example.entity;

import java.util.List;

/**
 * 功能：评论实体类
 * 作者：captain_dong
 * 日期：2024/1/27 1:09
 */
public class Comment {

    /** ID */
    private Integer id;

    /** 内容 */
    private String content;

    /** 评论人 */
    private Integer userId;

    /** 父级ID */
    private Integer pid;

    /** 根节点ID */
    private Integer rootId;

    /** 评论时间 */
    private String time;

    /** 博客ID */
    private Integer fid;

    /**模块*/
    private String module;

    private String userName;

    private String userAvatar; /**用户头像*/

    private List<Comment> children;

    private String replyUser; /**回复给谁  就是谁的名称*/

    public String getReplyUser() {
        return replyUser;
    }

    public void setReplyUser(String replyUser) {
        this.replyUser = replyUser;
    }

    public List<Comment> getChildren() {
        return children;
    }

    public void setChildren(List<Comment> children) {
        this.children = children;
    }

    /**子评论(回复)*/



    public String getUserAvatar() {
        return userAvatar;
    }

    public void setUserAvatar(String userAvatar) {
        this.userAvatar = userAvatar;
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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getRootId() {
        return rootId;
    }

    public void setRootId(Integer rootId) {
        this.rootId = rootId;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getFid() {
        return fid;
    }

    public void setFid(Integer fid) {
        this.fid = fid;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }
}