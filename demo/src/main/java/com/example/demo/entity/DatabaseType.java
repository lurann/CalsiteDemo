package com.example.demo.entity;

import lombok.Getter;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.dialect.*;

@Getter
public enum DatabaseType {
    MYSQL(MysqlSqlDialect.DEFAULT, "mysql"),
    POSTGRESQL(PostgresqlSqlDialect.DEFAULT, "postgresql"),
    ORACLE(OracleSqlDialect.DEFAULT, "oracle"),
    SQLSERVER(MssqlSqlDialect.DEFAULT, "mssql"),
    H2(H2SqlDialect.DEFAULT, "h2"),
    ANSI(AnsiSqlDialect.DEFAULT, "ansisql");



    private final org.apache.calcite.sql.SqlDialect dialect;
    private final String displayName;

    DatabaseType(SqlDialect dialect, String displayName) {
        this.dialect = dialect;
        this.displayName = displayName;
    }
    public static DatabaseType getByName(String name) {
        for (DatabaseType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

}
