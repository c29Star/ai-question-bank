package com.aiqb.service.impl;

import com.aiqb.mapper.ExamRecordMapper;
import com.aiqb.mapper.QuestionMapper;
import com.aiqb.mapper.WrongQuestionMapper;
import com.aiqb.service.StatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private final ExamRecordMapper examRecordMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionMapper questionMapper;

    @Override
    public Map<String, Object> personal(Long userId) {
        Map<String, Object> result = new HashMap<>();

        // 1. 总览
        List<Map<String, Object>> records = examRecordMapper.selectWithDetail(userId);
        int totalExams = (int) records.stream().filter(r -> "GRADED".equals(r.get("status")) || "SUBMITTED".equals(r.get("status"))).count();
        int avgScore = records.isEmpty() ? 0 :
                (int) records.stream()
                        .filter(r -> r.get("score") != null)
                        .mapToInt(r -> ((Number) r.get("score")).intValue())
                        .average().orElse(0);

        // 2. 成绩趋势
        List<Map<String, Object>> scoreTrend = new ArrayList<>();
        records.stream()
                .filter(r -> r.get("score") != null && r.get("submit_time") != null)
                .sorted(Comparator.comparing(r -> (LocalDateTime) r.get("submit_time")))
                .forEach(r -> {
                    Map<String, Object> point = new HashMap<>();
                    point.put("date", r.get("submit_time").toString().substring(0, 10));
                    point.put("score", r.get("score"));
                    point.put("title", r.get("paper_title"));
                    scoreTrend.add(point);
                });

        // 3. 错题分布
        List<Map<String, Object>> wrongDist = questionMapper.countByKnowledge(userId);

        // 4. 错题总数 & 已掌握
        long wrongTotal = wrongQuestionMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.aiqb.entity.WrongQuestion>()
                .eq(com.aiqb.entity.WrongQuestion::getUserId, userId));
        long masteredCount = wrongQuestionMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.aiqb.entity.WrongQuestion>()
                .eq(com.aiqb.entity.WrongQuestion::getUserId, userId)
                .eq(com.aiqb.entity.WrongQuestion::getMastered, true));
        long unmasteredCount = wrongQuestionMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.aiqb.entity.WrongQuestion>()
                .eq(com.aiqb.entity.WrongQuestion::getUserId, userId)
                .eq(com.aiqb.entity.WrongQuestion::getMastered, false));

        Map<String, Object> overview = new HashMap<>();
        overview.put("totalExams", totalExams);
        overview.put("avgScore", avgScore);
        overview.put("wrongTotal", wrongTotal);
        overview.put("masteredCount", masteredCount);
        overview.put("unmasteredCount", unmasteredCount);

        result.put("overview", overview);
        result.put("scoreTrend", scoreTrend);
        result.put("wrongDist", wrongDist);
        return result;
    }
}
