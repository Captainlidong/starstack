package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Comment;
import com.example.service.CommentService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 功能：博客评论接口开发
 * 作者：captain_dong
 * 日期：2024/1/27 1:11
 */
@RestController
@RequestMapping("/comment")
public class CommentController {
    @Resource
    private CommentService commentService;

    /**
     * 添加评论（仅用户角色，避免管理员身份产生无主的评论数据）
     * @param comment
     * @return
     */
    @PostMapping("/add")
    public Result add(@RequestBody Comment comment){
        Account currentUser = TokenUtils.getCurrentUser();
        if (!RoleEnum.USER.name().equals(currentUser.getRole())) {
            return Result.error("403", "请使用用户账号进行评论");
        }
        commentService.add(comment);
        return Result.success();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id){
        commentService.deleteById(id);
        return Result.success();
    }

    /**
     * 批量删除
     * @param ids
     * @return
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        commentService.deleteBatch(ids);
        return Result.success();
    }

    /**
     * 修改评论
     * @param comment
     * @return
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Comment comment){
        commentService.updateById(comment);
        return Result.success();
    }

    /**
     * 根据id查询评论
     * @param id
     * @return
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        Comment comment = commentService.selectById(id);
        return Result.success(comment);
    }

    /**
     * 查询所有
     * @param comment
     * @return
     */
    @GetMapping("/selectAll")
    public Result selectAll(Comment comment){
        List<Comment> list = commentService.selectAll(comment);
        return Result.success(list);
    }

    /**
     * 单独给用户的查询接口
     * @param comment
     * @return
     */
    @GetMapping("/selectForUser")
    public Result selectForUser(Comment comment){
        List<Comment> commentList = commentService.selectForUser(comment);
        return Result.success(commentList);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Comment comment,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Comment> page = commentService.selectPage(comment, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 查询评论个数
     * @param fid
     * @param module
     * @return
     */
    @GetMapping("/selectCount")
    public Result selectCount(@RequestParam Integer fid,@RequestParam String module){
        Integer count = commentService.selectCount(fid, module);
        return Result.success(count);
    }
}