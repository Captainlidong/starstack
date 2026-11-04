package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Account;
import com.example.entity.Follow;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.FollowMapper;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 关注/粉丝业务
 */
@Service
public class FollowService {

    @Resource
    private FollowMapper followMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private NotificationService notificationService;

    /** 关注某人（幂等：重复关注直接忽略） */
    public void follow(Integer targetUserId) {
        Account current = TokenUtils.getCurrentUser();
        if (current.getId() == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
        }
        if (current.getId().equals(targetUserId)) {
            throw new CustomException(ResultCodeEnum.FOLLOW_SELF_ERROR);
        }
        if (followMapper.selectByUserAndFollower(targetUserId, current.getId()) != null) {
            return;
        }
        Follow f = new Follow();
        f.setUserId(targetUserId);
        f.setFollowerId(current.getId());
        f.setTime(DateUtil.now());
        followMapper.insert(f);
        User target = userMapper.selectById(targetUserId);
        if (target != null) {
            String nickname = current.getName() != null && !current.getName().isEmpty() ? current.getName() : current.getUsername();
            notificationService.send(targetUserId, "FOLLOW", "新增粉丝",
                    nickname + " 关注了你", current.getId());
        }
    }

    /** 取消关注（幂等） */
    public void unfollow(Integer targetUserId) {
        Account current = TokenUtils.getCurrentUser();
        if (current.getId() == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
        }
        Follow f = new Follow();
        f.setUserId(targetUserId);
        f.setFollowerId(current.getId());
        followMapper.delete(f);
    }

    /** 当前登录用户是否已关注 targetUserId（游客返回 false） */
    public boolean isFollowed(Integer targetUserId) {
        Account current = TokenUtils.getCurrentUser();
        if (current.getId() == null) {
            return false;
        }
        return followMapper.selectByUserAndFollower(targetUserId, current.getId()) != null;
    }

    /** 粉丝列表（关注了 userId 的人） */
    public PageInfo<User> fans(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = followMapper.selectFans(userId, currentUserIdOrMinusOne());
        return PageInfo.of(list);
    }

    /** 关注列表（userId 关注的人） */
    public PageInfo<User> following(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<User> list = followMapper.selectFollowing(userId, currentUserIdOrMinusOne());
        return PageInfo.of(list);
    }

    private Integer currentUserIdOrMinusOne() {
        Account current = TokenUtils.getCurrentUser();
        return current.getId() != null ? current.getId() : -1;
    }
}
