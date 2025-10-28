package com.dxd.onlineexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dxd.onlineexam.dto.GradingRequest;
import com.dxd.onlineexam.entity.*;
import com.dxd.onlineexam.mapper.*;
import com.dxd.onlineexam.vo.GradingDetailVO;
import com.dxd.onlineexam.vo.PendingExamVO;
import com.dxd.onlineexam.vo.PendingPaperVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradingService {
    
    private final ExamMapper examMapper;
    private final PaperInstanceMapper paperInstanceMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final QuestionMapper questionMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final ScoreMapper scoreMapper;
    private final UserMapper userMapper;

    /**
     * 获取待自动判卷的考试列表
     */
    public List<PendingExamVO> getPendingAutoGradingExams() {
        QueryWrapper<Exam> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "published")
               .lt("end_time", LocalDateTime.now());
        
        List<Exam> exams = examMapper.selectList(wrapper);
        
        return exams.stream().map(exam -> {
            PendingExamVO vo = new PendingExamVO();
            vo.setExamId(exam.getExamId());
            vo.setExamName(exam.getExamName());
            vo.setEndTime(exam.getEndTime());
            
            // 已提交试卷数
            QueryWrapper<PaperInstance> submittedWrapper = new QueryWrapper<>();
            submittedWrapper.eq("exam_id", exam.getExamId())
                           .eq("status", "submitted");
            int submittedCount = Math.toIntExact(paperInstanceMapper.selectCount(submittedWrapper));
            vo.setSubmittedCount(submittedCount);
            
            // 未自动判卷数（客观题分数为null）
            QueryWrapper<PaperInstance> ungradedWrapper = new QueryWrapper<>();
            ungradedWrapper.eq("exam_id", exam.getExamId())
                          .eq("status", "submitted")
                          .isNull("objective_score");
            int ungradedCount = Math.toIntExact(paperInstanceMapper.selectCount(ungradedWrapper));
            vo.setUngradedCount(ungradedCount);
            
            return vo;
        }).filter(vo -> vo.getUngradedCount() > 0)
          .collect(Collectors.toList());
    }

    /**
     * 执行自动判卷（批改客观题）
     */
    @Transactional
    public Map<String, Object> autoGradeExam(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        // 获取所有已提交但未自动判卷的试卷
        QueryWrapper<PaperInstance> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_id", examId)
               .eq("status", "submitted")
               .isNull("objective_score");
        
        List<PaperInstance> paperInstances = paperInstanceMapper.selectList(wrapper);
        
        int gradedCount = 0;
        BigDecimal totalObjectiveScore = BigDecimal.ZERO;
        
        for (PaperInstance paperInstance : paperInstances) {
            BigDecimal objectiveScore = autoGradePaper(paperInstance.getPaperInstanceId());
            
            paperInstance.setObjectiveScore(objectiveScore);
            paperInstance.setUpdateTime(LocalDateTime.now());
            paperInstanceMapper.updateById(paperInstance);
            
            totalObjectiveScore = totalObjectiveScore.add(objectiveScore);
            gradedCount++;
            
            // 检查是否有主观题
            boolean hasSubjective = hasSubjectiveQuestions(examId);
            
            if (!hasSubjective) {
                // 没有主观题，直接计算总分并创建成绩记录
                paperInstance.setSubjectiveScore(BigDecimal.ZERO);
                paperInstance.setTotalScore(objectiveScore);
                paperInstance.setIsGraded(1);
                paperInstanceMapper.updateById(paperInstance);
                
                // 创建成绩记录
                createScoreRecord(paperInstance);
            }
        }
        
        BigDecimal avgObjectiveScore = gradedCount > 0 
                ? totalObjectiveScore.divide(BigDecimal.valueOf(gradedCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        Map<String, Object> result = new HashMap<>();
        result.put("gradedCount", gradedCount);
        result.put("avgObjectiveScore", avgObjectiveScore);
        
        return result;
    }

    /**
     * 自动批改单份试卷的客观题
     */
    private BigDecimal autoGradePaper(Long paperInstanceId) {
        PaperInstance paperInstance = paperInstanceMapper.selectById(paperInstanceId);
        
        QueryWrapper<AnswerRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("paper_instance_id", paperInstanceId);
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(wrapper);
        
        BigDecimal totalScore = BigDecimal.ZERO;
        
        for (AnswerRecord record : answerRecords) {
            Question question = questionMapper.selectById(record.getQuestionId());
            
            // 只批改客观题
            if ("subjective".equals(question.getType())) {
                continue;
            }
            
            // 获取该题在考试中的分数
            QueryWrapper<ExamQuestion> eqWrapper = new QueryWrapper<>();
            eqWrapper.eq("exam_id", paperInstance.getExamId())
                     .eq("question_id", question.getQuestionId());
            ExamQuestion examQuestion = examQuestionMapper.selectOne(eqWrapper);
            
            if (examQuestion == null) {
                continue;
            }
            
            // 判断答案是否正确
            boolean isCorrect = checkAnswer(question, record.getStudentAnswer());
            
            BigDecimal actualScore = isCorrect ? examQuestion.getScore() : BigDecimal.ZERO;
            totalScore = totalScore.add(actualScore);
            
            // 更新答题记录
            record.setIsCorrect(isCorrect ? 1 : 0);
            record.setActualScore(actualScore);
            record.setUpdateTime(LocalDateTime.now());
            answerRecordMapper.updateById(record);
        }
        
        return totalScore;
    }

    /**
     * 检查答案是否正确
     */
    private boolean checkAnswer(Question question, String studentAnswer) {
        if (studentAnswer == null || question.getCorrectAnswer() == null) {
            return false;
        }
        
        String correctAnswer = question.getCorrectAnswer().trim();
        studentAnswer = studentAnswer.trim();
        
        // 多选题需要排序后比较
        if ("multiple".equals(question.getType())) {
            char[] correctChars = correctAnswer.toCharArray();
            char[] studentChars = studentAnswer.toCharArray();
            Arrays.sort(correctChars);
            Arrays.sort(studentChars);
            return Arrays.equals(correctChars, studentChars);
        }
        
        // 单选题和判断题直接比较
        return correctAnswer.equalsIgnoreCase(studentAnswer);
    }

    /**
     * 检查考试是否有主观题
     */
    private boolean hasSubjectiveQuestions(Long examId) {
        QueryWrapper<ExamQuestion> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_id", examId);
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(wrapper);
        
        for (ExamQuestion eq : examQuestions) {
            Question question = questionMapper.selectById(eq.getQuestionId());
            if (question != null && "subjective".equals(question.getType())) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 获取待批改主观题的试卷列表
     */
    public List<PendingPaperVO> getPendingManualGradingPapers(Long examId) {
        QueryWrapper<PaperInstance> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "submitted")
               .eq("is_graded", 0)
               .isNotNull("objective_score");
        
        if (examId != null) {
            wrapper.eq("exam_id", examId);
        }
        
        wrapper.orderByAsc("submit_time");
        List<PaperInstance> paperInstances = paperInstanceMapper.selectList(wrapper);
        
        return paperInstances.stream().map(pi -> {
            PendingPaperVO vo = new PendingPaperVO();
            vo.setPaperInstanceId(pi.getPaperInstanceId());
            vo.setStudentId(pi.getStudentId());
            
            // 获取学生姓名
            User student = userMapper.selectById(pi.getStudentId());
            vo.setStudentName(student != null ? student.getRealName() : "");
            
            vo.setExamId(pi.getExamId());
            
            // 获取考试名称
            Exam exam = examMapper.selectById(pi.getExamId());
            vo.setExamName(exam != null ? exam.getExamName() : "");
            
            vo.setSubmitTime(pi.getSubmitTime());
            vo.setObjectiveScore(pi.getObjectiveScore());
            
            // 统计主观题数量
            QueryWrapper<ExamQuestion> eqWrapper = new QueryWrapper<>();
            eqWrapper.eq("exam_id", pi.getExamId());
            List<ExamQuestion> examQuestions = examQuestionMapper.selectList(eqWrapper);
            
            int subjectiveCount = 0;
            for (ExamQuestion eq : examQuestions) {
                Question question = questionMapper.selectById(eq.getQuestionId());
                if (question != null && "subjective".equals(question.getType())) {
                    subjectiveCount++;
                }
            }
            vo.setSubjectiveQuestionCount(subjectiveCount);
            
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取试卷批改详情
     */
    public GradingDetailVO getGradingDetail(Long paperInstanceId) {
        PaperInstance paperInstance = paperInstanceMapper.selectById(paperInstanceId);
        if (paperInstance == null) {
            throw new RuntimeException("试卷不存在");
        }
        
        // 获取学生信息
        User student = userMapper.selectById(paperInstance.getStudentId());
        
        // 获取考试信息
        Exam exam = examMapper.selectById(paperInstance.getExamId());
        
        GradingDetailVO vo = new GradingDetailVO();
        vo.setPaperInstanceId(paperInstanceId);
        vo.setStudentName(student != null ? student.getRealName() : "");
        vo.setExamName(exam != null ? exam.getExamName() : "");
        vo.setObjectiveScore(paperInstance.getObjectiveScore());
        
        // 获取所有主观题及答案
        QueryWrapper<ExamQuestion> eqWrapper = new QueryWrapper<>();
        eqWrapper.eq("exam_id", paperInstance.getExamId())
                 .orderByAsc("question_number");
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(eqWrapper);
        
        QueryWrapper<AnswerRecord> arWrapper = new QueryWrapper<>();
        arWrapper.eq("paper_instance_id", paperInstanceId);
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(arWrapper);
        
        List<GradingDetailVO.SubjectiveQuestion> subjectiveQuestions = new ArrayList<>();
        
        for (ExamQuestion eq : examQuestions) {
            Question question = questionMapper.selectById(eq.getQuestionId());
            
            if (question == null || !"subjective".equals(question.getType())) {
                continue;
            }
            
            // 找到对应的答题记录
            AnswerRecord answerRecord = answerRecords.stream()
                    .filter(ar -> ar.getQuestionId().equals(eq.getQuestionId()))
                    .findFirst()
                    .orElse(null);
            
            GradingDetailVO.SubjectiveQuestion sq = new GradingDetailVO.SubjectiveQuestion();
            sq.setQuestionId(question.getQuestionId());
            sq.setQuestionNumber(eq.getQuestionNumber());
            sq.setContent(question.getContent());
            sq.setScore(eq.getScore());
            sq.setReferenceAnswer(question.getReferenceAnswer());
            
            if (answerRecord != null) {
                sq.setStudentAnswer(answerRecord.getStudentAnswer());
                sq.setActualScore(answerRecord.getActualScore());
                sq.setComment(answerRecord.getTeacherComment());
            }
            
            subjectiveQuestions.add(sq);
        }
        
        vo.setSubjectiveQuestions(subjectiveQuestions);
        
        return vo;
    }

    /**
     * 提交主观题批改结果
     */
    @Transactional
    public Map<String, Object> submitManualGrading(Long paperInstanceId, GradingRequest request, Long graderId) {
        PaperInstance paperInstance = paperInstanceMapper.selectById(paperInstanceId);
        if (paperInstance == null) {
            throw new RuntimeException("试卷不存在");
        }
        
        BigDecimal subjectiveScore = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();
        
        // 批改每道主观题
        for (GradingRequest.GradeItem item : request.getGrades()) {
            QueryWrapper<AnswerRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("paper_instance_id", paperInstanceId)
                   .eq("question_id", item.getQuestionId());
            
            AnswerRecord answerRecord = answerRecordMapper.selectOne(wrapper);
            
            if (answerRecord != null) {
                answerRecord.setActualScore(item.getActualScore());
                answerRecord.setTeacherComment(item.getComment());
                answerRecord.setGraderId(graderId);
                answerRecord.setGradeTime(now);
                answerRecord.setUpdateTime(now);
                answerRecordMapper.updateById(answerRecord);
                
                subjectiveScore = subjectiveScore.add(item.getActualScore());
            }
        }
        
        // 更新试卷实例
        BigDecimal totalScore = paperInstance.getObjectiveScore().add(subjectiveScore);
        paperInstance.setSubjectiveScore(subjectiveScore);
        paperInstance.setTotalScore(totalScore);
        paperInstance.setIsGraded(1);
        paperInstance.setUpdateTime(now);
        paperInstanceMapper.updateById(paperInstance);
        
        // 创建或更新成绩记录
        createScoreRecord(paperInstance);
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalScore", totalScore);
        
        return result;
    }

    /**
     * 创建成绩记录
     */
    private void createScoreRecord(PaperInstance paperInstance) {
        // 检查是否已存在成绩记录
        QueryWrapper<Score> wrapper = new QueryWrapper<>();
        wrapper.eq("paper_instance_id", paperInstance.getPaperInstanceId());
        Score existingScore = scoreMapper.selectOne(wrapper);
        
        if (existingScore != null) {
            // 更新现有记录
            existingScore.setObjectiveScore(paperInstance.getObjectiveScore());
            existingScore.setSubjectiveScore(paperInstance.getSubjectiveScore());
            existingScore.setTotalScore(paperInstance.getTotalScore());
            existingScore.setUpdateTime(LocalDateTime.now());
            scoreMapper.updateById(existingScore);
        } else {
            // 创建新记录
            User student = userMapper.selectById(paperInstance.getStudentId());
            Exam exam = examMapper.selectById(paperInstance.getExamId());
            
            Score score = new Score();
            score.setPaperInstanceId(paperInstance.getPaperInstanceId());
            score.setExamId(paperInstance.getExamId());
            score.setStudentId(paperInstance.getStudentId());
            score.setClassId(student != null ? student.getClassId() : null);
            score.setObjectiveScore(paperInstance.getObjectiveScore());
            score.setSubjectiveScore(paperInstance.getSubjectiveScore());
            score.setTotalScore(paperInstance.getTotalScore());
            
            // 判断是否及格
            if (exam != null && exam.getPassingScore() != null) {
                score.setIsPassed(paperInstance.getTotalScore().compareTo(exam.getPassingScore()) >= 0 ? 1 : 0);
            }
            
            score.setCreateTime(LocalDateTime.now());
            score.setUpdateTime(LocalDateTime.now());
            
            scoreMapper.insert(score);
        }
        
        // 计算排名
        calculateRanking(paperInstance.getExamId());
    }

    /**
     * 计算排名
     */
    private void calculateRanking(Long examId) {
        QueryWrapper<Score> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_id", examId)
               .orderByDesc("total_score");
        
        List<Score> scores = scoreMapper.selectList(wrapper);
        
        int rank = 1;
        for (Score score : scores) {
            score.setRank(rank++);
            scoreMapper.updateById(score);
        }
    }
}

