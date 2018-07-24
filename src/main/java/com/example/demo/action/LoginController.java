package com.example.demo.action;

import com.example.demo.service.BbsUserService;
import org.beetl.sql.core.SQLManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

/**
 * Created by qianshu on 2018/7/24.
 */
@Controller
public class LoginController {

    @Autowired
    SQLManager sql;
    @Autowired
    BbsUserService bbsUserService;

    static final String CODE_NAME = "verCode";
}
