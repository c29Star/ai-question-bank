package com.aiqb.service;

import com.aiqb.dto.QuestionDTO;
import com.aiqb.entity.Question;
import com.aiqb.vo.QuestionVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

public interface QuestionService {

    IPage<Question> page(Long subjectId, String type, String keyword, Integer difficulty, Integer pageNum, Integer pageSize);

    IPage<QuestionVO> pageVO(Long subjectId, String type, String keyword, Integer difficulty, Integer current, Integer size);

    /** 兼容旧调用（默认带答案） */
    default IPage<QuestionVO> pageVO(Long subjectId, String type, String keyword, Integer difficulty, Integer current, Integer size, boolean includeAnswer) {
        // 默认实现忽略 includeAnswer（保留旧行为），子类应重写
        return pageVO(subjectId, type, keyword, difficulty, current, size);
    }

    Question getById(Long id);

    /** 题目详情，可选是否带答案 */
    default Question getById(Long id, boolean includeAnswer) {
        // 默认实现忽略 includeAnswer，子类应重写
        return getById(id);
    }

    /** 题目详情 VO 版（题目、答案分离后给学生用） */
    com.aiqb.vo.QuestionVO getVOById(Long id, boolean includeAnswer);

    Long create(QuestionDTO dto, Long userId);

    void update(QuestionDTO dto);

    void delete(List<Long> ids);

    /** 随机抽题（兼容旧调用） */
    default List<Question> randomPick(Long subjectId, String type, Integer difficulty, String knowledgePoint, Integer limit) {
        return randomPick(subjectId, type, difficulty, knowledgePoint, limit, null);
    }

    /** 随机抽题（支持排除指定 id） */
    List<Question> randomPick(Long subjectId, String type, Integer difficulty, String knowledgePoint, Integer limit, Long excludeId);

    /** 随机抽题（按是否带答案返回 VO） */
    default List<com.aiqb.vo.QuestionVO> randomPickVO(Long subjectId, String type, Integer difficulty, String knowledgePoint, Integer limit, Long excludeId, boolean includeAnswer) {
        List<Question> list = randomPick(subjectId, type, difficulty, knowledgePoint, limit, excludeId);
        return list == null ? java.util.List.of() : list.stream()
                .map(q -> includeAnswer ? com.aiqb.vo.QuestionVO.from(q, null) : com.aiqb.vo.QuestionVO.forStudent(q, null))
                .toList();
    }

    /** 批量导入（Excel） */
    int importFromExcel(MultipartFile file, Long subjectId, Long userId);

    /** 导出 Excel 模板 */
    void exportTemplate(HttpServletResponse response);

    /** 导出题目 */
    void exportQuestions(Long subjectId, HttpServletResponse response);
}
