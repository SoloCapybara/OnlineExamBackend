package com.dxd.onlineexam.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.dxd.onlineexam.entity.*;
import com.dxd.onlineexam.mapper.*;
import com.dxd.onlineexam.vo.StatisticsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsService {
    
    private final ExamMapper examMapper;
    private final ScoreMapper scoreMapper;
    private final PaperInstanceMapper paperInstanceMapper;
    private final AnswerRecordMapper answerRecordMapper;
    private final ExamQuestionMapper examQuestionMapper;
    private final QuestionMapper questionMapper;
    private final ClassMapper classMapper;

    /**
     * 获取考试统计数据
     */
    public StatisticsVO getExamStatistics(Long examId) {
        Exam exam = examMapper.selectById(examId);
        if (exam == null) {
            throw new RuntimeException("考试不存在");
        }
        
        StatisticsVO vo = new StatisticsVO();
        vo.setExamName(exam.getExamName());
        
        // 参与人数
        QueryWrapper<PaperInstance> piWrapper = new QueryWrapper<>();
        piWrapper.eq("exam_id", examId);
        int participantCount = Math.toIntExact(paperInstanceMapper.selectCount(piWrapper));
        vo.setParticipantCount(participantCount);
        
        // 已提交人数
        QueryWrapper<PaperInstance> submittedWrapper = new QueryWrapper<>();
        submittedWrapper.eq("exam_id", examId)
                       .eq("status", "submitted");
        int submittedCount = Math.toIntExact(paperInstanceMapper.selectCount(submittedWrapper));
        vo.setSubmittedCount(submittedCount);
        
        // 获取所有成绩
        QueryWrapper<Score> scoreWrapper = new QueryWrapper<>();
        scoreWrapper.eq("exam_id", examId)
                   .isNotNull("total_score");
        List<Score> scores = scoreMapper.selectList(scoreWrapper);
        
        if (scores.isEmpty()) {
            vo.setAvgScore(BigDecimal.ZERO);
            vo.setMaxScore(BigDecimal.ZERO);
            vo.setMinScore(BigDecimal.ZERO);
            vo.setPassRate(BigDecimal.ZERO);
            vo.setExcellentRate(BigDecimal.ZERO);
            vo.setScoreDistribution(new ArrayList<>());
            vo.setQuestionAnalysis(new ArrayList<>());
            return vo;
        }
        
        // 计算平均分、最高分、最低分
        BigDecimal totalScore = BigDecimal.ZERO;
        BigDecimal maxScore = scores.get(0).getTotalScore();
        BigDecimal minScore = scores.get(0).getTotalScore();
        
        for (Score score : scores) {
            BigDecimal ts = score.getTotalScore();
            totalScore = totalScore.add(ts);
            if (ts.compareTo(maxScore) > 0) {
                maxScore = ts;
            }
            if (ts.compareTo(minScore) < 0) {
                minScore = ts;
            }
        }
        
        BigDecimal avgScore = totalScore.divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        vo.setAvgScore(avgScore);
        vo.setMaxScore(maxScore);
        vo.setMinScore(minScore);
        
        // 及格率和优秀率
        BigDecimal passingScore = exam.getPassingScore();
        long passedCount = scores.stream()
                .filter(s -> s.getTotalScore().compareTo(passingScore) >= 0)
                .count();
        
        BigDecimal excellentThreshold = exam.getTotalScore().multiply(new BigDecimal("0.9"));
        long excellentCount = scores.stream()
                .filter(s -> s.getTotalScore().compareTo(excellentThreshold) >= 0)
                .count();
        
        BigDecimal passRate = BigDecimal.valueOf(passedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        
        BigDecimal excellentRate = BigDecimal.valueOf(excellentCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(scores.size()), 2, RoundingMode.HALF_UP);
        
        vo.setPassRate(passRate);
        vo.setExcellentRate(excellentRate);
        
        // 分数段分布
        List<StatisticsVO.ScoreDistribution> distribution = new ArrayList<>();
        distribution.add(createScoreDistribution("0-59", scores, BigDecimal.ZERO, new BigDecimal("59.99")));
        distribution.add(createScoreDistribution("60-69", scores, new BigDecimal("60"), new BigDecimal("69.99")));
        distribution.add(createScoreDistribution("70-79", scores, new BigDecimal("70"), new BigDecimal("79.99")));
        distribution.add(createScoreDistribution("80-89", scores, new BigDecimal("80"), new BigDecimal("89.99")));
        distribution.add(createScoreDistribution("90-100", scores, new BigDecimal("90"), new BigDecimal("100")));
        vo.setScoreDistribution(distribution);
        
        // 题目分析
        List<StatisticsVO.QuestionAnalysis> questionAnalysis = analyzeQuestions(examId);
        vo.setQuestionAnalysis(questionAnalysis);
        
        return vo;
    }

    /**
     * 创建分数段分布
     */
    private StatisticsVO.ScoreDistribution createScoreDistribution(
            String range, List<Score> scores, BigDecimal min, BigDecimal max) {
        
        int count = (int) scores.stream()
                .filter(s -> s.getTotalScore().compareTo(min) >= 0 
                          && s.getTotalScore().compareTo(max) <= 0)
                .count();
        
        StatisticsVO.ScoreDistribution dist = new StatisticsVO.ScoreDistribution();
        dist.setRange(range);
        dist.setCount(count);
        return dist;
    }

    /**
     * 分析题目
     */
    private List<StatisticsVO.QuestionAnalysis> analyzeQuestions(Long examId) {
        QueryWrapper<ExamQuestion> eqWrapper = new QueryWrapper<>();
        eqWrapper.eq("exam_id", examId)
                 .orderByAsc("question_number");
        List<ExamQuestion> examQuestions = examQuestionMapper.selectList(eqWrapper);
        
        // 获取所有已提交的试卷
        QueryWrapper<PaperInstance> piWrapper = new QueryWrapper<>();
        piWrapper.eq("exam_id", examId)
                 .eq("status", "submitted");
        List<PaperInstance> paperInstances = paperInstanceMapper.selectList(piWrapper);
        
        if (paperInstances.isEmpty()) {
            return new ArrayList<>();
        }
        
        return examQuestions.stream().map(eq -> {
            Question question = questionMapper.selectById(eq.getQuestionId());
            
            StatisticsVO.QuestionAnalysis analysis = new StatisticsVO.QuestionAnalysis();
            analysis.setQuestionNumber(eq.getQuestionNumber());
            analysis.setQuestionType(question.getType());
            analysis.setFullScore(eq.getScore());
            
            // 获取该题的所有答题记录
            List<AnswerRecord> questionRecords = new ArrayList<>();
            for (PaperInstance pi : paperInstances) {
                QueryWrapper<AnswerRecord> arWrapper = new QueryWrapper<>();
                arWrapper.eq("paper_instance_id", pi.getPaperInstanceId())
                        .eq("question_id", eq.getQuestionId());
                AnswerRecord record = answerRecordMapper.selectOne(arWrapper);
                if (record != null && record.getActualScore() != null) {
                    questionRecords.add(record);
                }
            }
            
            if (questionRecords.isEmpty()) {
                analysis.setCorrectRate(BigDecimal.ZERO);
                analysis.setAvgScore(BigDecimal.ZERO);
                return analysis;
            }
            
            // 计算正确率（客观题）
            if (!"subjective".equals(question.getType())) {
                long correctCount = questionRecords.stream()
                        .filter(r -> r.getIsCorrect() != null && r.getIsCorrect() == 1)
                        .count();
                
                BigDecimal correctRate = BigDecimal.valueOf(correctCount)
                        .multiply(BigDecimal.valueOf(100))
                        .divide(BigDecimal.valueOf(questionRecords.size()), 2, RoundingMode.HALF_UP);
                
                analysis.setCorrectRate(correctRate);
            }
            
            // 计算平均分
            BigDecimal totalScore = BigDecimal.ZERO;
            for (AnswerRecord record : questionRecords) {
                totalScore = totalScore.add(record.getActualScore());
            }
            
            BigDecimal avgScore = totalScore.divide(
                    BigDecimal.valueOf(questionRecords.size()), 
                    2, 
                    RoundingMode.HALF_UP);
            
            analysis.setAvgScore(avgScore);
            
            return analysis;
        }).collect(Collectors.toList());
    }

    /**
     * 获取班级对比数据
     */
    public List<Map<String, Object>> getClassComparison(Long examId) {
        // 获取该考试的所有成绩，按班级分组
        QueryWrapper<Score> scoreWrapper = new QueryWrapper<>();
        scoreWrapper.eq("exam_id", examId)
                   .isNotNull("total_score")
                   .isNotNull("class_id");
        List<Score> scores = scoreMapper.selectList(scoreWrapper);
        
        // 按班级分组
        Map<Long, List<Score>> scoresByClass = scores.stream()
                .collect(Collectors.groupingBy(Score::getClassId));

        //总的统计结果集合，用于返回
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Map.Entry<Long, List<Score>> entry : scoresByClass.entrySet()) {
            Long classId = entry.getKey(); //每个班级的id
            List<Score> classScores = entry.getValue(); //每个班级的所有分数
            
            com.dxd.onlineexam.entity.Class clazz = classMapper.selectById(classId);
            if (clazz == null) {
                continue;
            }

            //每个班级的统计数据集合
            Map<String, Object> classData = new HashMap<>();
            classData.put("classId", classId);
            classData.put("className", clazz.getClassName());
            classData.put("participantCount", classScores.size());
            
            // 计算平均分
            BigDecimal totalScore = BigDecimal.ZERO;
            BigDecimal maxScore = classScores.get(0).getTotalScore();
            BigDecimal minScore = classScores.get(0).getTotalScore();
            
            for (Score score : classScores) {
                BigDecimal ts = score.getTotalScore();
                totalScore = totalScore.add(ts);
                if (ts.compareTo(maxScore) > 0) {
                    maxScore = ts;
                }
                if (ts.compareTo(minScore) < 0) {
                    minScore = ts;
                }
            }
            
            BigDecimal avgScore = totalScore.divide(
                    BigDecimal.valueOf(classScores.size()), 
                    2, 
                    RoundingMode.HALF_UP);
            
            classData.put("avgScore", avgScore);
            classData.put("maxScore", maxScore);
            classData.put("minScore", minScore);
            
            // 计算及格率
            Exam exam = examMapper.selectById(examId);
            BigDecimal passingScore = exam != null ? exam.getPassingScore() : BigDecimal.ZERO;
            long passedCount = classScores.stream()
                    .filter(s -> s.getTotalScore().compareTo(passingScore) >= 0)
                    .count();
            
            BigDecimal passRate = BigDecimal.valueOf(passedCount)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(classScores.size()), 2, RoundingMode.HALF_UP);
            
            classData.put("passRate", passRate);

            //加入到统计数据集
            result.add(classData);
        }
        
        // 按平均分降序排序
        result.sort((a, b) -> {
            BigDecimal avgA = (BigDecimal) a.get("avgScore"); //a的平局分
            BigDecimal avgB = (BigDecimal) b.get("avgScore"); //b的平均分
            //avgB.compareTo(avgA) > 0 表示b的平均分大于a的平均分，返回1,
            //avgB.compareTo(avgA) < 0 表示b的平均分小于a的平均分，返回-1,
            //avgB.compareTo(avgA) == 0 表示b的平均分等于a的平均分，返回0
            return avgB.compareTo(avgA); 
        });
        
        return result;
    }
}

