package com.example.demo.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.avatica.jdbc.JdbcMeta;
import org.apache.calcite.avatica.remote.LocalService;
import org.apache.calcite.avatica.server.*;
import org.apache.calcite.avatica.util.Quoting;
import org.apache.calcite.config.CalciteConnectionProperty;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.apache.calcite.schema.impl.AbstractSchema;
import org.apache.calcite.schema.impl.ViewTable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
@ConfigurationProperties(prefix = "avatica")
public class CalciteDataSourceConfig {
    @Value("${calcite.datasource-file}")
    private String datasourceFile;

    @Value("${calcite.server.port:8765}")
    private int serverPort;

    @Value("${calcite.server.enabled:true}")
    private boolean serverEnabled;

    @Value("${calcite.server.serialization:JSON}")  // JSON or PROTOBUF
    private String serialization;

    // 线程池配置属性
    @Value("${avatica.thread-pool.core-size:5}")
    private int corePoolSize;

    @Value("${avatica.thread-pool.max-size:10}")
    private int maxPoolSize;

    @Value("${avatica.thread-pool.queue-capacity:100}")
    private int queueCapacity;

    @Value("${avatica.thread-pool.thread-name-prefix:avatica-server-}")
    private String threadNamePrefix;

    private HttpServer avaticaServer;
    org.apache.calcite.avatica.Meta meta;
    private ExecutorService serverExecutorService;

    @Bean("calciteDataSource")
    @ConditionalOnMissingBean(name = "calciteDataSource")
    public HikariDataSource calciteDataSource() {
        HikariConfig config = new HikariConfig();
        Properties info = new Properties();
        info.setProperty("lex","JAVA");
        info.setProperty("caseSensitive","false");
        info.setProperty(CalciteConnectionProperty.QUOTING.camelName(), Quoting.BACK_TICK.name());
        info.setProperty("model", datasourceFile);

        config.setJdbcUrl("jdbc:calcite:");
        config.setDataSourceProperties(info);
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(1);
        config.setPoolName("calcite-client-thread-pool");

        HikariDataSource ds = new HikariDataSource(config);

        log.info("创建Calcite线程池成功");
        return ds;
    }

    @Bean("avaticaTaskExecutor")
    public ThreadPoolTaskExecutor avaticaTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setThreadNamePrefix(threadNamePrefix);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

    @Bean("avaticaServerExecutor")
    public ExecutorService avaticaServerExecutor() {
        serverExecutorService = new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(queueCapacity),
                new ThreadFactory() {
                    private final AtomicInteger threadNumber = new AtomicInteger(1);
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, threadNamePrefix + threadNumber.getAndIncrement());
                        t.setDaemon(false);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );

        log.info("Avatica server thread pool created: coreSize={}, maxSize={}",
                corePoolSize, maxPoolSize);
        return serverExecutorService;
    }

    @Bean
    @ConditionalOnProperty(name = "calcite.server.enabled", havingValue = "true", matchIfMissing = true)
    public HttpServer avaticaServer(DataSource dataSource) throws Exception {
        if (!serverEnabled){
            return null;
        }

        configureUnderlyingServer();
        AvaticaHandler handler = getAvaticaHandler();

        avaticaServer = new HttpServer(serverPort, handler);
        avaticaServer.start();

        log.info("Avatica server started on port {} with {} serialization using thread pool",
                serverPort, serialization);
        return avaticaServer;
    }

    private void configureUnderlyingServer() {
        System.setProperty("org.eclipse.jetty.server.executor.maxThreads",
                String.valueOf(maxPoolSize));
        System.setProperty("org.eclipse.jetty.server.executor.minThreads",
                String.valueOf(corePoolSize));
        System.setProperty("org.eclipse.jetty.server.executor.idleTimeout", "60000");

        // 配置 Jetty 其他参数
        System.setProperty("org.eclipse.jetty.server.Request.maxFormContentSize", "1000000");
        System.setProperty("org.eclipse.jetty.server.Request.maxFormKeys", "1000");

        log.info("Configured underlying server thread pool: maxThreads={}, minThreads={}",
                maxPoolSize, corePoolSize);
    }

    private AvaticaHandler getAvaticaHandler() throws SQLException {
        if (meta == null){
            synchronized ( this){
                Properties info = new Properties();
                info.setProperty("lex", "JAVA");
                info.setProperty("caseSensitive", "false");
                info.setProperty(CalciteConnectionProperty.QUOTING.camelName(), Quoting.BACK_TICK.name());
                info.setProperty("model", datasourceFile);
                meta = new JdbcMeta("jdbc:calcite:", info);
                log.info("Avatica meta created with model: {}", datasourceFile);
            }

        }

        LocalService service = new LocalService(meta);

        AvaticaHandler handler;
        if ("PROTOBUF".equalsIgnoreCase(serialization)) {
            handler = new AvaticaProtobufHandler(service);
        } else {
            handler = new AvaticaJsonHandler(service);
        }
        return handler;
    }

    @PreDestroy
    public void stopServer() {
        if (avaticaServer != null) {
            avaticaServer.stop();
            log.info("Avatica server stopped");
        }
    }

}

