package com.example.demo.entity;

import lombok.Data;

@Data
public class ChangeDialectsBO {
    private String sql;
    private String target;
    private String source;

}
