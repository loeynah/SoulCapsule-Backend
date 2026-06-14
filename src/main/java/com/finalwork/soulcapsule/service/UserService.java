package com.finalwork.soulcapsule.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finalwork.soulcapsule.dto.LoginResponse;
import com.finalwork.soulcapsule.dto.UserRequest;
import com.finalwork.soulcapsule.entity.User;
import com.finalwork.soulcapsule.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public void register(UserRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username.trim());
        if (userMapper.selectCount(wrapper) > 0) {
            throw new IllegalStateException("用户名已存在");
        }

        User user = new User();
        user.setUsername(username.trim());
        user.setPassword(passwordEncoder.encode(password));
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
    }

    public LoginResponse login(UserRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username.trim());
        User user = userMapper.selectOne(wrapper);
        if (user == null) {
            throw new IllegalStateException("用户名不存在");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalStateException("密码错误");
        }

        return new LoginResponse(user.getId(), user.getUsername());
    }
}
