package com.resale.platform.service.impl;

import com.resale.platform.common.BusinessException;
import com.resale.platform.common.ExceptionEnum;
import com.resale.platform.entity.Comment;
import com.resale.platform.entity.User;
import com.resale.platform.mapper.CommentMapper;
import com.resale.platform.mapper.UserMapper;
import com.resale.platform.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final UserMapper userMapper;

    @Override
    public List<Map<String, Object>> getComments(Long productId, Integer page, Integer size) {
        List<Comment> rootComments = commentMapper.findRootByGoodsId(productId);
        return rootComments.stream().map(comment -> {
            Map<String, Object> map = commentToMap(comment);
            List<Comment> replies = commentMapper.findReplies(comment.getId());
            map.put("replies", replies.stream().map(this::commentToMap).collect(Collectors.toList()));
            return map;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Map<String, Object> addComment(Long userId, Map<String, Object> body) {
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
        return commentToMap(comment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long id) {
        Comment comment = commentMapper.selectById(id);
        if (comment == null) throw new BusinessException(ExceptionEnum.NOT_FOUND);
        if (!comment.getUserId().equals(userId)) throw new BusinessException(ExceptionEnum.FORBIDDEN);
        comment.setIsDeleted(1);
        comment.setUpdatedAt(LocalDateTime.now());
        commentMapper.updateById(comment);
    }

    @Override
    public List<Map<String, Object>> getMyComments(Long userId) {
        List<Comment> comments = commentMapper.findByUserId(userId);
        return comments.stream().map(this::commentToMap).collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getCommentStats(Long productId) {
        int count = commentMapper.countByGoodsId(productId);
        Double avgRating = commentMapper.avgRatingByGoodsId(productId);
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCount", count);
        stats.put("avgRating", avgRating != null ? Math.round(avgRating * 10) / 10.0 : 5.0);
        return stats;
    }

    @Override
    public void likeComment(Long id) {
        commentMapper.incrementLikeCount(id);
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
}
