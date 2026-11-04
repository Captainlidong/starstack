package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Report;
import com.example.service.ReportService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 举报接口：用户提交举报，管理员处理
 */
@RestController
@RequestMapping("/report")
public class ReportController {

    @Resource
    private ReportService reportService;

    /** 提交举报（仅用户角色） */
    @PostMapping("/add")
    public Result add(@RequestBody Report report) {
        Account current = TokenUtils.getCurrentUser();
        if (!RoleEnum.USER.name().equals(current.getRole())) {
            return Result.error("403", "请使用用户账号进行举报");
        }
        reportService.add(report);
        return Result.success();
    }

    /** 举报列表（仅管理员） */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(required = false) String status,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        Account current = TokenUtils.getCurrentUser();
        if (!RoleEnum.ADMIN.name().equals(current.getRole())) {
            return Result.error("403", "仅管理员可查看举报列表");
        }
        return Result.success(reportService.selectPage(status, pageNum, pageSize));
    }

    /** 处理举报（仅管理员）：body 携带 {id, status: RESOLVED/DISMISSED, result: 处理说明} */
    @PutMapping("/handle")
    public Result handle(@RequestBody Report report) {
        Account current = TokenUtils.getCurrentUser();
        if (!RoleEnum.ADMIN.name().equals(current.getRole())) {
            return Result.error("403", "仅管理员可处理举报");
        }
        reportService.handle(report);
        return Result.success();
    }
}
