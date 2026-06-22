package com.aiqb.mapper;

import com.aiqb.entity.Answer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {

    @Select("SELECT * FROM answers WHERE record_id = #{recordId}")
    List<Answer> selectByRecordId(Long recordId);
}
