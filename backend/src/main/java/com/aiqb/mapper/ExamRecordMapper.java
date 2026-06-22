package com.aiqb.mapper;

import com.aiqb.entity.ExamRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface ExamRecordMapper extends BaseMapper<ExamRecord> {

    @Select("SELECT er.*, u.username, e.title AS exam_title, p.title AS paper_title " +
            "FROM exam_records er " +
            "LEFT JOIN users u ON er.user_id = u.id " +
            "LEFT JOIN exams e ON er.exam_id = e.id " +
            "LEFT JOIN papers p ON er.paper_id = p.id " +
            "WHERE er.user_id = #{userId} " +
            "ORDER BY er.start_time DESC")
    List<Map<String, Object>> selectWithDetail(Long userId);

    @Select("SELECT * FROM exam_records WHERE user_id = #{userId} AND exam_id = #{examId} ORDER BY start_time DESC")
    List<ExamRecord> selectByUserAndExam(Long userId, Long examId);
}
