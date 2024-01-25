package com.example.demoredis.controller;

import com.example.demoredis.service.RedisUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
class UserController {

    private final RedisUserService redisUserService;

    public UserController(RedisUserService redisUserService) {
        this.redisUserService = redisUserService;
    }

    @PostMapping("/{userID}/{ID}")
    public void setUserData(
            @PathVariable String userID,
            @PathVariable String ID,
            @RequestBody String data
    ) {
        redisUserService.setUserData(userID, ID, data);
    }

    @GetMapping("/{userID}/{ID}")
    public String getUserData(
            @PathVariable String userID,
            @PathVariable String ID
    ) {
        return redisUserService.getUserData(userID, ID);
    }
}