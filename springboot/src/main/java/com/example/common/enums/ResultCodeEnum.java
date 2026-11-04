package com.example.common.enums;

public enum ResultCodeEnum {
    SUCCESS("200", "成功"),

    PARAM_ERROR("400", "参数异常"),
    TOKEN_INVALID_ERROR("401", "无效的token"),
    TOKEN_CHECK_ERROR("401", "token验证失败，请重新登录"),
    PARAM_LOST_ERROR("4001", "参数缺失"),

    SYSTEM_ERROR("500", "系统异常"),
    USER_EXIST_ERROR("5001", "用户名已存在"),
    USER_NOT_LOGIN("5002", "用户未登录"),
    USER_ACCOUNT_ERROR("5003", "账号或密码错误"),
    USER_NOT_EXIST_ERROR("5004", "用户不存在"),
    PARAM_PASSWORD_ERROR("5005", "原密码输入错误"),
    ACTIVITY_SIGN_ERROR("5006", "活动已报名"),
    CAPTCHA_ERROR("5007", "验证码错误或已过期"),
    PASSWORD_WEAK_ERROR("5009", "密码强度不足：至少8位，且需同时包含字母和数字"),
    USERNAME_FORMAT_ERROR("5010", "账号仅支持4-16位字母、数字或下划线"),
    PHONE_FORMAT_ERROR("5011", "电话格式不正确，请输入11位手机号"),
    EMAIL_FORMAT_ERROR("5012", "邮箱格式不正确"),
    CONTENT_SENSITIVE_ERROR("5013", "内容包含违禁词，请修改后再发布"),
    REPORT_DUPLICATE_ERROR("5014", "您已举报过该内容，请耐心等待处理"),
    BLOG_NOT_VISIBLE_ERROR("5015", "该博客不存在或未公开"),
    FOLLOW_SELF_ERROR("5016", "不能关注自己"),
    COMMENT_EMPTY_ERROR("5017", "评论内容不能为空")
    ;

    public String code;
    public String msg;

    ResultCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
