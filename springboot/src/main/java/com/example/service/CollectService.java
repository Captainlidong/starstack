package com.example.service;

import com.example.common.enums.LikesModuleEnum;
import com.example.entity.Account;
import com.example.entity.Blog;
import com.example.entity.Collect;
import com.example.mapper.BlogMapper;
import com.example.mapper.CollectMapper;
import com.example.utils.TokenUtils;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 功能：收藏功能业务 开发
 * 作者：captain_dong
 * 日期：2024/1/26 20:52
 */
@Service
public class CollectService {
    @Resource
    CollectMapper collectMapper;

    @Resource
    UserBehaviorService userBehaviorService;

    @Resource
    BlogMapper blogMapper;

    @Resource
    NotificationService notificationService;

    public void set(Collect collect) {
        Account currentUser = TokenUtils.getCurrentUser();
        collect.setUserId(currentUser.getId());
        Collect dbCollect = collectMapper.selectUserCollect(collect);
        if (dbCollect==null){
            collectMapper.insert(collect);
            // 收藏博客时记录用户行为（用于协同过滤推荐）
            if (LikesModuleEnum.BLOG.getValue().equals(collect.getModule())) {
                userBehaviorService.record(collect.getFid(), UserBehaviorService.TYPE_COLLECT);
                // 通知博客作者（自己收藏自己不发）
                Blog blog = blogMapper.selectById(collect.getFid());
                if (blog != null && currentUser.getId() != null && !currentUser.getId().equals(blog.getUserId())) {
                    String nickname = currentUser.getName() != null && !currentUser.getName().isEmpty()
                            ? currentUser.getName() : currentUser.getUsername();
                    notificationService.send(blog.getUserId(), "COLLECT", "收到收藏",
                            nickname + " 收藏了你的博客《" + blog.getTitle() + "》", blog.getId());
                }
            }
        }else {
            collectMapper.deleteById(dbCollect.getId());
            // 取消收藏时移除对应行为记录
            if (LikesModuleEnum.BLOG.getValue().equals(collect.getModule())) {
                userBehaviorService.remove(collect.getFid(), UserBehaviorService.TYPE_COLLECT);
            }
        }
    }

    /**
     * 查询当前用户是否收藏过
     * @param fid
     * @param module
     * @return
     */
    public Collect selectUserCollect(Integer fid,String module){
        Account currentUser = TokenUtils.getCurrentUser();
        Collect collect = new Collect();
        collect.setUserId(currentUser.getId());
        collect.setFid(fid);
        collect.setModule(module);
        return collectMapper.selectUserCollect(collect);
    }

    public int selectByFidAndModule(Integer fid,String module){
        return collectMapper.selectByFidAndModule(fid,module);
    }
}