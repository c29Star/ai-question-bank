package com.aiqb.controller;

import com.aiqb.common.Result;
import com.aiqb.dto.ExamDTO;
import com.aiqb.dto.SubmitAnswerDTO;
import com.aiqb.entity.Exam;
import com.aiqb.security.LoginUser;
import com.aiqb.service.ExamService;
import com.aiqb.vo.ExamResultVO;
import com.aiqb.vo.ExamStartVO;
import com.aiqb.vo.ExamVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "考试管理")
@RestController
@RequestMapping("/exams")
@RequiredArgsConstructor
public class ExamController {

    private final ExamService examService;

    @Operation(summary = "分页查询（教师/管理员）")
    @GetMapping
    public Result<IPage<ExamVO>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.success(examService.page(current, size, status));
    }

    @Operation(summary = "创建考试（草稿）")
    @PostMapping
    public Result<Long> save(@Valid @RequestBody ExamDTO dto, @AuthenticationPrincipal LoginUser user) {
        return Result.success("创建成功", examService.save(dto, user.getUserId()));
    }

    @Operation(summary = "更新考试")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody ExamDTO dto) {
        examService.update(dto);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除考试")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        examService.delete(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "发布考试")
    @PutMapping("/{id}/publish")
    public Result<Void> publish(@PathVariable Long id) {
        examService.publish(id);
        return Result.success("已发布", null);
    }

    @Operation(summary = "归档考试")
    @PutMapping("/{id}/archive")
    public Result<Void> archive(@PathVariable Long id) {
        examService.archive(id);
        return Result.success("已归档", null);
    }

    @Operation(summary = "学生：可参加考试")
    @GetMapping("/available")
    public Result<List<Map<String, Object>>> available(@AuthenticationPrincipal LoginUser user) {
        return Result.success(examService.available(user.getUserId()));
    }

    @Operation(summary = "学生：开始/继续考试")
    @PostMapping("/{id}/start")
    public Result<ExamStartVO> start(@PathVariable Long id, @AuthenticationPrincipal LoginUser user) {
        return Result.success(examService.start(id, user.getUserId()));
    }

    @Operation(summary = "学生：保存单题答案")
    @PostMapping("/save-answer")
    public Result<Void> saveAnswer(@RequestParam Long recordId,
                                   @RequestParam Long questionId,
                                   @RequestParam String userAnswer,
                                   @RequestParam(required = false) Integer timeSpent) {
        examService.saveAnswer(recordId, questionId, userAnswer, timeSpent);
        return Result.success("已保存", null);
    }

    @Operation(summary = "学生：提交考试")
    @PostMapping("/submit")
    public Result<ExamResultVO> submit(@RequestBody SubmitAnswerDTO dto, @AuthenticationPrincipal LoginUser user) {
        return Result.success("提交成功", examService.submit(dto, user.getUserId()));
    }

    @Operation(summary = "学生：历史记录")
    @GetMapping("/my-records")
    public Result<List<Map<String, Object>>> myRecords(@AuthenticationPrincipal LoginUser user) {
        return Result.success(examService.myRecords(user.getUserId()));
    }

    @Operation(summary = "教师：查某场考试的所有学生答卷")
    @GetMapping("/{id}/records")
    public Result<List<Map<String, Object>>> examRecords(@PathVariable Long id) {
        return Result.success(examService.examRecords(id));
    }
}
