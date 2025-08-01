package com.example.demo.config;

import com.example.demo.entity.DatabaseType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.util.SqlVisitor;

import java.util.*;

@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class ConversionConfig {
    /**
     * todo: 添加ANSI SQL内置函数与关键字
     */

    // ANSI SQL内置函数
    private Set<String> builtInFunctions = Set.of(
            "AVG", "SUM", "COUNT", "MAX", "MIN", "DISTINCT"
    );
    // ANSI SQL内置关键字
    private Set<String> buildInKeyWords = Set.of(
            "SELECT", "FROM", "WHERE", "AND", "OR", "JOIN","GROUP BY","ORDER BY", "HAVING", "AS", "ASC", "DESC",
            "DISTINCT", "INNER JOIN", "LEFT JOIN", "RIGHT JOIN", "FULL JOIN", "OUTER JOIN", "ON"
    );
    // 数据库发行版
    private DatabaseType databaseType;
    // 外部函数
    private Set<String> externalFunctions;
    // 外部关键字
    private Set<String> externalKeywords;
    // 标识符 引号 字符
    private String identifierQuote;
    // 是否大小写敏感
    private boolean caseSensitive;

    public ConversionConfig(DatabaseType databaseType, Set<String> externalFunctions, Set<String> externalKeywords, String identifierQuote, boolean caseSensitive) {
        this.databaseType = databaseType;
        this.externalFunctions = externalFunctions;
        this.externalKeywords = externalKeywords;
        this.identifierQuote = identifierQuote;
        this.caseSensitive = caseSensitive;
    }

    // 增加自定义函数
    public ConversionConfig addFunctions(Set<String> names){
        this.externalFunctions.addAll(names);
        return this;
    }
    // 增加自定义关键字
    public ConversionConfig addKeywords(Set<String> names){
        this.externalKeywords.addAll(names);
        return this;
    }






}
