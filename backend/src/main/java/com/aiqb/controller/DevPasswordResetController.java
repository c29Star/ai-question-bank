package com.aiqb.controller;

import com.aiqb.common.Result;
import com.aiqb.entity.User;
import com.aiqb.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 临时密码重置工具（开发用）
 * 仅用于修复默认账号密码；生产环境请删除此 Controller。
 */
@Slf4j
@Tag(name = "开发工具")
@RestController
@RequestMapping("/dev")
@RequiredArgsConstructor
public class DevPasswordResetController {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Operation(summary = "一键重置所有默认账号密码为 admin123")
    @PostMapping("/reset-default-passwords")
    public Result<String> resetDefaultPasswords() {
        String defaultPwd = "admin123";
        String hash = passwordEncoder.encode(defaultPwd);
        List<User> users = userMapper.selectList(
            new LambdaQueryWrapper<User>().in(User::getUsername, List.of("admin", "teacher1", "student1", "teacher2", "student2"))
        );
        int n = 0;
        for (User u : users) {
            u.setPassword(hash);
            userMapper.updateById(u);
            n++;
        }
        log.warn("[DEV] 重置了 {} 个默认用户密码为 admin123", n);
        return Result.success("已重置 " + n + " 个用户密码为 admin123（hash: " + hash.substring(0, 20) + "...）");
    }

    @Operation(summary = "重置指定用户的密码")
    @PostMapping("/reset-password")
    public Result<String> resetPassword(@RequestParam String username, @RequestParam String newPassword) {
        String hash = passwordEncoder.encode(newPassword);
        User u = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (u == null) return Result.error(404, "用户不存在: " + username);
        u.setPassword(hash);
        userMapper.updateById(u);
        log.warn("[DEV] 重置用户 {} 的密码", username);
        return Result.success("用户 " + username + " 密码已重置（hash: " + hash.substring(0, 20) + "...）");
    }
}
