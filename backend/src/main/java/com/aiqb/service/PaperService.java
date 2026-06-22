package com.aiqb.service;

import com.aiqb.dto.AutoPaperDTO;
import com.aiqb.dto.PaperDTO;
import com.aiqb.entity.Paper;
import com.aiqb.vo.PaperDetailVO;

import java.util.List;

public interface PaperService {

    List<Paper> list(Long subjectId);

    PaperDetailVO detail(Long id);

    Long create(PaperDTO dto, Long userId);

    void update(PaperDTO dto);

    void delete(Long id);

    /** 随机组卷 */
    Long autoGenerate(AutoPaperDTO dto, Long userId);
}
