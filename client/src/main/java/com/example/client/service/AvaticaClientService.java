package com.example.client.service;

import com.example.client.entity.User;
import com.example.client.entity.UserPg;
import com.example.client.mapper.UserMapper;
import com.example.client.monitor.HashCodeBasedConnectionTracker;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;


import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class AvaticaClientService  {
    
    private final UserMapper userMapper;
    private final ThreadPoolTaskExecutor avaticaTaskExecutor;
    private final DataSource dataSource;
    private final HashCodeBasedConnectionTracker connectionTracker;

    public AvaticaClientService(UserMapper userMapper, ThreadPoolTaskExecutor avaticaTaskExecutor, DataSource dataSource, HashCodeBasedConnectionTracker connectionTracker) {
        this.userMapper = userMapper;
        this.avaticaTaskExecutor = avaticaTaskExecutor;
        this.dataSource = dataSource;
        this.connectionTracker = connectionTracker;
    }


    public List<User> executeQueryUser(String userName, String type) {
        return userMapper.getUserByName(userName, type);

        //        log.info("executeQueryUser: {}-{}", userName, type);
//        return CompletableFuture.supplyAsync(() -> {
//            try (Connection connection = dataSource.getConnection();){
//                connectionTracker.logConnectionWithHashCode(connection, "executeQueryUser");
//
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//            return userMapper.getUserByName(userName, type);
//        }, avaticaTaskExecutor);

    }

    public List<UserPg> executeQueryUserPg(String userName, String type) {
        return userMapper.getUserByNamePg(userName, type);
    }

    public CompletableFuture<Object> executeSql(String sql) {
        return CompletableFuture.supplyAsync(() -> {
            String trimmedSql = sql.trim().toUpperCase();

            try(Connection connection = dataSource.getConnection();){
                connectionTracker.logConnectionWithHashCode(connection, "executeQueryUser");
                if (trimmedSql.startsWith("SELECT")) {
                    return userMapper.executeSelectSql(sql);
                } else if (trimmedSql.startsWith("INSERT")) {
                    return userMapper.executeInsertSql(sql);
                } else if (trimmedSql.startsWith("UPDATE")) {
                    return userMapper.executeUpdateSql(sql);
                } else if (trimmedSql.startsWith("DELETE")) {
                    return userMapper.executeDeleteSql(sql);
                } else {
                    throw new IllegalArgumentException("不支持的SQL类型: " + sql);
                }
            }catch (Exception e){
                log.error("executeSql error: {}", e.getMessage());
            }
            return null;
        }, avaticaTaskExecutor);
    }


    public String getPoolStatus() {

        return dataSource.getClass().getName();
    }
}
