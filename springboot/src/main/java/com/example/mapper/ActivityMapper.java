package com.example.mapper;

import com.example.entity.Activity;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 操作activity相关数据接口
 */
public interface ActivityMapper {

    /**
     * 新增活动
     * @param activity
     */
    void insert(Activity activity);

    /**
     * 根据 id 删除活动
     * @param id
     */
    void deleteById(Integer id);

    /**
     * 修改活动
     * @param activity
     */
    void updateById(Activity activity);

    /**
     *  更新活动阅读数
     * @param activityId
     */
    @Update("update activity set read_count = read_count + 1 where id = #{activityId}")
    void updateReadCount(Integer activityId);

    /**
     * 根据id查询活动
     * @param id
     * @return
     */
    Activity selectById(Integer id);

    /**
     *  查询所有活动
     * @param activity
     * @return
     */
    List<Activity> selectAll(Activity activity);

    /**
     * 分页查询 用户报名的活动
     * @param activity
     */
    List<Activity> selectUser(Activity activity);

    /**
     * 分页查询 用户点赞过的活动
     * @param activity
     * @return
     */
    List<Activity> selectUserLike(Activity activity);

    /**
     * 分页查询 用户收藏的活动
     * @param activity
     * @return
     */
    List<Activity> selectUserCollect(Activity activity);

    /**
     * 分页查询 用户评论过的活动
     * @param activity
     * @return
     */
    List<Activity> selectUserComment(Activity activity);
}
