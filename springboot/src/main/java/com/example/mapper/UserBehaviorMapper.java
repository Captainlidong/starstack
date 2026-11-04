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
}
