package com.example.controller;

import com.example.common.Result;
import com.example.entity.Blog;
import com.example.service.RecommendationService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/recommend")
public class RecommendController {

    @Resource
    private RecommendationService recommendationService;

    /**
     * 用户协同过滤推荐（分页）
     * 前端按 Result 包装结构取 data.list / data.total，故此处手动分页包装
     */
    @GetMapping("/user/{userId}")
    public Result recommendForUser(@PathVariable Integer userId,
                                   @RequestParam(defaultValue = "1") Integer pageNum,
                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        List<Blog> recommendations = recommendationService.userBasedCF(userId);
        int from = Math.min((pageNum - 1) * pageSize, recommendations.size());
        int to = Math.min(from + pageSize, recommendations.size());

        Page<Blog> page = new Page<>(pageNum, pageSize);
        page.addAll(recommendations.subList(from, to));
        page.setTotal(recommendations.size());
        return Result.success(new PageInfo<>(page));
    }
}
