package com.example.utils;

/**
 * 字段格式校验工具：注册/新增时校验账号，个人资料校验电话、邮箱（选填字段留空放行）
 */
public class ValidateUtils {

    /** 账号：4-16位字母、数字或下划线（允许数字开头） */
    private static final String USERNAME_PATTERN = "^\\w{4,16}$";

    /** 电话：11位国内手机号 */
    private static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";

    /** 邮箱：常规格式 */
    private static final String EMAIL_PATTERN = "^[\\w.%+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

    private ValidateUtils() {
    }

    public static boolean isValidUsername(String username) {
        return username != null && username.matches(USERNAME_PATTERN);
    }

    /**
     * 选填字段：留空（null 或空白）放行，填了必须是合法手机号
     */
    public static boolean isValidPhone(String phone) {
        return isBlank(phone) || phone.matches(PHONE_PATTERN);
    }

    /**
     * 选填字段：留空（null 或空白）放行，填了必须是合法邮箱
     */
    public static boolean isValidEmail(String email) {
        return isBlank(email) || email.matches(EMAIL_PATTERN);
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
