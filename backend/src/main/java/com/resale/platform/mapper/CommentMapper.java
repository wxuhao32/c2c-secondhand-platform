package com.resale.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resale.platform.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Select("SELECT * FROM comment WHERE goods_id = #{goodsId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<Comment> findByGoodsId(@Param("goodsId") Long goodsId);

    @Select("SELECT * FROM comment WHERE goods_id = #{goodsId} AND parent_id = 0 AND is_deleted = 0 ORDER BY created_at DESC")
    List<Comment> findRootByGoodsId(@Param("goodsId") Long goodsId);

    @Select("SELECT * FROM comment WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY created_at ASC")
    List<Comment> findReplies(@Param("parentId") Long parentId);

    @Select("SELECT * FROM comment WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<Comment> findByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM comment WHERE goods_id = #{goodsId} AND is_deleted = 0")
    int countByGoodsId(@Param("goodsId") Long goodsId);

    @Select("SELECT AVG(rating) FROM comment WHERE goods_id = #{goodsId} AND is_deleted = 0 AND rating > 0")
    Double avgRatingByGoodsId(@Param("goodsId") Long goodsId);

    @Update("UPDATE comment SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);
}
