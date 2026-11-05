package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.common.enums.LikesModuleEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.*;
import com.example.mapper.ActivityMapper;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 功能：活动业务处理
 * 作者：captain_dong
 * 日期：2024/12/21 20:46
 */
@Service
public class ActivityService {
    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private ActivitySignService activitySignService;

    @Resource
    private LikesService likesService;

    @Resource
    private CollectService  collectService;

    @Resource
    private UserMapper userMapper;

    @Resource
    private NotificationService notificationService;

    /**
     * 新增
     * @param activity
     */
    @CacheEvict(value = "activityTop", allEntries = true)
    public void add(Activity activity) {
        activityMapper.insert(activity);
        // 新活动上线：广播通知全部用户
        try {
            List<User> users = userMapper.selectAll(new User());
            for (User u : users) {
                notificationService.send(u.getId(), "SYSTEM", "新活动上线",
                        "新活动《" + activity.getName() + "》已发布，快来查看吧！", activity.getId());
            }
        } catch (Exception ignored) {
            // 广播失败不影响活动发布
        }
    }

    /**
     * 删除
     * @param id
     */
    @CacheEvict(value = "activityTop", allEntries = true)
    public void deleteById(Integer id) {
        activityMapper.deleteById(id);
    }

    /**
     * 批量删除
     * @param ids
     */
    @CacheEvict(value = "activityTop", allEntries = true)
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            activityMapper.deleteById(id);
        }
    }

    /**
     *  修改
     * @param activity
     */
    @CacheEvict(value = "activityTop", allEntries = true)
    public void updateById(Activity activity) {
        activityMapper.updateById(activity);
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    public Activity selectById(Integer id) {
        Activity activity = activityMapper.selectById(id);  //查询活动
        this.setAct(activity, TokenUtils.getCurrentUser()); //是否报名了活动

        int likesCount = likesService.selectByFidAndModule(id, LikesModuleEnum.ACTIVITY.getValue()); //获取点赞数
        int collectCount = collectService.selectByFidAndModule(id, LikesModuleEnum.ACTIVITY.getValue());//获取收藏数
        activity.setLikesCount(likesCount);  //设置点赞数
        activity.setCollectCount(collectCount); //设置收藏数

        Likes likes = likesService.selectUserLikes(id, LikesModuleEnum.ACTIVITY.getValue());//设置用户是否点赞
        activity.setIsLike(likes != null);//设置用户是否点赞

        Collect collect = collectService.selectUserCollect(id, LikesModuleEnum.ACTIVITY.getValue());//设置用户是否收藏
        activity.setIsCollect(collect != null); //设置用户是否收藏

        return activity;
    }

    /**
     * 查询所有
     * @param activity
     * @return
     */
    public List<Activity> selectAll(Activity activity) {
        return activityMapper.selectAll(activity);
    }

    /**
     * 分页查询
     * @param activity
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Activity> selectPage(Activity activity, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Activity> list = activityMapper.selectAll(activity);
        PageInfo<Activity> pageInfo = PageInfo.of(list);
        List<Activity> activityList = pageInfo.getList();
        Account currentUser = TokenUtils.getCurrentUser();
        for (Activity act : activityList) {
            this.setAct(act, currentUser); //是否报名了活动
        }
        return pageInfo;
    }

    /**
     *  设置活动额外状态信息
     * @param activity
     * @param currentUser
     */
    private void setAct(Activity activity, Account currentUser){
        activity.setIsEnd(DateUtil.parseDate(activity.getEnd()).isBefore(new Date()));  // 活动的结束时间在当前时间之前  就表示活动结束了
        ActivitySign activitySign = activitySignService.selectByActivityIdAndUserId(activity.getId(), currentUser.getId());// 查询当前用户是否报名了该活动
        activity.setIsSign(activitySign != null);// 报名了就设置为true
    }

    /**
     * 热门活动
     */
    @Cacheable(value = "activityTop", key = "'all'")
    public List<Activity> selectTop() {
        List<Activity> activityList = this.selectAll(null);
        activityList= activityList.stream().sorted((b1, b2) -> b2.getReadCount().compareTo(b1.getReadCount()))
                .limit(2)
                .collect(Collectors.toList());
        return activityList;
    }

    /**
     * 阅读数+1
     * @param activityId
     */
    public void updateReadCount(Integer  activityId){
        activityMapper.updateReadCount(activityId);
    }

    /**
     * 分页查询 用户报名的活动  -->查询出用户报名的活动列表
     * @param activity
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Activity> selectUser(Activity activity, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if(RoleEnum.USER.name().equals(currentUser.getRole())){
            activity.setUserId(currentUser.getId());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Activity> list = activityMapper.selectUser(activity);
        PageInfo<Activity> pageInfo = PageInfo.of(list);
        List<Activity> activityList = pageInfo.getList();
        for (Activity act : activityList) {
            this.setAct(act, currentUser); //是否报名了活动
        }
        return pageInfo;
    }

    /**
     * 分页查询 用户点赞过的活动
     * @param activity
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Activity> selectUserLike(Activity activity, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if(RoleEnum.USER.name().equals(currentUser.getRole())){
            activity.setUserId(currentUser.getId());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Activity> list = activityMapper.selectUserLike(activity);
        PageInfo<Activity> pageInfo = PageInfo.of(list);
        List<Activity> activityList = pageInfo.getList();
        for (Activity act : activityList) {
            this.setAct(act, currentUser); //是否报名了活动
        }
        return pageInfo;
    }

    /**
     * 分页查询 用户收藏的活动
     * @param activity
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Activity> selectUserCollect(Activity activity, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if(RoleEnum.USER.name().equals(currentUser.getRole())){
            activity.setUserId(currentUser.getId());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Activity> list = activityMapper.selectUserCollect(activity);
        PageInfo<Activity> pageInfo = PageInfo.of(list);
        List<Activity> activityList = pageInfo.getList();
        for (Activity act : activityList) {
            this.setAct(act, currentUser); //是否报名了活动
        }
        return pageInfo;
    }

    /**
     * 分页查询 用户评论过的活动
     * @param activity
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Activity> selectUserComment(Activity activity, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if(RoleEnum.USER.name().equals(currentUser.getRole())){
            activity.setUserId(currentUser.getId());
        }
        PageHelper.startPage(pageNum, pageSize);
        List<Activity> list = activityMapper.selectUserComment(activity);
        PageInfo<Activity> pageInfo = PageInfo.of(list);
        List<Activity> activityList = pageInfo.getList();
        for (Activity act : activityList) {
            this.setAct(act, currentUser); //是否报名了活动
        }
        return pageInfo;
    }
}