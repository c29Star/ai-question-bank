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

    Question getById(Long id);

    Long create(QuestionDTO dto, Long userId);

    void update(QuestionDTO dto);

    void delete(List<Long> ids);

    /** 随机抽题（兼容旧调用） */
    default List<Question> randomPick(Long subjectId, String type, Integer difficulty, String knowledgePoint, Integer limit) {
        return randomPick(subjectId, type, difficulty, knowledgePoint, limit, null);
    }

    /** 随机抽题（支持排除指定 id） */
    List<Question> randomPick(Long subjectId, String type, Integer difficulty, String knowledgePoint, Integer limit, Long excludeId);

    /** 批量导入（Excel） */
    int importFromExcel(MultipartFile file, Long subjectId, Long userId);

    /** 导出 Excel 模板 */
    void exportTemplate(HttpServletResponse response);

    /** 导出题目 */
    void exportQuestions(Long subjectId, HttpServletResponse response);
}
