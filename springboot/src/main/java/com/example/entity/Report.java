package com.example.entity;

/**
 * 举报实体
 */
public class Report {
    private Integer id;
    /** 举报人ID */
    private Integer reporterId;
    /** 目标类型：BLOG/ACTIVITY */
    private String targetType;
    /** 目标ID */
    private Integer targetId;
    /** 举报理由 */
    private String reason;
    /** 状态：PENDING待处理/RESOLVED举报属实/DISMISSED无问题 */
    private String status;
    /** 处理结果说明 */
    private String result;
    /** 举报时间 */
    private String time;
    /** 处理时间 */
    private String handleTime;
    /** 处理管理员 */
    private String adminName;

    /** 展示字段：举报人昵称（不落库） */
    private String reporterName;
    /** 展示字段：目标标题（不落库） */
    private String targetTitle;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getReporterId() {
        return reporterId;
    }

    public void setReporterId(Integer reporterId) {
        this.reporterId = reporterId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public void setTargetId(Integer targetId) {
        this.targetId = targetId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getHandleTime() {
        return handleTime;
    }

    public void setHandleTime(String handleTime) {
        this.handleTime = handleTime;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getTargetTitle() {
        return targetTitle;
    }

    public void setTargetTitle(String targetTitle) {
        this.targetTitle = targetTitle;
    }
}
