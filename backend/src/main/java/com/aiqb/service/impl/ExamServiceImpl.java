package com.aiqb.service.impl;

import com.aiqb.common.BusinessException;
import com.aiqb.dto.ExamDTO;
import com.aiqb.dto.SubmitAnswerDTO;
import com.aiqb.entity.*;
import com.aiqb.mapper.*;
import com.aiqb.service.ExamService;
import com.aiqb.vo.ExamQuestionVO;
import com.aiqb.vo.ExamResultVO;
import com.aiqb.vo.ExamStartVO;
import com.aiqb.vo.ExamVO;
import com.aiqb.vo.QuestionVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExamServiceImpl implements ExamService {

    private final ExamMapper examMapper;
    private final PaperMapper paperMapper;
    private final PaperQuestionMapper paperQuestionMapper;
    private final QuestionMapper questionMapper;
    private final ExamRecordMapper examRecordMapper;
    private final AnswerMapper answerMapper;
    private final WrongQuestionMapper wrongQuestionMapper;
    private final com.aiqb.mapper.SubjectMapper subjectMapper;
    private final com.aiqb.mapper.UserMapper userMapper;

    @Override
    @Transactional
    public Long save(ExamDTO dto, Long userId) {
        Exam exam = new Exam();
        exam.setPaperId(dto.getPaperId());
        exam.setTitle(dto.getTitle());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setDuration(dto.getDuration());
        exam.setMaxAttempts(dto.getMaxAttempts());
        exam.setStatus("DRAFT");
        exam.setCreatedBy(userId);
        examMapper.insert(exam);
        return exam.getId();
    }

    @Override
    @Transactional
    public void update(ExamDTO dto) {
        if (dto.getId() == null) throw BusinessException.badRequest("ID 不能为空");
        Exam exam = examMapper.selectById(dto.getId());
        if (exam == null) throw BusinessException.notFound("考试不存在");
        if ("ONGOING".equals(exam.getStatus()) || "FINISHED".equals(exam.getStatus())) {
            throw BusinessException.badRequest("进行中或已结束的考试不可修改");
        }
        exam.setTitle(dto.getTitle());
        exam.setStartTime(dto.getStartTime());
        exam.setEndTime(dto.getEndTime());
        exam.setDuration(dto.getDuration());
        exam.setMaxAttempts(dto.getMaxAttempts());
        exam.setPaperId(dto.getPaperId());
        examMapper.updateById(exam);
    }

    @Override
    public void delete(Long id) {
        examMapper.deleteById(id);
    }

    @Override
    public void publish(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) throw BusinessException.notFound("考试不存在");
        exam.setStatus("PUBLISHED");
        examMapper.updateById(exam);
    }

    @Override
    public void archive(Long id) {
        Exam exam = examMapper.selectById(id);
        if (exam == null) throw BusinessException.notFound("考试不存在");
        exam.setStatus("ARCHIVED");
        examMapper.updateById(exam);
    }

    @Override
    public IPage<ExamVO> page(Integer pageNum, Integer pageSize, String status) {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) wrapper.eq(Exam::getStatus, status);
        wrapper.orderByDesc(Exam::getId);
        IPage<Exam> pg = examMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return pg.convert(e -> {
            String paperName = null;
            if (e.getPaperId() != null) {
                Paper p = paperMapper.selectById(e.getPaperId());
                if (p != null) paperName = p.getTitle();
            }
            return ExamVO.from(e, paperName);
        });
    }

    @Override
    public List<Map<String, Object>> available(Long userId) {
        LambdaQueryWrapper<Exam> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Exam::getStatus, "PUBLISHED")
                .and(w -> w.isNull(Exam::getEndTime).or().gt(Exam::getEndTime, LocalDateTime.now()));
        List<Exam> exams = examMapper.selectList(wrapper);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Exam e : exams) {
            // 检查次数
            List<ExamRecord> records = examRecordMapper.selectByUserAndExam(userId, e.getId());
            long finishedCount = records.stream().filter(r -> !"IN_PROGRESS".equals(r.getStatus())).count();
            if (finishedCount >= e.getMaxAttempts()) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("title", e.getTitle());
            map.put("duration", e.getDuration());
            map.put("startTime", e.getStartTime());
            map.put("endTime", e.getEndTime());
            map.put("maxAttempts", e.getMaxAttempts());
            map.put("usedAttempts", (int) finishedCount);
            // 是否已有进行中的 record
            records.stream().filter(r -> "IN_PROGRESS".equals(r.getStatus())).findFirst().ifPresent(r -> {
                map.put("ongoingRecordId", r.getId());
            });
            result.add(map);
        }
        return result;
    }

    @Override
    @Transactional
    public ExamStartVO start(Long examId, Long userId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) throw BusinessException.notFound("考试不存在");
        if (!"PUBLISHED".equals(exam.getStatus())) {
            throw BusinessException.badRequest("考试未发布");
        }
        if (exam.getEndTime() != null && exam.getEndTime().isBefore(LocalDateTime.now())) {
            throw BusinessException.badRequest("考试已截止");
        }
        // 检查是否有进行中的 record
        List<ExamRecord> existing = examRecordMapper.selectByUserAndExam(userId, examId);
        ExamRecord record = existing.stream()
                .filter(r -> "IN_PROGRESS".equals(r.getStatus()))
                .findFirst()
                .orElse(null);

        if (record == null) {
            // 检查次数
            long finished = existing.stream().filter(r -> !"IN_PROGRESS".equals(r.getStatus())).count();
            if (finished >= exam.getMaxAttempts()) {
                throw BusinessException.badRequest("已达最大参考次数");
            }
            // 创建新 record
            record = new ExamRecord();
            record.setExamId(examId);
            record.setUserId(userId);
            record.setPaperId(exam.getPaperId());
            Paper paper = paperMapper.selectById(exam.getPaperId());
            record.setTotalScore(paper.getTotalScore());
            record.setStatus("IN_PROGRESS");
            record.setStartTime(LocalDateTime.now());
            examRecordMapper.insert(record);

            // 同步更新 exam.status 为 ONGOING
            exam.setStatus("ONGOING");
            examMapper.updateById(exam);
        }

        // 组装返回
        Paper paper = paperMapper.selectById(exam.getPaperId());
        List<PaperQuestion> pqs = paperQuestionMapper.selectByPaperId(paper.getId());
        List<ExamQuestionVO> questionVOs = new ArrayList<>();
        // 一次性查出所有相关科目，避免 N+1
        java.util.Set<Long> subjectIds = new java.util.HashSet<>();
        for (PaperQuestion pq : pqs) {
            Question qq = questionMapper.selectById(pq.getQuestionId());
            if (qq != null) subjectIds.add(qq.getSubjectId());
        }
        java.util.Map<Long, String> subjectNameMap = new java.util.HashMap<>();
        for (Long sid : subjectIds) {
            var s = subjectMapper.selectById(sid);
            if (s != null) subjectNameMap.put(sid, s.getName());
        }
        for (PaperQuestion pq : pqs) {
            Question q = questionMapper.selectById(pq.getQuestionId());
            if (q == null) continue;
            String subjectName = subjectNameMap.get(q.getSubjectId());
            ExamQuestionVO vo = new ExamQuestionVO();
            vo.setQuestionId(q.getId());
            vo.setScore(pq.getScore());
            vo.setSortOrder(pq.getSortOrder());
            // 用 QuestionVO 包装，options 字段会自动解析为 List<String>
            QuestionVO qvo = QuestionVO.from(q, subjectName);
            // ⚠️ 安全：考试作答接口绝对不能把正确答案泄给学生
            qvo.setAnswer(null);
            qvo.setExplanation(null);
            vo.setQuestion(qvo);
            questionVOs.add(vo);
        }
        // 取已作答的答案（续答场景），存到 savedAnswer 字段而非 question.answer
        List<Answer> savedAnswers = answerMapper.selectByRecordId(record.getId());
        Map<Long, String> userAnswerMap = new HashMap<>();
        for (Answer a : savedAnswers) {
            userAnswerMap.put(a.getQuestionId(), a.getUserAnswer());
        }
        for (ExamQuestionVO vo : questionVOs) {
            String ua = userAnswerMap.get(vo.getQuestionId());
            if (ua != null) vo.setSavedAnswer(ua);
        }

        ExamStartVO startVO = new ExamStartVO();
        startVO.setRecord(record);
        startVO.setPaper(paper);
        startVO.setQuestions(questionVOs);
        return startVO;
    }

    @Override
    @Transactional
    public void saveAnswer(Long recordId, Long questionId, String userAnswer, Integer timeSpent) {
        ExamRecord record = examRecordMapper.selectById(recordId);
        if (record == null) throw BusinessException.notFound("考试记录不存在");
        if (!"IN_PROGRESS".equals(record.getStatus())) {
            throw BusinessException.badRequest("考试已结束");
        }
        // upsert
        LambdaQueryWrapper<Answer> wrapper = new LambdaQueryWrapper<Answer>()
                .eq(Answer::getRecordId, recordId)
                .eq(Answer::getQuestionId, questionId);
        Answer exist = answerMapper.selectOne(wrapper);
        if (exist == null) {
            Answer a = new Answer();
            a.setRecordId(recordId);
            a.setQuestionId(questionId);
            a.setUserAnswer(userAnswer);
            a.setTimeSpent(timeSpent);
            answerMapper.insert(a);
        } else {
            exist.setUserAnswer(userAnswer);
            if (timeSpent != null) exist.setTimeSpent(timeSpent);
            answerMapper.updateById(exist);
        }
    }

    @Override
    @Transactional
    public ExamResultVO submit(SubmitAnswerDTO dto, Long userId) {
        ExamRecord record = examRecordMapper.selectById(dto.getRecordId());
        if (record == null) throw BusinessException.notFound("考试记录不存在");
        if (!record.getUserId().equals(userId)) throw BusinessException.forbidden("无权操作");
        if (!"IN_PROGRESS".equals(record.getStatus())) {
            throw BusinessException.badRequest("考试已提交");
        }

        // 1. 保存所有答案
        for (SubmitAnswerDTO.AnswerItem item : dto.getAnswers()) {
            saveAnswer(record.getId(), item.getQuestionId(), item.getUserAnswer(), item.getTimeSpent());
        }

        // 2. 自动判分
        List<Answer> allAnswers = answerMapper.selectByRecordId(record.getId());
        List<PaperQuestion> pqs = paperQuestionMapper.selectByPaperId(record.getPaperId());
        Map<Long, Integer> scoreMap = new HashMap<>();
        for (PaperQuestion pq : pqs) scoreMap.put(pq.getQuestionId(), pq.getScore());

        int totalScore = 0;
        for (Answer a : allAnswers) {
            Question q = questionMapper.selectById(a.getQuestionId());
            if (q == null) continue;
            int qScore = scoreMap.getOrDefault(q.getId(), 0);
            boolean correct = judge(q, a.getUserAnswer());
            a.setIsCorrect(correct);
            if ("ESSAY".equals(q.getType())) {
                // 主观题不自动打分，等教师批改
                a.setScore(0);
            } else {
                a.setScore(correct ? qScore : 0);
                totalScore += a.getScore();
            }
            answerMapper.updateById(a);
        }

        // 3. 更新 record
        record.setScore(totalScore);
        record.setStatus("SUBMITTED");
        record.setSubmitTime(LocalDateTime.now());
        record.setDurationUsed(dto.getDurationUsed() != null ? dto.getDurationUsed() :
                (int) Duration.between(record.getStartTime(), record.getSubmitTime()).getSeconds());
        examRecordMapper.updateById(record);

        // 4. 错题入库
        for (Answer a : allAnswers) {
            if (Boolean.FALSE.equals(a.getIsCorrect())) {
                addWrongQuestion(userId, a.getQuestionId());
            }
        }

        // 5. 如果所有题都是客观题，自动改完直接 GRADED
        boolean hasEssay = false;
        for (Answer a : allAnswers) {
            Question q = questionMapper.selectById(a.getQuestionId());
            if (q != null && "ESSAY".equals(q.getType())) { hasEssay = true; break; }
        }
        if (!hasEssay) {
            record.setStatus("GRADED");
            examRecordMapper.updateById(record);
        }

        // 6. 组装返回
        return buildResultVO(record, allAnswers);
    }

    @Override
    public List<Map<String, Object>> myRecords(Long userId) {
        return examRecordMapper.selectWithDetail(userId);
    }

    @Override
    public List<Map<String, Object>> examRecords(Long examId) {
        // 教师视角：列出这场考试所有学生的 record（含用户名、得分、用时、状态）
        LambdaQueryWrapper<ExamRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ExamRecord::getExamId, examId).orderByDesc(ExamRecord::getScore);
        List<ExamRecord> records = examRecordMapper.selectList(wrapper);
        List<Map<String, Object>> out = new ArrayList<>();
        for (ExamRecord r : records) {
            Map<String, Object> m = new HashMap<>();
            m.put("recordId", r.getId());
            m.put("userId", r.getUserId());
            m.put("username", userNickname(r.getUserId()));
            m.put("paperId", r.getPaperId());
            m.put("score", r.getScore());
            m.put("totalScore", r.getTotalScore());
            m.put("status", r.getStatus());
            m.put("startTime", r.getStartTime());
            m.put("submitTime", r.getSubmitTime());
            m.put("durationUsed", r.getDurationUsed());
            out.add(m);
        }
        return out;
    }

    private String userNickname(Long userId) {
        try {
            com.aiqb.entity.User u = userMapper.selectById(userId);
            return u != null ? u.getNickname() : ("user#" + userId);
        } catch (Exception e) {
            return "user#" + userId;
        }
    }

    // ============== 私有方法 ==============

    /**
     * 自动判分（单选/多选/判断）
     */
    private boolean judge(Question q, String userAnswer) {
        if (userAnswer == null || userAnswer.isEmpty()) return false;
        String correct = q.getAnswer();
        switch (q.getType()) {
            case "SINGLE":
            case "JUDGE":
                return correct.equalsIgnoreCase(userAnswer.trim());
            case "MULTIPLE":
                // 多选：选项排序后比较
                String[] u = userAnswer.toUpperCase().split("");
                String[] c = correct.toUpperCase().split("");
                java.util.Arrays.sort(u);
                java.util.Arrays.sort(c);
                return java.util.Arrays.equals(u, c);
            case "ESSAY":
                return false; // 主观题人工判
            default:
                return false;
        }
    }

    private void addWrongQuestion(Long userId, Long questionId) {
        LambdaQueryWrapper<WrongQuestion> wrapper = new LambdaQueryWrapper<WrongQuestion>()
                .eq(WrongQuestion::getUserId, userId)
                .eq(WrongQuestion::getQuestionId, questionId);
        WrongQuestion exist = wrongQuestionMapper.selectOne(wrapper);
        if (exist == null) {
            WrongQuestion wq = new WrongQuestion();
            wq.setUserId(userId);
            wq.setQuestionId(questionId);
            wq.setWrongCount(1);
            wq.setMastered(false);
            wrongQuestionMapper.insert(wq);
        } else {
            exist.setWrongCount(exist.getWrongCount() + 1);
            exist.setMastered(false);
            exist.setLastWrongAt(LocalDateTime.now());
            wrongQuestionMapper.updateById(exist);
        }
    }

    private ExamResultVO buildResultVO(ExamRecord record, List<Answer> answers) {
        ExamResultVO vo = new ExamResultVO();
        vo.setScore(record.getScore());
        vo.setTotalScore(record.getTotalScore());
        List<ExamResultVO.QuestionResult> results = new ArrayList<>();
        // 一次性查所有科目名，避免 N+1
        java.util.Set<Long> subjectIds = new java.util.HashSet<>();
        for (Answer a : answers) {
            Question qq = questionMapper.selectById(a.getQuestionId());
            if (qq != null && qq.getSubjectId() != null) subjectIds.add(qq.getSubjectId());
        }
        java.util.Map<Long, String> subjectNameMap = new java.util.HashMap<>();
        for (Long sid : subjectIds) {
            var s = subjectMapper.selectById(sid);
            if (s != null) subjectNameMap.put(sid, s.getName());
        }
        for (Answer a : answers) {
            Question q = questionMapper.selectById(a.getQuestionId());
            if (q == null) continue;
            ExamResultVO.QuestionResult r = new ExamResultVO.QuestionResult();
            // 交卷后结果：使用 QuestionVO 包装，options 自动解析为 List<String>
            // 此处刻意保留 answer/explanation（交卷后学生应能看对错与解析）
            r.setQuestion(QuestionVO.from(q, subjectNameMap.get(q.getSubjectId())));
            r.setUserAnswer(a.getUserAnswer());
            r.setIsCorrect(a.getIsCorrect());
            r.setScore(a.getScore());
            results.add(r);
        }
        vo.setResults(results);
        return vo;
    }
}
