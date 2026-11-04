package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Account;
import com.example.entity.Activity;
import com.example.entity.ActivitySign;
import com.example.exception.CustomException;
import com.example.mapper.ActivityMapper;
import com.example.mapper.ActivitySignMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 功能：
 * 作者：captain_dong
 * 日期：2024/6/26 13:15
 */
@Service
public class ActivitySignService {

    @Resource
    ActivitySignMapper activitySignMapper;

    @Resource
    ActivityMapper activityMapper;

    @Resource
    NotificationService notificationService;

    /**
     *  功能：添加活动报名记录
     * @param activitySign
     */
    public void add(ActivitySign activitySign) {
        Account currentUser = TokenUtils.getCurrentUser();  // 获取当前用户
        ActivitySign as = this.selectByActivityIdAndUserId(activitySign.getActivityId(), currentUser.getId());  // 查看用户是否已经报名
        if (as != null) { // 如果已经报名
            throw new CustomException(ResultCodeEnum.ACTIVITY_SIGN_ERROR);  // 抛出异常
        }
        activitySign.setUserId(currentUser.getId());  // 设置报名用户id
        activitySign.setTime(DateUtil.now()); // 设置报名时间
        activitySignMapper.insert(activitySign);  // 添加报名记录
        // 报名成功通知本人
        Activity activity = activityMapper.selectById(activitySign.getActivityId());
        if (activity != null) {
            notificationService.send(currentUser.getId(), "SYSTEM", "报名成功",
                    "你已成功报名活动《" + activity.getName() + "》", activity.getId());
        }
    }

    /**
     * 功能：查看用户是否已经报名
     * @param actId
     * @param userId
     * @return
     */
    public ActivitySign selectByActivityIdAndUserId(Integer actId, Integer userId) {
        return activitySignMapper.selectByActivityIdAndUserId(actId, userId);
    }

    /**
     * 功能：分页查询
     * @param activitySign
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<ActivitySign> selectPage(ActivitySign activitySign, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<ActivitySign> list = activitySignMapper.selectAll(activitySign);
        return PageInfo.of(list);
    }

    /**
     * 功能：删除
     * @param id
     */
    public void deleteById(Integer id) {
        activitySignMapper.deleteById(id);
    }

    /**
     *  功能：批量删除
     * @param ids
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            this.deleteById(id);
        }
    }

    /**
     * 用户取消报名
     * @param activityId
     * @param useId
     */
    public void userDelete(Integer activityId, Integer useId) {
        activitySignMapper.userDelete(activityId, useId);
    }
}