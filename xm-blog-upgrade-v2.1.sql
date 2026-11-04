-- ============================================================
-- xm-blog v2.1 升级脚本：仪表盘用户增长趋势
-- 执行方式：本地与服务器各执行一次
-- ============================================================

USE `xm-blog`;

-- 1. user 表加注册时间列
ALTER TABLE `user` ADD COLUMN `create_time` varchar(255) DEFAULT NULL COMMENT '注册时间' AFTER `birth`;

-- 2. 存量用户统一填充为系统上线日（保证增长趋势图有起点）
UPDATE `user` SET `create_time` = '2026-10-26' WHERE `create_time` IS NULL;
