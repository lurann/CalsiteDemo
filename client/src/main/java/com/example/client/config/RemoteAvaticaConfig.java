package com.example.client.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "avatica.client")
@Data
public class RemoteAvaticaConfig {
    private String url = "jdbc:avatica:remote:url=http://localhost:8765";
    private String serialization = "JSON";
    private int connectTimeout = 30000;
    private int socketTimeout = 60000;
}
