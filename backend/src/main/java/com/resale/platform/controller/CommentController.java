package com.resale.platform.controller;

import com.resale.platform.common.Result;
import com.resale.platform.entity.Comment;
import com.resale.platform.entity.User;
import com.resale.platform.mapper.CommentMapper;
import com.resale.platform.mapper.UserMapper;
import com.resale.platform.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @GetMapping("/product/{productId}")
    public Result<List<Map<String, Object>>> getComments(
            @PathVariable Long productId,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        List<Comment> rootComments = commentMapper.findRootByGoodsId(productId);
        List<Map<String, Object>> result = rootComments.stream().map(comment -> {
            Map<String, Object> map = commentToMap(comment);
            List<Comment> replies = commentMapper.findReplies(comment.getId());
            map.put("replies", replies.stream().map(this::commentToMap).collect(Collectors.toList()));
            return map;
        }).collect(Collectors.toList());
        return Result.success(result);
    }

    @PostMapping
    public Result<Map<String, Object>> addComment(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        Comment comment = new Comment();
        comment.setGoodsId(Long.valueOf(body.get("productId").toString()));
        comment.setUserId(userId);
        comment.setContent((String) body.get("content"));
        comment.setParentId(body.get("parentId") != null ? Long.valueOf(body.get("parentId").toString()) : 0L);
        comment.setReplyToId(body.get("replyToId") != null ? Long.valueOf(body.get("replyToId").toString()) : 0L);
        comment.setRating(body.get("rating") != null ? Integer.valueOf(body.get("rating").toString()) : 5);
        comment.setLikeCount(0);
        comment.setIsDeleted(0);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        return Result.success(commentToMap(comment));
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteComment(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Comment comment = commentMapper.selectById(id);
        if (comment == null) return Result.error(404, "评论不存在");
        if (!comment.getUserId().equals(userId)) return Result.error(403, "无权删除此评论");
        comment.setIsDeleted(1);
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.updateById(comment);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<Map<String, Object>>> getMyComments() {
        Long userId = getCurrentUserId();
        List<Comment> comments = commentMapper.findByUserId(userId);
        List<Map<String, Object>> result = comments.stream().map(this::commentToMap).collect(Collectors.toList());
        return Result.success(result);
    }

    @GetMapping("/product/{productId}/stats")
    public Result<Map<String, Object>> getCommentStats(@PathVariable Long productId) {
        int count = commentMapper.countByGoodsId(productId);
        Double avgRating = commentMapper.avgRatingByGoodsId(productId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", count);
        stats.put("avgRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 5.0);
        return Result.success(stats);
    }

    @PostMapping("/{id}/like")
    public Result<Void> likeComment(@PathVariable Long id) {
        commentMapper.incrementLikeCount(id);
        return Result.success();
    }

    private Map<String, Object> commentToMap(Comment comment) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", comment.getId());
        map.put("productId", comment.getGoodsId());
        map.put("userId", comment.getUserId());
        map.put("parentId", comment.getParentId());
        map.put("replyToId", comment.getReplyToId());
        map.put("content", comment.getContent());
        map.put("rating", comment.getRating());
        map.put("likeCount", comment.getLikeCount());
        map.put("createdAt", comment.getCreatedAt());

        User user = userMapper.selectById(comment.getUserId());
        if (user != null) {
            map.put("username", user.getNickname() != null ? user.getNickname() : user.getUsername());
            map.put("avatar", user.getAvatar());
        }
        if (comment.getReplyToId() != null && comment.getReplyToId() > 0) {
            User replyToUser = userMapper.selectById(comment.getReplyToId());
            if (replyToUser != null) {
                map.put("replyToName", replyToUser.getNickname() != null ? replyToUser.getNickname() : replyToUser.getUsername());
            }
        }
        return map;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
