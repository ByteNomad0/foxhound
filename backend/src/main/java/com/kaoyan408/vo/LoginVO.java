package com.kaoyan408.vo;

import com.kaoyan408.entity.User;
import lombok.Data;

@Data
public class LoginVO {

    private String token;

    private User user;
}
