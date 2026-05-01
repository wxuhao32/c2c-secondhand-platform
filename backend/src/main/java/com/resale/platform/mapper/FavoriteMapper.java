package com.resale.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resale.platform.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Delete;

import java.util.List;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT * FROM favorite WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<Favorite> findByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId} AND goods_id = #{goodsId} AND is_deleted = 0")
    int countByUserAndGoods(@Param("userId") Long userId, @Param("goodsId") Long goodsId);
}
