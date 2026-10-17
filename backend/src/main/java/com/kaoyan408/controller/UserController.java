package com.kaoyan408.controller;

import com.kaoyan408.common.Result;
import com.kaoyan408.dto.ChangePasswordDTO;
import com.kaoyan408.dto.UpdateProfileDTO;
import com.kaoyan408.entity.User;
import com.kaoyan408.security.SecurityUtil;
import com.kaoyan408.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public Result<User> me() {
        return Result.ok(userService.getById(SecurityUtil.getUserId()));
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody UpdateProfileDTO dto) {
        return Result.ok(userService.updateProfile(SecurityUtil.getUserId(), dto));
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        userService.changePassword(SecurityUtil.getUserId(), dto);
        return Result.ok();
    }
}
