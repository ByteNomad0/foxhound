package com.kaoyan408.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaoyan408.entity.User;
import com.kaoyan408.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动时自动播种默认账号，保证 BCrypt 密码哈希正确。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createIfAbsent("admin", "admin123", "管理员", "admin@kaoyan408.com", "ADMIN");
        createIfAbsent("student", "student123", "小研同学", "student@kaoyan408.com", "USER");
    }

    private void createIfAbsent(String username, String rawPwd, String nickname, String email, String role) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (count != null && count > 0) {
            return;
        }
        User u = new User();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(rawPwd));
        u.setNickname(nickname);
        u.setEmail(email);
        u.setRole(role);
        u.setStatus(1);
        userMapper.insert(u);
        log.info("已创建默认账号：{} / {}", username, rawPwd);
    }
}
