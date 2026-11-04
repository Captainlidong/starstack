package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Blog;
import com.example.service.BlogService;
import com.example.service.UserBehaviorService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageInfo;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Set;

/**
 * 功能：博客信息前端操作接口
 * 作者：captain_dong
 * 日期：2023/12/20 22:29
 */
@RestController
@RequestMapping("/blog")
public class BlogController {
    @Resource
    private BlogService blogService;

    @Resource
    private UserBehaviorService userBehaviorService;

    /**
     * 新增（仅用户角色，避免管理员身份产生无作者的博客数据）
     */
    @PostMapping("/add")
    public Result add(@RequestBody Blog blog){
        Account currentUser = TokenUtils.getCurrentUser();
        if (!RoleEnum.USER.name().equals(currentUser.getRole())) {
            return Result.error("403", "请使用用户账号发表博客");
        }
        blogService.add(blog);
        return Result.success();
    }

    /**
     * 删除（作者本人或管理员）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id){
        blogService.deleteById(id, TokenUtils.getCurrentUser());
        return Result.success();
    }

    /**
     * 批量删除（作者本人或管理员）
     */
    @DeleteMapping("/delete/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids){
        blogService.deleteBatch(ids, TokenUtils.getCurrentUser());
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Blog blog){
        blogService.updateById(blog);
        return Result.success();
    }

    /**
     * 管理员内容审核：pass=true 通过发布；false 驳回（body 可携带 rejectReason 驳回原因）
     */
    @PutMapping("/audit/{id}/{pass}")
    public Result audit(@PathVariable Integer id, @PathVariable boolean pass,
                        @RequestBody(required = false) Blog body){
        Account currentUser = TokenUtils.getCurrentUser();
        if (!RoleEnum.ADMIN.name().equals(currentUser.getRole())) {
            return Result.error("403", "仅管理员可执行审核操作");
        }
        String reason = body != null ? body.getRejectReason() : null;
        blogService.audit(id, pass, reason, currentUser);
        return Result.success();
    }

    /**
     * 博客阅读量+1，同时记录当前用户的浏览行为（用于协同过滤推荐）
     * @param blogId
     * @return
     */
    @PutMapping("/updateReadCount/{blogId}")
    public Result updateReadCount(@PathVariable Integer blogId){
        blogService.updateReadCount(blogId);
        userBehaviorService.record(blogId, UserBehaviorService.TYPE_VIEW);
        return Result.success();
    }

    /**
     * 置顶/取消置顶（仅管理员）
     * @param id    博客ID
     * @param state 1置顶 0取消置顶
     * @return
     */
    @PutMapping("/setTop/{id}/{state}")
    public Result setTop(@PathVariable Integer id, @PathVariable Integer state){
        Account currentUser = TokenUtils.getCurrentUser();
        if (!RoleEnum.ADMIN.name().equals(currentUser.getRole())) {
            return Result.error("403", "仅管理员可以置顶博客");
        }
        if (state == null || (state != 0 && state != 1)) {
            return Result.error("400", "参数错误");
        }
        blogService.setTop(id, state);
        return Result.success();
    }
    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id){
        return Result.success(blogService.selectById(id));
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Blog blog){
       List<Blog> list= blogService.selectAll(blog);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Blog blog,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Blog> page=blogService.selectPage(blog,pageNum,pageSize);
        return Result.success(page);
    }

    /**
     * 分页查询 用户发表的博客
     */
    @GetMapping("/selectUserBlog")
    public Result selectUserBlog(Blog blog,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Blog> page=blogService.selectUserBlog(blog,pageNum,pageSize);
        return Result.success(page);
    }

    /**
     * 分页查询 用户点赞的博客
     */
    @GetMapping("/selectUserLike")
    public Result selectUserLike(Blog blog,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Blog> page=blogService.selectUserLike(blog,pageNum,pageSize);
        return Result.success(page);
    }

    /**
     * 分页查询 用户收藏的博客
     */
     @GetMapping("/selectUserCollect")
    public Result selectUserCollect(Blog blog,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Blog> page=blogService.selectUserCollect(blog,pageNum,pageSize);
        return Result.success(page);
    }

    /**
     * 分页查询 用户评论的博客
     */
    @GetMapping("/selectUserComment")
    public Result selectUserComment(Blog blog,
                                 @RequestParam(defaultValue = "1") Integer pageNum,
                                 @RequestParam(defaultValue = "10") Integer pageSize){
        PageInfo<Blog> page=blogService.selectUserComment(blog,pageNum,pageSize);
        return Result.success(page);
    }

    /**
     * 博客榜单
     */
    @GetMapping("/selectTop")
    public Result selectTop(){
        List<Blog> list=blogService.selectTop();
        return Result.success(list);
    }

    /**
     * 博客推荐
     */
    @GetMapping("/selectRecommend/{blogId}")
    public Result selectRecommend(@PathVariable Integer blogId){
       Set<Blog> blogSet=blogService.selectRecommend(blogId);
        return Result.success(blogSet);
    }

}