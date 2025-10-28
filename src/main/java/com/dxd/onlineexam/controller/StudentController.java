package com.dxd.onlineexam.controller;

import com.dxd.onlineexam.common.Result;
import com.dxd.onlineexam.dto.SaveAnswerRequest;
import com.dxd.onlineexam.dto.SubmitPaperRequest;
import com.dxd.onlineexam.service.StudentService;
import com.dxd.onlineexam.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 学生端控制器
 */
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
public class StudentController {
    
    private final StudentService studentService;

    /**
     * 学生首页统计
     */
    @GetMapping("/home/stats")
    public Result<Map<String, Integer>> getHomeStats(@RequestParam Long studentId) {
        Map<String, Integer> stats = studentService.getHomeStats(studentId);
        return Result.success(stats);
    }

    /**
     * 获取考试列表
     */
    @GetMapping("/exams")
    public Result<List<ExamVO>> getExamList(@RequestParam Long studentId) {
        List<ExamVO> exams = studentService.getExamList(studentId);
        return Result.success(exams);
    }

    /**
     * 获取成绩列表
     */
    @GetMapping("/scores")
    public Result<List<ScoreVO>> getScoreList(@RequestParam Long studentId) {
        List<ScoreVO> scores = studentService.getScoreList(studentId);
        return Result.success(scores);
    }

    /**
     * 获取考试详情
     */
    @GetMapping("/exams/{examId}")
    public Result<ExamDetailVO> getExamDetail(@PathVariable Long examId) {
        ExamDetailVO examDetail = studentService.getExamDetail(examId);
        return Result.success(examDetail);
    }

    /**
     * 开始考试
     */
    @PostMapping("/exams/{examId}/start")
    public Result<PaperVO> startExam(@PathVariable Long examId, @RequestParam Long studentId) {
        PaperVO paper = studentService.startExam(examId, studentId);
        return Result.success(paper);
    }

    /**
     * 保存答案
     */
    @PostMapping("/exams/{examId}/save-answer")
    public Result<Void> saveAnswer(@PathVariable Long examId, @RequestBody SaveAnswerRequest request) {
        studentService.saveAnswer(request);
        return Result.success();
    }

    /**
     * 提交试卷
     */
    @PostMapping("/exams/{examId}/submit")
    public Result<Map<String, Object>> submitPaper(@PathVariable Long examId, @RequestBody SubmitPaperRequest request) {
        Map<String, Object> result = studentService.submitPaper(request);
        return Result.success(result);
    }

    /**
     * 获取成绩详情
     */
    @GetMapping("/scores/{examId}")
    public Result<ScoreDetailVO> getScoreDetail(@PathVariable Long examId, @RequestParam Long studentId) {
        ScoreDetailVO scoreDetail = studentService.getScoreDetail(examId, studentId);
        return Result.success(scoreDetail);
    }
}

