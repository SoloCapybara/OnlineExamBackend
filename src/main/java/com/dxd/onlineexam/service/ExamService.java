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
    private final SubjectMapper subjectMapper;
    private final ExamClassMapper examClassMapper;

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
        
        LocalDateTime now = LocalDateTime.now();
        
        return exams.stream().map(exam -> {
            // 动态计算并更新考试状态
            String actualStatus = calculateExamStatus(exam, now);
            if (!actualStatus.equals(exam.getStatus())) {
                exam.setStatus(actualStatus);
                exam.setUpdateTime(now);
                examMapper.updateById(exam);
            }
            
            ExamListVO vo = new ExamListVO();
            vo.setExamId(exam.getExamId());
            vo.setExamName(exam.getExamName());
            vo.setStartTime(exam.getStartTime());
            vo.setEndTime(exam.getEndTime());
            vo.setDuration(exam.getDuration());
            vo.setTotalScore(exam.getTotalScore());
            vo.setStatus(actualStatus);
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
     * 根据时间动态计算考试状态
     */
    private String calculateExamStatus(Exam exam, LocalDateTime now) {
        String currentStatus = exam.getStatus();
        
        // 草稿状态不自动改变
        if ("draft".equals(currentStatus)) {
            return "draft";
        }
        
        // 添加调试日志
        System.out.println("计算考试状态 - 考试ID: " + exam.getExamId() + ", 名称: " + exam.getExamName());
        System.out.println("  当前时间: " + now);
        System.out.println("  开始时间: " + exam.getStartTime());
        System.out.println("  结束时间: " + exam.getEndTime());
        
        // 已发布、未开始、进行中、已结束状态都根据时间判断
        // （兼容旧的published状态）
        if (now.isBefore(exam.getStartTime())) {
            System.out.println("  -> 未开始");
            return "pending";  // 未开始
        } else if (now.isAfter(exam.getEndTime())) {
            System.out.println("  -> 已结束");
            return "finished"; // 已结束
        } else {
            System.out.println("  -> 进行中");
            return "ongoing";  // 进行中
        }
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
        vo.setSubjectId(exam.getSubjectId());
        
        // 查询科目名称
        if (exam.getSubjectId() != null) {
            Subject subject = subjectMapper.selectById(exam.getSubjectId());
            if (subject != null) {
                vo.setSubjectName(subject.getSubjectName());
            }
        }
        
        vo.setStartTime(exam.getStartTime());
        vo.setEndTime(exam.getEndTime());
        vo.setDuration(exam.getDuration());
        vo.setTotalScore(exam.getTotalScore());
        vo.setPassingScore(exam.getPassingScore());
        vo.setQuestionCount(exam.getQuestionCount());
        vo.setStatus(exam.getStatus());
        
        // 查询班级IDs
        QueryWrapper<ExamClass> ecWrapper = new QueryWrapper<>();
        ecWrapper.eq("exam_id", examId);
        List<ExamClass> examClasses = examClassMapper.selectList(ecWrapper);
        List<Long> classIds = examClasses.stream()
                .map(ExamClass::getClassId)
                .collect(Collectors.toList());
        vo.setClassIds(classIds);
        
        // 查询题目列表
        QueryWrapper<ExamQuestion> eqWrapper = new QueryWrapper<>();
        eqWrapper.eq("exam_id", examId).orderByAsc("question_number");
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(eqWrapper);
        
        List<QuestionVO> questionVOs = examQuestions.stream().map(eq -> {
            Question question = questionMapper.selectById(eq.getQuestionId());
            if (question == null) {
                return null;
            }
            
            QuestionVO qvo = new QuestionVO();
            qvo.setQuestionId(question.getQuestionId());
            qvo.setContent(question.getContent());
            qvo.setType(question.getType());
            qvo.setScore(eq.getScore());
            
            // 设置类型名称
            String typeName = "";
            switch (question.getType()) {
                case "single_choice": typeName = "单选题"; break;
                case "multiple_choice": typeName = "多选题"; break;
                case "true_false": typeName = "判断题"; break;
                case "essay": typeName = "主观题"; break;
                default: typeName = question.getType();
            }
            qvo.setTypeName(typeName);
            
            return qvo;
        }).filter(q -> q != null).collect(Collectors.toList());
        
        vo.setQuestions(questionVOs);
        
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
        
        // 关联班级
        if (request.getTargetClassIds() != null && !request.getTargetClassIds().isEmpty()) {
            for (Long classId : request.getTargetClassIds()) {
                ExamClass examClass = new ExamClass();
                examClass.setExamId(exam.getExamId());
                examClass.setClassId(classId);
                examClassMapper.insert(examClass);
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
        
        // 删除旧的班级关联
        QueryWrapper<ExamClass> deleteClassWrapper = new QueryWrapper<>();
        deleteClassWrapper.eq("exam_id", examId);
        examClassMapper.delete(deleteClassWrapper);
        
        // 重新关联班级
        if (request.getTargetClassIds() != null && !request.getTargetClassIds().isEmpty()) {
            for (Long classId : request.getTargetClassIds()) {
                ExamClass examClass = new ExamClass();
                examClass.setExamId(examId);
                examClass.setClassId(classId);
                examClassMapper.insert(examClass);
            }
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
        
        LocalDateTime now = LocalDateTime.now();
        
        // 添加调试日志
        System.out.println("=== 发布考试状态判断 ===");
        System.out.println("考试ID: " + examId);
        System.out.println("考试名称: " + exam.getExamName());
        System.out.println("当前时间: " + now);
        System.out.println("开始时间: " + exam.getStartTime());
        System.out.println("结束时间: " + exam.getEndTime());
        System.out.println("now.isBefore(startTime): " + now.isBefore(exam.getStartTime()));
        System.out.println("now.isAfter(endTime): " + now.isAfter(exam.getEndTime()));
        System.out.println("now.isBefore(endTime): " + now.isBefore(exam.getEndTime()));
        
        // 发布时根据时间设置正确的状态
        String status;
        if (now.isBefore(exam.getStartTime())) {
            status = "pending";  // 未开始
            System.out.println("判断结果: 未开始");
        } else if (now.isAfter(exam.getEndTime())) {
            status = "finished"; // 已结束
            System.out.println("判断结果: 已结束");
        } else {
            status = "ongoing";  // 进行中
            System.out.println("判断结果: 进行中");
        }
        System.out.println("最终状态: " + status);
        System.out.println("======================");
        
        exam.setStatus(status);
        exam.setUpdateTime(now);
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

