package com.example.service;

import com.example.entity.Blog;
import com.example.entity.UserBehavior;
import com.example.mapper.BlogMapper;
import com.example.mapper.UserBehaviorMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    /** 相似度低于该值的用户不参与推荐（过滤噪声） */
    private static final double SIMILARITY_THRESHOLD = 0.1;
    /** 最多参考前 N 个相似用户 */
    private static final int MAX_SIMILAR_USERS = 10;
    /** 推荐条数 */
    private static final int RECOMMEND_LIMIT = 5;

    @Resource
    private UserBehaviorMapper userBehaviorMapper;

    @Resource
    private BlogMapper blogMapper;

    /**
     * 基于用户的协同过滤推荐（User-CF）：
     * 1. 行为矩阵：同一 (用户, 博客) 的多行为分数累加（浏览1 + 点赞2 + 收藏3）
     * 2. 取相似度达标的前 10 个相似用户
     * 3. 候选博客得分 = Σ(相似度 × 相似用户评分)，排除已交互和自己写的，取前 5
     * 4. 无行为或候选为空时，回退热门博客（冷启动兜底）
     */
    public List<Blog> userBasedCF(Integer userId) {
        List<Blog> allBlogs = blogMapper.selectAll(null);
        Map<Integer, Map<Integer, Double>> matrix = buildUserItemMatrix();
        Map<Integer, Double> targetVector = matrix.get(userId);

        // 冷启动：目标用户没有任何行为记录
        if (targetVector == null || targetVector.isEmpty()) {
            return topHotBlogs(allBlogs, userId, Collections.emptySet());
        }

        // 1. 计算相似用户（余弦相似度，阈值过滤）
        Map<Integer, Double> similarUsers = new HashMap<>();
        for (Map.Entry<Integer, Map<Integer, Double>> entry : matrix.entrySet()) {
            if (entry.getKey().equals(userId)) {
                continue;
            }
            double similarity = cosineSimilarity(targetVector, entry.getValue());
            if (similarity >= SIMILARITY_THRESHOLD) {
                similarUsers.put(entry.getKey(), similarity);
            }
        }

        // 2. 候选打分：Σ(相似度 × 相似用户对该博客的评分)
        Map<Integer, Double> candidateScores = new HashMap<>();
        for (Map.Entry<Integer, Double> similarUser : similarUsers.entrySet()) {
            for (Map.Entry<Integer, Double> behaviorEntry : matrix.get(similarUser.getKey()).entrySet()) {
                candidateScores.merge(behaviorEntry.getKey(), similarUser.getValue() * behaviorEntry.getValue(), Double::sum);
            }
        }

        // 3. 排除已交互过和自己写的，按得分取前 5
        List<Blog> recommended = allBlogs.stream()
                .filter(blog -> !targetVector.containsKey(blog.getId()))
                .filter(blog -> !blog.getUserId().equals(userId))
                .filter(blog -> candidateScores.containsKey(blog.getId()))
                .sorted(Comparator.comparing((Blog blog) -> candidateScores.get(blog.getId())).reversed())
                .limit(RECOMMEND_LIMIT)
                .collect(Collectors.toList());

        // 4. 有行为但算不出候选时热门兜底（排除已交互）
        if (recommended.isEmpty()) {
            recommended = topHotBlogs(allBlogs, userId, targetVector.keySet());
        }
        // 5. 最后兜底：把别人写的博客都交互过的重度用户，第二级也会为空，
        //    此时只排除自己写的热门博客，保证推荐页不出现空列表
        if (recommended.isEmpty()) {
            recommended = topHotBlogs(allBlogs, userId, Collections.emptySet());
        }
        return recommended;
    }

    /**
     * 构建 用户-博客 评分矩阵，同一 (用户, 博客) 的多种行为分数累加
     */
    private Map<Integer, Map<Integer, Double>> buildUserItemMatrix() {
        List<UserBehavior> behaviors = userBehaviorMapper.getAllBehaviors();
        Map<Integer, Map<Integer, Double>> matrix = new HashMap<>();
        for (UserBehavior behavior : behaviors) {
            matrix.computeIfAbsent(behavior.getUserId(), k -> new HashMap<>())
                    .merge(behavior.getBlogId(), getBehaviorScore(behavior.getBehaviorType()), Double::sum);
        }
        return matrix;
    }

    /**
     * 行为类型转权重分数
     */
    private double getBehaviorScore(Byte type) {
        if (type == null) {
            return 0.0;
        }
        switch (type) {
            case 1: return 1.0; // 浏览
            case 2: return 2.0; // 点赞
            case 3: return 3.0; // 收藏
            default: return 0.0;
        }
    }

    /**
     * 余弦相似度
     */
    private double cosineSimilarity(Map<Integer, Double> v1, Map<Integer, Double> v2) {
        Map<Integer, Double> small = v1.size() <= v2.size() ? v1 : v2;
        Map<Integer, Double> large = small == v1 ? v2 : v1;

        double dotProduct = 0.0;
        for (Map.Entry<Integer, Double> entry : small.entrySet()) {
            Double other = large.get(entry.getKey());
            if (other != null) {
                dotProduct += entry.getValue() * other;
            }
        }
        if (dotProduct == 0.0) {
            return 0.0;
        }
        double normV1 = Math.sqrt(v1.values().stream().mapToDouble(x -> x * x).sum());
        double normV2 = Math.sqrt(v2.values().stream().mapToDouble(x -> x * x).sum());
        return dotProduct / (normV1 * normV2 + 1e-8);
    }

    /**
     * 冷启动兜底：热门博客（浏览量降序，排除自己写的和已交互的）
     */
    private List<Blog> topHotBlogs(List<Blog> allBlogs, Integer userId, java.util.Set<Integer> excludeBlogIds) {
        return allBlogs.stream()
                .filter(blog -> !blog.getUserId().equals(userId))
                .filter(blog -> !excludeBlogIds.contains(blog.getId()))
                .sorted(Comparator.comparingInt((Blog blog) -> blog.getReadCount() == null ? 0 : blog.getReadCount()).reversed())
                .limit(RECOMMEND_LIMIT)
                .collect(Collectors.toList());
    }
}
