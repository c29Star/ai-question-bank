package com.aiqb.mapper;

import com.aiqb.entity.WrongQuestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface WrongQuestionMapper extends BaseMapper<WrongQuestion> {

    @Select("SELECT wq.*, q.content, q.type, q.knowledge_point, q.difficulty, q.options, q.answer, q.explanation " +
            "FROM wrong_questions wq " +
            "JOIN questions q ON wq.question_id = q.id " +
            "WHERE wq.user_id = #{userId} " +
            "ORDER BY wq.last_wrong_at DESC")
    List<Map<String, Object>> selectWithQuestion(Long userId);
}
