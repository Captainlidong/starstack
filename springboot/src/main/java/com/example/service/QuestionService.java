package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.Account;
import com.example.entity.Question;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.QuestionMapper;
import com.example.mapper.UserMapper;
import com.example.utils.TokenUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 问答业务：用户向作者提问（一问一答），作者回答后通知提问者
 */
@Service
public class QuestionService {

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private SensitiveWordService sensitiveWordService;

    @Resource
    private NotificationService notificationService;

    /** 向作者提问（标题必填，敏感词拦截，不能向自己提问） */
    public void add(Integer authorId, Question question) {
        Account current = TokenUtils.getCurrentUser();
        if (current.getId() == null) {
            throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
        }
        if (current.getId().equals(authorId)) {
            throw new CustomException("400", "不能向自己提问");
        }
        if (StrUtil.isBlank(question.getTitle())) {
            throw new CustomException(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        sensitiveWordService.checkNoSensitiveWord(question.getTitle(), question.getContent());
        question.setAskerId(current.getId());
        question.setAuthorId(authorId);
        question.setStatus("WAIT");
        question.setTime(DateUtil.now());
        questionMapper.insert(question);
        User author = userMapper.selectById(authorId);
        if (author != null) {
            notificationService.send(authorId, "ASK", "收到提问",
                    current.getName() + " 向你提问：" + question.getTitle(), current.getId());
        }
    }

    /** 我提出的问答 */
    public List<Question> myAsk(Integer askerId) {
        return questionMapper.selectMyAsk(askerId);
    }

    /** 我收到的问答 */
    public List<Question> myReceived(Integer authorId) {
        return questionMapper.selectMyReceived(authorId);
    }

    /** 作者回答（仅被提问作者本人可回答），回答后通知提问者 */
    public void answer(Integer id, String answer, Account current) {
        Question db = questionMapper.selectById(id);
        if (db == null || !current.getId().equals(db.getAuthorId())) {
            throw new CustomException(ResultCodeEnum.PARAM_ERROR);
        }
        if (StrUtil.isBlank(answer)) {
            throw new CustomException(ResultCodeEnum.COMMENT_EMPTY_ERROR);
        }
        Question upd = new Question();
        upd.setId(id);
        upd.setAnswer(answer.trim());
        upd.setAnswerTime(DateUtil.now());
        questionMapper.updateAnswer(upd);
        notificationService.send(db.getAskerId(), "ANSWER", "提问已回答",
                "你的提问《" + db.getTitle() + "》已获得回答", db.getAuthorId());
    }
}
