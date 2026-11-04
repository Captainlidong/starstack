package com.example.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HtmlUtil;
import com.example.common.enums.ResultCodeEnum;
import com.example.entity.SensitiveWord;
import com.example.exception.CustomException;
import com.example.mapper.SensitiveWordMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 敏感词业务：内存缓存词库，增删后自动刷新；发布内容前统一校验
 */
@Service
public class SensitiveWordService {

    @Resource
    private SensitiveWordMapper sensitiveWordMapper;

    private volatile Set<String> wordCache = Collections.emptySet();

    @PostConstruct
    public void loadCache() {
        try {
            wordCache = new HashSet<>(sensitiveWordMapper.selectAllWords());
        } catch (Exception e) {
            wordCache = Collections.emptySet();
        }
    }

    /**
     * 敏感词校验：命中即抛 CONTENT_SENSITIVE_ERROR
     * 文本先去除 HTML 标签再匹配，防止用标签拆分敏感词绕过
     */
    public void checkNoSensitiveWord(String... texts) {
        StringBuilder sb = new StringBuilder();
        for (String t : texts) {
            if (t != null) {
                sb.append(HtmlUtil.cleanHtmlTag(t)).append(' ');
            }
        }
        String plain = sb.toString();
        for (String w : wordCache) {
            if (plain.contains(w)) {
                throw new CustomException(ResultCodeEnum.CONTENT_SENSITIVE_ERROR);
            }
        }
    }

    public PageInfo<SensitiveWord> selectPage(Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(sensitiveWordMapper.selectAll());
    }

    /** 新增敏感词（幂等：已存在直接忽略） */
    public void add(String word) {
        if (StrUtil.isBlank(word)) {
            throw new CustomException(ResultCodeEnum.PARAM_LOST_ERROR);
        }
        word = word.trim();
        if (sensitiveWordMapper.selectByWord(word) != null) {
            return;
        }
        SensitiveWord sw = new SensitiveWord();
        sw.setWord(word);
        sensitiveWordMapper.insert(sw);
        loadCache();
    }

    public void deleteById(Integer id) {
        sensitiveWordMapper.deleteById(id);
        loadCache();
    }
}
