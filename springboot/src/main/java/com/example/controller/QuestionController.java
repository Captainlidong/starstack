package com.example.controller;

import com.example.common.Result;
import com.example.entity.Account;
import com.example.entity.Question;
import com.example.service.QuestionService;
import com.example.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 问答接口（一问一答，需登录）
 */
@RestController
@RequestMapping("/question")
public class QuestionController {

    @Resource
    private QuestionService questionService;

    /** 向作者提问 */
    @PostMapping("/add")
    public Result add(@RequestBody Question question) {
        questionService.add(question.getAuthorId(), question);
        return Result.success();
    }

    /** 我提出的问答 */
    @GetMapping("/myAsk")
    public Result myAsk() {
        Account current = TokenUtils.getCurrentUser();
        List<Question> list = questionService.myAsk(current.getId());
        return Result.success(list);
    }

    /** 我收到的问答（向我提问） */
    @GetMapping("/myReceived")
    public Result myReceived() {
        Account current = TokenUtils.getCurrentUser();
        List<Question> list = questionService.myReceived(current.getId());
        return Result.success(list);
    }

    /** 作者回答提问（仅被提问作者本人） */
    @PutMapping("/answer")
    public Result answer(@RequestBody Question question) {
        Account current = TokenUtils.getCurrentUser();
        questionService.answer(question.getId(), question.getAnswer(), current);
        return Result.success();
    }
}
