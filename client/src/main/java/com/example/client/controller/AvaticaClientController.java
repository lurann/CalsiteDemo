package com.example.client.controller;

import com.example.client.entity.User;
import com.example.client.entity.UserBO;
import com.example.client.service.AvaticaClientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/avatica")
public class AvaticaClientController {

    private AvaticaClientService avaticaClientService;

    public AvaticaClientController(AvaticaClientService avaticaClientService){
        this.avaticaClientService = avaticaClientService;
    }

    @PostMapping("/query")
    public CompletableFuture<List<User>> executeQuery(@RequestBody UserBO userBO) {
       return avaticaClientService.executeQueryUser(userBO.getUserName());
    }


}
