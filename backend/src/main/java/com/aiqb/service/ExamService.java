package com.aiqb.service;

import com.aiqb.dto.ExamDTO;
import com.aiqb.dto.SubmitAnswerDTO;
import com.aiqb.entity.Exam;
import com.aiqb.vo.ExamResultVO;
import com.aiqb.vo.ExamStartVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;
import java.util.Map;

public interface ExamService {

    /** 教师：创建/编辑考试 */
    Long save(ExamDTO dto, Long userId);

    void update(ExamDTO dto);

    void delete(Long id);

    void publish(Long id);

    void archive(Long id);

    /** 教师/管理员：列出所有考试 */
    IPage<Exam> page(Integer pageNum, Integer pageSize, String status);

    /** 学生：可参加的考试 */
    List<Map<String, Object>> available(Long userId);

    /** 学生：开始考试（创建 record） */
    ExamStartVO start(Long examId, Long userId);

    /** 学生：保存单题答案（断点续答） */
    void saveAnswer(Long recordId, Long questionId, String userAnswer, Integer timeSpent);

    /** 学生：提交整场考试 */
    ExamResultVO submit(SubmitAnswerDTO dto, Long userId);

    /** 学生：历史记录 */
    List<Map<String, Object>> myRecords(Long userId);
}
