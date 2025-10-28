package com.dxd.onlineexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dxd.onlineexam.entity.*;
import com.dxd.onlineexam.mapper.*;
import com.dxd.onlineexam.vo.ScoreDetailVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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

    /**
     * 获取成绩列表
     */
    public List<Map<String, Object>> getScoreList(Long examId, Long classId, String status) {
        QueryWrapper<Score> wrapper = new QueryWrapper<>();
        
        if (examId != null) {
            wrapper.eq("exam_id", examId);
        }
        
        if (classId != null) {
            wrapper.eq("class_id", classId);
        }
        
        if ("graded".equals(status)) {
            wrapper.isNotNull("total_score");
        } else if ("pending".equals(status)) {
            wrapper.isNull("total_score");
        }
        
        wrapper.orderByDesc("create_time");
        List<Score> scores = scoreMapper.selectList(wrapper);
        
        return scores.stream().map(score -> {
            Map<String, Object> map = new HashMap<>();
            
            map.put("scoreId", score.getScoreId());
            map.put("studentId", score.getStudentId());
            
            // 获取学生信息
            User student = userMapper.selectById(score.getStudentId());
            map.put("studentName", student != null ? student.getRealName() : "");
            
            // 获取考试信息
            Exam exam = examMapper.selectById(score.getExamId());
            map.put("examName", exam != null ? exam.getExamName() : "");
            
            // 获取班级信息
            if (score.getClassId() != null) {
                com.dxd.onlineexam.entity.Class clazz = classMapper.selectById(score.getClassId());
                map.put("className", clazz != null ? clazz.getClassName() : "");
            }
            
            map.put("objectiveScore", score.getObjectiveScore());
            map.put("subjectiveScore", score.getSubjectiveScore());
            map.put("totalScore", score.getTotalScore());
            map.put("rank", score.getRank());
            map.put("isGraded", score.getTotalScore() != null);
            
            // 获取提交时间
            QueryWrapper<PaperInstance> piWrapper = new QueryWrapper<>();
            piWrapper.eq("paper_instance_id", score.getPaperInstanceId());
            PaperInstance paperInstance = paperInstanceMapper.selectOne(piWrapper);
            map.put("submitTime", paperInstance != null ? paperInstance.getSubmitTime() : null);
            
            return map;
        }).collect(Collectors.toList());
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
            detail.setScore(eq.getScore());
            
            if (answerRecord != null) {
                detail.setStudentAnswer(answerRecord.getStudentAnswer());
                detail.setIsCorrect(answerRecord.getIsCorrect() != null && answerRecord.getIsCorrect() == 1);
                detail.setActualScore(answerRecord.getActualScore());
                detail.setTeacherComment(answerRecord.getTeacherComment());
            }
            
            detail.setCorrectAnswer(question.getCorrectAnswer());
            
            return detail;
        }).collect(Collectors.toList());
        
        vo.setQuestions(questionDetails);
        
        return vo;
    }
}

