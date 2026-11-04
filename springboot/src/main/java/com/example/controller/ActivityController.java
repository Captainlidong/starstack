package com.example.controller;

import com.example.common.Result;
import com.example.entity.Activity;
import com.example.entity.Blog;
import com.example.service.ActivityService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 功能：活动前端操作接口
 * 作者：captain_dong
 * 日期：2023/12/21 20:45
 */
@RestController
@RequestMapping("/activity")
public class ActivityController {
    @Resource
    private ActivityService activityService;
    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Activity activity){
        activityService.add(activity);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        activityService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        activityService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Activity activity){
        activityService.updateById(activity);
        return Result.success();
    }

    /**
     * 更新活动阅读量  即 ：阅读量+1
     * @param activityId
     * @return
     */
    @PutMapping("/updateReadCount/{activityId}")
    public Result updateReadCount(@PathVariable Integer activityId){
        activityService.updateReadCount(activityId);
        return Result.success();
    }

    /**
     * 根据id查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        Activity activity=activityService.selectById(id);
        return Result.success(activity);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Activity activity){
        List<Activity> list= activityService.selectAll(activity);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Activity activity,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Activity> page=activityService.selectPage(activity,pageNum,pageSize);
        return Result.success(page);
    }

    /**
     * 分页查询 用户报名的活动
     */
    @GetMapping("/selectUser")
    public Result selectUser(Activity activity,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Activity> page=activityService.selectUser(activity,pageNum,pageSize);
        return Result.success(page);
    }
    /**
     * 分页查询 用户点赞过的活动
     */
    @GetMapping("/selectUserLike")
    public Result selectUserLike(Activity activity,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Activity> page=activityService.selectUserLike(activity,pageNum,pageSize);
        return Result.success(page);
    }
    /**
     * 分页查询 用户收藏的活动
     */
    @GetMapping("/selectUserCollect")
    public Result selectUserCollect(Activity activity,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Activity> page=activityService.selectUserCollect(activity,pageNum,pageSize);
        return Result.success(page);
    }
    /**
     * 分页查询 用户评论过的活动
     */
    @GetMapping("/selectUserComment")
    public Result selectUserComment(Activity activity,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Activity> page=activityService.selectUserComment(activity,pageNum,pageSize);
        return Result.success(page);
    }

    /**
     * 热门活动
     * @return
     */
    @GetMapping("/selectTop")
    public Result selectTop(){
        List<Activity> list=activityService.selectTop();
        return Result.success(list);
    }
}