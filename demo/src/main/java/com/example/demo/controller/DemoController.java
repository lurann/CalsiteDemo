package com.example.demo.controller;


import com.example.demo.entity.ChangeDialectsBO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserBO;
import com.example.demo.service.CalciteQueryService;
import org.apache.ibatis.jdbc.SQL;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
public class DemoController {

    private final CalciteQueryService calciteQueryService;

    public DemoController(CalciteQueryService calciteQueryService) {
        this.calciteQueryService = calciteQueryService;
    }

    @GetMapping("/query")
    public List<User> query(@RequestParam("sql") String sql){
        return calciteQueryService.executeQuery(sql);
    }

    @PostMapping("/queryUser")
    public List<User> queryUser(@RequestBody UserBO userBO) {
        return calciteQueryService.executeQueryUser(userBO.getUserName());
    }

    @PostMapping("/changeDialects")
    public String changeDialects(@RequestBody ChangeDialectsBO changeDialectsBO) {
        return calciteQueryService.changeDialects(changeDialectsBO.getSql(), changeDialectsBO.getSource(), changeDialectsBO.getTarget());
    }
}
