package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.mapper.DashboardMapper;
import com.example.mapper.ReportMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘业务：聚合全站统计数据，趋势序列在 Java 侧补零生成完整 7 天日期
 */
@Service
public class DashboardService {

    @Resource
    private DashboardMapper dashboardMapper;

    @Resource
    private ReportMapper reportMapper;

    @Cacheable(value = "dashboardStats", key = "'all'")
    public Map<String, Object> stats() {
        Map<String, Object> data = new HashMap<>();
        String today = DateUtil.today();
        String startDate = DateUtil.format(DateUtil.offsetDay(DateUtil.date(), -6), "yyyy-MM-dd");

        // ---- 统计卡 ----
        data.put("totalUsers", dashboardMapper.countUsers());
        data.put("newUsersToday", dashboardMapper.countUsersToday(today));
        data.put("totalBlogs", dashboardMapper.countBlogs());
        data.put("totalReads", dashboardMapper.sumReads());
        data.put("totalComments", dashboardMapper.countComments());
        data.put("totalActivities", dashboardMapper.countActivities());
        data.put("totalSigns", dashboardMapper.countSigns());
        data.put("totalLikes", dashboardMapper.countLikes());
        data.put("totalCollects", dashboardMapper.countCollects());
        data.put("totalFollows", dashboardMapper.countFollows());
        data.put("pendingBlogs", dashboardMapper.countBlogsByStatus("PENDING"));
        data.put("pendingReports", dashboardMapper.countPendingReports());

        // ---- 图表 ----
        data.put("categoryDistribution", dashboardMapper.selectCategoryDistribution());
        data.put("blogTrend", fillTrend(dashboardMapper.selectBlogTrend(startDate), startDate));
        data.put("userTrend", fillTrend(dashboardMapper.selectUserTrend(startDate), startDate));
        data.put("statusDistribution", statusDistribution());
        data.put("hotBlogs", dashboardMapper.selectHotBlogs());

        // ---- 待办 ----
        PageHelper.startPage(1, 5);
        PageInfo<Map<String, Object>> pendingBlogPage = PageInfo.of(dashboardMapper.selectLatestPendingBlogs());
        data.put("latestPendingBlogs", pendingBlogPage.getList());
        PageHelper.startPage(1, 5);
        PageInfo<com.example.entity.Report> pendingReportPage = PageInfo.of(reportMapper.selectPage("PENDING"));
        data.put("latestPendingReports", pendingReportPage.getList());
        return data;
    }

    /** 博客状态分布（含全部状态） */
    private List<Map<String, Object>> statusDistribution() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[][] statuses = {{"PUBLISHED", "已发布"}, {"PENDING", "待审核"}, {"DRAFT", "草稿"}, {"REJECTED", "已驳回"}};
        for (String[] s : statuses) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", s[1]);
            m.put("value", dashboardMapper.countBlogsByStatus(s[0]));
            list.add(m);
        }
        return list;
    }

    /** 趋势序列补零：按最近 7 天完整日期生成，缺失的日期补 0 */
    private List<Map<String, Object>> fillTrend(List<Map<String, Object>> rows, String startDate) {
        Map<String, Object> byDate = new HashMap<>();
        for (Map<String, Object> row : rows) {
            byDate.put(String.valueOf(row.get("name")), row.get("value"));
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            String date = DateUtil.format(DateUtil.offsetDay(DateUtil.parse(startDate), i), "yyyy-MM-dd");
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", date.substring(5));   // MM-dd 展示
            m.put("value", byDate.getOrDefault(date, 0));
            result.add(m);
        }
        return result;
    }
}
