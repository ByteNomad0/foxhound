package com.kaoyan408.service;

import com.kaoyan408.common.BizException;
import com.kaoyan408.dto.ChangePasswordDTO;
import com.kaoyan408.dto.UpdateProfileDTO;
import com.kaoyan408.entity.User;
import com.kaoyan408.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public User getById(Long id) {
        User u = userMapper.selectById(id);
        if (u == null) {
            throw new BizException(404, "用户不存在");
        }
        return u;
    }

    public User updateProfile(Long id, UpdateProfileDTO dto) {
        User u = getById(id);
        if (dto.getNickname() != null) {
            u.setNickname(dto.getNickname());
        }
        if (dto.getEmail() != null) {
            u.setEmail(dto.getEmail());
        }
        if (dto.getAvatar() != null) {
            u.setAvatar(dto.getAvatar());
        }
        userMapper.updateById(u);
        return u;
    }

    public void changePassword(Long id, ChangePasswordDTO dto) {
        User u = getById(id);
        if (!passwordEncoder.matches(dto.getOldPassword(), u.getPassword())) {
            throw new BizException(400, "原密码不正确");
        }
        u.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(u);
    }
}
