package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.service.DashboardService;
import com.example.utils.TokenUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 仪表盘接口（仅管理员）
 */
@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    /** 全站统计数据（统计卡 + 图表 + 待办，一次请求返回） */
    @GetMapping("/stats")
    public Result stats() {
        if (!RoleEnum.ADMIN.name().equals(TokenUtils.getCurrentUser().getRole())) {
            return Result.error("403", "仅管理员可查看仪表盘");
        }
        return Result.success(dashboardService.stats());
    }
}
