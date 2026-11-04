package com.example.controller;

import com.example.common.Result;
import com.example.service.FollowService;
import com.example.service.SpaceService;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 个人空间接口（游客可访问，公开视角）
 */
@RestController
@RequestMapping("/space")
public class SpaceController {

    @Resource
    private SpaceService spaceService;

    @Resource
    private FollowService followService;

    /** 空间头部信息与统计 */
    @GetMapping("/info/{userId}")
    public Result info(@PathVariable Integer userId) {
        return Result.success(spaceService.info(userId));
    }

    /** 空间文章列表（公开已发布） */
    @GetMapping("/blogs/{userId}")
    public Result blogs(@PathVariable Integer userId,
                        @RequestParam(defaultValue = "1") Integer pageNum,
                        @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(spaceService.blogs(userId, pageNum, pageSize));
    }

    /** 粉丝列表 */
    @GetMapping("/fans/{userId}")
    public Result fans(@PathVariable Integer userId,
                       @RequestParam(defaultValue = "1") Integer pageNum,
                       @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(spaceService.fans(userId, pageNum, pageSize));
    }

    /** 关注列表 */
    @GetMapping("/following/{userId}")
    public Result following(@PathVariable Integer userId,
                            @RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(spaceService.following(userId, pageNum, pageSize));
    }

    /** 当前登录用户是否已关注目标用户（游客返回 false） */
    @GetMapping("/isFollowed/{userId}")
    public Result isFollowed(@PathVariable Integer userId) {
        return Result.success(followService.isFollowed(userId));
    }
}
