package com.example.service;

import com.example.common.enums.LikesModuleEnum;
import com.example.entity.Account;
import com.example.entity.Blog;
import com.example.entity.Likes;
import com.example.mapper.BlogMapper;
import com.example.mapper.LikesMapper;
import com.example.utils.TokenUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 功能：点赞功能业务开发类
 * 作者：captain_dong
 * 日期：2024/1/26 17:44
 */
@Service
public class LikesService {
    @Resource
    LikesMapper likesMapper;

    @Resource
    UserBehaviorService userBehaviorService;

    @Resource
    BlogMapper blogMapper;

    @Resource
    NotificationService notificationService;

    /**
     * 点赞或取消点赞
     * @param likes
     */
    public void set(Likes likes) {
        Account currentUser = TokenUtils.getCurrentUser();
        likes.setUserId(currentUser.getId());
        Likes dblikes= likesMapper.selectUserLikes(likes);
        if (dblikes==null){
            likesMapper.insert(likes);
            // 点赞博客时记录用户行为（用于协同过滤推荐）
            if (LikesModuleEnum.BLOG.getValue().equals(likes.getModule())) {
                userBehaviorService.record(likes.getFid(), UserBehaviorService.TYPE_LIKE);
                // 通知博客作者（自己赞自己不发）
                Blog blog = blogMapper.selectById(likes.getFid());
                if (blog != null && currentUser.getId() != null && !currentUser.getId().equals(blog.getUserId())) {
                    String nickname = currentUser.getName() != null && !currentUser.getName().isEmpty()
                            ? currentUser.getName() : currentUser.getUsername();
                    notificationService.send(blog.getUserId(), "LIKE", "收到点赞",
                            nickname + " 赞了你的博客《" + blog.getTitle() + "》", blog.getId());
                }
            }
        }else {
            likesMapper.deleteById(dblikes.getId());
            // 取消点赞时移除对应行为记录
            if (LikesModuleEnum.BLOG.getValue().equals(likes.getModule())) {
                userBehaviorService.remove(likes.getFid(), UserBehaviorService.TYPE_LIKE);
            }
        }

    }

    /**
     * 查询当前用户点赞信息（是否点过赞）
     * @param fid
     * @param module
     * @return
     */
    public Likes selectUserLikes(Integer fid,String module){
        Account currentUser = TokenUtils.getCurrentUser();
        Likes likes=new Likes();
        likes.setUserId(currentUser.getId());
        likes.setFid(fid);
        likes.setModule(module);
        return likesMapper.selectUserLikes(likes);
    }

    public int selectByFidAndModule(Integer fid, String module) {
        return likesMapper.selectByFidAndModule(fid, module);
    }
}