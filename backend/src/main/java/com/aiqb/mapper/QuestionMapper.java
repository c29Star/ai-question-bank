package com.aiqb.mapper;

import com.aiqb.entity.Question;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 随机抽题（按科目、题型、难度、数量、知识点）
     */
    List<Question> randomPick(@Param("subjectId") Long subjectId,
                              @Param("type") String type,
                              @Param("difficulty") Integer difficulty,
                              @Param("knowledgePoint") String knowledgePoint,
                              @Param("limit") Integer limit);

    /**
     * 按知识点统计错题数
     */
    @Select("SELECT knowledge_point AS name, COUNT(*) AS value " +
            "FROM wrong_questions wq JOIN questions q ON wq.question_id = q.id " +
            "WHERE wq.user_id = #{userId} AND wq.mastered = 0 " +
            "GROUP BY knowledge_point")
    List<Map<String, Object>> countByKnowledge(@Param("userId") Long userId);
}
