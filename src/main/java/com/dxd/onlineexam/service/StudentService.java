package com.dxd.onlineexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dxd.onlineexam.entity.*;
import com.dxd.onlineexam.mapper.*;
import com.dxd.onlineexam.vo.*;
import com.dxd.onlineexam.dto.SaveAnswerRequest;
import com.dxd.onlineexam.dto.SubmitPaperRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentService {
    
    private final ExamMapper examMapper;
    private final PaperInstanceMapper paperInstanceMapper;
    private final ScoreMapper scoreMapper;
    private final QuestionMapper questionMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final QuestionOptionMapper questionOptionMapper;

    /**
     * 学生首页统计
     */
    public Map<String, Integer> getHomeStats(Long studentId) {
        Map<String, Integer> stats = new HashMap<>();
        
        // 待参加考试（已发布且在时间范围内的）
        QueryWrapper<Exam> pendingWrapper = new QueryWrapper<>();
        pendingWrapper.eq("status", "published")
                     .gt("end_time", java.time.LocalDateTime.now());
        int pendingExams = Math.toIntExact(examMapper.selectCount(pendingWrapper));
        
        // 进行中的考试（学生已开始但未提交的）
        QueryWrapper<PaperInstance> ongoingWrapper = new QueryWrapper<>();
        ongoingWrapper.eq("student_id", studentId)
                     .eq("status", "ongoing");
        int ongoingExams = Math.toIntExact(paperInstanceMapper.selectCount(ongoingWrapper));
        
        // 已完成的考试
        QueryWrapper<PaperInstance> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("student_id", studentId)
                       .eq("status", "submitted");
        int completedExams = Math.toIntExact(paperInstanceMapper.selectCount(completedWrapper));
        
        stats.put("pendingExams", pendingExams);
        stats.put("ongoingExams", ongoingExams);
        stats.put("completedExams", completedExams);
        
        return stats;
    }

    /**
     * 获取考试列表
     */
    public List<ExamVO> getExamList(Long studentId) {
        QueryWrapper<Exam> wrapper = new QueryWrapper<>();
        wrapper.eq("status", "published")
               .orderByDesc("start_time");
        
        List<Exam> exams = examMapper.selectList(wrapper);
        
        return exams.stream().map(exam -> {
            ExamVO vo = new ExamVO();
            vo.setExamId(exam.getExamId());
            vo.setExamName(exam.getExamName());
            vo.setStartTime(exam.getStartTime());
            vo.setEndTime(exam.getEndTime());
            vo.setDuration(exam.getDuration());
            vo.setTotalScore(exam.getTotalScore());
            
            // 判断状态
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (now.isBefore(exam.getStartTime())) {
                vo.setStatus("pending");
            } else if (now.isAfter(exam.getEndTime())) {
                vo.setStatus("completed");
            } else {
                vo.setStatus("ongoing");
            }
            
            // 检查是否已提交
            QueryWrapper<PaperInstance> piWrapper = new QueryWrapper<>();
            piWrapper.eq("exam_id", exam.getExamId())
                    .eq("student_id", studentId);
            PaperInstance paperInstance = paperInstanceMapper.selectOne(piWrapper);
            vo.setIsSubmitted(paperInstance != null && "submitted".equals(paperInstance.getStatus()));
            
            // 如果已批改，显示分数
            if (paperInstance != null && paperInstance.getIsGraded() == 1) {
                vo.setScore(paperInstance.getTotalScore());
            }
            
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取成绩列表
     */
    public List<ScoreVO> getScoreList(Long studentId) {
        QueryWrapper<Score> wrapper = new QueryWrapper<>();
        wrapper.eq("student_id", studentId)
               .orderByDesc("create_time");
        
        List<Score> scores = scoreMapper.selectList(wrapper);
        
        return scores.stream().map(score -> {
            ScoreVO vo = new ScoreVO();
            
            // 获取考试信息
            Exam exam = examMapper.selectById(score.getExamId());
            vo.setExamId(score.getExamId());
            vo.setExamName(exam != null ? exam.getExamName() : "");
            vo.setExamDate(score.getCreateTime().toLocalDate().toString());
            vo.setObjectiveScore(score.getObjectiveScore());
            vo.setSubjectiveScore(score.getSubjectiveScore());
            vo.setTotalScore(score.getTotalScore());
            
            // 排名
            if (score.getRank() != null) {
                // 获取总人数
                QueryWrapper<Score> countWrapper = new QueryWrapper<>();
                countWrapper.eq("exam_id", score.getExamId());
                long totalStudents = scoreMapper.selectCount(countWrapper);
                vo.setRank(score.getRank() + "/" + totalStudents);
            }
            
            vo.setIsGraded(score.getTotalScore() != null);
            
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 获取考试详情
     */
    public ExamDetailVO getExamDetail(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        ExamDetailVO vo = new ExamDetailVO();
        vo.setExamId(exam.getExamId());
        vo.setExamName(exam.getExamName());
        vo.setDescription(exam.getDescription());
        vo.setStartTime(exam.getStartTime());
        vo.setEndTime(exam.getEndTime());
        vo.setDuration(exam.getDuration());
        vo.setTotalScore(exam.getTotalScore());
        vo.setPassingScore(exam.getPassingScore());
        vo.setQuestionCount(exam.getQuestionCount());
        vo.setStatus(exam.getStatus());
        
        return vo;
    }

    /**
     * 开始考试（创建试卷实例）
     */
    @Transactional
    public PaperVO startExam(Long examId, Long studentId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        // 检查考试时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(exam.getStartTime())) {
            throw new RuntimeException("考试未开始");
        }
        if (now.isAfter(exam.getEndTime())) {
            throw new RuntimeException("考试已结束");
        }
        
        // 检查是否已经开始过
        QueryWrapper<PaperInstance> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_id", examId)
               .eq("student_id", studentId);
        PaperInstance existingPaper = paperInstanceMapper.selectOne(wrapper);
        
        if (existingPaper != null) {
            if ("submitted".equals(existingPaper.getStatus())) {
                throw new RuntimeException("试卷已提交");
            }
            // 继续之前的考试
            return buildPaperVO(existingPaper, exam);
        }
        
        // 创建新试卷实例
        PaperInstance paperInstance = new PaperInstance();
        paperInstance.setExamId(examId);
        paperInstance.setStudentId(studentId);
        paperInstance.setStartTime(now);
        paperInstance.setRemainingTime(exam.getDuration() * 60); // 转换为秒
        paperInstance.setStatus("ongoing");
        paperInstance.setIsGraded(0);
        paperInstance.setCreateTime(now);
        paperInstance.setUpdateTime(now);
        
        paperInstanceMapper.insert(paperInstance);
        
        return buildPaperVO(paperInstance, exam);
    }

    /**
     * 构建试卷VO
     */
    private PaperVO buildPaperVO(PaperInstance paperInstance, Exam exam) {
        PaperVO vo = new PaperVO();
        vo.setExamId(exam.getExamId());
        vo.setPaperInstanceId(paperInstance.getPaperInstanceId());
        vo.setExamName(exam.getExamName());
        vo.setStartTime(paperInstance.getStartTime());
        
        // 计算剩余时间
        long elapsedSeconds = Duration.between(paperInstance.getStartTime(), LocalDateTime.now()).getSeconds();
        int remainingTime = Math.max(0, paperInstance.getRemainingTime() - (int)elapsedSeconds);
        vo.setRemainingTime(remainingTime);
        
        // 获取考试题目
        QueryWrapper<ExamQuestion> eqWrapper = new QueryWrapper<>();
        eqWrapper.eq("exam_id", exam.getExamId())
                 .orderByAsc("question_number");
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(eqWrapper);
        
        List<PaperVO.QuestionVO> questionVOs = examQuestions.stream().map(eq -> {
            Question question = questionMapper.selectById(eq.getQuestionId());
            
            PaperVO.QuestionVO qvo = new PaperVO.QuestionVO();
            qvo.setQuestionId(question.getQuestionId());
            qvo.setQuestionNumber(eq.getQuestionNumber());
            qvo.setQuestionType(question.getType());
            qvo.setQuestionContent(question.getContent());
            qvo.setScore(eq.getScore());
            
            // 获取选项（非主观题）
            if (!"subjective".equals(question.getType())) {
                QueryWrapper<QuestionOption> optionWrapper = new QueryWrapper<>();
                optionWrapper.eq("question_id", question.getQuestionId())
                           .orderByAsc("sort_order");
                List<QuestionOption> options = questionOptionMapper.selectList(optionWrapper);
                
                List<PaperVO.OptionVO> optionVOs = options.stream().map(option -> {
                    PaperVO.OptionVO ovo = new PaperVO.OptionVO();
                    ovo.setOptionId(option.getOptionLabel());
                    ovo.setContent(option.getContent());
                    return ovo;
                }).collect(Collectors.toList());
                
                qvo.setOptions(optionVOs);
            }
            
            return qvo;
        }).collect(Collectors.toList());
        
        vo.setQuestions(questionVOs);
        
        return vo;
    }

    /**
     * 保存答案
     */
    @Transactional
    public void saveAnswer(SaveAnswerRequest request) {
        PaperInstance paperInstance = paperInstanceMapper.selectById(request.getPaperInstanceId());
        if (paperInstance == null) {
            throw new RuntimeException("试卷实例不存在");
        }
        
        if ("submitted".equals(paperInstance.getStatus())) {
            throw new RuntimeException("试卷已提交，无法修改答案");
        }
        
        // 检查答题记录是否存在
        QueryWrapper<AnswerRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("paper_instance_id", request.getPaperInstanceId())
               .eq("question_id", request.getQuestionId());
        AnswerRecord existingRecord = answerRecordMapper.selectOne(wrapper);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (existingRecord != null) {
            // 更新答案
            existingRecord.setStudentAnswer(request.getAnswer());
            existingRecord.setUpdateTime(now);
            answerRecordMapper.updateById(existingRecord);
        } else {
            // 新增答题记录
            AnswerRecord record = new AnswerRecord();
            record.setPaperInstanceId(request.getPaperInstanceId());
            record.setQuestionId(request.getQuestionId());
            record.setStudentAnswer(request.getAnswer());
            record.setCreateTime(now);
            record.setUpdateTime(now);
            answerRecordMapper.insert(record);
        }
    }

    /**
     * 提交试卷
     */
    @Transactional
    public Map<String, Object> submitPaper(SubmitPaperRequest request) {
        PaperInstance paperInstance = paperInstanceMapper.selectById(request.getPaperInstanceId());
        if (paperInstance == null) {
            throw new RuntimeException("试卷实例不存在");
        }
        
        if ("submitted".equals(paperInstance.getStatus())) {
            throw new RuntimeException("试卷已提交");
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 保存所有答案
        for (SubmitPaperRequest.AnswerItem item : request.getAnswers()) {
            QueryWrapper<AnswerRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("paper_instance_id", request.getPaperInstanceId())
                   .eq("question_id", item.getQuestionId());
            AnswerRecord existingRecord = answerRecordMapper.selectOne(wrapper);
            
            if (existingRecord != null) {
                existingRecord.setStudentAnswer(item.getAnswer());
                existingRecord.setUpdateTime(now);
                answerRecordMapper.updateById(existingRecord);
            } else {
                AnswerRecord record = new AnswerRecord();
                record.setPaperInstanceId(request.getPaperInstanceId());
                record.setQuestionId(item.getQuestionId());
                record.setStudentAnswer(item.getAnswer());
                record.setCreateTime(now);
                record.setUpdateTime(now);
                answerRecordMapper.insert(record);
            }
        }
        
        // 自动批改客观题
        BigDecimal objectiveScore = autoGradeObjectiveQuestions(request.getPaperInstanceId());
        
        // 更新试卷实例状态
        paperInstance.setStatus("submitted");
        paperInstance.setSubmitTime(now);
        paperInstance.setObjectiveScore(objectiveScore);
        paperInstance.setUpdateTime(now);
        paperInstanceMapper.updateById(paperInstance);
        
        Map<String, Object> result = new HashMap<>();
        result.put("submitTime", now);
        result.put("objectiveScore", objectiveScore);
        result.put("totalScore", null);
        
        return result;
    }

    /**
     * 自动批改客观题
     */
    private BigDecimal autoGradeObjectiveQuestions(Long paperInstanceId) {
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
     * 获取成绩详情
     */
    public ScoreDetailVO getScoreDetail(Long examId, Long studentId) {
        // 获取试卷实例
        QueryWrapper<PaperInstance> piWrapper = new QueryWrapper<>();
        piWrapper.eq("exam_id", examId)
                 .eq("student_id", studentId);
        PaperInstance paperInstance = paperInstanceMapper.selectOne(piWrapper);
        
        if (paperInstance == null) {
            throw new RuntimeException("未找到考试记录");
        }
        
        if (!"submitted".equals(paperInstance.getStatus())) {
            throw new RuntimeException("考试尚未提交");
        }
        
        Exam exam = examMapper.selectById(examId);
        
        ScoreDetailVO vo = new ScoreDetailVO();
        vo.setExamName(exam.getExamName());
        vo.setObjectiveScore(paperInstance.getObjectiveScore());
        vo.setSubjectiveScore(paperInstance.getSubjectiveScore());
        vo.setTotalScore(paperInstance.getTotalScore());
        vo.setSubmitTime(paperInstance.getSubmitTime());
        
        // 获取排名
        QueryWrapper<Score> scoreWrapper = new QueryWrapper<>();
        scoreWrapper.eq("paper_instance_id", paperInstance.getPaperInstanceId());
        Score score = scoreMapper.selectOne(scoreWrapper);
        
        if (score != null && score.getRank() != null) {
            vo.setRank(score.getRank());
            
            // 获取总人数
            QueryWrapper<Score> countWrapper = new QueryWrapper<>();
            countWrapper.eq("exam_id", examId);
            int totalStudents = Math.toIntExact(scoreMapper.selectCount(countWrapper));
            vo.setTotalStudents(totalStudents);
        }
        
        // 获取答题详情
        QueryWrapper<AnswerRecord> arWrapper = new QueryWrapper<>();
        arWrapper.eq("paper_instance_id", paperInstance.getPaperInstanceId());
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(arWrapper);
        
        QueryWrapper<ExamQuestion> eqWrapper = new QueryWrapper<>();
        eqWrapper.eq("exam_id", examId)
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

