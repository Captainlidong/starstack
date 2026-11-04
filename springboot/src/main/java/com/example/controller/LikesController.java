package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Likes;
import com.example.service.LikesService;
import com.example.utils.TokenUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 功能：点赞接口开发控制类
 * 作者：captain_dong
 * 日期：2024/1/26 17:44
 */
@RestController
@RequestMapping("/likes")
public class LikesController {
    @Resource
    LikesService likesService;

    /**
     * 功能：点赞或取消点赞（仅用户角色，管理员产生的点赞会形成无主数据）
     * @param likes
     * @return
     */
    @PostMapping("/set")
    public Result set(@RequestBody Likes likes){
        Account currentUser = TokenUtils.getCurrentUser();
        if (!RoleEnum.USER.name().equals(currentUser.getRole())) {
            return Result.error("403", "请使用用户账号进行点赞操作");
        }
        likesService.set(likes);
        return Result.success();
    }

}
