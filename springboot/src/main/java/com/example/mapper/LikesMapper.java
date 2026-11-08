package com.example.mapper;

import com.example.entity.Likes;
import org.apache.ibatis.annotations.Param;

public interface LikesMapper {
    void insert(Likes likes);
    Likes selectUserLikes(Likes likes);

    void deleteById(Integer id);

    int selectByFidAndModule(@Param("fid") Integer fid, @Param("module") String module);

    @org.apache.ibatis.annotations.Delete("delete from likes where user_id = #{userId}")
    void deleteByUserId(Integer userId);

    /** 用户所发博客收到的全部点赞（删用户时级联清理） */
    @org.apache.ibatis.annotations.Delete("delete from likes where module = 'BLOG' and fid in (select id from blog where user_id = #{userId})")
    void deleteByBlogAuthor(Integer userId);
}
