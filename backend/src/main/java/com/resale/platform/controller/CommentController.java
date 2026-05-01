package com.resale.platform.controller;

import com.resale.platform.common.AuditLog;
import com.resale.platform.common.Result;
import com.resale.platform.security.SecurityUser;
import com.resale.platform.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Tag(name = "评论管理", description = "商品评论、回复、点赞等接口")
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/product/{productId}")
    @Operation(summary = "获取商品评论", description = "获取指定商品的评论列表，含回复")
    public Result<List<Map<String, Object>>> getComments(
            @Parameter(description = "商品ID") @PathVariable Long productId,
            @Parameter(description = "页码") @RequestParam(required = false) Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false) Integer size) {
        List<Map<String, Object>> comments = commentService.getComments(productId, page, size);
        return Result.success(comments);
    }

    @PostMapping
    @Operation(summary = "发表评论", description = "对商品发表评论或回复")
    @AuditLog(module = "评论", action = "发表", description = "用户发表评论")
    public Result<Map<String, Object>> addComment(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        Map<String, Object> comment = commentService.addComment(userId, body);
        return Result.success(comment);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除评论", description = "删除自己的评论")
    @AuditLog(module = "评论", action = "删除", description = "用户删除评论")
    public Result<Void> deleteComment(
            @Parameter(description = "评论ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        commentService.deleteComment(userId, id);
        return Result.success();
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的评论", description = "获取当前用户发表的所有评论")
    public Result<List<Map<String, Object>>> getMyComments() {
        Long userId = getCurrentUserId();
        List<Map<String, Object>> comments = commentService.getMyComments(userId);
        return Result.success(comments);
    }

    @GetMapping("/product/{productId}/stats")
    @Operation(summary = "获取评论统计", description = "获取商品评论数量和平均评分")
    public Result<Map<String, Object>> getCommentStats(
            @Parameter(description = "商品ID") @PathVariable Long productId) {
        Map<String, Object> stats = commentService.getCommentStats(productId);
        return Result.success(stats);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "点赞评论", description = "对评论点赞")
    public Result<Void> likeComment(
            @Parameter(description = "评论ID") @PathVariable Long id) {
        commentService.likeComment(id);
        return Result.success();
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
