package com.example.client.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import org.apache.calcite.avatica.com.fasterxml.jackson.annotation.JsonRawValue;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.Date;

@Data
@TableName("t_user")
public class UserPg {
    @TableId
    private Long id; // 主键ID

    private String username; // 用户名
    private String email; // 邮箱
    private String password; // 密码

    private Byte age; // 年龄（TINYINT UNSIGNED）
    private Integer score; // 分数
    private BigDecimal balance; // 余额

    private String nickname; // 昵称
    private String bio; // 个人简介

    @TableField("created_at")
    private Timestamp createdAt; // 创建时间

    @TableField("updated_at")
    private Timestamp updatedAt; // 更新时间

    @TableField("last_login")
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date lastLogin; // 最后登录时间（DATE类型）

    @TableField("is_active")
    private Boolean isActive; // 是否激活（TINYINT(1)，默认为1）

    @TableField("is_vip")
    private Boolean isVip; // 是否为VIP（TINYINT(1)，默认为0）

    private String gender; // 性别（ENUM('M', 'F', 'Unknown')）
    private String status; // 状态（ENUM('Active', 'Inactive', 'Suspended')）

    @JsonRawValue
    private  String preferences; // 偏好设置（JSON类型）

}