package com.dxd.onlineexam.service;

import com.dxd.onlineexam.entity.Class;
import com.dxd.onlineexam.entity.Subject;
import com.dxd.onlineexam.mapper.ClassMapper;
import com.dxd.onlineexam.mapper.SubjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommonService {
    
    private final ClassMapper classMapper;
    private final SubjectMapper subjectMapper;

    /**
     * 获取班级列表
     */
    public List<Class> getClassList() {
        return classMapper.selectList(null);
    }

    /**
     * 获取科目列表
     */
    public List<Subject> getSubjectList() {
        return subjectMapper.selectList(null);
    }
}

