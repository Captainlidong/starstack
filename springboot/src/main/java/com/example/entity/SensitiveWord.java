package com.example.entity;

/**
 * 敏感词实体
 */
public class SensitiveWord {
    private Integer id;
    /** 敏感词 */
    private String word;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }
}
