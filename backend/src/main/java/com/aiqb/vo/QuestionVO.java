package com.aiqb.vo;

import com.aiqb.entity.Question;
import lombok.Data;

import java.util.Arrays;
import java.util.List;

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

    public static QuestionVO from(Question q, String subjectName) {
        QuestionVO v = new QuestionVO();
        v.id = q.getId();
        v.subjectId = q.getSubjectId();
        v.subjectName = subjectName;
        v.type = q.getType();
        v.difficulty = q.getDifficulty();
        v.content = q.getContent();
        v.answer = q.getAnswer();
        v.explanation = q.getExplanation();
        v.knowledgePoint = q.getKnowledgePoint();
        v.score = q.getScore();
        v.createdBy = q.getCreatedBy();
        v.createdAt = q.getCreatedAt() == null ? null : q.getCreatedAt().toString();
        // options 字段是 JSON 数组字符串，前端期望数组
        if (q.getOptions() != null && !q.getOptions().isEmpty()) {
            try {
                // 简单解析：去掉 [ ] 和引号
                String o = q.getOptions().trim();
                if (o.startsWith("[")) o = o.substring(1);
                if (o.endsWith("]")) o = o.substring(0, o.length() - 1);
                v.options = Arrays.stream(o.split(","))
                        .map(s -> s.trim().replaceAll("^\"|\"$", "").replaceAll("\\\\\"", "\""))
                        .filter(s -> !s.isEmpty())
                        .toList();
            } catch (Exception e) {
                v.options = List.of();
            }
        } else {
            v.options = List.of();
        }
        return v;
    }
}
