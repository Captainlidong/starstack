package com.example.mapper;

import com.example.entity.Question;

import java.util.List;

public interface QuestionMapper {
    void insert(Question question);

    /** 我提出的问答 */
    List<Question> selectMyAsk(Integer askerId);

    /** 我收到的问答 */
    List<Question> selectMyReceived(Integer authorId);

    void updateAnswer(Question question);

    Question selectById(Integer id);

    @org.apache.ibatis.annotations.Delete("delete from question where asker_id = #{userId} or author_id = #{userId}")
    void deleteByUser(Integer userId);
}
