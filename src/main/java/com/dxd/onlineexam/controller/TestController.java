package com.dxd.onlineexam.controller;

import com.dxd.onlineexam.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器 - 用于验证系统是否正常启动
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("后端服务运行正常！");
    }
}

