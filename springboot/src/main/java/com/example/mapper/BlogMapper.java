package com.example.mapper;

import com.example.entity.Blog;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface BlogMapper {
    /**
     * 新增
     * @param blog
     */
    void insert(Blog blog);

    /**
     * 删除
     * @param id
     */
    int deleteById(Integer id);

    /**
     * 修改
     * @param blog
     */
    void updateById(Blog blog);

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    Blog selectById(Integer id);

    /**
     * 查询所有
     * @param blog
     * @return
     */
    List<Blog> selectAll(Blog blog);

    /**
     *  查询当前博客的作者信息
     * @param  userId
     */
    @Select("SELECT * FROM blog WHERE user_id = #{userId}")
    List<Blog> selectUserBlog(Integer userId);

    /**
     * 热榜：按综合热度分（阅读量 + 点赞×5 + 收藏×10 + 评论×3）降序取前 20
     */
    List<Blog> selectHot();

    /**
     * 置顶/取消置顶
     * @param id   博客ID
     * @param top  1置顶 0取消
     */
    void setTop(@Param("id") Integer id, @Param("top") Integer top);

    /**
     * 更新阅读数
     * @param blogId
     */
    @Update("update blog set read_count = read_count + 1 where id = #{blogId}")
    void updateReadCount(Integer blogId);

    /**
     * 分页查询 用户点赞的博客
     * @param blog

     */
    List<Blog> selectUserLike(Blog blog);

    /**
     * 分页查询 用户收藏的博客
     * @param blog
     * @return
     */
    List<Blog> selectUserCollect(Blog blog);

    /**
     * 分页查询 用户评论的博客
     * @param blog
     * @return
     */
    List<Blog> selectUserComment(Blog blog);

    /** 统计用户公开已发布的博客数（个人空间） */
    @Select("select count(*) from blog where user_id = #{userId} and status = 'PUBLISHED' and visibility = 'PUBLIC'")
    int countPublicByUser(Integer userId);

    /** 统计用户公开已发布博客的总阅读量（个人空间） */
    @Select("select ifnull(sum(read_count), 0) from blog where user_id = #{userId} and status = 'PUBLISHED'")
    int sumReadsByUser(Integer userId);

    /** 统计用户公开博客收到的点赞总数（个人空间） */
    @Select("select count(*) from likes l join blog b on l.fid = b.id where l.module = '博客' and b.user_id = #{userId} and b.status = 'PUBLISHED' and b.visibility = 'PUBLIC'")
    int countLikesByUser(Integer userId);

    /** 统计用户公开博客收到的收藏总数（个人空间） */
    @Select("select count(*) from collect c join blog b on c.fid = b.id where c.module = '博客' and b.user_id = #{userId} and b.status = 'PUBLISHED' and b.visibility = 'PUBLIC'")
    int countCollectByUser(Integer userId);

    /** 个人空间：用户公开已发布的博客（按日期倒序） */
    List<Blog> selectSpaceBlogs(Integer userId);

    @org.apache.ibatis.annotations.Delete("delete from blog where user_id = #{userId}")
    void deleteByUserId(Integer userId);
}
