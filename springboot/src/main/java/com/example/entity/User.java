package com.example.entity;

/**
 * 功能：
 * 作者：captain_dong
 * 日期：2023/12/19 21:39
 */
public class User extends Account{
    private Integer id;
    private String username;
    private String password;//密码
    private String name;
    private String avatar;//头像
    private String role;//角色
    private String sex;//性别
    private String phone;//手机
    private String email;//邮箱
    private String info;//个人简介
    private String birth;//生日
    private Integer blogCount;//博客数量
    private Integer likesCount;//点赞数量
    private Integer collectCount;//收藏数量
    /** 展示字段：当前登录用户是否已关注该用户（粉丝/关注列表用，不落库） */
    private Integer isFollowed;
    /** 展示字段：粉丝数（博客详情作者卡用，不落库） */
    private Integer fansCount;
    /** 注册时间（仪表盘用户增长趋势用） */
    private String createTime;

    public Integer getBlogCount() {
        return blogCount;
    }

    public void setBlogCount(Integer blogCount) {
        this.blogCount = blogCount;
    }

    public Integer getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(Integer likeCount) {
        this.likesCount = likeCount;
    }

    public Integer getCollectCount() {
        return collectCount;
    }

    public void setCollectCount(Integer collectCount) {
        this.collectCount = collectCount;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getAvatar() {
        return avatar;
    }

    @Override
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String getRole() {
        return role;
    }

    @Override
    public void setRole(String role) {
        this.role = role;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getBirth() {
        return birth;
    }

    public void setBirth(String birth) {
        this.birth = birth;
    }

    public Integer getIsFollowed() {
        return isFollowed;
    }

    public void setIsFollowed(Integer isFollowed) {
        this.isFollowed = isFollowed;
    }

    public Integer getFansCount() {
        return fansCount;
    }

    public void setFansCount(Integer fansCount) {
        this.fansCount = fansCount;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}