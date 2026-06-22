package com.aiqb.service.impl;

import com.aiqb.common.BusinessException;
import com.aiqb.dto.AutoPaperDTO;
import com.aiqb.dto.PaperDTO;
import com.aiqb.entity.Paper;
import com.aiqb.entity.PaperQuestion;
import com.aiqb.entity.Question;
import com.aiqb.mapper.PaperMapper;
import com.aiqb.mapper.PaperQuestionMapper;
import com.aiqb.mapper.QuestionMapper;
import com.aiqb.service.PaperService;
import com.aiqb.service.QuestionService;
import com.aiqb.vo.PaperDetailVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionService questionService;

    @Override
    public List<Paper> list(Long subjectId) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) wrapper.eq(Paper::getSubjectId, subjectId);
        wrapper.orderByDesc(Paper::getId);
        return paperMapper.selectList(wrapper);
    }

    @Override
    public PaperDetailVO detail(Long id) {
        Paper paper = paperMapper.selectById(id);
        if (paper == null) throw BusinessException.notFound("试卷不存在");

        List<PaperQuestion> pqs = paperQuestionMapper.selectByPaperId(id);
        List<Question> questions = new ArrayList<>();
        for (PaperQuestion pq : pqs) {
            Question q = questionMapper.selectById(pq.getQuestionId());
            if (q != null) questions.add(q);
        }
        PaperDetailVO vo = new PaperDetailVO();
        vo.setPaper(paper);
        vo.setQuestions(questions);
        return vo;
    }

    @Override
    @Transactional
    public Long create(PaperDTO dto, Long userId) {
        validateQuestionScore(dto);
        Paper paper = new Paper();
        paper.setTitle(dto.getTitle());
        paper.setDescription(dto.getDescription());
        paper.setSubjectId(dto.getSubjectId());
        paper.setTotalScore(dto.getTotalScore() != null ? dto.getTotalScore() : sumScore(dto.getScores()));
        paper.setDuration(dto.getDuration() != null ? dto.getDuration() : 60);
        paper.setCreatedBy(userId);
        paperMapper.insert(paper);

        savePaperQuestions(paper.getId(), dto.getQuestionIds(), dto.getScores());
        return paper.getId();
    }

    @Override
    @Transactional
    public void update(PaperDTO dto) {
        if (dto.getId() == null) throw BusinessException.badRequest("ID 不能为空");
        validateQuestionScore(dto);
        Paper paper = paperMapper.selectById(dto.getId());
        if (paper == null) throw BusinessException.notFound("试卷不存在");
        paper.setTitle(dto.getTitle());
        paper.setDescription(dto.getDescription());
        paper.setSubjectId(dto.getSubjectId());
        paper.setTotalScore(dto.getTotalScore() != null ? dto.getTotalScore() : sumScore(dto.getScores()));
        paper.setDuration(dto.getDuration());
        paperMapper.updateById(paper);

        // 重新写入题目关联
        paperQuestionMapper.deleteByPaperId(paper.getId());
        savePaperQuestions(paper.getId(), dto.getQuestionIds(), dto.getScores());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        paperQuestionMapper.deleteByPaperId(id);
        paperMapper.deleteById(id);
    }

    @Override
    @Transactional
    public Long autoGenerate(AutoPaperDTO dto, Long userId) {
        // 1. 创建试卷
        Paper paper = new Paper();
        paper.setTitle(dto.getTitle());
        paper.setDescription(dto.getDescription());
        paper.setSubjectId(dto.getSubjectId());
        paper.setDuration(dto.getDuration() != null ? dto.getDuration() : 60);
        int totalScore = dto.getRules().stream().mapToInt(r -> r.getScore() * r.getCount()).sum();
        paper.setTotalScore(totalScore);
        paper.setCreatedBy(userId);
        paperMapper.insert(paper);

        // 2. 按规则抽题
        List<Long> allQuestionIds = new ArrayList<>();
        List<Integer> allScores = new ArrayList<>();
        for (AutoPaperDTO.PaperRule rule : dto.getRules()) {
            List<Question> picked = questionService.randomPick(
                    dto.getSubjectId(), rule.getType(), rule.getDifficulty(),
                    rule.getKnowledgePoint(), rule.getCount());
            if (picked.size() < rule.getCount()) {
                throw BusinessException.badRequest(
                        "题型 " + rule.getType() + " 数量不足：需要 " + rule.getCount() + "，仅有 " + picked.size());
            }
            for (Question q : picked) {
                allQuestionIds.add(q.getId());
                allScores.add(rule.getScore());
            }
        }
        savePaperQuestions(paper.getId(), allQuestionIds, allScores);
        return paper.getId();
    }

    private void validateQuestionScore(PaperDTO dto) {
        if (dto.getQuestionIds() != null && dto.getScores() != null
                && dto.getQuestionIds().size() != dto.getScores().size()) {
            throw BusinessException.badRequest("题目与分值数量不一致");
        }
    }

    private int sumScore(List<Integer> scores) {
        if (scores == null) return 0;
        return scores.stream().mapToInt(Integer::intValue).sum();
    }

    private void savePaperQuestions(Long paperId, List<Long> questionIds, List<Integer> scores) {
        if (questionIds == null) return;
        for (int i = 0; i < questionIds.size(); i++) {
            PaperQuestion pq = new PaperQuestion();
            pq.setPaperId(paperId);
            pq.setQuestionId(questionIds.get(i));
            pq.setScore(scores != null ? scores.get(i) : 0);
            pq.setSortOrder(i);
            paperQuestionMapper.insert(pq);
        }
    }
}
