package com.example.controller;

import com.example.common.Result;
import com.example.service.FollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 关注/取关接口（需登录）
 */
@RestController
@RequestMapping("/follow")
public class FollowController {

    @Resource
    private FollowService followService;

    /** 关注某人 */
    @PostMapping("/{userId}")
    public Result follow(@PathVariable Integer userId) {
        followService.follow(userId);
        return Result.success();
    }

    /** 取消关注 */
    @DeleteMapping("/{userId}")
    public Result unfollow(@PathVariable Integer userId) {
        followService.unfollow(userId);
        return Result.success();
    }
}
