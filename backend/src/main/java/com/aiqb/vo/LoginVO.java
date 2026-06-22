package com.aiqb.vo;

import com.aiqb.entity.User;
import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private User user;
}
