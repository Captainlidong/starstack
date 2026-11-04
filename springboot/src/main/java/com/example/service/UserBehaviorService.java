package com.example.service;

import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.UserBehavior;
import com.example.mapper.UserBehaviorMapper;
import com.example.utils.TokenUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 用户行为记录服务（用于协同过滤推荐）
 * 行为类型：1浏览 2点赞 3收藏
 */
@Service
public class UserBehaviorService {

    private static final Logger log = LoggerFactory.getLogger(UserBehaviorService.class);

    public static final byte TYPE_VIEW = 1;
    public static final byte TYPE_LIKE = 2;
    public static final byte TYPE_COLLECT = 3;

    @Resource
    private UserBehaviorMapper userBehaviorMapper;

    /**
     * 记录用户行为：同一用户同一博客同一行为只保留一条，重复触发仅刷新时间
     * 仅记录普通用户（user_behavior 外键关联 user 表）；任何异常不影响主业务
     */
    public void record(Integer blogId, byte behaviorType) {
        try {
            Account account = TokenUtils.getCurrentUser();
            if (!isNormalUser(account)) {
                return;
            }
            UserBehavior db = userBehaviorMapper.selectOne(account.getId(), blogId, behaviorType);
            if (db == null) {
                UserBehavior behavior = new UserBehavior();
                behavior.setUserId(account.getId());
                behavior.setBlogId(blogId);
                behavior.setBehaviorType(behaviorType);
                behavior.setWeight(1.0f);
                userBehaviorMapper.insert(behavior);
            } else {
                userBehaviorMapper.updateTimestamp(db.getId());
            }
        } catch (Exception e) {
            log.warn("记录用户行为失败: blogId={}, type={}", blogId, behaviorType, e);
        }
    }

    /**
     * 移除用户行为（取消点赞/取消收藏时调用）
     */
    public void remove(Integer blogId, byte behaviorType) {
        try {
            Account account = TokenUtils.getCurrentUser();
            if (!isNormalUser(account)) {
                return;
            }
            userBehaviorMapper.deleteBehavior(account.getId(), blogId, behaviorType);
        } catch (Exception e) {
            log.warn("移除用户行为失败: blogId={}, type={}", blogId, behaviorType, e);
        }
    }

    private boolean isNormalUser(Account account) {
        return account != null && account.getId() != null
                && RoleEnum.USER.name().equals(account.getRole());
    }
}
