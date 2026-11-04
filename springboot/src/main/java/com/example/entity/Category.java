package com.example.entity;

/**
 * 功能：博客分类
 * 作者：captain_dong
 * 日期：2023/12/20 18:04
 */
public class Category {

    /** ID */
    private Integer id;
    /** 分类名称 */
    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}