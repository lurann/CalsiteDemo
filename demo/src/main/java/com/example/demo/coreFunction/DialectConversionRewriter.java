package com.example.demo.coreFunction;

import com.example.demo.config.ConversionConfig;
import com.example.demo.config.convertDefaultRule;
import com.example.demo.entity.DatabaseType;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParserPos;
import org.apache.calcite.sql.util.SqlShuttle;

import java.util.*;

/**
 * 方言转换重写器 - 核心转换逻辑实现
 */
public class DialectConversionRewriter extends SqlShuttle {


    private final DatabaseType sourceType;
    private final DatabaseType targetType;
    private final ConversionConfig sourceConfig;
    private final ConversionConfig targetConfig;

    public DialectConversionRewriter(DatabaseType source, DatabaseType target, ConversionConfig sourceConfig, ConversionConfig targetConfig) {
        this.sourceType = source;
        this.targetType = target;
        this.sourceConfig = sourceConfig;
        this.targetConfig = targetConfig;
    }

    @Override
    public SqlNode visit(SqlCall call) {
        try{
            SqlNode result = convertFunction(call);
            return super.visit((SqlCall) result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public SqlNode visit(SqlIdentifier id) {
        SqlNode result = id;

        return super.visit((SqlIdentifier) result);
    }


    private SqlNode convertFunction(SqlCall call) throws Exception {
        if (!(call.getOperator() instanceof SqlFunction)){
            return call;
        }

        String sourceName = call.getOperator().getName().toUpperCase();
        if ((targetConfig.getExternalFunctions().contains(sourceName) && sourceConfig.getExternalFunctions().contains(sourceName))
                || sourceConfig.getBuiltInFunctions().contains(sourceName)){
            return call;
        }

        String targetName = convertDefaultRule.getRule(sourceName, targetType);
        if (targetName == null) {
            // 函数不存在
            throw new Exception("未找到该函数");
        }else if (!targetName.equals(sourceName)){
            return convertToTargetFunction(call, targetName);
        }else {
            return call;
        }
    }


    private SqlNode convertToTargetFunction(SqlCall call, String targetFunctionName) {
        List<SqlNode> operands = call.getOperandList();
        String upperFuncName = targetFunctionName.toUpperCase();
        return switch (upperFuncName) {
            // 判空函数
            case "NVL","ISNULL","IFNULL" -> SqlStdOperatorTable.COALESCE.createCall(
                    call.getParserPosition(),
                    operands.toArray(new SqlNode[0])
            );
            // 时间函数
            case "SYSDATE", "NOW", "GETDATE" -> createFunctionCall(
                    convertDefaultRule.getRule(upperFuncName, targetType),
                    new ArrayList<>(),
                    call.getParserPosition()
            );
            // 字符串长度函数
            case "LENGTH","LEN" -> createFunctionCall(
                    convertDefaultRule.getRule(upperFuncName, targetType),
                    operands,
                    call.getParserPosition()
            );




            default ->
                    createFunctionCall(targetFunctionName, operands, call.getParserPosition());
        };
    }

    private static SqlNode createFunctionCall(String functionName, List<SqlNode> operands, SqlParserPos pos) {
        SqlFunction function = new SqlFunction(
                functionName,
                SqlKind.OTHER_FUNCTION,
                null,
                null,
                null,
                SqlFunctionCategory.SYSTEM
        );
        return function.createCall(pos, operands.toArray(new SqlNode[0]));
    }

    private SqlNode convertCase(SqlIdentifier id) {
        List<String> newNames = new ArrayList<>();

        for (String name : id.names) {
            if (shouldPreserveCase(name)) {
                newNames.add(name);
            } else {
                if (targetConfig != null && targetConfig.isCaseSensitive()) {
                    newNames.add(name);
                } else {
                    newNames.add(name.toUpperCase());
                }
            }
        }

        if (!newNames.equals(id.names)) {
            return new SqlIdentifier(newNames, id.getParserPosition());
        }
        return id;
    }

    /**
     * 判断是否应该保持大小写不变
     */
    private boolean shouldPreserveCase(String name) {
        // 检查是否为内置函数


        // 检查配置中的函数和关键字
        if (sourceConfig != null) {
            if (sourceConfig.getBuiltInFunctions() != null &&
                    sourceConfig.getBuiltInFunctions().contains(name.toUpperCase())) {
                return true;
            }
            if (sourceConfig.getBuildInKeyWords() != null &&
                    sourceConfig.getBuildInKeyWords().contains(name.toUpperCase())) {
                return true;
            }
            if (sourceConfig.getExternalFunctions() != null &&
                    sourceConfig.getExternalFunctions().contains(name.toUpperCase())) {
                return true;
            }
            if (sourceConfig.getExternalKeywords() != null &&
                    sourceConfig.getExternalKeywords().contains(name.toUpperCase())) {
                return true;
            }
        }

        if (targetConfig != null) {
            if (targetConfig.getBuiltInFunctions() != null &&
                    targetConfig.getBuiltInFunctions().contains(name.toUpperCase())) {
                return true;
            }
            if (targetConfig.getBuildInKeyWords() != null &&
                    targetConfig.getBuildInKeyWords().contains(name.toUpperCase())) {
                return true;
            }
            if (targetConfig.getExternalFunctions() != null &&
                    targetConfig.getExternalFunctions().contains(name.toUpperCase())) {
                return true;
            }
            if (targetConfig.getExternalKeywords() != null &&
                    targetConfig.getExternalKeywords().contains(name.toUpperCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断是否为表名
     */
    private boolean isLikelyTableName(String name) {
        String upperName = name.toUpperCase();
        Set<String> keywords = new HashSet<>();

        if (sourceConfig != null && sourceConfig.getBuildInKeyWords() != null) {
            keywords.addAll(sourceConfig.getBuildInKeyWords());
        }
        if (targetConfig != null && targetConfig.getBuildInKeyWords() != null) {
            keywords.addAll(targetConfig.getBuildInKeyWords());
        }
        if (sourceConfig != null && sourceConfig.getExternalKeywords() != null) {
            keywords.addAll(sourceConfig.getExternalKeywords());
        }
        if (targetConfig != null && targetConfig.getExternalKeywords() != null) {
            keywords.addAll(targetConfig.getExternalKeywords());
        }

        return !keywords.contains(upperName) &&
                !name.contains(".") &&
                !name.startsWith("@");
    }

}
