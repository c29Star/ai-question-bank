package com.aiqb.service;

import com.aiqb.dto.LoginDTO;
import com.aiqb.dto.RegisterDTO;
import com.aiqb.entity.User;
import com.aiqb.vo.LoginVO;

public interface AuthService {

    LoginVO register(RegisterDTO dto);

    LoginVO login(LoginDTO dto);

    User getCurrentUser();
}
