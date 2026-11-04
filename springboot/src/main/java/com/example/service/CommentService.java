package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.common.enums.LikesModuleEnum;
import com.example.common.enums.RoleEnum;
import com.example.entity.Account;
import com.example.entity.Blog;
import com.example.entity.Comment;
import com.example.exception.CustomException;
import com.example.mapper.BlogMapper;
import com.example.mapper.CommentMapper;
import com.example.utils.TokenUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.security.auth.login.CredentialException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 功能：博客评论 业务类
 * 作者：captain_dong
 * 日期：2024/1/27 1:12
 */
@Service
public class CommentService {
    @Resource
    private CommentMapper commentMapper;

    @Resource
    private BlogMapper blogMapper;

    @Resource
    private NotificationService notificationService;

    /**
     * 添加评论
     * @param comment
     */
    public void add(Comment comment) {
        //空评论校验：防止空内容污染评论区
        if (cn.hutool.core.util.StrUtil.isBlank(comment.getContent())) {
            throw new CustomException(ResultCodeEnum.COMMENT_EMPTY_ERROR);
        }
        Account currentUser = TokenUtils.getCurrentUser();
        if (RoleEnum.USER.name().equals(currentUser.getRole())){
            comment.setUserId(currentUser.getId());
        }
        comment.setTime(DateUtil.now());
        commentMapper.insert(comment);  //先插入数据  拿到主键ID 再设置数据
        if (comment.getRootId() == null){
            comment.setRootId(comment.getId());
            commentMapper.updateById(comment);  //注意：  更新一下 root_id
        }
        // 评论/回复通知（自己评论自己不发）
        sendCommentNotification(comment, currentUser);
    }

    /** 评论通知博客作者；回复通知被回复人 */
    private void sendCommentNotification(Comment comment, Account currentUser) {
        try {
            if (!LikesModuleEnum.BLOG.getValue().equals(comment.getModule())) {
                return;  // 活动评论暂不通知（活动由管理员发布，无个人作者）
            }
            String nickname = currentUser.getName() != null && !currentUser.getName().isEmpty()
                    ? currentUser.getName() : currentUser.getUsername();
            if (comment.getPid() != null) {
                // 回复：通知被回复评论的作者
                Comment parent = commentMapper.selectById(comment.getPid());
                if (parent != null && !currentUser.getId().equals(parent.getUserId())) {
                    notificationService.send(parent.getUserId(), "COMMENT", "收到回复",
                            nickname + " 回复了你的评论：" + cut(comment.getContent()), comment.getFid());
                }
            } else {
                // 根评论：通知博客作者
                Blog blog = blogMapper.selectById(comment.getFid());
                if (blog != null && !currentUser.getId().equals(blog.getUserId())) {
                    notificationService.send(blog.getUserId(), "COMMENT", "收到评论",
                            nickname + " 评论了你的博客《" + blog.getTitle() + "》：" + cut(comment.getContent()), blog.getId());
                }
            }
        } catch (Exception ignored) {
            // 通知失败不影响评论主流程
        }
    }

    private String cut(String s) {
        if (s == null) return "";
        return s.length() > 30 ? s.substring(0, 30) + "…" : s;
    }

    /**
     * 删除评论
     * @param id
     */
    public void deleteById(Integer id) {
        commentMapper.deleteById(id);
    }

    /**
     *  批量删除评论
     * @param ids
     */
    public void deleteBatch(List<Integer> ids) {
        for (Integer id : ids) {
            commentMapper.deleteById(id);
        }
    }

    /**
     * 更新评论
     * @param comment
     */
    public void updateById(Comment comment) {
        commentMapper.updateById(comment);
    }

    /**
     * 根据ID查询
     */
    public Comment selectById(Integer id) {
        return commentMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Comment> selectAll(Comment comment) {
        return commentMapper.selectAll(comment);
    }

    public List<Comment> selectForUser(Comment comment){
        List<Comment> commentList = commentMapper.selectForUser(comment);
        for (Comment c : commentList) {  //查询回复列表
            Comment param = new Comment();
            param.setRootId(c.getId());
            List<Comment> children=this.selectAll(param);
            children=children.stream().filter(child->!child.getId().equals(c.getId())).collect(Collectors.toList());  //排除当前查询结果里最外层节点

            c.setChildren(children);
        }
        return commentList;
    }

    /**
     * 分页查询
     */
    public PageInfo<Comment> selectPage(Comment comment, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Comment> list = commentMapper.selectAll(comment);
        return PageInfo.of(list);
    }

    /**
     * 查询评论个数
     * @param fid
     * @param module
     * @return
     */
    public Integer selectCount(Integer fid, String module) {
        return commentMapper.selectCount(fid,module);
    }
}