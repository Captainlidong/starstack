package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Collect;
import com.example.service.CollectService;
import com.example.utils.TokenUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 功能：收藏功能 接口开发
 * 作者：captain_dong
 * 日期：2024/1/26 20:50
 */
@RestController
@RequestMapping("/collect")
public class CollectController {
    @Resource
    CollectService collectService;

    /**
     * 收藏与取消收藏（仅用户角色，管理员产生的收藏会形成无主数据）
     * @param collect
     * @return
     */
    @PostMapping("/set")
    public Result set(@RequestBody Collect collect){
        Account currentUser = TokenUtils.getCurrentUser();
        if (!RoleEnum.USER.name().equals(currentUser.getRole())) {
            return Result.error("403", "请使用用户账号进行收藏操作");
        }
        collectService.set(collect);
        return Result.success();
    }
}
