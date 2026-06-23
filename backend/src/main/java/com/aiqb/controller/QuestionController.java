package com.aiqb.controller;

import com.aiqb.common.BusinessException;
import com.aiqb.common.Result;
import com.aiqb.dto.QuestionDTO;
import com.aiqb.entity.Question;
import com.aiqb.service.QuestionService;
import com.aiqb.vo.QuestionVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.aiqb.security.LoginUser;

import java.util.List;

@Tag(name = "题目管理")
@RestController
@RequestMapping("/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @Operation(summary = "分页查询（带 subjectName）")
    @GetMapping
    public Result<IPage<QuestionVO>> page(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(questionService.pageVO(subjectId, type, keyword, difficulty, current, size));
    }

    @Operation(summary = "获取详情")
    @GetMapping("/{id}")
    public Result<Question> getById(@PathVariable Long id) {
        return Result.success(questionService.getById(id));
    }

    @Operation(summary = "创建题目")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody QuestionDTO dto, @AuthenticationPrincipal LoginUser user) {
        return Result.success("创建成功", questionService.create(dto, user.getUserId()));
    }

    @Operation(summary = "更新题目")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody QuestionDTO dto) {
        questionService.update(dto);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除题目（支持单 id 或多个 id）")
    @DeleteMapping
    public Result<Void> delete(@RequestParam(required = false) Long id, @RequestBody(required = false) List<Long> ids) {
        if (id != null) questionService.delete(List.of(id));
        else if (ids != null && !ids.isEmpty()) questionService.delete(ids);
        else throw BusinessException.badRequest("缺少 id 参数");
        return Result.success("删除成功", null);
    }

    @Operation(summary = "随机抽题")
    @GetMapping("/random")
    public Result<List<Question>> randomPick(
            @RequestParam Long subjectId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer difficulty,
            @RequestParam(required = false) String knowledgePoint,
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(questionService.randomPick(subjectId, type, difficulty, knowledgePoint, limit));
    }

    @Operation(summary = "导入 Excel")
    @PostMapping("/import/{subjectId}")
    public Result<Integer> importExcel(
            @PathVariable Long subjectId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal LoginUser user) {
        return Result.success("导入成功", questionService.importFromExcel(file, subjectId, user.getUserId()));
    }

    @Operation(summary = "下载导入模板")
    @GetMapping("/template")
    public void template(HttpServletResponse response) {
        questionService.exportTemplate(response);
    }

    @Operation(summary = "导出题目")
    @GetMapping("/export")
    public void export(@RequestParam(required = false) Long subjectId, HttpServletResponse response) {
        questionService.exportQuestions(subjectId, response);
    }
}
