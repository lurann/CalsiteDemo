package com.example.demo.config;

import com.example.demo.entity.DatabaseType;

import java.util.HashMap;
import java.util.Map;

public class convertDefaultRule {


    private static Map<DatabaseType, String> IF_NULL_RULE = Map.of(
            DatabaseType.MYSQL, "IFNULL", 
            DatabaseType.POSTGRESQL, "COALESCE",
            DatabaseType.ORACLE, "NVL",
            DatabaseType.SQLSERVER, "ISNULL"
    );

    private static Map<DatabaseType, String> NOW_RULE = Map.of(
            DatabaseType.MYSQL, "NOW()",
            DatabaseType.POSTGRESQL, "NOW()",
            DatabaseType.ORACLE, "SYSDATE",
             DatabaseType.SQLSERVER, "GETDATE"
    );

    private static Map<DatabaseType, String> DATE_FORMAT_RULE = Map.of(
            DatabaseType.MYSQL, "DATE_FORMAT",
            DatabaseType.POSTGRESQL, "TO_CHAR",
            DatabaseType.ORACLE, "TO_CHAR",
             DatabaseType.SQLSERVER, "CONVERT"
    );

    private static Map<DatabaseType, String> LENGTH_RULE = Map.of(
            DatabaseType.MYSQL, "LENGTH",
            DatabaseType.POSTGRESQL, "LENGTH",
            DatabaseType.ORACLE, "LENGTH",
             DatabaseType.SQLSERVER, "LEN"
    );
    private static final Map<String, Map<DatabaseType, String>> FUNCTION_TO_RULE_MAP = new HashMap<>();

    static {
        FUNCTION_TO_RULE_MAP.put("IFNULL", IF_NULL_RULE);
        FUNCTION_TO_RULE_MAP.put("NVL", IF_NULL_RULE);
        FUNCTION_TO_RULE_MAP.put("ISNULL", IF_NULL_RULE);
        FUNCTION_TO_RULE_MAP.put("COALESCE", IF_NULL_RULE);

        FUNCTION_TO_RULE_MAP.put("NOW", NOW_RULE);
        FUNCTION_TO_RULE_MAP.put("SYSDATE", NOW_RULE);
        FUNCTION_TO_RULE_MAP.put("GETDATE", NOW_RULE);

        FUNCTION_TO_RULE_MAP.put("DATE_FORMAT", DATE_FORMAT_RULE);
        FUNCTION_TO_RULE_MAP.put("TO_CHAR", DATE_FORMAT_RULE);
        FUNCTION_TO_RULE_MAP.put("CONVERT", DATE_FORMAT_RULE);

        FUNCTION_TO_RULE_MAP.put("LENGTH", LENGTH_RULE);
        FUNCTION_TO_RULE_MAP.put("LEN", LENGTH_RULE);
    }

    public static boolean addRules(String similarFunctionName, DatabaseType databaseType, String rule){
        Map<DatabaseType, String> ruleMap = FUNCTION_TO_RULE_MAP.getOrDefault(similarFunctionName, null);
        if (ruleMap == null) {
            System.out.println("not find the rule map");
            return false;
        }
        return ruleMap.put(databaseType, rule) == null;
    }
    public static String getRule(String sourceFunctionName, DatabaseType databaseType){
        Map<DatabaseType, String> ruleMap = FUNCTION_TO_RULE_MAP.getOrDefault(sourceFunctionName, null);
        if (ruleMap == null) {
            System.out.println("not find the rule map"+ sourceFunctionName);
            return null;
        }
        return ruleMap.get(databaseType);
    }
}
