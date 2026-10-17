package com.kaoyan408.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaoyan408.common.BizException;
import com.kaoyan408.dto.LoginDTO;
import com.kaoyan408.dto.RegisterDTO;
import com.kaoyan408.entity.User;
import com.kaoyan408.mapper.UserMapper;
import com.kaoyan408.security.JwtUtil;
import com.kaoyan408.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public User register(RegisterDTO dto) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (count != null && count > 0) {
            throw new BizException(400, "用户名已存在");
        }
        User u = new User();
        u.setUsername(dto.getUsername());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setNickname(dto.getNickname() == null || dto.getNickname().isBlank() ? dto.getUsername() : dto.getNickname());
        u.setEmail(dto.getEmail());
        u.setRole("USER");
        u.setStatus(1);
        userMapper.insert(u);
        return u;
    }

    public LoginVO login(LoginDTO dto) {
        User u = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, dto.getUsername()));
        if (u == null || !passwordEncoder.matches(dto.getPassword(), u.getPassword())) {
            throw new BizException(400, "用户名或密码错误");
        }
        if (u.getStatus() == null || u.getStatus() != 1) {
            throw new BizException(403, "账号已被禁用，请联系管理员");
        }
        LoginVO vo = new LoginVO();
        vo.setToken(jwtUtil.generateToken(u.getId(), u.getUsername(), u.getRole()));
        vo.setUser(u);
        return vo;
    }
}
