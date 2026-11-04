-- ============================================================
-- xm-blog v2.2 升级脚本：私信 + 问答
-- 执行方式：本地与服务器各执行一次
-- ============================================================

USE `xm-blog`;

-- 1. 私信表
CREATE TABLE `message` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `sender_id` int NOT NULL COMMENT '发送者ID',
  `receiver_id` int NOT NULL COMMENT '接收者ID',
  `content` varchar(500) NOT NULL COMMENT '内容',
  `is_read` int DEFAULT '0' COMMENT '0未读 1已读',
  `time` varchar(255) DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`id`),
  KEY `idx_msg_sender` (`sender_id`),
  KEY `idx_msg_receiver` (`receiver_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='私信表';

-- 2. 问答表（一问一答）
CREATE TABLE `question` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `asker_id` int NOT NULL COMMENT '提问者ID',
  `author_id` int NOT NULL COMMENT '被提问作者ID',
  `title` varchar(100) NOT NULL COMMENT '问题标题',
  `content` varchar(1000) DEFAULT NULL COMMENT '问题描述',
  `answer` varchar(2000) DEFAULT NULL COMMENT '作者回答',
  `answer_time` varchar(255) DEFAULT NULL COMMENT '回答时间',
  `time` varchar(255) DEFAULT NULL COMMENT '提问时间',
  `status` varchar(20) DEFAULT 'WAIT' COMMENT '状态：WAIT待回答/ANSWERED已回答',
  PRIMARY KEY (`id`),
  KEY `idx_question_author` (`author_id`),
  KEY `idx_question_asker` (`asker_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='问答表';
