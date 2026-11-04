package com.example.mapper;

import com.example.entity.SensitiveWord;

import java.util.List;

public interface SensitiveWordMapper {
    List<String> selectAllWords();

    List<SensitiveWord> selectAll();

    void insert(SensitiveWord word);

    SensitiveWord selectByWord(String word);

    void deleteById(Integer id);
}
