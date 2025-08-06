package com.example.client.controller;

import com.example.client.entity.SqlBo;
import com.example.client.entity.User;
import com.example.client.entity.UserBO;
import com.example.client.entity.UserPg;
import com.example.client.service.AvaticaClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/avatica")
public class AvaticaClientController {

    private AvaticaClientService avaticaClientService;

    public AvaticaClientController(AvaticaClientService avaticaClientService){
        this.avaticaClientService = avaticaClientService;
    }

    @PostMapping("/query")
    public List<User> executeQuery(@RequestBody UserBO userBO) {
       return avaticaClientService.executeQueryUser(userBO.getUserName(), userBO.getType());
    }

    @PostMapping("/fullSql")
    public CompletableFuture<Object> executeQuery(@RequestBody SqlBo sql) {
        return avaticaClientService.executeSql(sql.getSql());
    }

    @GetMapping("/poolStatus")
    public String getStatus() {
        return avaticaClientService.getPoolStatus();
    }

    @PostMapping("/querypg")
    public List<UserPg> executeQuerypg(@RequestBody UserBO userBO) {
        return avaticaClientService.executeQueryUserPg(userBO.getUserName(), userBO.getType());
    }



}
