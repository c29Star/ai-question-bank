package com.aiqb.service;

import com.aiqb.dto.AutoPaperDTO;
import com.aiqb.dto.PaperDTO;
import com.aiqb.entity.Paper;
import com.aiqb.vo.PaperDetailVO;
import com.aiqb.vo.PaperVO;
import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.List;

public interface PaperService {

    IPage<PaperVO> page(Long subjectId, Integer current, Integer size);

    List<PaperVO> listVO(Long subjectId);

    List<Paper> list(Long subjectId);

    PaperDetailVO detail(Long id);

    Long create(PaperDTO dto, Long userId);

    void update(PaperDTO dto);

    void delete(Long id);

    /** 随机组卷 */
    Long autoGenerate(AutoPaperDTO dto, Long userId);
}
