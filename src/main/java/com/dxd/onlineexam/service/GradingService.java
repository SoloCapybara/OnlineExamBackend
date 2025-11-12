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
        // 放宽条件：只要存在“已提交且未自动判客观题”的试卷，就显示对应考试
        QueryWrapper<PaperInstance> piWrapper = new QueryWrapper<>();
        piWrapper.eq("status", "submitted")
                 .isNull("objective_score");

        List<PaperInstance> pendingPapers = paperInstanceMapper.selectList(piWrapper);

        // 按 examId 分组统计
        Map<Long, Long> examIdToUngradedCount = pendingPapers.stream()
                .collect(Collectors.groupingBy(PaperInstance::getExamId, Collectors.counting()));

        return examIdToUngradedCount.entrySet().stream().map(entry -> {
            Long examId = entry.getKey();
            int ungradedCount = entry.getValue().intValue();

            Exam exam = examMapper.selectById(examId);
            PendingExamVO vo = new PendingExamVO();
            vo.setExamId(examId);
            vo.setExamName(exam != null ? exam.getExamName() : "");
            vo.setEndTime(exam != null ? exam.getEndTime() : null);
            vo.setUngradedCount(ungradedCount);

            // 统计已提交总数（用于展示）
            QueryWrapper<PaperInstance> submittedCountWrapper = new QueryWrapper<>();
            submittedCountWrapper.eq("exam_id", examId)
                                 .eq("status", "submitted");
            int submittedCount = Math.toIntExact(paperInstanceMapper.selectCount(submittedCountWrapper));
            vo.setSubmittedCount(submittedCount);

            return vo;
        }).sorted(Comparator.comparing(PendingExamVO::getEndTime, Comparator.nullsLast(Comparator.naturalOrder())))
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
            if ("subjective".equalsIgnoreCase(question.getType())) {
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
            
            BigDecimal actualScore = computeObjectiveScore(question, record.getStudentAnswer(), examQuestion.getScore());
            totalScore = totalScore.add(actualScore);
            
            // 更新答题记录（满分记为正确，否则记为不完全正确）
            record.setIsCorrect(actualScore != null && actualScore.compareTo(examQuestion.getScore()) == 0 ? 1 : 0);
            record.setActualScore(actualScore);
            record.setUpdateTime(LocalDateTime.now());
            answerRecordMapper.updateById(record);
        }
        
        return totalScore;
    }

    /**
     * 检查答案是否正确
     */
    private BigDecimal computeObjectiveScore(Question question, String studentAnswer, BigDecimal fullScore) {
        if (question == null || fullScore == null || fullScore.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        String qType = question.getType() == null ? "" : question.getType().trim().toLowerCase();
        String correct = question.getCorrectAnswer();
        if (correct == null || correct.trim().isEmpty()) {
            return BigDecimal.ZERO;
        }
        String answer = studentAnswer == null ? "" : studentAnswer;
        
        // 单选/判断：完全匹配给满分，否则0分
        if ("single".equals(qType) || "single_choice".equals(qType)
                || "judge".equals(qType) || "true_false".equals(qType)) {
            return correct.trim().equalsIgnoreCase(answer.trim()) ? fullScore : BigDecimal.ZERO;
        }
        
        // 多选：
        // - 任一错误选项（不在正确集合中）→ 0分
        // - 选中了正确集合的非空子集（不全）→ 0.5×满分
        // - 全部正确且不多选 → 满分
        if ("multiple".equals(qType) || "multiple_choice".equals(qType)) {
            Set<String> correctSet = normalizeOptionSet(correct);
            Set<String> studentSet = normalizeOptionSet(answer);
            if (studentSet.isEmpty()) {
                return BigDecimal.ZERO;
            }
            // 有任何不在正确集合的选项 → 0分
            for (String s : studentSet) {
                if (!correctSet.contains(s)) {
                    return BigDecimal.ZERO;
                }
            }
            // 全部命中
            if (studentSet.equals(correctSet)) {
                return fullScore;
            }
            // 正确子集，给一半分
            return fullScore.multiply(new BigDecimal("0.5")).setScale(2, RoundingMode.HALF_UP);
        }
        
        return BigDecimal.ZERO;
    }

    /**
     * 规范化选项集合
     * 将字符串格式的选项转换为标准化的选项集合（Set）
     * 
     * @param raw 原始选项字符串，支持两种格式：
     *            - 逗号分隔："A,C,D" 或 "A, C, D"
     *            - 连续字符："ACD" 或 "acd"
     * @return 规范化后的选项集合，每个选项都是大写字母（如 ["A", "C", "D"]）
     *         如果输入为 null，返回空集合
     * 
     * 处理逻辑：
     * 1. 去除空格，转换为大写
     * 2. 移除所有非字母、非逗号的字符
     * 3. 如果包含逗号，按逗号分割
     * 4. 如果不包含逗号，按字符逐个解析（只保留 A-Z 的字母）
     * 
     * 示例：
     * - "A,C,D" -> ["A", "C", "D"]
     * - "ACD" -> ["A", "C", "D"]
     * - "a, c, d" -> ["A", "C", "D"]
     * - "A B C" -> ["A", "B", "C"]
     */
    private Set<String> normalizeOptionSet(String raw) {
        if (raw == null) return Collections.emptySet();
        String cleaned = raw.trim().toUpperCase().replaceAll("[^A-Z,]", "");
        // 支持"ACD"或"A,C,D"两种形式
        Set<String> set = new HashSet<>();
        if (cleaned.contains(",")) {
            for (String part : cleaned.split(",")) {
                if (!part.isEmpty()) set.add(part);
            }
        } else {
            for (char c : cleaned.toCharArray()) {
                if (c >= 'A' && c <= 'Z') set.add(String.valueOf(c));
            }
        }
        return set;
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
            if (question != null) {
                String t = question.getType() == null ? "" : question.getType().trim().toLowerCase();
                if ("subjective".equals(t) || "essay".equals(t)) {
                    return true;
                }
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
                if (question != null) {
                    String t = question.getType() == null ? "" : question.getType().trim().toLowerCase();
                    if ("subjective".equals(t) || "essay".equals(t)) {
                        subjectiveCount++;
                    }
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
        
        // 获取本试卷实例所有题
        QueryWrapper<ExamQuestion> eqWrapper = new QueryWrapper<>();
        eqWrapper.eq("exam_id", paperInstance.getExamId())
                 .orderByAsc("question_number");
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(eqWrapper);

        //获取本试卷所有答题记录
        QueryWrapper<AnswerRecord> arWrapper = new QueryWrapper<>();
        arWrapper.eq("paper_instance_id", paperInstanceId);
        List<AnswerRecord> answerRecords = answerRecordMapper.selectList(arWrapper);
        
        //所有主观题集合
        List<GradingDetailVO.SubjectiveQuestion> subjectiveQuestions = new ArrayList<>();
        
        for (ExamQuestion eq : examQuestions) {
            Question question = questionMapper.selectById(eq.getQuestionId());
            
            if (question == null) {
                continue;
            }
            String t = question.getType() == null ? "" : question.getType().trim().toLowerCase();
            if (!("subjective".equals(t) || "essay".equals(t))) {
                continue;
            }
            
            // 找到对应的答题记录
            AnswerRecord answerRecord = answerRecords.stream()
                    .filter(ar -> ar.getQuestionId().equals(eq.getQuestionId()))
                    .findFirst() //找到第一个匹配的元素(从前到后处理)
                    .orElse(null); //如果有值返回值，没有返回null

            //答题详情对象
            GradingDetailVO.SubjectiveQuestion sq = new GradingDetailVO.SubjectiveQuestion();

            sq.setQuestionId(question.getQuestionId());
            sq.setQuestionNumber(eq.getQuestionNumber());
            sq.setContent(question.getContent());
            sq.setScore(eq.getScore());
            sq.setReferenceAnswer(question.getReferenceAnswer());

            //如果有答题记录就设置学生答案，实际分数，教师评语进详情对象
            if (answerRecord != null) {
                sq.setStudentAnswer(answerRecord.getStudentAnswer());
                sq.setActualScore(answerRecord.getActualScore());
                sq.setComment(answerRecord.getTeacherComment());
            }

            //加入主观题集合
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
        // 总排名（所有班级）
        QueryWrapper<Score> overallWrapper = new QueryWrapper<>();
        overallWrapper.eq("exam_id", examId)
                      .orderByDesc("total_score");
        List<Score> allScores = scoreMapper.selectList(overallWrapper);
        int rank = 1;
        for (Score s : allScores) {
            s.setRank(rank++);
            scoreMapper.updateById(s);
        }

        // 班级内排名（按 class_id 分组）
        Map<Long, List<Score>> classToScores = new HashMap<>();
        for (Score s : allScores) {
            if (s.getClassId() == null) continue;
            classToScores.computeIfAbsent(s.getClassId(), k -> new ArrayList<>()).add(s);
        }
        for (Map.Entry<Long, List<Score>> entry : classToScores.entrySet()) {
            List<Score> classScores = entry.getValue();
            // 已经按总分降序排列，无需再次排序
            int classRank = 1;
            for (Score s : classScores) {
                s.setClassRank(classRank++);
                scoreMapper.updateById(s);
            }
        }
    }
}

