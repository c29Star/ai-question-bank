package com.aiqb.controller;

import com.aiqb.common.Result;
import com.aiqb.security.LoginUser;
import com.aiqb.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "统计分析")
@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @Operation(summary = "个人统计")
    @GetMapping("/personal")
    public Result<Map<String, Object>> personal(@AuthenticationPrincipal LoginUser user) {
        return Result.success(statsService.personal(user.getUserId()));
    }
}
