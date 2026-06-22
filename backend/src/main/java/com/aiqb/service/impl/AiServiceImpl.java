package com.aiqb.service.impl;

import com.aiqb.ai.DashScopeService;
import com.aiqb.common.BusinessException;
import com.aiqb.entity.Question;
import com.aiqb.entity.WrongQuestion;
import com.aiqb.mapper.QuestionMapper;
import com.aiqb.mapper.WrongQuestionMapper;
import com.aiqb.service.AiService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiServiceImpl implements AiService {

    private final DashScopeService dashScopeService;
    private final QuestionMapper questionMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String explainQuestion(Long questionId, Long userId) {
        Question q = questionMapper.selectById(questionId);
        if (q == null) throw BusinessException.notFound("题目不存在");

        String system = "你是一位经验丰富的教师，擅长用简洁清晰的语言讲解题目。"
                + "请针对用户提供的题目，给出详细但不超过 300 字的解析。";

        StringBuilder user = new StringBuilder();
        user.append("题目类型：").append(typeName(q.getType())).append("\n");
        user.append("难度：").append(q.getDifficulty()).append("/5\n");
        user.append("知识点：").append(q.getKnowledgePoint()).append("\n");
        user.append("题干：").append(q.getContent()).append("\n");
        if (q.getOptions() != null && !q.getOptions().isEmpty()) {
            user.append("选项：").append(q.getOptions()).append("\n");
        }
        user.append("正确答案：").append(q.getAnswer());

        String input = user.toString();
        return dashScopeService.chat(system, user.toString(), userId, "EXPLAIN", input);
    }

    @Override
    public List<Question> recommendSimilar(Long wrongQuestionId, Integer count, Long userId) {
        WrongQuestion wq = wrongQuestionMapper.selectById(wrongQuestionId);
        if (wq == null) throw BusinessException.notFound("错题记录不存在");
        Question q = questionMapper.selectById(wq.getQuestionId());
        if (q == null) throw BusinessException.notFound("原题不存在");

        // 1. 优先按知识点查同类题
        List<Question> similar = new ArrayList<>();
        if (q.getKnowledgePoint() != null && !q.getKnowledgePoint().isEmpty()) {
            similar = questionMapper.randomPick(
                    q.getSubjectId(), null, null, q.getKnowledgePoint(), count);
        }
        if (similar.size() < count) {
            // 2. 不足则按科目 + 题型补
            int need = count - similar.size();
            List<Question> more = questionMapper.randomPick(
                    q.getSubjectId(), q.getType(), null, null, need);
            for (Question m : more) {
                if (similar.stream().noneMatch(s -> s.getId().equals(m.getId()))) {
                    similar.add(m);
                }
            }
        }

        // 3. 还不够则用 AI 生成
        if (similar.size() < count) {
            String generated = aiGenerateSimilar(q, count - similar.size(), userId);
            try {
                JsonNode arr = objectMapper.readTree(generated);
                if (arr.isArray()) {
                    for (JsonNode item : arr) {
                        Question newQ = new Question();
                        newQ.setSubjectId(q.getSubjectId());
                        newQ.setType(q.getType());
                        newQ.setDifficulty(q.getDifficulty());
                        newQ.setContent(item.path("content").asText());
                        newQ.setOptions(item.path("options").toString());
                        newQ.setAnswer(item.path("answer").asText());
                        newQ.setExplanation(item.path("explanation").asText());
                        newQ.setKnowledgePoint(q.getKnowledgePoint());
                        newQ.setCreatedBy(userId);
                        questionMapper.insert(newQ);
                        similar.add(newQ);
                        if (similar.size() >= count) break;
                    }
                }
            } catch (Exception e) {
                log.warn("解析 AI 生成题目失败", e);
            }
        }

        // 限制数量
        return similar.subList(0, Math.min(similar.size(), count));
    }

    private String aiGenerateSimilar(Question q, int count, Long userId) {
        String system = "你是一位出题专家。请根据用户提供的题目，生成 " + count + " 道同类题。"
                + "仅返回 JSON 数组，每个元素包含 content（题干）、options（JSON 字符串数组，如 [\"A. xxx\", \"B. xxx\"]）、"
                + "answer（正确答案，如 A）、explanation（解析）。不要任何其他文字。";

        String user = "原题：\n" + q.getContent() + "\n答案：" + q.getAnswer()
                + "\n知识点：" + q.getKnowledgePoint();

        String result = dashScopeService.chat(system, user, userId, "RECOMMEND", user);
        // 简单清洗：去掉 markdown 代码块
        result = result.trim();
        if (result.startsWith("```")) {
            int first = result.indexOf("\n");
            int last = result.lastIndexOf("```");
            if (first > 0 && last > first) {
                result = result.substring(first + 1, last);
            }
        }
        return result;
    }

    private String typeName(String type) {
        return switch (type) {
            case "SINGLE" -> "单选题";
            case "MULTIPLE" -> "多选题";
            case "JUDGE" -> "判断题";
            case "ESSAY" -> "简答题";
            default -> type;
        };
    }
}
