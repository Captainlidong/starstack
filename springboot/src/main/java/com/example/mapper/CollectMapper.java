package com.example.mapper;

import com.example.entity.Collect;
import com.example.entity.Likes;
import org.apache.ibatis.annotations.Param;

public interface CollectMapper {
    void insert(Collect collect);
    Collect selectUserCollect(Collect collect);

    void deleteById(Integer id);

    int selectByFidAndModule(@Param("fid") Integer fid, @Param("module") String module);

    @org.apache.ibatis.annotations.Delete("delete from collect where user_id = #{userId}")
    void deleteByUserId(Integer userId);

    /** 用户所发博客收到的全部收藏（删用户时级联清理） */
    @org.apache.ibatis.annotations.Delete("delete from collect where module = 'BLOG' and fid in (select id from blog where user_id = #{userId})")
    void deleteByBlogAuthor(Integer userId);
}
