package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Account;
import com.example.entity.Blog;
import com.example.entity.Report;
import com.example.exception.CustomException;
import com.example.mapper.BlogMapper;
import com.example.mapper.ReportMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 举报业务：用户提交举报，管理员处理并产生举报结果通知
 */
@Service
public class ReportService {

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private NotificationService notificationService;

    /** 提交举报（同一用户对同一目标的未处理举报不可重复提交） */
    public void add(Report report) {
        Account current = TokenUtils.getCurrentUser();
        Report exist = reportMapper.selectByReporterAndTarget(current.getId(), report.getTargetType(), report.getTargetId());
        if (exist != null && "PENDING".equals(exist.getStatus())) {
            throw new CustomException(ResultCodeEnum.REPORT_DUPLICATE_ERROR);
        }
        report.setReporterId(current.getId());
        report.setStatus("PENDING");
        report.setTime(DateUtil.now());
        reportMapper.insert(report);
    }

    public PageInfo<Report> selectPage(String status, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(reportMapper.selectPage(status));
    }

    /**
     * 管理员处理举报
     * RESOLVED（举报属实）：博客内容下架（REJECTED）并通知作者与举报人
     * DISMISSED（无问题）：通知举报人"内容未违反平台规则"
     */
    public void handle(Report report) {
        Account current = TokenUtils.getCurrentUser();
        Report db = reportMapper.selectById(report.getId());
        if (db == null || !"PENDING".equals(db.getStatus())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        db.setStatus(report.getStatus());
        db.setResult(report.getResult());
        db.setHandleTime(DateUtil.now());
        db.setAdminName(current.getName() != null ? current.getName() : current.getUsername());
        reportMapper.updateById(db);

        // 举报属实且目标是博客：内容下架并通知作者
        if ("RESOLVED".equals(db.getStatus()) && "BLOG".equals(db.getTargetType())) {
            Blog blog = blogMapper.selectById(db.getTargetId());
            if (blog != null) {
                Blog upd = new Blog();
                upd.setId(blog.getId());
                upd.setStatus("REJECTED");
                upd.setRejectReason("内容经举报核实违反平台规则：" + (db.getResult() == null ? "" : db.getResult()));
                blogMapper.updateById(upd);
                notificationService.send(blog.getUserId(), "AUDIT", "内容违规提醒",
                        "你的博客《" + blog.getTitle() + "》经举报核实违反平台规则，已下架。", blog.getId());
            }
        }
        // 通知举报人处理结果
        String msg = "RESOLVED".equals(db.getStatus())
                ? "你举报的内容经核实违反平台规则，已处理。感谢你的反馈！"
                : "你举报的内容经核实并未违反平台规则，感谢你的反馈！";
        notificationService.send(db.getReporterId(), "REPORT", "举报处理结果", msg, db.getTargetId());
    }
}
