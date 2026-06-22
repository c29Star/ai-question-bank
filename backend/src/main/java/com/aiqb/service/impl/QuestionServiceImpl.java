package com.aiqb.service.impl;

import com.aiqb.common.BusinessException;
import com.aiqb.dto.QuestionDTO;
import com.aiqb.entity.Question;
import com.aiqb.mapper.QuestionMapper;
import com.aiqb.service.QuestionService;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements QuestionService {

    private final QuestionMapper questionMapper;

    @Override
    public IPage<Question> page(Long subjectId, String type, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
        if (subjectId != null) wrapper.eq(Question::getSubjectId, subjectId);
        if (type != null && !type.isEmpty()) wrapper.eq(Question::getType, type);
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Question::getContent, keyword)
                    .or().like(Question::getKnowledgePoint, keyword));
        }
        wrapper.orderByDesc(Question::getId);
        return questionMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Question getById(Long id) {
        Question q = questionMapper.selectById(id);
        if (q == null) throw BusinessException.notFound("题目不存在");
        return q;
    }

    @Override
    public Long create(QuestionDTO dto, Long userId) {
        Question q = new Question();
        copyDtoToEntity(dto, q);
        q.setCreatedBy(userId);
        questionMapper.insert(q);
        return q.getId();
    }

    @Override
    public void update(QuestionDTO dto) {
        if (dto.getId() == null) throw BusinessException.badRequest("ID 不能为空");
        Question q = new Question();
        copyDtoToEntity(dto, q);
        q.setId(dto.getId());
        int rows = questionMapper.updateById(q);
        if (rows == 0) throw BusinessException.notFound("题目不存在");
    }

    @Override
    @Transactional
    public void delete(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        questionMapper.deleteByIds(ids);
    }

    @Override
    public List<Question> randomPick(Long subjectId, String type, Integer difficulty, String knowledgePoint, Integer limit) {
        return questionMapper.randomPick(subjectId, type, difficulty, knowledgePoint, limit);
    }

    @Override
    @Transactional
    public int importFromExcel(MultipartFile file, Long subjectId, Long userId) {
        try {
            List<QuestionRow> rows = EasyExcel.read(file.getInputStream())
                    .head(QuestionRow.class)
                    .sheet()
                    .doReadSync();
            int count = 0;
            for (QuestionRow row : rows) {
                if (row.getContent() == null || row.getContent().isEmpty()) continue;
                Question q = new Question();
                q.setSubjectId(subjectId);
                q.setType(row.getType() == null ? "SINGLE" : row.getType().toUpperCase());
                q.setDifficulty(row.getDifficulty() == null ? 1 : row.getDifficulty());
                q.setContent(row.getContent());
                q.setOptions(row.getOptions());
                q.setAnswer(row.getAnswer());
                q.setExplanation(row.getExplanation());
                q.setKnowledgePoint(row.getKnowledgePoint());
                q.setCreatedBy(userId);
                questionMapper.insert(q);
                count++;
            }
            return count;
        } catch (IOException e) {
            log.error("导入题目失败", e);
            throw BusinessException.badRequest("文件读取失败: " + e.getMessage());
        }
    }

    @Override
    public void exportTemplate(HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("题目导入模板", StandardCharsets.UTF_8).replace("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), QuestionRow.class)
                    .sheet("题目")
                    .doWrite(java.util.Collections.emptyList());
        } catch (IOException e) {
            throw BusinessException.error("导出模板失败");
        }
    }

    @Override
    public void exportQuestions(Long subjectId, HttpServletResponse response) {
        try {
            LambdaQueryWrapper<Question> wrapper = new LambdaQueryWrapper<>();
            if (subjectId != null) wrapper.eq(Question::getSubjectId, subjectId);
            List<Question> list = questionMapper.selectList(wrapper);
            // 转换为导出 row
            List<QuestionRow> rows = list.stream().map(q -> {
                QuestionRow r = new QuestionRow();
                r.setType(q.getType());
                r.setDifficulty(q.getDifficulty());
                r.setContent(q.getContent());
                r.setOptions(q.getOptions());
                r.setAnswer(q.getAnswer());
                r.setExplanation(q.getExplanation());
                r.setKnowledgePoint(q.getKnowledgePoint());
                return r;
            }).toList();
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("题目列表", StandardCharsets.UTF_8).replace("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            EasyExcel.write(response.getOutputStream(), QuestionRow.class)
                    .sheet("题目")
                    .doWrite(rows);
        } catch (IOException e) {
            throw BusinessException.error("导出失败");
        }
    }

    private void copyDtoToEntity(QuestionDTO dto, Question q) {
        q.setSubjectId(dto.getSubjectId());
        q.setType(dto.getType());
        q.setDifficulty(dto.getDifficulty());
        q.setContent(dto.getContent());
        q.setOptions(dto.getOptions());
        q.setAnswer(dto.getAnswer());
        q.setExplanation(dto.getExplanation());
        q.setKnowledgePoint(dto.getKnowledgePoint());
    }
}
