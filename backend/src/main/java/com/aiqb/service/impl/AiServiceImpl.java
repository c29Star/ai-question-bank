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

        // 1) 先按题目自身字段构造结构化解析（不依赖外网 AI，保证可用性）
        String local = buildLocalExplanation(q);

        // 2) 试着用 dashScope（通义千问）生成增强版。如果成功就替换，否则用 local
        try {
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
            String ai = dashScopeService.chat(system, user.toString(), userId, "EXPLAIN", user.toString());
            // dashScope 失败会返回 [错误]/[异常重试]/[模拟] 前缀的字符串 —— 检测到则用 local
            if (ai != null && !ai.isEmpty() && !isFallbackText(ai)) {
                return ai;
            }
        } catch (Exception ignore) {
            // 任何异常都回落到本地解析
        }
        return local;
    }

    /**
     * 本地构造的题目解析：不依赖外部 AI，根据题目字段生成结构化教学文案。
     */
    private String buildLocalExplanation(Question q) {
        StringBuilder sb = new StringBuilder();
        sb.append("📌 **考点定位**\n");
        sb.append("本题属于 `").append(q.getKnowledgePoint() == null ? "通用" : q.getKnowledgePoint()).append("` 知识点，");
        sb.append("题型为 **").append(typeName(q.getType())).append("**，");
        sb.append("难度 ").append(q.getDifficulty()).append(" / 5。\n\n");

        sb.append("📝 **题目回顾**\n");
        sb.append(q.getContent()).append("\n\n");

        // 选项
        if (q.getOptions() != null && !q.getOptions().isEmpty()) {
            sb.append("🔢 **选项列表**\n");
            try {
                List<String> opts = objectMapper.readValue(q.getOptions(),
                    objectMapper.getTypeFactory().constructCollectionType(List.class, String.class));
                for (int i = 0; i < opts.size(); i++) {
                    sb.append("- ").append((char) ('A' + i)).append(". ").append(opts.get(i)).append("\n");
                }
            } catch (Exception ignore) {
                sb.append(q.getOptions()).append("\n");
            }
            sb.append("\n");
        }

        sb.append("✅ **正确答案**\n");
        sb.append("**").append(q.getAnswer()).append("**\n\n");

        sb.append("💡 **解题思路**\n");
        if (q.getExplanation() != null && !q.getExplanation().isEmpty()) {
            sb.append(q.getExplanation()).append("\n\n");
        } else {
            // 没有 explanation 时根据题型给一个通用模板
            sb.append(genericHint(q)).append("\n\n");
        }

        sb.append("🎯 **学习建议**\n");
        sb.append("- 回顾 `").append(q.getKnowledgePoint() == null ? "该知识点" : q.getKnowledgePoint()).append("` 的基础概念\n");
        sb.append("- 找 2-3 道同类型题练习巩固（推荐使用 AI 推荐同类变式题功能）\n");
        sb.append("- 如反复出错，可加入错题本重点复习");

        return sb.toString();
    }

    private String genericHint(Question q) {
        switch (q.getType() == null ? "SINGLE" : q.getType()) {
            case "SINGLE":   return "从题干关键词入手，逐项排除错误选项，找到唯一正确项。";
            case "MULTIPLE": return "注意多选题答案可能不止一个；先选出明显正确的，再反向排除。";
            case "JUDGE":    return "判断题考察概念准确性，细抠定义中的限定词（'一定'、'通常'、'可以'）。";
            case "FILL":     return "根据上下文推断空格处应填的关键术语或公式，注意大小写。";
            case "ESSAY":    return "简答题按「定义 → 关键点 → 举例」三段式作答，逻辑要完整。";
            default:         return "结合题干与所学知识作答。";
        }
    }

    @Override
    public List<Question> recommendSimilar(Long wrongQuestionId, Integer count, Long userId) {
        WrongQuestion wq = wrongQuestionMapper.selectById(wrongQuestionId);
        if (wq == null) throw BusinessException.notFound("错题记录不存在");
        Question q = questionMapper.selectById(wq.getQuestionId());
        if (q == null) throw BusinessException.notFound("原题不存在");

        // 1. 优先按知识点查同类题（排除原题）
        List<Question> similar = new ArrayList<>();
        if (q.getKnowledgePoint() != null && !q.getKnowledgePoint().isEmpty()) {
            similar = questionMapper.randomPick(
                    q.getSubjectId(), null, null, q.getKnowledgePoint(), count, q.getId());
        }
        if (similar.size() < count) {
            // 2. 不足则按科目 + 题型补（排除原题 + 已选）
            int need = count - similar.size();
            List<Question> more = questionMapper.randomPick(
                    q.getSubjectId(), q.getType(), null, null, need, q.getId());
            for (Question m : more) {
                if (similar.stream().noneMatch(s -> s.getId().equals(m.getId()))) {
                    similar.add(m);
                }
            }
        }

        // 3. 还不够则用 AI 生成
        if (similar.size() < count) {
            // 3a. 先尝试 dashScope 真 AI 生成
            try {
                String generated = aiGenerateSimilar(q, count - similar.size(), userId);
                if (generated != null && !generated.startsWith("[")) {
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
                }
            } catch (Exception e) {
                log.warn("AI 生成题目失败，使用本地降级方案", e);
            }

            // 3b. 仍然不够：用本题字段动态生成一道"变形题"入库（不依赖 AI）
            while (similar.size() < count) {
                Question generated = synthesizeSimilarLocally(q, userId);
                questionMapper.insert(generated);
                similar.add(generated);
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

    /** 判断 AI 返回是否是降级/错误文本（dashScope 没配/调用失败时的固定回退） */
    private boolean isFallbackText(String s) {
        if (s == null) return true;
        String t = s.trim();
        if (t.isEmpty()) return true;
        // [错误]/[异常重试]/[模拟回答]/[模拟]
        return t.startsWith("[错误]") || t.startsWith("[异常") || t.startsWith("[模拟");
    }

/**
     * 本地动态构造一道同类题（不依赖 AI）：
     * 基于原题的 subject/knowledgePoint/题型，在题干前后加「变形」标记，
     * 并复用正确答案与解释。保证即使数据库同知识点题目不足，也能凑够 count 道。
     */
    private Question synthesizeSimilarLocally(Question q, Long userId) {
        Question g = new Question();
        g.setSubjectId(q.getSubjectId());
        g.setType(q.getType());
        g.setDifficulty(q.getDifficulty());
        g.setContent("[变形练习] " + q.getContent());
        g.setOptions(q.getOptions());
        g.setAnswer(q.getAnswer());
        g.setExplanation("本题与原题同属「" + (q.getKnowledgePoint() == null ? "通用" : q.getKnowledgePoint())
                + "」知识点，由系统自动基于你的错题生成，巩固同类考点。"
                + (q.getExplanation() == null ? "" : ("\n参考解析：" + q.getExplanation())));
        g.setKnowledgePoint(q.getKnowledgePoint());
        g.setScore(q.getScore() == null ? 5 : q.getScore());
        g.setCreatedBy(userId);
        return g;
    }

    private String typeName(String type) {
        return switch (type) {
            case "SINGLE" -> "单选题";
            case "MULTIPLE" -> "多选题";
            case "JUDGE" -> "判断题";
            case "ESSAY" -> "简答题";
            case "QA" -> "问答题";
            case "CALC" -> "计算题";
            case "FILL" -> "填空题";
            default -> type;
        };
    }
}
