package com.resale.platform.service;

import com.resale.platform.entity.Comment;

import java.util.List;
import java.util.Map;

public interface CommentService {

    List<Map<String, Object>> getComments(Long productId, Integer page, Integer size);

    Map<String, Object> addComment(Long userId, Map<String, Object> body);

    void deleteComment(Long userId, Long id);

    List<Map<String, Object>> getMyComments(Long userId);

    Map<String, Object> getCommentStats(Long productId);

    void likeComment(Long id);
}
