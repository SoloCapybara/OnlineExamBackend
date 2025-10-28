package com.dxd.onlineexam.controller;

import com.dxd.onlineexam.common.Result;
import com.dxd.onlineexam.dto.ExamRequest;
import com.dxd.onlineexam.dto.GradingRequest;
import com.dxd.onlineexam.dto.QuestionRequest;
import com.dxd.onlineexam.service.ExamService;
import com.dxd.onlineexam.service.GradingService;
import com.dxd.onlineexam.service.ScoreService;
import com.dxd.onlineexam.service.StatisticsService;
import com.dxd.onlineexam.service.TeacherService;
import com.dxd.onlineexam.vo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 教师端控制器
 */
@RestController
@RequestMapping("/api/teacher")
@RequiredArgsConstructor
public class TeacherController {
    
    private final TeacherService teacherService;
    private final ExamService examService;
    private final GradingService gradingService;
    private final ScoreService scoreService;
    private final StatisticsService statisticsService;

    /**
     * 教师首页统计
     */
    @GetMapping("/home/stats")
    public Result<Map<String, Object>> getHomeStats() {
        Map<String, Object> stats = teacherService.getHomeStats();
        return Result.success(stats);
    }

    /**
     * 获取题目列表
     */
    @GetMapping("/questions")
    public Result<List<QuestionVO>> getQuestionList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) String difficulty) {
        List<QuestionVO> questions = teacherService.getQuestionList(type, subjectId, difficulty);
        return Result.success(questions);
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/questions/{questionId}")
    public Result<QuestionVO> getQuestionDetail(@PathVariable Long questionId) {
        QuestionVO question = teacherService.getQuestionDetail(questionId);
        return Result.success(question);
    }

    /**
     * 添加题目
     */
    @PostMapping("/questions")
    public Result<Map<String, Long>> addQuestion(@RequestBody QuestionRequest request, @RequestParam Long teacherId) {
        Long questionId = teacherService.addQuestion(request, teacherId);
        Map<String, Long> result = new HashMap<>();
        result.put("questionId", questionId);
        return Result.success(result);
    }

    /**
     * 修改题目
     */
    @PutMapping("/questions/{questionId}")
    public Result<Void> updateQuestion(@PathVariable Long questionId, @RequestBody QuestionRequest request) {
        teacherService.updateQuestion(questionId, request);
        return Result.success();
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/questions/{questionId}")
    public Result<Void> deleteQuestion(@PathVariable Long questionId) {
        teacherService.deleteQuestion(questionId);
        return Result.success();
    }

    // ==================== 考试管理 ====================

    /**
     * 获取考试列表
     */
    @GetMapping("/exams")
    public Result<List<ExamListVO>> getExamList(@RequestParam(required = false) String status) {
        List<ExamListVO> exams = examService.getExamList(status);
        return Result.success(exams);
    }

    /**
     * 获取考试详情
     */
    @GetMapping("/exams/{examId}")
    public Result<ExamDetailVO> getExamDetail(@PathVariable Long examId) {
        ExamDetailVO exam = examService.getExamDetail(examId);
        return Result.success(exam);
    }

    /**
     * 创建考试
     */
    @PostMapping("/exams")
    public Result<Map<String, Long>> createExam(@RequestBody ExamRequest request, @RequestParam Long teacherId) {
        Long examId = examService.createExam(request, teacherId);
        Map<String, Long> result = new HashMap<>();
        result.put("examId", examId);
        return Result.success(result);
    }

    /**
     * 修改考试
     */
    @PutMapping("/exams/{examId}")
    public Result<Void> updateExam(@PathVariable Long examId, @RequestBody ExamRequest request) {
        examService.updateExam(examId, request);
        return Result.success();
    }

    /**
     * 删除考试
     */
    @DeleteMapping("/exams/{examId}")
    public Result<Void> deleteExam(@PathVariable Long examId) {
        examService.deleteExam(examId);
        return Result.success();
    }

    /**
     * 发布考试
     */
    @PostMapping("/exams/{examId}/publish")
    public Result<Void> publishExam(@PathVariable Long examId) {
        examService.publishExam(examId);
        return Result.success();
    }

    // ==================== 判卷系统 ====================

    /**
     * 获取待自动判卷的考试列表
     */
    @GetMapping("/grading/auto/pending-exams")
    public Result<List<PendingExamVO>> getPendingAutoGradingExams() {
        List<PendingExamVO> exams = gradingService.getPendingAutoGradingExams();
        return Result.success(exams);
    }

    /**
     * 执行自动判卷
     */
    @PostMapping("/grading/auto/{examId}")
    public Result<Map<String, Object>> autoGradeExam(@PathVariable Long examId) {
        Map<String, Object> result = gradingService.autoGradeExam(examId);
        return Result.success(result);
    }

    /**
     * 获取待批改主观题的试卷列表
     */
    @GetMapping("/grading/manual/pending-papers")
    public Result<List<PendingPaperVO>> getPendingManualGradingPapers(
            @RequestParam(required = false) Long examId) {
        List<PendingPaperVO> papers = gradingService.getPendingManualGradingPapers(examId);
        return Result.success(papers);
    }

    /**
     * 获取试卷批改详情
     */
    @GetMapping("/grading/manual/papers/{paperInstanceId}")
    public Result<GradingDetailVO> getGradingDetail(@PathVariable Long paperInstanceId) {
        GradingDetailVO detail = gradingService.getGradingDetail(paperInstanceId);
        return Result.success(detail);
    }

    /**
     * 提交批改结果
     */
    @PostMapping("/grading/manual/papers/{paperInstanceId}/submit")
    public Result<Map<String, Object>> submitManualGrading(
            @PathVariable Long paperInstanceId,
            @RequestBody GradingRequest request,
            @RequestParam Long graderId) {
        Map<String, Object> result = gradingService.submitManualGrading(paperInstanceId, request, graderId);
        return Result.success(result);
    }

    // ==================== 成绩管理 ====================

    /**
     * 获取成绩列表
     */
    @GetMapping("/scores")
    public Result<List<Map<String, Object>>> getScoreList(
            @RequestParam(required = false) Long examId,
            @RequestParam(required = false) Long classId,
            @RequestParam(required = false) String status) {
        List<Map<String, Object>> scores = scoreService.getScoreList(examId, classId, status);
        return Result.success(scores);
    }

    /**
     * 获取成绩详情
     */
    @GetMapping("/scores/{scoreId}")
    public Result<ScoreDetailVO> getScoreDetail(@PathVariable Long scoreId) {
        ScoreDetailVO detail = scoreService.getScoreDetail(scoreId);
        return Result.success(detail);
    }

    // ==================== 成绩统计 ====================

    /**
     * 获取考试统计数据
     */
    @GetMapping("/statistics/exam/{examId}")
    public Result<StatisticsVO> getExamStatistics(@PathVariable Long examId) {
        StatisticsVO statistics = statisticsService.getExamStatistics(examId);
        return Result.success(statistics);
    }

    /**
     * 获取班级对比数据
     */
    @GetMapping("/statistics/class-comparison/{examId}")
    public Result<List<Map<String, Object>>> getClassComparison(@PathVariable Long examId) {
        List<Map<String, Object>> comparison = statisticsService.getClassComparison(examId);
        return Result.success(comparison);
    }
}

