package com.dxd.onlineexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dxd.onlineexam.entity.*;
import com.dxd.onlineexam.mapper.*;
import com.dxd.onlineexam.dto.QuestionRequest;
import com.dxd.onlineexam.vo.QuestionVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeacherService {
    
    private final QuestionMapper questionMapper;
    private final QuestionOptionMapper questionOptionMapper;
    private final ExamMapper examMapper;
    private final PaperInstanceMapper paperInstanceMapper;
    private final SubjectMapper subjectMapper;

    /**
     * 教师首页统计
     */
    public Map<String, Object> getHomeStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // 题库题目总数
        stats.put("questionCount", questionMapper.selectCount(null));
        
        // 进行中考试
        QueryWrapper<Exam> ongoingWrapper = new QueryWrapper<>();
        ongoingWrapper.eq("status", "published")
                     .le("start_time", java.time.LocalDateTime.now())
                     .ge("end_time", java.time.LocalDateTime.now());
        stats.put("ongoingExams", examMapper.selectCount(ongoingWrapper));
        
        // 待批改考试（已结束但有未批改试卷的）
        QueryWrapper<PaperInstance> pendingGradingWrapper = new QueryWrapper<>();
        pendingGradingWrapper.eq("status", "submitted")
                            .eq("is_graded", 0);
        stats.put("pendingGradingExams", paperInstanceMapper.selectCount(pendingGradingWrapper));
        
        // 已完成考试
        QueryWrapper<Exam> completedWrapper = new QueryWrapper<>();
        completedWrapper.eq("status", "completed");
        stats.put("completedExams", examMapper.selectCount(completedWrapper));
        
        // 待批改主观题试卷
        stats.put("pendingSubjectivePapers", paperInstanceMapper.selectCount(pendingGradingWrapper));
        
        return stats;
    }

    /**
     * 获取题目列表
     */
    public List<QuestionVO> getQuestionList(String type, Long subjectId, String difficulty) {
        QueryWrapper<Question> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1);
        
        if (type != null && !"all".equals(type)) {
            wrapper.eq("type", type);
        }
        if (subjectId != null) {
            wrapper.eq("subject_id", subjectId);
        }
        if (difficulty != null && !"all".equals(difficulty)) {
            wrapper.eq("difficulty", difficulty);
        }
        
        wrapper.orderByDesc("create_time");
        List<Question> questions = questionMapper.selectList(wrapper);
        
        return questions.stream().map(question -> {
            QuestionVO vo = new QuestionVO();
            vo.setQuestionId(question.getQuestionId());
            vo.setQuestionCode(question.getQuestionCode());
            vo.setContent(question.getContent());
            vo.setType(question.getType());
            vo.setTypeName(getTypeName(question.getType()));
            
            // 获取科目名称
            Subject subject = subjectMapper.selectById(question.getSubjectId());
            vo.setSubject(subject != null ? subject.getSubjectName() : "");
            
            vo.setDifficulty(question.getDifficulty());
            vo.setDifficultyName(getDifficultyName(question.getDifficulty()));
            vo.setScore(question.getDefaultScore());
            
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

    private String getTypeName(String type) {
        switch (type) {
            case "single": return "单选题";
            case "multiple": return "多选题";
            case "judge": return "判断题";
            case "subjective": return "主观题";
            default: return type;
        }
    }

    private String getDifficultyName(String difficulty) {
        switch (difficulty) {
            case "easy": return "简单";
            case "medium": return "中等";
            case "hard": return "困难";
            default: return difficulty;
        }
    }

    /**
     * 获取题目详情
     */
    public QuestionVO getQuestionDetail(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null || question.getStatus() == 0) {
            throw new RuntimeException("题目不存在");
        }
        
        QuestionVO vo = new QuestionVO();
        vo.setQuestionId(question.getQuestionId());
        vo.setQuestionCode(question.getQuestionCode());
        vo.setContent(question.getContent());
        vo.setType(question.getType());
        vo.setTypeName(getTypeName(question.getType()));
        
        // 获取科目名称
        Subject subject = subjectMapper.selectById(question.getSubjectId());
        vo.setSubject(subject != null ? subject.getSubjectName() : "");
        
        vo.setDifficulty(question.getDifficulty());
        vo.setDifficultyName(getDifficultyName(question.getDifficulty()));
        vo.setScore(question.getDefaultScore());
        vo.setCorrectAnswer(question.getCorrectAnswer());
        vo.setAnalysis(question.getAnalysis());
        
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
    }

    /**
     * 添加题目
     */
    @Transactional
    public Long addQuestion(QuestionRequest request, Long teacherId) {
        LocalDateTime now = LocalDateTime.now();
        
        // 生成题目编码
        String questionCode = generateQuestionCode();
        
        // 创建题目
        Question question = new Question();
        question.setQuestionCode(questionCode);
        question.setContent(request.getContent());
        question.setType(request.getType());
        question.setSubjectId(request.getSubjectId());
        question.setDifficulty(request.getDifficulty());
        question.setDefaultScore(request.getScore());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setAnalysis(request.getAnalysis());
        question.setReferenceAnswer(request.getReferenceAnswer());
        question.setCreatorId(teacherId);
        question.setUseCount(0);
        question.setStatus(1);
        question.setCreateTime(now);
        question.setUpdateTime(now);
        
        questionMapper.insert(question);
        
        // 添加选项（如果不是主观题）
        if (!"subjective".equals(request.getType()) && request.getOptions() != null) {
            int sortOrder = 1;
            for (QuestionRequest.OptionItem optionItem : request.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setQuestionId(question.getQuestionId());
                option.setOptionLabel(optionItem.getOptionId());
                option.setContent(optionItem.getContent());
                option.setSortOrder(sortOrder++);
                questionOptionMapper.insert(option);
            }
        }
        
        return question.getQuestionId();
    }

    /**
     * 修改题目
     */
    @Transactional
    public void updateQuestion(Long questionId, QuestionRequest request) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }
        
        // 更新题目信息
        question.setContent(request.getContent());
        question.setType(request.getType());
        question.setSubjectId(request.getSubjectId());
        question.setDifficulty(request.getDifficulty());
        question.setDefaultScore(request.getScore());
        question.setCorrectAnswer(request.getCorrectAnswer());
        question.setAnalysis(request.getAnalysis());
        question.setReferenceAnswer(request.getReferenceAnswer());
        question.setUpdateTime(LocalDateTime.now());
        
        questionMapper.updateById(question);
        
        // 删除旧选项
        QueryWrapper<QuestionOption> deleteWrapper = new QueryWrapper<>();
        deleteWrapper.eq("question_id", questionId);
        questionOptionMapper.delete(deleteWrapper);
        
        // 添加新选项（如果不是主观题）
        if (!"subjective".equals(request.getType()) && request.getOptions() != null) {
            int sortOrder = 1;
            for (QuestionRequest.OptionItem optionItem : request.getOptions()) {
                QuestionOption option = new QuestionOption();
                option.setQuestionId(questionId);
                option.setOptionLabel(optionItem.getOptionId());
                option.setContent(optionItem.getContent());
                option.setSortOrder(sortOrder++);
                questionOptionMapper.insert(option);
            }
        }
    }

    /**
     * 删除题目（软删除）
     */
    public void deleteQuestion(Long questionId) {
        Question question = questionMapper.selectById(questionId);
        if (question == null) {
            throw new RuntimeException("题目不存在");
        }
        
        question.setStatus(0);
        question.setUpdateTime(LocalDateTime.now());
        questionMapper.updateById(question);
    }

    /**
     * 生成题目编码
     */
    private String generateQuestionCode() {
        // 简单实现：Q + 时间戳 + 随机数
        return "Q" + System.currentTimeMillis() + String.format("%03d", (int)(Math.random() * 1000));
    }
}

