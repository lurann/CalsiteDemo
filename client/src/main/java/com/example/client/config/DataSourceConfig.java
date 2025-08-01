package com.example.client.config;


import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class DataSourceConfig {


    public ThreadPoolTaskExecutor avaticaClientExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("avatica-client-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }



    public HikariDataSource avaticaDataSource(RemoteAvaticaConfig remoteAvaticaConfig) {
        HikariConfig config = new HikariConfig();

        // 构建连接 URL
        String url = remoteAvaticaConfig.getUrl() +
                ";serialization=" + remoteAvaticaConfig.getSerialization();

        config.setJdbcUrl(url);
        config.setDriverClassName("org.apache.calcite.avatica.remote.Driver");

        // 连接池配置
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);     // 30秒
        config.setIdleTimeout(600000);          // 10分钟
        config.setMaxLifetime(1800000);         // 30分钟
        config.setLeakDetectionThreshold(60000); // 1分钟检测连接泄漏
        config.setPoolName("avatica-client-pool");

        // 连接测试查询
        config.setConnectionTestQuery("SELECT 1");

        // 性能优化
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");

        return new HikariDataSource(config);
    }
}