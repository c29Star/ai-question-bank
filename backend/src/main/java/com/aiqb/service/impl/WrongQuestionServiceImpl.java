package com.aiqb.service.impl;

import com.aiqb.common.BusinessException;
import com.aiqb.entity.Question;
import com.aiqb.entity.Subject;
import com.aiqb.entity.WrongQuestion;
import com.aiqb.mapper.QuestionMapper;
import com.aiqb.mapper.SubjectMapper;
import com.aiqb.mapper.WrongQuestionMapper;
import com.aiqb.service.WrongQuestionService;
import com.aiqb.vo.QuestionVO;
import com.aiqb.vo.WrongQuestionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WrongQuestionServiceImpl implements WrongQuestionService {

    private final WrongQuestionMapper wrongQuestionMapper;
    private final QuestionMapper questionMapper;
    private final SubjectMapper subjectMapper;

    @Override
    public List<Map<String, Object>> myWrongQuestions(Long userId, Boolean mastered) {
        List<Map<String, Object>> all = wrongQuestionMapper.selectWithQuestion(userId);
        if (mastered == null) return all;
        return all.stream()
                .filter(m -> mastered.equals(m.get("mastered")))
                .collect(Collectors.toList());
    }

    @Override
    public IPage<WrongQuestionVO> page(Long userId, Boolean mastered, String keyword, Integer current, Integer size) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(WrongQuestion::getUserId, userId);
        if (mastered != null) wrapper.eq(WrongQuestion::getMastered, mastered);
        wrapper.orderByDesc(WrongQuestion::getLastWrongAt);
        IPage<WrongQuestion> pg = wrongQuestionMapper.selectPage(new Page<>(current, size), wrapper);
        List<WrongQuestionVO> records = pg.getRecords().stream().map(this::toVO).filter(java.util.Objects::nonNull).toList();
        // 关键字过滤（题目内容 / 知识点）
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword;
            records = records.stream().filter(v ->
                (v.getContent() != null && v.getContent().contains(kw)) ||
                (v.getKnowledgePoint() != null && v.getKnowledgePoint().contains(kw))
            ).toList();
        }
        // 关键修复：total 必须用数据库原始总数（pg.getTotal()），不能用 records.size()
        long total = pg.getTotal();
        if (keyword != null && !keyword.isEmpty()) {
            // 关键字过滤后，total 不再是 DB 查到的全量，需要在过滤后的 records 上回写
            total = records.size();
        }
        IPage<WrongQuestionVO> result = new Page<>(pg.getCurrent(), pg.getSize(), total);
        result.setTotal(total);
        result.setRecords(records);
        return result;
    }

    private WrongQuestionVO toVO(WrongQuestion wq) {
        if (wq == null) return null;
        WrongQuestionVO vo = new WrongQuestionVO();
        vo.setId(wq.getId());
        vo.setUserId(wq.getUserId());
        vo.setQuestionId(wq.getQuestionId());
        vo.setWrongCount(wq.getWrongCount());
        vo.setMastered(Boolean.TRUE.equals(wq.getMastered()));
        vo.setLastWrongAt(wq.getLastWrongAt() == null ? null : wq.getLastWrongAt().toString());
        Question q = questionMapper.selectById(wq.getQuestionId());
        if (q != null) {
            vo.setType(q.getType());
            vo.setDifficulty(q.getDifficulty());
            vo.setContent(q.getContent());
            vo.setCorrectAnswer(q.getAnswer());
            vo.setExplanation(q.getExplanation());
            vo.setKnowledgePoint(q.getKnowledgePoint());
            vo.setSubjectId(q.getSubjectId());
            // 解析 options（复用 QuestionVO 鲁棒解析）
            vo.setOptions(QuestionVO.parseOptions(q.getOptions()));
            if (q.getSubjectId() != null) {
                Subject s = subjectMapper.selectById(q.getSubjectId());
                if (s != null) vo.setSubjectName(s.getName());
            }
        }
        return vo;
    }

    @Override
    public void markMastered(Long id, Long userId, Boolean mastered) {
        WrongQuestion wq = wrongQuestionMapper.selectById(id);
        if (wq == null) throw BusinessException.notFound("错题记录不存在");
        if (!wq.getUserId().equals(userId)) throw BusinessException.forbidden("无权操作");
        wq.setMastered(mastered);
        wrongQuestionMapper.updateById(wq);
    }

    @Override
    public List<Map<String, Object>> statsByKnowledge(Long userId) {
        return questionMapper.countByKnowledge(userId);
    }
}
