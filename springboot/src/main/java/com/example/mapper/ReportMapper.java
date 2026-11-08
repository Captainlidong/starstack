package com.example.mapper;

import com.example.entity.Report;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ReportMapper {
    void insert(Report report);

    Report selectByReporterAndTarget(@Param("reporterId") Integer reporterId,
                                     @Param("targetType") String targetType,
                                     @Param("targetId") Integer targetId);

    List<Report> selectPage(@Param("status") String status);

    Report selectById(Integer id);

    void updateById(Report report);

    @org.apache.ibatis.annotations.Delete("delete from report where reporter_id = #{userId}")
    void deleteByUserId(Integer userId);
}
