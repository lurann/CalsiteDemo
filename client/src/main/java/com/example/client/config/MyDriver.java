package com.example.client.config;


import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class MyDriver extends org.apache.calcite.avatica.remote.Driver{
    static {
        new MyDriver().register();
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        Map<String, String> urlParameter = new HashMap<>(4);
        urlParameter.put("serialization", info.getProperty("serialization"));
        if ("BASIC".equals(info.getProperty("authentication"))){
            urlParameter.put("authentication", info.getProperty("authentication"));
            urlParameter.put("avatica_user", info.getProperty("username"));
            urlParameter.put("avatica_password", info.getProperty("password"));
        }
        StringBuilder resurl = new StringBuilder(url);

        // 检查URL是否已经有参数
        boolean hasParameters = url.contains(";");

        // 批量添加参数
        for (Map.Entry<String, String> entry : urlParameter.entrySet()) {
            // 避免重复添加已存在的参数
            if (!url.contains(entry.getKey() + "=")) {
                if (hasParameters) {
                    resurl.append(";");
                } else {
                    resurl.append(";");
                    hasParameters = true;
                }
                resurl.append(entry.getKey()).append("=").append(entry.getValue());
            }
        }
        return super.connect(resurl.toString(), info);
    }

    @Override
    protected String getConnectStringPrefix() {
        return "jdbc:ah:";
    }
}
