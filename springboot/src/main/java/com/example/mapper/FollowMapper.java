package com.example.mapper;

import com.example.entity.Follow;
import com.example.entity.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FollowMapper {
    void insert(Follow follow);

    void delete(Follow follow);

    Follow selectByUserAndFollower(@Param("userId") Integer userId, @Param("followerId") Integer followerId);

    /** 粉丝列表：关注了 userId 的所有人（isFollowed=当前登录用户是否已关注对方） */
    List<User> selectFans(@Param("userId") Integer userId, @Param("currentUserId") Integer currentUserId);

    /** 关注列表：userId 关注的所有人 */
    List<User> selectFollowing(@Param("userId") Integer userId, @Param("currentUserId") Integer currentUserId);

    int countFans(Integer userId);

    int countFollowing(Integer userId);
}
