package com.resale.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resale.platform.entity.Goods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {

    @Select("SELECT * FROM goods WHERE seller_id = #{sellerId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<Goods> findBySellerId(@Param("sellerId") Long sellerId);

    @Select("SELECT * FROM goods WHERE category_id = #{categoryId} AND is_deleted = 0 AND status = 1 ORDER BY created_at DESC")
    List<Goods> findByCategoryId(@Param("categoryId") Long categoryId);

    @Select("SELECT * FROM goods WHERE status = 1 AND is_deleted = 0 ORDER BY created_at DESC")
    List<Goods> findOnSale();

    @Select("SELECT * FROM goods WHERE title LIKE CONCAT('%',#{keyword},'%') AND is_deleted = 0 AND status = 1")
    List<Goods> searchByKeyword(@Param("keyword") String keyword);

    @Update("UPDATE goods SET view_count = view_count + 1 WHERE id = #{id}")
    void incrementViewCount(@Param("id") Long id);

    @Update("UPDATE goods SET like_count = like_count + 1 WHERE id = #{id}")
    void incrementLikeCount(@Param("id") Long id);

    @Update("UPDATE goods SET like_count = CASE WHEN like_count > 0 THEN like_count - 1 ELSE 0 END WHERE id = #{id}")
    void decrementLikeCount(@Param("id") Long id);
}
