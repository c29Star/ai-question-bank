package com.aiqb.vo;

import com.aiqb.entity.Paper;
import com.aiqb.entity.Question;
import lombok.Data;

import java.util.List;

@Data
public class PaperDetailVO {
    private Paper paper;
    private List<Question> questions;
}
