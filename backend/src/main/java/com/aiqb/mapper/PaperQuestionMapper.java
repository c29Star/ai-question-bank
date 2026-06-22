package com.aiqb.mapper;

import com.aiqb.entity.PaperQuestion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PaperQuestionMapper extends BaseMapper<PaperQuestion> {

    @Select("SELECT * FROM paper_questions WHERE paper_id = #{paperId} ORDER BY sort_order")
    List<PaperQuestion> selectByPaperId(Long paperId);

    @Delete("DELETE FROM paper_questions WHERE paper_id = #{paperId}")
    int deleteByPaperId(Long paperId);
}
