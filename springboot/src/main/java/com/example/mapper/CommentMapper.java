package com.example.mapper;

import com.example.entity.Comment;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface CommentMapper {
    /**
     * 新增
     */
    int insert(Comment comment);

    /**
     * 删除
     */
    int deleteById(Integer id);

    /**
     * 修改
     */
    int updateById(Comment comment);

    /**
     * 根据ID查询
     */
    Comment selectById(Integer id);

    /**
     * 查询所有
     */
    List<Comment> selectAll(Comment comment);

    /**
     * 查询前台展示评论的信息
     * @param comment
     * @return
     */
    List<Comment> selectForUser(Comment comment);

    /**
     * 查询前台展示评论的数量
     * @param fid
     * @param module
     * @return
     */
    @Select("select count(*) from comment where fid = #{fid} and module = #{module}")
    Integer selectCount(@Param("fid") Integer fid, @Param("module") String module);

    @org.apache.ibatis.annotations.Delete("delete from comment where user_id = #{userId}")
    void deleteByUserId(Integer userId);

    /** 用户所发博客下的全部评论（删用户时级联清理） */
    @org.apache.ibatis.annotations.Delete("delete from comment where module = 'BLOG' and fid in (select id from blog where user_id = #{userId})")
    void deleteByBlogAuthor(Integer userId);
}
