package com.example.demo.config;

import com.example.demo.entity.DatabaseType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
@Component
public class DialectConfigManager {

    private static final Map<DatabaseType, ConversionConfig> DIALECT_CONFIGS = new HashMap<>();

    static {
        // MySQL 配置
        DIALECT_CONFIGS.put(DatabaseType.MYSQL, new ConversionConfig(
                DatabaseType.MYSQL,
                Set.of("IFNULL", "CONCAT", "NOW", "LENGTH", "SUBSTRING", "COALESCE"),
                Set.of(),
                "`",  // MySQL 使用反引号
                false // MySQL 默认不区分大小写
        ));

        // PostgreSQL 配置
        DIALECT_CONFIGS.put(DatabaseType.POSTGRESQL, new ConversionConfig(
                DatabaseType.POSTGRESQL,
                Set.of("COALESCE", "CONCAT", "NOW", "LENGTH", "SUBSTRING"),
                Set.of(),
                "\"", // PostgreSQL 使用双引号
                true  // PostgreSQL 区分大小写
        ));

        // Oracle 配置
        DIALECT_CONFIGS.put(DatabaseType.ORACLE, new ConversionConfig(
                DatabaseType.ORACLE,
                Set.of("NVL", "CONCAT", "SYSDATE", "LENGTH", "SUBSTR", "COALESCE"),
                Set.of(),
                "\"", // Oracle 使用双引号
                true  // Oracle 区分大小写
        ));

        // SQL Server 配置
        DIALECT_CONFIGS.put(DatabaseType.SQLSERVER, new ConversionConfig(
                DatabaseType.SQLSERVER,
                Set.of("ISNULL", "CONCAT", "GETDATE", "LEN", "SUBSTRING", "COALESCE"),
                Set.of(),
                "\"", // SQL Server 使用双引号
                false // SQL Server 默认不区分大小写
        ));
    }

    public static ConversionConfig getDialectConfig(DatabaseType databaseType) throws Exception {
        if (DIALECT_CONFIGS.containsKey(databaseType)){
            return DIALECT_CONFIGS.get(databaseType);
        }else {
            throw new Exception("未找到数据库类型");
        }
    }

    public static void registerConfig(DatabaseType databaseType, ConversionConfig config) {
        DIALECT_CONFIGS.put(databaseType, config);
    }

    private static ConversionConfig createDefaultConfig(DatabaseType databaseType) {
        return new ConversionConfig(
                databaseType,
                new HashSet<>(),
                new HashSet<>(),
                "\"",
                false
        );
    }
}
