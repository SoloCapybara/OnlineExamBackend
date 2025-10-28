package com.dxd.onlineexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dxd.onlineexam.dto.ExamRequest;
import com.dxd.onlineexam.entity.*;
import com.dxd.onlineexam.mapper.*;
import com.dxd.onlineexam.vo.ExamDetailVO;
import com.dxd.onlineexam.vo.ExamListVO;
import com.dxd.onlineexam.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExamService {
    
    private final ExamMapper examMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final QuestionMapper questionMapper;
    private final PaperInstanceMapper paperInstanceMapper;
    private final QuestionOptionMapper questionOptionMapper;

    /**
     * 获取考试列表
     */
    public List<ExamListVO> getExamList(String status) {
        QueryWrapper<Exam> wrapper = new QueryWrapper<>();
        
        if (status != null && !"all".equals(status)) {
            wrapper.eq("status", status);
        }
        
        wrapper.orderByDesc("create_time");
        List<Exam> exams = examMapper.selectList(wrapper);
        
        return exams.stream().map(exam -> {
            ExamListVO vo = new ExamListVO();
            vo.setExamId(exam.getExamId());
            vo.setExamName(exam.getExamName());
            vo.setStartTime(exam.getStartTime());
            vo.setEndTime(exam.getEndTime());
            vo.setDuration(exam.getDuration());
            vo.setTotalScore(exam.getTotalScore());
            vo.setStatus(exam.getStatus());
            vo.setCreateTime(exam.getCreateTime());
            
            // 统计参与人数
            QueryWrapper<PaperInstance> piWrapper = new QueryWrapper<>();
            piWrapper.eq("exam_id", exam.getExamId());
            long participantCount = paperInstanceMapper.selectCount(piWrapper);
            vo.setParticipantCount((int)participantCount);
            
            // 统计已提交人数
            QueryWrapper<PaperInstance> submittedWrapper = new QueryWrapper<>();
            submittedWrapper.eq("exam_id", exam.getExamId())
                           .eq("status", "submitted");
            long submittedCount = paperInstanceMapper.selectCount(submittedWrapper);
            vo.setSubmittedCount((int)submittedCount);
            
            // 统计已批改人数
            QueryWrapper<PaperInstance> gradedWrapper = new QueryWrapper<>();
            gradedWrapper.eq("exam_id", exam.getExamId())
                        .eq("is_graded", 1);
            long gradedCount = paperInstanceMapper.selectCount(gradedWrapper);
            vo.setGradedCount((int)gradedCount);
            
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
     * 创建考试
     */
    @Transactional
    public Long createExam(ExamRequest request, Long teacherId) {
        LocalDateTime now = LocalDateTime.now();
        
        // 计算总分
        BigDecimal totalScore = BigDecimal.ZERO;
        for (Long questionId : request.getQuestionIds()) {
            BigDecimal score = request.getQuestionScores() != null 
                    ? request.getQuestionScores().getOrDefault(questionId, BigDecimal.ZERO)
                    : BigDecimal.ZERO;
            
            if (score.compareTo(BigDecimal.ZERO) == 0) {
                Question question = questionMapper.selectById(questionId);
                score = question != null ? question.getDefaultScore() : BigDecimal.ZERO;
            }
            
            totalScore = totalScore.add(score);
        }
        
        // 创建考试
        Exam exam = new Exam();
        exam.setExamName(request.getExamName());
        exam.setDescription(request.getDescription());
        exam.setSubjectId(request.getSubjectId());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setDuration(request.getDuration());
        exam.setTotalScore(totalScore);
        exam.setPassingScore(request.getPassingScore());
        exam.setQuestionCount(request.getQuestionIds().size());
        exam.setStatus("draft");
        exam.setCreatorId(teacherId);
        exam.setCreateTime(now);
        exam.setUpdateTime(now);
        
        examMapper.insert(exam);
        
        // 关联题目
        int questionNumber = 1;
        for (Long questionId : request.getQuestionIds()) {
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExamId(exam.getExamId());
            examQuestion.setQuestionId(questionId);
            examQuestion.setQuestionNumber(questionNumber++);
            
            // 设置分数
            BigDecimal score = request.getQuestionScores() != null 
                    ? request.getQuestionScores().getOrDefault(questionId, null)
                    : null;
            
            if (score == null) {
                Question question = questionMapper.selectById(questionId);
                score = question != null ? question.getDefaultScore() : BigDecimal.ZERO;
            }
            
            examQuestion.setScore(score);
            examQuestionMapper.insert(examQuestion);
            
            // 更新题目使用次数
            Question question = questionMapper.selectById(questionId);
            if (question != null) {
                question.setUseCount(question.getUseCount() + 1);
                questionMapper.updateById(question);
            }
        }
        
        return exam.getExamId();
    }

    /**
     * 修改考试
     */
    @Transactional
    public void updateExam(Long examId, ExamRequest request) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        if (!"draft".equals(exam.getStatus())) {
            throw new RuntimeException("只能修改未发布的考试");
        }
        
        // 计算总分
        BigDecimal totalScore = BigDecimal.ZERO;
        for (Long questionId : request.getQuestionIds()) {
            BigDecimal score = request.getQuestionScores() != null 
                    ? request.getQuestionScores().getOrDefault(questionId, BigDecimal.ZERO)
                    : BigDecimal.ZERO;
            
            if (score.compareTo(BigDecimal.ZERO) == 0) {
                Question question = questionMapper.selectById(questionId);
                score = question != null ? question.getDefaultScore() : BigDecimal.ZERO;
            }
            
            totalScore = totalScore.add(score);
        }
        
        // 更新考试信息
        exam.setExamName(request.getExamName());
        exam.setDescription(request.getDescription());
        exam.setSubjectId(request.getSubjectId());
        exam.setStartTime(request.getStartTime());
        exam.setEndTime(request.getEndTime());
        exam.setDuration(request.getDuration());
        exam.setTotalScore(totalScore);
        exam.setPassingScore(request.getPassingScore());
        exam.setQuestionCount(request.getQuestionIds().size());
        exam.setUpdateTime(LocalDateTime.now());
        
        examMapper.updateById(exam);
        
        // 删除旧的题目关联
        QueryWrapper<ExamQuestion> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("exam_id", examId);
        examQuestionMapper.delete(deleteWrapper);
        
        // 重新关联题目
        int questionNumber = 1;
        for (Long questionId : request.getQuestionIds()) {
            ExamQuestion examQuestion = new ExamQuestion();
            examQuestion.setExamId(examId);
            examQuestion.setQuestionId(questionId);
            examQuestion.setQuestionNumber(questionNumber++);
            
            // 设置分数
            BigDecimal score = request.getQuestionScores() != null 
                    ? request.getQuestionScores().getOrDefault(questionId, null)
                    : null;
            
            if (score == null) {
                Question question = questionMapper.selectById(questionId);
                score = question != null ? question.getDefaultScore() : BigDecimal.ZERO;
            }
            
            examQuestion.setScore(score);
            examQuestionMapper.insert(examQuestion);
        }
    }

    /**
     * 删除考试
     */
    public void deleteExam(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        if (!"draft".equals(exam.getStatus())) {
            throw new RuntimeException("只能删除未发布的考试");
        }
        
        // 删除考试
        examMapper.deleteById(examId);
        
        // 删除题目关联
        QueryWrapper<ExamQuestion> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_id", examId);
        examQuestionMapper.delete(wrapper);
    }

    /**
     * 发布考试
     */
    public void publishExam(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        exam.setStatus("published");
        exam.setUpdateTime(LocalDateTime.now());
        examMapper.updateById(exam);
    }

    /**
     * 获取考试题目列表（含选项）
     */
    public List<QuestionVO> getExamQuestions(Long examId) {
        QueryWrapper<ExamQuestion> wrapper = new QueryWrapper<>();
        wrapper.eq("exam_id", examId)
               .orderByAsc("question_number");
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(wrapper);
        
        return examQuestions.stream().map(eq -> {
            Question question = questionMapper.selectById(eq.getQuestionId());
            
            QuestionVO vo = new QuestionVO();
            vo.setQuestionId(question.getQuestionId());
            vo.setQuestionCode(question.getQuestionCode());
            vo.setContent(question.getContent());
            vo.setType(question.getType());
            vo.setScore(eq.getScore());
            
            // 获取选项
            if (!"subjective".equals(question.getType())) {
                QueryWrapper<QuestionOption> optionWrapper = new QueryWrapper<>();
                optionWrapper.eq("question_id", question.getQuestionId())
                           .orderByAsc("sort_order");
                List<QuestionOption> options = questionOptionMapper.selectList(optionWrapper);
                
                List<QuestionVO.OptionVO> optionVOs = options.stream().map(option -> {
                    QuestionVO.OptionVO optionVO = new QuestionVO.OptionVO();
                    optionVO.setOptionId(option.getOptionLabel());
                    optionVO.setContent(option.getContent());
                    return optionVO;
                }).collect(Collectors.toList());
                
                vo.setOptions(optionVOs);
            }
            
            return vo;
        }).collect(Collectors.toList());
    }
}

