package com.example.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 仪表盘聚合查询
 */
public interface DashboardMapper {

    int countUsers();

    int countUsersToday(@Param("today") String today);

    int countBlogs();

    int sumReads();

    int countComments();

    int countActivities();

    int countSigns();

    int countLikes();

    int countCollects();

    int countFollows();

    int countBlogsByStatus(@Param("status") String status);

    int countPendingReports();

    /** 分类博客数量分布（已发布公开，含 0 的分类） */
    List<Map<String, Object>> selectCategoryDistribution();

    /** 近 7 天发文趋势（date 为 yyyy-MM-dd 字符串，与 CURDATE() 可直接比较） */
    List<Map<String, Object>> selectBlogTrend(@Param("startDate") String startDate);

    /** 近 7 天注册用户趋势 */
    List<Map<String, Object>> selectUserTrend(@Param("startDate") String startDate);

    /** 热门博客 Top10（公开已发布，按浏览量） */
    List<Map<String, Object>> selectHotBlogs();

    /** 最新待审核博客 5 条 */
    List<Map<String, Object>> selectLatestPendingBlogs();
}
