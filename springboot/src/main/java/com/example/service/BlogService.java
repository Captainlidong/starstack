package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONUtil;
import com.example.common.enums.LikesModuleEnum;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.BlogMapper;
import com.example.service.SensitiveWordService;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 功能：
 * 作者：captain_dong
 * 日期：2023/12/20 22:29
 */
@Service
public class BlogService {

    @Resource
    private BlogMapper blogMapper;//博客

    @Resource
    private  UserService userService;//用户

    @Resource
    private LikesService likesService; //点赞

    @Resource
    private CollectService collectService;//收藏

    @Resource
    private SensitiveWordService sensitiveWordService;//敏感词

    @Resource
    private NotificationService notificationService;//消息通知

    @Resource
    private com.example.mapper.FollowMapper followMapper;//关注（作者卡粉丝数统计）

    /**
     * 状态处理：DRAFT 存草稿；提交发布先过敏感词再进入待审核；管理员编辑直接发布
     */
    private void applyStatus(Blog blog, Account currentUser) {
        if (StrUtil.isBlank(blog.getVisibility())) {
            blog.setVisibility("PUBLIC");
        }
        if (RoleEnum.ADMIN.name().equals(currentUser.getRole())) {
            blog.setStatus("PUBLISHED");  // 管理员编辑/新增直接发布
            blog.setRejectReason("");
            return;
        }
        if ("DRAFT".equals(blog.getStatus())) {
            blog.setStatus("DRAFT");      // 存草稿不校验敏感词（不对外）
        } else {
            // 提交发布：敏感词自动拦截 + 进入待审核
            sensitiveWordService.checkNoSensitiveWord(blog.getTitle(), blog.getDescr(), blog.getContent());
            blog.setStatus("PENDING");
            blog.setRejectReason("");
        }
    }

    /**
     * 新增
     * @param blog
     */
    public void add(Blog blog) {
        //设置发布日期为当前日期
        blog.setDate(DateUtil.today());

        //设置发布人--用户
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())){
            blog.setUserId(currentUser.getId());
        }
        applyStatus(blog, currentUser);
        blogMapper.insert(blog);
    }

    /**
     * 删除（作者本人或管理员）
     * @param id
     */
    public void deleteById(Integer id, Account currentUser) {
        checkOwner(id, currentUser);
        blogMapper.deleteById(id);
    }

    /**
     * 批量删除（作者本人或管理员）
     * @param ids
     */
    public void deleteBatch(List<Integer> ids, Account currentUser) {
        for (Integer id : ids) {
            checkOwner(id, currentUser);
            blogMapper.deleteById(id);
        }
    }

    /** 非管理员只能删除自己的博客 */
    private void checkOwner(Integer id, Account currentUser) {
        if (RoleEnum.ADMIN.name().equals(currentUser.getRole())) {
            return;
        }
        Blog db = blogMapper.selectById(id);
        if (db == null || !currentUser.getId().equals(db.getUserId())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
    }

    /**
     * 修改
     * @param blog
     */
    public void updateById(Blog blog) {
        Account currentUser = TokenUtils.getCurrentUser();
        applyStatus(blog, currentUser);
        blogMapper.updateById(blog);
    }

    /**
     * 管理员审核：pass=true 通过发布；false 驳回并记录原因（同时通知作者）
     */
    public void audit(Integer id, boolean pass, String reason, Account current) {
        Blog blog = blogMapper.selectById(id);
        if (blog == null) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        Blog upd = new Blog();
        upd.setId(id);
        if (pass) {
            upd.setStatus("PUBLISHED");
            upd.setRejectReason("");
            blogMapper.updateById(upd);
            notificationService.send(blog.getUserId(), "AUDIT", "审核通过",
                    "你的博客《" + blog.getTitle() + "》已通过审核并发布。", id);
        } else {
            upd.setStatus("REJECTED");
            upd.setRejectReason(StrUtil.isBlank(reason) ? "内容不符合平台规范" : reason);
            blogMapper.updateById(upd);
            notificationService.send(blog.getUserId(), "AUDIT", "审核未通过",
                    "你的博客《" + blog.getTitle() + "》未通过审核：" + upd.getRejectReason() + "，修改后可重新提交。", id);
        }
    }

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    public Blog selectById(Integer id) {
        Blog blog = blogMapper.selectById(id);
        // 可见性权限：草稿/待审核/驳回/私人博客仅作者本人和管理员可查看
        Account current = TokenUtils.getCurrentUser();
        boolean isAdmin = RoleEnum.ADMIN.name().equals(current.getRole());
        boolean isAuthor = current.getId() != null && current.getId().equals(blog.getUserId());
        if ((!"PUBLISHED".equals(blog.getStatus()) && !"REJECTED".equals(blog.getStatus()))
                || !"PUBLIC".equals(blog.getVisibility())) {
            if (!isAdmin && !isAuthor) {
                throw new CustomException(ResultCodeEnum.BLOG_NOT_VISIBLE_ERROR);
            }
        }
        User user = userService.selectById(blog.getUserId());
        if (user != null) {   // 防御：作者不存在（历史无主数据）时跳过作者统计，避免详情页 500
            // 作者统计基于公开已发布博客，草稿/待审/驳回不计入
            user.setBlogCount(blogMapper.countPublicByUser(user.getId()));
            user.setLikesCount(blogMapper.countLikesByUser(user.getId()));
            user.setCollectCount(blogMapper.countCollectByUser(user.getId()));
            user.setFansCount(followMapper.countFans(user.getId()));
        }


        blog.setUser(user);  // 设置作者信息
        // 查询当前博客的点赞数据
        int likesCount = likesService.selectByFidAndModule(id, LikesModuleEnum.BLOG.getValue());
        blog.setLikesCount(likesCount);
        Likes userLikes = likesService.selectUserLikes(id, LikesModuleEnum.BLOG.getValue());
        blog.setUserLike(userLikes != null);

        // 查询当前博客的收藏数据
        int collectCount = collectService.selectByFidAndModule(id, LikesModuleEnum.BLOG.getValue());
        blog.setCollectCount(collectCount);
        Collect userCollect = collectService.selectUserCollect(id, LikesModuleEnum.BLOG.getValue());
        blog.setUserCollect(userCollect != null);
        return blog;
    }

    /**
     * 查询所有
     * @param blog
     * @return
     */
    public List<Blog> selectAll(Blog blog) {
        return blogMapper.selectAll(blog);
    }

    /**
     * 分页查询
     * @param blog
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Blog> selectPage(Blog blog, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Blog> list = blogMapper.selectAll(blog);
        for (Blog blog1 : list) {
            int likesCount=likesService.selectByFidAndModule(blog1.getId(),LikesModuleEnum.BLOG.getValue());
            blog1.setLikesCount(likesCount);
        }
        return PageInfo.of(list);
    }

    /**
     * 置顶/取消置顶（仅管理员可调用，调用方需校验角色）
     * @param id   博客ID
     * @param top  1置顶 0取消
     */
    public void setTop(Integer id, Integer top) {
        blogMapper.setTop(id, top);
    }

    /**
     * 博客榜单：按综合热度分（阅读量 + 点赞×5 + 收藏×10 + 评论×3）降序取前 20，
     * 与首页综合排序同口径，高互动的新文章也能上榜
     * @return
     */
    public List<Blog> selectTop() {
        return blogMapper.selectHot();
    }

    /**
     * 博客推荐
     * @param blogId
     * @return
     */
    public Set<Blog> selectRecommend(Integer blogId) {
        Blog blog = this.selectById(blogId);
        String tags = blog.getTags();
        Set<Blog> blogSet=new HashSet<>();
        if (ObjUtil.isNotEmpty(tags)){
            List<Blog> blogList = this.selectAll(new Blog());  // 空参默认公开视角，仅推荐已发布公开的博客
            JSONArray tagsArr = JSONUtil.parseArray(tags);
            for (Object tag : tagsArr) {
                //筛选出包含当前博客标签的其他的博客列表
                Set<Blog> collect=blogList.stream().filter(b->b.getTags().contains(tag.toString())&& !blogId.equals(b.getId()))
                        .collect(Collectors.toSet());
                blogSet.addAll(collect);
            }
        }
        blogSet=blogSet.stream().limit(5).collect(Collectors.toSet());
        blogSet.forEach(b->{
            int likesCount=likesService.selectByFidAndModule(b.getId(),LikesModuleEnum.BLOG.getValue());
            b.setLikesCount(likesCount);
        });
        return blogSet;
    }

    /**
     * 更新阅读量
     * @param blogId
     */
    public void updateReadCount(Integer blogId) {
        blogMapper.updateReadCount(blogId);
    }

    /**
     * 分页查询 用户发表过的博客
     * @param blog
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Blog> selectUserBlog(Blog blog, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if(RoleEnum.USER.name().equals(currentUser.getRole())){
            blog.setUserId(currentUser.getId());
        }
        blog.setScope("own");  // 本人视角：包含草稿/待审核/驳回等全部状态
        return this.selectPage(blog,pageNum,pageSize);
    }

    /**
     * 分页查询 用户点赞的博客
     * @param blog
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Blog> selectUserLike(Blog blog, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if(RoleEnum.USER.name().equals(currentUser.getRole())){
            blog.setUserId(currentUser.getId());
        }
        PageHelper.startPage(pageNum,pageSize);
        List<Blog> list = blogMapper.selectUserLike(blog);
        PageInfo<Blog> pageInfo = PageInfo.of(list);
        List<Blog> blogList = pageInfo.getList();
        for (Blog b : blogList){
            int likeCount = likesService.selectByFidAndModule(b.getId(), LikesModuleEnum.BLOG.getValue());
            b.setLikesCount(likeCount);
        }
        return pageInfo;
    }

    /**
     * 分页查询 用户收藏的博客
     * @param blog
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Blog> selectUserCollect(Blog blog, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if(RoleEnum.USER.name().equals(currentUser.getRole())){
            blog.setUserId(currentUser.getId());
        }
        PageHelper.startPage(pageNum,pageSize);
        List<Blog> list = blogMapper.selectUserCollect(blog);
        PageInfo<Blog> pageInfo = PageInfo.of(list);
        List<Blog> blogList = pageInfo.getList();
        for (Blog b : blogList){
            int likeCount = likesService.selectByFidAndModule(b.getId(), LikesModuleEnum.BLOG.getValue());
            b.setLikesCount(likeCount);
        }
        return pageInfo;
    }

    /**
     * 分页查询 用户评论的博客
     * @param blog
     * @param pageNum
     * @param pageSize
     * @return
     */
    public PageInfo<Blog> selectUserComment(Blog blog, Integer pageNum, Integer pageSize) {
        Account currentUser = TokenUtils.getCurrentUser();
        if(RoleEnum.USER.name().equals(currentUser.getRole())){
            blog.setUserId(currentUser.getId());
        }
        PageHelper.startPage(pageNum,pageSize);
        List<Blog> list = blogMapper.selectUserComment(blog);
        PageInfo<Blog> pageInfo = PageInfo.of(list);
        List<Blog> blogList = pageInfo.getList();
        for (Blog b : blogList){
            int likeCount = likesService.selectByFidAndModule(b.getId(), LikesModuleEnum.BLOG.getValue());
            b.setLikesCount(likeCount);
        }
        return pageInfo;
    }
}