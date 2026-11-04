package com.example.controller;

import com.example.common.Result;
import com.example.entity.ActivitySign;
import com.example.service.ActivitySignService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 功能：活动报名控制层
 * 作者：captain_dong
 * 日期：2024/6/26 13:17
 */
@RestController
@RequestMapping("/activitySign")
public class ActivitySignController {

    @Resource
    ActivitySignService activitySignService;

    /**
     * 添加报名
     * @param activitySign
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody ActivitySign activitySign) {
        activitySignService.add(activitySign);
        return Result.success();
    }

     /**
     * 删除报名
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        activitySignService.deleteById(id);
        return Result.success();
    }

    /**
     * 用户取消报名
     */
    @DeleteMapping("/delete/user/{activityId}/{useId}")
    public Result userDelete(@PathVariable Integer activityId, @PathVariable Integer useId) {
        activitySignService.userDelete(activityId, useId);
        return Result.success();
    }

    /**
     * 批量删除报名
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {
        activitySignService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 分页查询报名
     */
    @GetMapping("/selectPage")
    public Result selectPage(ActivitySign activitySign,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<ActivitySign> page = activitySignService.selectPage(activitySign, pageNum, pageSize);
        return Result.success(page);
    }

}
