package com.example.controller;

import com.example.common.Result;
import com.example.service.CaptchaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 图形验证码（登录/注册使用）
 */
@RestController
public class CaptchaController {

    @Resource
    private CaptchaService captchaService;

    @GetMapping("/captcha")
    public Result captcha() {
        return Result.success(captchaService.generate());
    }
}
