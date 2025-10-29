package com.dxd.onlineexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dxd.onlineexam.entity.*;
import com.dxd.onlineexam.mapper.*;
import com.dxd.onlineexam.vo.ScoreDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreService {
    
    private final ScoreMapper scoreMapper;
    private final ExamMapper examMapper;
    private final UserMapper userMapper;
    private final ClassMapper classMapper;
    private final PaperInstanceMapper paperInstanceMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;

    /**
     * 获取成绩列表
     */
    public List<Map<String, Object>> getScoreList(Long examId, Long classId, String status) {
        List<Map<String, Object>> result = new ArrayList<>();

        // 1) 已批改（graded）- 直接来自 score 表
        if (status == null || "graded".equals(status)) {
            QueryWrapper<Score> gradedWrapper = new QueryWrapper<>();
            if (examId != null) gradedWrapper.eq("exam_id", examId);
            if (classId != null) gradedWrapper.eq("class_id", classId);
            gradedWrapper.isNotNull("total_score").orderByDesc("create_time");
            List<Score> graded = scoreMapper.selectList(gradedWrapper);
            for (Score s : graded) {
                Map<String, Object> map = new HashMap<>();
                map.put("scoreId", s.getScoreId());
                map.put("studentId", s.getStudentId());
                User stu = userMapper.selectById(s.getStudentId());
                map.put("studentName", stu != null ? stu.getRealName() : "");
                Exam ex = examMapper.selectById(s.getExamId());
                map.put("examName", ex != null ? ex.getExamName() : "");
                if (s.getClassId() != null) {
                    com.dxd.onlineexam.entity.Class clazz = classMapper.selectById(s.getClassId());
                    map.put("className", clazz != null ? clazz.getClassName() : "");
                } else {
                    map.put("className", "");
                }
                map.put("objectiveScore", s.getObjectiveScore());
                map.put("subjectiveScore", s.getSubjectiveScore());
                map.put("totalScore", s.getTotalScore());
                map.put("rank", s.getRank());
                map.put("isGraded", true);
                // 提交时间
                PaperInstance pi = paperInstanceMapper.selectById(s.getPaperInstanceId());
                map.put("submitTime", pi != null ? pi.getSubmitTime() : null);
                map.put("status", "graded");
                result.add(map);
            }
        }

        // 2) 部分批改（partial）- paper_instance 提交、客观题已判、未总评
        if (status == null || "partial".equals(status)) {
            QueryWrapper<PaperInstance> partialWrapper = new QueryWrapper<>();
            partialWrapper.eq("status", "submitted")
                    .isNotNull("objective_score")
                    .eq("is_graded", 0);
            if (examId != null) partialWrapper.eq("exam_id", examId);
            List<PaperInstance> partial = paperInstanceMapper.selectList(partialWrapper);
            for (PaperInstance pi : partial) {
                User stu = userMapper.selectById(pi.getStudentId());
                Exam ex = examMapper.selectById(pi.getExamId());
                Map<String, Object> map = new HashMap<>();
                map.put("scoreId", null);
                map.put("studentId", pi.getStudentId());
                map.put("studentName", stu != null ? stu.getRealName() : "");
                map.put("examName", ex != null ? ex.getExamName() : "");
                // 班级
                if (stu != null && stu.getClassId() != null) {
                    com.dxd.onlineexam.entity.Class clazz = classMapper.selectById(stu.getClassId());
                    if (classId != null && !classId.equals(stu.getClassId())) {
                        continue; // 班级过滤
                    }
                    map.put("className", clazz != null ? clazz.getClassName() : "");
                } else {
                    if (classId != null) continue;
                    map.put("className", "");
                }
                map.put("objectiveScore", pi.getObjectiveScore());
                map.put("subjectiveScore", null);
                map.put("totalScore", null);
                map.put("rank", null);
                map.put("isGraded", false);
                map.put("submitTime", pi.getSubmitTime());
                map.put("status", "partial");
                result.add(map);
            }
        }

        // 3) 待批改（pending）- paper_instance 提交、客观题未判
        if (status == null || "pending".equals(status)) {
            QueryWrapper<PaperInstance> pendingWrapper = new QueryWrapper<>();
            pendingWrapper.eq("status", "submitted")
                    .isNull("objective_score");
            if (examId != null) pendingWrapper.eq("exam_id", examId);
            List<PaperInstance> pending = paperInstanceMapper.selectList(pendingWrapper);
            for (PaperInstance pi : pending) {
                User stu = userMapper.selectById(pi.getStudentId());
                Exam ex = examMapper.selectById(pi.getExamId());
                Map<String, Object> map = new HashMap<>();
                map.put("scoreId", null);
                map.put("studentId", pi.getStudentId());
                map.put("studentName", stu != null ? stu.getRealName() : "");
                map.put("examName", ex != null ? ex.getExamName() : "");
                if (stu != null && stu.getClassId() != null) {
                    com.dxd.onlineexam.entity.Class clazz = classMapper.selectById(stu.getClassId());
                    if (classId != null && !classId.equals(stu.getClassId())) {
                        continue;
                    }
                    map.put("className", clazz != null ? clazz.getClassName() : "");
                } else {
                    if (classId != null) continue;
                    map.put("className", "");
                }
                map.put("objectiveScore", null);
                map.put("subjectiveScore", null);
                map.put("totalScore", null);
                map.put("rank", null);
                map.put("isGraded", false);
                map.put("submitTime", pi.getSubmitTime());
                map.put("status", "pending");
                result.add(map);
            }
        }

        // 默认按提交时间降序、已批改在前
        result.sort((a, b) -> {
            boolean ag = "graded".equals(a.get("status"));
            boolean bg = "graded".equals(b.get("status"));
            if (ag != bg) return ag ? -1 : 1;
            java.time.LocalDateTime at = (java.time.LocalDateTime) a.get("submitTime");
            java.time.LocalDateTime bt = (java.time.LocalDateTime) b.get("submitTime");
            if (at == null && bt == null) return 0;
            if (at == null) return 1;
            if (bt == null) return -1;
            return bt.compareTo(at);
        });

        return result;
    }

    /**
     * 获取成绩详情
     */
    public ScoreDetailVO getScoreDetail(Long scoreId) {
        Score score = scoreMapper.selectById(scoreId);
        if (score == null) {
            throw new RuntimeException("成绩不存在");
        }
        
        PaperInstance paperInstance = paperInstanceMapper.selectById(score.getPaperInstanceId());
        if (paperInstance == null) {
            throw new RuntimeException("试卷实例不存在");
        }
        
        Exam exam = examMapper.selectById(score.getExamId());
        
        ScoreDetailVO vo = new ScoreDetailVO();
        vo.setExamName(exam != null ? exam.getExamName() : "");
        vo.setObjectiveScore(score.getObjectiveScore());
        vo.setSubjectiveScore(score.getSubjectiveScore());
        vo.setTotalScore(score.getTotalScore());
        vo.setRank(score.getRank());
        vo.setSubmitTime(paperInstance.getSubmitTime());
        
        // 获取总人数
        QueryWrapper<Score> countWrapper = new QueryWrapper<>();
        countWrapper.eq("exam_id", score.getExamId());
        int totalStudents = Math.toIntExact(scoreMapper.selectCount(countWrapper));
        vo.setTotalStudents(totalStudents);
        
        // 获取答题详情
        QueryWrapper<AnswerRecord> arWrapper = new QueryWrapper<>();
        arWrapper.eq("paper_instance_id", paperInstance.getPaperInstanceId());
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(arWrapper);
        
        QueryWrapper<ExamQuestion> eqWrapper = new QueryWrapper<>();
        eqWrapper.eq("exam_id", score.getExamId())
                 .orderByAsc("question_number");
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(eqWrapper);
        
        List<ScoreDetailVO.QuestionDetail> questionDetails = examQuestions.stream().map(eq -> {
            Question question = questionMapper.selectById(eq.getQuestionId());
            
            // 找到对应的答题记录
            AnswerRecord answerRecord = answerRecords.stream()
                    .filter(ar -> ar.getQuestionId().equals(eq.getQuestionId()))
                    .findFirst()
                    .orElse(null);
            
            ScoreDetailVO.QuestionDetail detail = new ScoreDetailVO.QuestionDetail();
            detail.setQuestionNumber(eq.getQuestionNumber());
            detail.setQuestionContent(question.getContent());
            detail.setQuestionType(question.getType());
            detail.setScore(answerRecord != null && answerRecord.getActualScore() != null ? answerRecord.getActualScore() : null);
            detail.setMaxScore(eq.getScore());
            // 类型中文名
            String t = question.getType() == null ? "" : question.getType().trim().toLowerCase();
            String typeName;
            switch (t) {
                case "single":
                case "single_choice": typeName = "单选题"; break;
                case "multiple":
                case "multiple_choice": typeName = "多选题"; break;
                case "judge":
                case "true_false": typeName = "判断题"; break;
                case "subjective":
                case "essay": typeName = "主观题"; break;
                default: typeName = "未知类型";
            }
            // 兼容字段
            try { com.dxd.onlineexam.vo.ScoreDetailVO.QuestionDetail.class.getDeclaredField("typeName"); detail.setTypeName(typeName); } catch (Exception ignore) {}
            
            if (answerRecord != null) {
                detail.setStudentAnswer(answerRecord.getStudentAnswer());
                detail.setIsCorrect(answerRecord.getIsCorrect() != null && answerRecord.getIsCorrect() == 1);
                detail.setActualScore(answerRecord.getActualScore());
                detail.setTeacherComment(answerRecord.getTeacherComment());
            }
            
            detail.setCorrectAnswer(question.getCorrectAnswer());
            detail.setAnalysis(question.getAnalysis());
            if ("subjective".equals(t) || "essay".equals(t)) {
                detail.setReferenceAnswer(question.getReferenceAnswer());
            } else {
                // 选项（客观题）
                QueryWrapper<QuestionOption> optionWrapper = new QueryWrapper<>();
                optionWrapper.eq("question_id", question.getQuestionId())
                           .orderByAsc("sort_order");
                List<QuestionOption> options = questionOptionMapper.selectList(optionWrapper);
                List<ScoreDetailVO.QuestionDetail.OptionDetail> optionDetails = options.stream().map(op -> {
                    ScoreDetailVO.QuestionDetail.OptionDetail od = new ScoreDetailVO.QuestionDetail.OptionDetail();
                    od.setLabel(op.getOptionLabel());
                    od.setContent(op.getContent());
                    return od;
                }).collect(java.util.stream.Collectors.toList());
                detail.setOptions(optionDetails);
            }
            
            return detail;
        }).collect(Collectors.toList());
        
        vo.setQuestions(questionDetails);
        
        return vo;
    }
}

