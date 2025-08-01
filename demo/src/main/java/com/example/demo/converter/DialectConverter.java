package com.example.demo.converter;

import com.example.demo.config.ConversionConfig;
import com.example.demo.config.DialectConfigManager;
import com.example.demo.coreFunction.DialectConversionRewriter;
import com.example.demo.entity.DatabaseType;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.dialect.MysqlSqlDialect;
import org.apache.calcite.sql.dialect.OracleSqlDialect;
import org.apache.calcite.sql.dialect.PostgresqlSqlDialect;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.pretty.SqlPrettyWriter;
import org.apache.calcite.sql.util.SqlVisitor;
import org.apache.calcite.sql.validate.SqlConformanceEnum;
import org.springframework.stereotype.Component;


import java.util.Map;
@Slf4j
@Component
public class DialectConverter {




    public String convert(String sql, DatabaseType sourceType, DatabaseType targetType) throws Exception {
        SqlNode sqlNode = parseSql(sql);
        return convert(sqlNode, sourceType, targetType, DialectConfigManager.getDialectConfig(sourceType), DialectConfigManager.getDialectConfig(targetType));

    }

    private SqlNode parseSql(String sql){
        SqlParser.Config config = SqlParser.config()
                .withLex(Lex.JAVA);

        try{
            SqlNode tmp = SqlParser.create(sql, config).parseStmt();
            return tmp;
        }catch (SqlParseException e){
            e.printStackTrace();
            return null;
        }
    }




    private static String convert(SqlNode sqlNode, DatabaseType source, DatabaseType target, ConversionConfig sourceConfig, ConversionConfig targetConfig) {


        // TODO：2.自定义重写器


        // 3. 应用方言转换重写器
        DialectConversionRewriter dialectRewriter = new DialectConversionRewriter(source, target, sourceConfig, targetConfig);
        sqlNode = sqlNode.accept(dialectRewriter);


        // 4. 生成目标SQL
        SqlPrettyWriter writer = new SqlPrettyWriter(SqlPrettyWriter.config().withDialect(target.getDialect()).withQuoteAllIdentifiers(false));

        if (sqlNode != null) {
            sqlNode.unparse(writer, 0, 0);
        }

        return writer.toString();
    }
}

