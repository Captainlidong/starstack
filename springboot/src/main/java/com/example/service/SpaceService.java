package com.example.service;

import com.example.entity.Blog;
import com.example.entity.User;
import com.example.mapper.BlogMapper;
import com.example.mapper.FollowMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 个人空间业务：公开资料 + 统计 + 公开博客/粉丝/关注列表（游客可访问）
 */
@Service
public class SpaceService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private FollowMapper followMapper;

    /** 空间头部信息与统计 */
    public Map<String, Object> info(Integer userId) {
        Map<String, Object> map = new HashMap<>();
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setPassword(null);
        }
        map.put("user", user);
        map.put("blogCount", blogMapper.countPublicByUser(userId));
        map.put("totalReads", blogMapper.sumReadsByUser(userId));
        map.put("likesCount", blogMapper.countLikesByUser(userId));
        map.put("collectCount", blogMapper.countCollectByUser(userId));
        map.put("fansCount", followMapper.countFans(userId));
        map.put("followCount", followMapper.countFollowing(userId));
        return map;
    }

    /** 空间文章列表（公开已发布，按日期倒序） */
    public PageInfo<Blog> blogs(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(blogMapper.selectSpaceBlogs(userId));
    }

    /** 粉丝列表 */
    public PageInfo<User> fans(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(followMapper.selectFans(userId, currentUserIdOrMinusOne()));
    }

    /** 关注列表 */
    public PageInfo<User> following(Integer userId, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(followMapper.selectFollowing(userId, currentUserIdOrMinusOne()));
    }

    private Integer currentUserIdOrMinusOne() {
        com.example.entity.Account current = com.example.utils.TokenUtils.getCurrentUser();
        return current.getId() != null ? current.getId() : -1;
    }
}
