package com.example.mapper;

import com.example.entity.Category;

import java.util.List;

/**
 * 功能：博客分类数据持久化
 * 作者：captain_dong
 * 日期：2023/12/20 18:07
 */
public interface CategoryMapper {
    /**
     * 新增
     * @param category
     */
    void insert(Category category);

    /**
     * 删除
     * @param id
     */
    void deleteById(Integer id);

    /**
     * 修改
     * @param category
     */
    void updateById(Category category);

    /**
     * 根据ID进行查询
     * @param id
     * @return
     */
    Category selectById(Integer id);

    /**
     * 查询所有
     * @param category
     * @return
     */
    List<Category> selectAll(Category category);
}
