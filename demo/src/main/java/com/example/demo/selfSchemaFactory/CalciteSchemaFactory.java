package com.example.demo.selfSchemaFactory;

import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.adapter.jdbc.JdbcSchema;
import org.apache.calcite.schema.Schema;
import org.apache.calcite.schema.SchemaFactory;
import org.apache.calcite.schema.SchemaPlus;

import javax.sql.DataSource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class CalciteSchemaFactory implements SchemaFactory {

    private static final ConcurrentHashMap<String, Schema> schemaCache = new ConcurrentHashMap<>();    @Override

    public Schema create(SchemaPlus parentSchema, String name, Map<String, Object> operand) {

        String cacheKey = generateCacheKey(operand);

        return schemaCache.computeIfAbsent(cacheKey, key ->{
            try{
                DataSource dataSource = createDataSource(operand);

                // 从配置中获取 catalog 和 schema 参数
                String catalog = (String) operand.get("catalog");
                String schemaName = (String) operand.get("schema");

                Schema schema = JdbcSchema.create(parentSchema, name, dataSource, catalog, schemaName);

                String threadPoolName = (String) operand.getOrDefault("poolName", "calcite-pool");
                log.info("创建Calcite的{}连接池成功: catalog={}, schema={}", threadPoolName, catalog, schemaName);

                return schema;
            }catch(Exception e){
                throw new RuntimeException("创建线程池失败", e);
            }
        });
    }

    private DataSource createDataSource(Map<String, Object> operand) {
        com.zaxxer.hikari.HikariConfig config = new com.zaxxer.hikari.HikariConfig();

        // 必填参数
        String jdbcUrl = (String) operand.get("jdbcUrl");
        String jdbcUser = (String) operand.get("jdbcUser");
        String jdbcPassword = (String) operand.get("jdbcPassword");
        String jdbcDriver = (String) operand.get("jdbcDriver");

        if (jdbcUrl == null || jdbcUser == null || jdbcPassword == null || jdbcDriver == null){
            throw new IllegalArgumentException("jdbcUrl, jdbcUser, jdbcPassword, jdbcDriver 参数不能为空");
        }

        config.setJdbcUrl(jdbcUrl);
        config.setUsername(jdbcUser);
        config.setPassword(jdbcPassword);
        config.setDriverClassName(jdbcDriver);

        // 非必填参数
        config.setPoolName((String) operand.getOrDefault("poolName", "calcite-pool"));
        config.setMaximumPoolSize(getIntProperty(operand, "maximumPoolSize", 10));
        config.setMinimumIdle(getIntProperty(operand, "minimumIdle", 2));
        config.setConnectionTimeout(getLongProperty(operand, "connectionTimeout", 30000L));
        config.setIdleTimeout(getLongProperty(operand, "idleTimeout", 600000L));
        config.setMaxLifetime(getLongProperty(operand, "maxLifetime", 1800000L));
        config.setLeakDetectionThreshold(getLongProperty(operand, "leakDetectionThreshold", 60000L));

        config.setRegisterMbeans(true);
        config.setAllowPoolSuspension(true);

        return new com.zaxxer.hikari.HikariDataSource(config);
    }

    private String generateCacheKey(Map<String, Object> operand) {
        return operand.get("jdbcUrl") + "|" +
                operand.get("jdbcUser") + "|" +
                operand.get("catalog") + "|" +
                operand.get("schema");
    }

    private int getIntProperty(Map<String, Object> operand, String key, int defaultValue) {
        Object value = operand.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    private long getLongProperty(Map<String, Object> operand, String key, long defaultValue) {
        Object value = operand.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}