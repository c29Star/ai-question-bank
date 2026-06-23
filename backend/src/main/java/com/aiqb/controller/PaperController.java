package com.aiqb.controller;

import com.aiqb.common.Result;
import com.aiqb.dto.AutoPaperDTO;
import com.aiqb.dto.PaperDTO;
import com.aiqb.security.LoginUser;
import com.aiqb.service.PaperService;
import com.aiqb.vo.PaperDetailVO;
import com.aiqb.vo.PaperVO;
import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "试卷管理")
@RestController
@RequestMapping("/papers")
@RequiredArgsConstructor
public class PaperController {

    private final PaperService paperService;

    @Operation(summary = "分页列表（含 subjectName、questionCount）")
    @GetMapping
    public Result<IPage<PaperVO>> page(
            @RequestParam(required = false) Long subjectId,
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size) {
        return Result.success(paperService.page(subjectId, current, size));
    }

    @Operation(summary = "全部列表（简化 VO）")
    @GetMapping("/list")
    public Result<List<PaperVO>> list(@RequestParam(required = false) Long subjectId) {
        return Result.success(paperService.listVO(subjectId));
    }

    @Operation(summary = "详情（含题目）")
    @GetMapping("/{id}")
    public Result<PaperDetailVO> detail(@PathVariable Long id) {
        return Result.success(paperService.detail(id));
    }

    @Operation(summary = "创建试卷")
    @PostMapping
    public Result<Long> create(@Valid @RequestBody PaperDTO dto, @AuthenticationPrincipal LoginUser user) {
        return Result.success("创建成功", paperService.create(dto, user.getUserId()));
    }

    @Operation(summary = "更新试卷")
    @PutMapping
    public Result<Void> update(@Valid @RequestBody PaperDTO dto) {
        paperService.update(dto);
        return Result.success("更新成功", null);
    }

    @Operation(summary = "删除试卷")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        paperService.delete(id);
        return Result.success("删除成功", null);
    }

    @Operation(summary = "随机组卷")
    @PostMapping("/auto-generate")
    public Result<Long> autoGenerate(@Valid @RequestBody AutoPaperDTO dto, @AuthenticationPrincipal LoginUser user) {
        return Result.success("组卷成功", paperService.autoGenerate(dto, user.getUserId()));
    }
}
