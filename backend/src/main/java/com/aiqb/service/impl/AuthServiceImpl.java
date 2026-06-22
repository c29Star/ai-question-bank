package com.aiqb.service.impl;

import com.aiqb.common.BusinessException;
import com.aiqb.dto.LoginDTO;
import com.aiqb.dto.RegisterDTO;
import com.aiqb.entity.User;
import com.aiqb.mapper.UserMapper;
import com.aiqb.security.JwtUtil;
import com.aiqb.security.LoginUser;
import com.aiqb.service.AuthService;
import com.aiqb.vo.LoginVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO register(RegisterDTO dto) {
        // 校验用户名重复
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (count > 0) {
            throw BusinessException.badRequest("用户名已被使用");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setNickname(dto.getNickname() != null ? dto.getNickname() : dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole() == null || dto.getRole().isEmpty() ? "STUDENT" : dto.getRole());
        user.setEnabled(true);
        userMapper.insert(user);

        return buildLoginVO(user);
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        User user = userMapper.selectByUsername(dto.getUsername());
        if (user == null) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw BusinessException.unauthorized("用户名或密码错误");
        }
        if (!user.getEnabled()) {
            throw BusinessException.forbidden("账号已被禁用");
        }
        return buildLoginVO(user);
    }

    @Override
    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw BusinessException.unauthorized("未登录");
        }
        Object principal = auth.getPrincipal();
        if (principal instanceof LoginUser lu) {
            User u = userMapper.selectById(lu.getUserId());
            if (u != null) {
                u.setPassword(null);
            }
            return u;
        }
        throw BusinessException.unauthorized("登录信息无效");
    }

    private LoginVO buildLoginVO(User user) {
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        user.setPassword(null);
        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUser(user);
        return vo;
    }
}
