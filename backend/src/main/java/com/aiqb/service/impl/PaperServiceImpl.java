package com.aiqb.service.impl;

import com.aiqb.common.BusinessException;
import com.aiqb.dto.AutoPaperDTO;
import com.aiqb.dto.PaperDTO;
import com.aiqb.entity.Paper;
import com.aiqb.entity.PaperQuestion;
import com.aiqb.entity.Question;
import com.aiqb.entity.Subject;
import com.aiqb.mapper.PaperMapper;
import com.aiqb.mapper.PaperQuestionMapper;
import com.aiqb.mapper.QuestionMapper;
import com.aiqb.mapper.SubjectMapper;
import com.aiqb.service.PaperService;
import com.aiqb.service.QuestionService;
import com.aiqb.vo.PaperDetailVO;
import com.aiqb.vo.PaperVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaperServiceImpl implements PaperService {

    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final SubjectMapper subjectMapper;
    private final QuestionService questionService;

    @Override
    public IPage<PaperVO> page(Long subjectId, Integer current, Integer size) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) wrapper.eq(Paper::getSubjectId, subjectId);
        wrapper.orderByDesc(Paper::getId);
        IPage<Paper> pg = paperMapper.selectPage(new Page<>(current, size), wrapper);
        return pg.convert(this::toVO);
    }

    @Override
    public List<PaperVO> listVO(Long subjectId) {
        LambdaQueryWrapper<Paper> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) wrapper.eq(Paper::getSubjectId, subjectId);
        wrapper.orderByDesc(Paper::getId);
        return paperMapper.selectList(wrapper).stream().map(this::toVO).collect(Collectors.toList());
    }

    private PaperVO toVO(Paper p) {
        String subjectName = null;
        if (p.getSubjectId() != null) {
            Subject s = subjectMapper.selectById(p.getSubjectId());
            if (s != null) subjectName = s.getName();
        }
        Integer count = paperQuestionMapper.selectCountByPaperId(p.getId());
        return PaperVO.from(p, subjectName, count == null ? 0 : count);
    }

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

        // 关键：给 paper 注入 subjectName，前端预览弹窗要用
        if (paper.getSubjectId() != null) {
            Subject s = subjectMapper.selectById(paper.getSubjectId());
            if (s != null) {
                // 用 transient 字段（不持久化到 DB），仅用于返回 JSON
                paper.setSubjectName(s.getName());
            }
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

        // 2. 按规则抽题（多规则间自动去重）
        List<Long> allQuestionIds = new ArrayList<>();
        List<Integer> allScores = new ArrayList<>();
        java.util.Set<Long> usedIds = new java.util.HashSet<>();
        for (AutoPaperDTO.PaperRule rule : dto.getRules()) {
            // 每次多抽一些以补偿去重过滤
            int want = rule.getCount();
            List<Question> picked = questionService.randomPick(
                    dto.getSubjectId(), rule.getType(), rule.getDifficulty(),
                    rule.getKnowledgePoint(), want * 2, null);
            // 过滤掉已用题
            picked = picked.stream().filter(q -> !usedIds.contains(q.getId())).collect(Collectors.toList());
            if (picked.size() < want) {
                throw BusinessException.badRequest(
                        "题型 " + rule.getType() + " 数量不足：需要 " + want + "，仅有 " + picked.size() + "（去重后）");
            }
            for (int i = 0; i < want; i++) {
                Question q = picked.get(i);
                usedIds.add(q.getId());
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
