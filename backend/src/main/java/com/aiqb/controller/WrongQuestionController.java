package com.aiqb.controller;

import com.aiqb.common.Result;
import com.aiqb.security.LoginUser;
import com.aiqb.service.WrongQuestionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "错题本")
@RestController
@RequestMapping("/wrong-questions")
@RequiredArgsConstructor
public class WrongQuestionController {

    private final WrongQuestionService wrongQuestionService;

    @Operation(summary = "我的错题")
    @GetMapping
    public Result<List<Map<String, Object>>> myWrong(
            @AuthenticationPrincipal LoginUser user,
            @RequestParam(required = false) Boolean mastered) {
        return Result.success(wrongQuestionService.myWrongQuestions(user.getUserId(), mastered));
    }

    @Operation(summary = "标记掌握")
    @PutMapping("/{id}/mastered")
    public Result<Void> markMastered(
            @PathVariable Long id,
            @RequestParam Boolean mastered,
            @AuthenticationPrincipal LoginUser user) {
        wrongQuestionService.markMastered(id, user.getUserId(), mastered);
        return Result.success("已更新", null);
    }

    @Operation(summary = "按知识点统计")
    @GetMapping("/stats")
    public Result<List<Map<String, Object>>> stats(@AuthenticationPrincipal LoginUser user) {
        return Result.success(wrongQuestionService.statsByKnowledge(user.getUserId()));
    }
}
