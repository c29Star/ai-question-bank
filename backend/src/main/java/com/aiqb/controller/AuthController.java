package com.aiqb.controller;

import com.aiqb.common.Result;
import com.aiqb.dto.LoginDTO;
import com.aiqb.dto.RegisterDTO;
import com.aiqb.entity.User;
import com.aiqb.service.AuthService;
import com.aiqb.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO dto) {
        return Result.success("注册成功", authService.register(dto));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success("登录成功", authService.login(dto));
    }

    @Operation(summary = "获取当前登录用户")
    @GetMapping("/me")
    public Result<User> me() {
        return Result.success(authService.getCurrentUser());
    }
}
