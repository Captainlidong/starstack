package com.example.utils;

import cn.hutool.crypto.digest.BCrypt;

/**
 * 密码哈希工具：BCrypt 加盐存储（每次哈希自动生成随机盐，数据库不存明文，抗彩虹表与暴力破解）
 */
public class PasswordUtils {

    /** 密码强度规则：至少8位，且同时包含字母和数字 */
    private static final String STRENGTH_PATTERN = "^(?=.*[A-Za-z])(?=.*\\d).{8,}$";

    private PasswordUtils() {
    }

    public static String hash(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    /**
     * 校验密码：库存为存量明文时退化为明文比对，登录成功后由调用方触发哈希升级
     */
    public static boolean matches(String rawPassword, String stored) {
        if (rawPassword == null || stored == null) {
            return false;
        }
        if (isHashed(stored)) {
            return BCrypt.checkpw(rawPassword, stored);
        }
        return rawPassword.equals(stored);
    }

    /**
     * 库存密码是否已是 BCrypt 哈希（$2 开头），用于存量明文密码的透明升级判断
     */
    public static boolean isHashed(String stored) {
        return stored != null && stored.startsWith("$2");
    }

    public static boolean isStrongEnough(String rawPassword) {
        return rawPassword != null && rawPassword.matches(STRENGTH_PATTERN);
    }
}
