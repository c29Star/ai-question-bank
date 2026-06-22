package com.aiqb.service;

import com.aiqb.dto.QuestionDTO;
import com.aiqb.entity.Question;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

public interface QuestionService {

    IPage<Question> page(Long subjectId, String type, String keyword, Integer pageNum, Integer pageSize);

    Question getById(Long id);

    Long create(QuestionDTO dto, Long userId);

    void update(QuestionDTO dto);

    void delete(List<Long> ids);

    /** 随机抽题 */
    List<Question> randomPick(Long subjectId, String type, Integer difficulty, String knowledgePoint, Integer limit);

    /** 批量导入（Excel） */
    int importFromExcel(MultipartFile file, Long subjectId, Long userId);

    /** 导出 Excel 模板 */
    void exportTemplate(HttpServletResponse response);

    /** 导出题目 */
    void exportQuestions(Long subjectId, HttpServletResponse response);
}
