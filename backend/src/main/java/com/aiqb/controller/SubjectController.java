package com.aiqb.controller;

import com.aiqb.common.Result;
import com.aiqb.entity.Subject;
import com.aiqb.mapper.SubjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "科目管理")
@RestController
@RequestMapping("/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectMapper subjectMapper;

    @GetMapping
    public Result<List<Subject>> list() {
        return Result.success(subjectMapper.selectList(null));
    }
}
