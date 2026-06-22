package com.aiqb.controller;

import com.aiqb.common.Result;
import com.aiqb.entity.Question;
import com.aiqb.security.LoginUser;
import com.aiqb.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AI 增强")
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "AI 生成题目解析")
    @PostMapping("/explain/{questionId}")
    public Result<String> explain(@PathVariable Long questionId, @AuthenticationPrincipal LoginUser user) {
        return Result.success(aiService.explainQuestion(questionId, user.getUserId()));
    }

    @Operation(summary = "AI 推同类题（基于错题）")
    @GetMapping("/recommend")
    public Result<List<Question>> recommend(
            @RequestParam Long wrongQuestionId,
            @RequestParam(defaultValue = "3") Integer count,
            @AuthenticationPrincipal LoginUser user) {
        return Result.success(aiService.recommendSimilar(wrongQuestionId, count, user.getUserId()));
    }
}
