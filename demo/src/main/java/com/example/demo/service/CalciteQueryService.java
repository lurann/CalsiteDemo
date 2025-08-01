package com.example.demo.service;

import com.example.demo.entity.DatabaseType;
import com.example.demo.entity.User;
import com.example.demo.mapper.CalciteMapper;
import com.example.demo.mapper.UserMapper;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.jdbc.CalciteConnection;
import org.apache.calcite.schema.SchemaPlus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.example.demo.converter.DialectConverter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CalciteQueryService {

    @Value("${calcite.datasource-file}")
    private String modelResource;

    private HikariDataSource dataSource;

    private final UserMapper userMapper;

    private final CalciteMapper calciteMapper;

    private final DialectConverter dialectConverter;

    public CalciteQueryService(UserMapper userMapper, CalciteMapper calciteMapper, HikariDataSource dataSource, DialectConverter dialectConverter) {
        this.userMapper = userMapper;
        this.calciteMapper = calciteMapper;
        this.dataSource = dataSource;
        this.dialectConverter = dialectConverter;
    }
    public List<User> executeQuery(String id){

        return userMapper.findById(id);
    }

    public List<User> executeQueryUser(String userName){
        try{
            Connection connection = dataSource.getConnection();
            CalciteConnection calciteConnection = connection.unwrap(CalciteConnection.class);
            SchemaPlus rootSchema = calciteConnection.getRootSchema();

            SchemaPlus schema = rootSchema.getSubSchema("calcite_views");
            if (schema != null) {
                // 检查特定视图是否存在
                if (schema.getTable("allUsers") != null) { // 替换为实际的视图名称
                    log.info("View exists");
                } else {
                    log.info("View does not exist");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        return calciteMapper.selectUsersByUsername(userName);
    }

    public String changeDialects(String sql, String source, String target){
        try {
            return dialectConverter.convert(sql, DatabaseType.getByName(source), DatabaseType.getByName(target));
        } catch (Exception e) {
            e.printStackTrace();
            return "转换失败";
        }
    }


}
