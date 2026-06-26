package com.aiqb.vo;

import com.aiqb.entity.Question;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
public class QuestionVO {
    private Long id;
    private Long subjectId;
    private String subjectName;
    private String type;
    private Integer difficulty;
    private String content;
    private List<String> options;
    private String answer;
    private String explanation;
    private String knowledgePoint;
    private Integer score;
    private Long createdBy;
    private String createdAt;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static QuestionVO from(Question q, String subjectName) {
        return from(q, subjectName, true);
    }

    /**
     * @param q             题目实体
     * @param subjectName   科目名（可为 null）
     * @param includeAnswer 是否包含 answer/explanation（学生接口应传 false，避免泄题）
     */
    public static QuestionVO from(Question q, String subjectName, boolean includeAnswer) {
        QuestionVO v = new QuestionVO();
        v.id = q.getId();
        v.subjectId = q.getSubjectId();
        v.subjectName = subjectName;
        v.type = q.getType();
        v.difficulty = q.getDifficulty();
        v.content = q.getContent();
        v.answer = includeAnswer ? q.getAnswer() : null;
        v.explanation = includeAnswer ? q.getExplanation() : null;
        v.knowledgePoint = q.getKnowledgePoint();
        v.score = q.getScore();
        v.createdBy = q.getCreatedBy();
        v.createdAt = q.getCreatedAt() == null ? null : q.getCreatedAt().toString();
        v.options = parseOptions(q.getOptions());
        return v;
    }

    /** 学生端组装（剥离答案与解析） */
    public static QuestionVO forStudent(Question q, String subjectName) {
        return from(q, subjectName, false);
    }

    /**
     * 解析题库 options 字段。数据库存的是 JSON 数组字符串（也可能由于历史数据是非标准格式），
     * 这里优先用 Jackson 解析，失败再降级到简单字符串切分。
     */
    public static List<String> parseOptions(String raw) {
        if (raw == null || raw.isEmpty()) return List.of();
        String s = raw.trim();
        // 1) 优先按 JSON 数组解析
        if (s.startsWith("[") && s.endsWith("]")) {
            try {
                List<String> arr = MAPPER.readValue(s, new TypeReference<List<String>>() {});
                if (arr != null) {
                    return arr.stream()
                            .map(x -> x == null ? "" : String.valueOf(x).trim())
                            .filter(x -> !x.isEmpty())
                            .toList();
                }
            } catch (Exception e) {
                log.debug("options JSON 解析失败，降级为字符串切分：{}", e.getMessage());
            }
        }
        // 2) 兜底：去掉 [ ] 后按逗号切，剥引号
        try {
            String o = s;
            if (o.startsWith("[")) o = o.substring(1);
            if (o.endsWith("]")) o = o.substring(0, o.length() - 1);
            return java.util.Arrays.stream(o.split(","))
                    .map(x -> x.trim().replaceAll("^\"|\"$", "").replaceAll("\\\\\"", "\""))
                    .filter(x -> !x.isEmpty())
                    .toList();
        } catch (Exception e) {
            log.warn("options 兜底解析仍失败：raw={}", raw);
            return List.of();
        }
    }
}

