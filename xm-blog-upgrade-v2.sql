-- ============================================================
-- xm-blog 社区化大版本 v2 升级脚本
-- 执行方式：本地与服务器各执行一次（可在 Navicat / phpMyAdmin / mysql 命令行执行）
-- 幂等性：ALTER/CREATE 均为首次执行型，请勿重复执行
-- ============================================================

USE `xm-blog`;

-- 1. blog 表：可见性 + 审核状态（存量博客自动为"已发布公开"，完全兼容）
ALTER TABLE `blog` ADD COLUMN `visibility` varchar(10) DEFAULT 'PUBLIC' COMMENT '可见性：PUBLIC公开/PRIVATE私人' AFTER `category_id`;
ALTER TABLE `blog` ADD COLUMN `status` varchar(20) DEFAULT 'PUBLISHED' COMMENT '状态：DRAFT草稿/PENDING待审核/PUBLISHED已发布/REJECTED已驳回' AFTER `visibility`;
ALTER TABLE `blog` ADD COLUMN `reject_reason` varchar(255) DEFAULT NULL COMMENT '驳回原因' AFTER `status`;

-- 2. 关注关系表
CREATE TABLE `follow` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int NOT NULL COMMENT '被关注人ID',
  `follower_id` int NOT NULL COMMENT '关注人ID',
  `time` varchar(255) DEFAULT NULL COMMENT '关注时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow` (`user_id`,`follower_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='关注关系表';

-- 3. 消息通知表
CREATE TABLE `notification` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int NOT NULL COMMENT '接收人ID',
  `type` varchar(20) NOT NULL COMMENT '类型：LIKE/COLLECT/COMMENT/FOLLOW/AUDIT/REPORT/SYSTEM',
  `title` varchar(100) DEFAULT NULL COMMENT '标题',
  `content` varchar(500) DEFAULT NULL COMMENT '内容',
  `target_id` int DEFAULT NULL COMMENT '关联对象ID（博客/活动/用户）',
  `is_read` int DEFAULT '0' COMMENT '0未读 1已读',
  `time` varchar(255) DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`id`),
  KEY `idx_notification_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='消息通知表';

-- 4. 举报表
CREATE TABLE `report` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `reporter_id` int NOT NULL COMMENT '举报人ID',
  `target_type` varchar(10) NOT NULL COMMENT '目标类型：BLOG/ACTIVITY',
  `target_id` int NOT NULL COMMENT '目标ID',
  `reason` varchar(255) NOT NULL COMMENT '举报理由',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态：PENDING待处理/RESOLVED举报属实/DISMISSED无问题',
  `result` varchar(255) DEFAULT NULL COMMENT '处理结果说明',
  `time` varchar(255) DEFAULT NULL COMMENT '举报时间',
  `handle_time` varchar(255) DEFAULT NULL COMMENT '处理时间',
  `admin_name` varchar(255) DEFAULT NULL COMMENT '处理管理员',
  PRIMARY KEY (`id`),
  KEY `idx_report_target` (`target_type`,`target_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='举报表';

-- 5. 敏感词表
CREATE TABLE `sensitive_word` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `word` varchar(100) NOT NULL COMMENT '敏感词',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_word` (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='敏感词库';

-- 6. 初始敏感词库（通用分类示例，管理端可增删）
INSERT INTO `sensitive_word` (`word`) VALUES
('赌博'),('博彩'),('六合彩'),('网络赌球'),('押注'),('下注'),
('毒品'),('冰毒'),('摇头丸'),('大麻'),('吸毒'),('贩毒'),('k粉'),
('代开发票'),('办证'),('刻章'),('高利贷'),('洗钱'),('刷单兼职'),('兼职刷单'),
('色情'),('淫秽'),('援交'),('一夜情'),('约炮'),('裸聊'),('卖淫'),('嫖娼'),
('加微信免费领'),('免费领取'),('点击链接领取'),('转账'),('汇款'),('打款'),
(' scam '),('钓鱼网站'),('木马'),('病毒链接'),('账号出借'),
('枪支'),('弹药'),('管制刀具'),('匕首'),('买卖枪'),('仿真枪'),
('自杀教程'),('自残'),('约架'),('寻衅滋事'),
('诈骗'),('骗子'),('电信诈骗'),('网络诈骗'),('传销'),('拉人头'),('资金盘'),('庞氏骗局'),
('代考'),('替考'),('作弊器'),('外挂'),('私服'),('破解版'),('盗版'),
('傻逼'),('煞笔'),('sb智障'),('脑残'),('滚蛋'),('废物'),('贱人'),('婊子'),('妈的'),
('反动'),('暴恐'),('恐怖袭击'),('爆炸物'),
('代孕'),('卖卵'),('人体器官'),('器官买卖'),
('加我私人'),('加我好友有福利'),('内部渠道'),('绝对赚钱'),('稳赚不赔'),('日入过万'),
('刷粉丝'),('刷赞'),('刷流量'),('水军');
