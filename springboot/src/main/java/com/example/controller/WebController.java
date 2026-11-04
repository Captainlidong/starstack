package com.example.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.example.common.Result;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.exception.CustomException;
import com.example.service.AdminService;
import com.example.service.CaptchaService;
import com.example.service.LoginAttemptService;
import com.example.service.UserService;
import com.example.utils.PasswordUtils;
import com.example.utils.ValidateUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 基础前端接口
 */
@RestController
public class WebController {

    @Resource
    private AdminService adminService;

    @Resource
    private UserService userService;

    @Resource
    private CaptchaService captchaService;

    @Resource
    private LoginAttemptService loginAttemptService;

    /**
     * 前端打包产物嵌入后端（classpath:/static/index.html 存在）时，根路径转发到前端首页；纯后端模式保持原样返回 JSON
     */
    @GetMapping("/")
    public void hello(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (new ClassPathResource("static/index.html").exists()) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSONUtil.toJsonStr(Result.success("访问成功")));
    }

    /**
     * 登录/注册页路由（GET）：前端嵌入后端时转发到前端页面，
     * 否则浏览器直接访问 /login、/register 会因只存在 POST 接口映射而返回 405
     */
    @GetMapping("/login")
    public void loginPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (new ClassPathResource("static/index.html").exists()) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSONUtil.toJsonStr(Result.success("login")));
    }

    @GetMapping("/register")
    public void registerPage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (new ClassPathResource("static/index.html").exists()) {
            request.getRequestDispatcher("/index.html").forward(request, response);
            return;
        }
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write(JSONUtil.toJsonStr(Result.success("register")));
    }

    /**
     * 登录（验证码 + 连续密码错误锁定双重防护）
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        if (ObjectUtil.isEmpty(account.getUsername()) || ObjectUtil.isEmpty(account.getPassword())
                || ObjectUtil.isEmpty(account.getRole())) {
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        // 连续密码错误达上限后锁定一段时间
        String lockKey = account.getRole() + ":" + account.getUsername();
        long lockRemainingMs = loginAttemptService.getLockRemainingMs(lockKey);
        if (lockRemainingMs > 0) {
            long minutes = (lockRemainingMs + 59999) / 60000;
            return Result.error("5008", String.format("密码连续错误5次，账号已锁定，请约%d分钟后再试", minutes));
        }
        if (!captchaService.verify(account.getCaptchaKey(), account.getCaptchaCode())) {
            return Result.error(ResultCodeEnum.CAPTCHA_ERROR);
        }
        Result result;
        try {
            if (RoleEnum.ADMIN.name().equals(account.getRole())) {
                account = adminService.login(account);
            }else if (RoleEnum.USER.name().equals(account.getRole())) {
                account = userService.login(account);
            }else {
                return Result.error(ResultCodeEnum.PARAM_ERROR);
            }
            result = Result.success(account);
        } catch (CustomException e) {
            // 密码错误计入失败次数，达到上限触发锁定
            if (ResultCodeEnum.USER_ACCOUNT_ERROR.code.equals(e.getCode())) {
                loginAttemptService.onFailure(lockKey);
            }
            throw e;
        }
        loginAttemptService.onSuccess(lockKey);
        return result;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody Account account) {
        if (StrUtil.isBlank(account.getUsername()) || StrUtil.isBlank(account.getPassword())
                || ObjectUtil.isEmpty(account.getRole())) {
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        // 账号格式校验（4-16位字母、数字或下划线，禁止中文）
        if (!ValidateUtils.isValidUsername(account.getUsername())) {
            return Result.error(ResultCodeEnum.USERNAME_FORMAT_ERROR);
        }
        // 密码强度校验（至少8位，同时包含字母和数字）
        if (!PasswordUtils.isStrongEnough(account.getPassword())) {
            return Result.error(ResultCodeEnum.PASSWORD_WEAK_ERROR);
        }
        if (!captchaService.verify(account.getCaptchaKey(), account.getCaptchaCode())) {
            return Result.error(ResultCodeEnum.CAPTCHA_ERROR);
        }
        if (RoleEnum.USER.name().equals(account.getRole())) {
            userService.register(account);
        }else {  //只允许用户注册
            return Result.error(ResultCodeEnum.PARAM_ERROR);
        }
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account) {
        if (StrUtil.isBlank(account.getUsername()) || StrUtil.isBlank(account.getPassword())
                || ObjectUtil.isEmpty(account.getNewPassword())) {
            return Result.error(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        // 新密码强度校验（至少8位，同时包含字母和数字）
        if (!PasswordUtils.isStrongEnough(account.getNewPassword())) {
            return Result.error(ResultCodeEnum.PASSWORD_WEAK_ERROR);
        }
        if (RoleEnum.ADMIN.name().equals(account.getRole())) {
            adminService.updatePassword(account);
        }else if(RoleEnum.USER.name().equals(account.getRole())) {
            userService.updatePassword(account);
        }
        return Result.success();
    }

}
