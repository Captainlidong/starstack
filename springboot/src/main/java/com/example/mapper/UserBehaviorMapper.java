package com.example.mapper;

import com.example.entity.UserBehavior;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserBehaviorMapper {
    void insert(UserBehavior userBehavior);

    List<UserBehavior> getByUserId(@Param("userId") Integer userId);

    List<UserBehavior> getAllBehaviors();

    UserBehavior selectOne(@Param("userId") Integer userId,
                           @Param("blogId") Integer blogId,
                           @Param("behaviorType") Byte behaviorType);

    void updateTimestamp(@Param("id") Integer id);

    void deleteBehavior(@Param("userId") Integer userId,
                        @Param("blogId") Integer blogId,
                        @Param("behaviorType") Byte behaviorType);

    @org.apache.ibatis.annotations.Delete("delete from user_behavior where user_id = #{userId}")
    void deleteByUserId(Integer userId);

    /** 他人对所删用户博客产生的行为记录（blog_id 外键需在删博客前清理） */
    @org.apache.ibatis.annotations.Delete("delete from user_behavior where blog_id in (select id from blog where user_id = #{userId})")
    void deleteByBlogAuthor(Integer userId);
}
