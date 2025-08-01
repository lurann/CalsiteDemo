package com.example.client.service;

import com.example.client.config.DataSourceConfig;
import com.example.client.entity.User;
import com.example.client.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
@Service
public class AvaticaClientService  {
    
    private final UserMapper userMapper;
    private final ThreadPoolTaskExecutor avaticaTaskExecutor;

    public AvaticaClientService(UserMapper userMapper, ThreadPoolTaskExecutor avaticaTaskExecutor) {
        this.userMapper = userMapper;
        this.avaticaTaskExecutor = avaticaTaskExecutor;
    }


    public CompletableFuture<List<User>> executeQueryUser(String userName) {
        return CompletableFuture.supplyAsync(() -> {
            return userMapper.getUserByName(userName);
        }, avaticaTaskExecutor);
    }
}
