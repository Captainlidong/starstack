package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.service.SensitiveWordService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import com.example.entity.SensitiveWord;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 敏感词管理接口（仅管理员，增删后词库缓存即时刷新）
 */
@RestController
@RequestMapping("/sensitiveWord")
public class SensitiveWordController {

    @Resource
    private SensitiveWordService sensitiveWordService;

    /** 敏感词分页列表 */
    @GetMapping("/selectPage")
    public Result selectPage(@RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        requireAdmin();
        return Result.success(sensitiveWordService.selectPage(pageNum, pageSize));
    }

    /** 新增敏感词 */
    @PostMapping("/add")
    public Result add(@RequestBody SensitiveWord word) {
        requireAdmin();
        sensitiveWordService.add(word.getWord());
        return Result.success();
    }

    /** 删除敏感词 */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        requireAdmin();
        sensitiveWordService.deleteById(id);
        return Result.success();
    }

    private void requireAdmin() {
        Account current = TokenUtils.getCurrentUser();
        if (!RoleEnum.ADMIN.name().equals(current.getRole())) {
            throw new com.example.exception.CustomException("403", "仅管理员可管理敏感词");
        }
    }
}
