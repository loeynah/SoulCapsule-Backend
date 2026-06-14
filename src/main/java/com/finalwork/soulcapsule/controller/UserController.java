package com.finalwork.soulcapsule.controller;

import com.finalwork.soulcapsule.common.ApiResult;
import com.finalwork.soulcapsule.dto.LoginResponse;
import com.finalwork.soulcapsule.dto.UserRequest;
import com.finalwork.soulcapsule.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResult<Void> register(@RequestBody UserRequest request) {
        userService.register(request);
        return ApiResult.success("注册成功", null);
    }

    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@RequestBody UserRequest request) {
        LoginResponse response = userService.login(request);
        return ApiResult.success("登录成功", response);
    }
}
