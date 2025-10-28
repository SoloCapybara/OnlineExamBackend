package com.dxd.onlineexam.controller;

import com.dxd.onlineexam.common.Result;
import com.dxd.onlineexam.entity.Class;
import com.dxd.onlineexam.entity.Subject;
import com.dxd.onlineexam.service.CommonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 公共接口控制器
 */
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class CommonController {
    
    private final CommonService commonService;

    /**
     * 获取班级列表
     */
    @GetMapping("/classes")
    public Result<List<Class>> getClassList() {
        List<Class> classes = commonService.getClassList();
        return Result.success(classes);
    }

    /**
     * 获取科目列表
     */
    @GetMapping("/subjects")
    public Result<List<Subject>> getSubjectList() {
        List<Subject> subjects = commonService.getSubjectList();
        return Result.success(subjects);
    }
}

