package com.resale.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resale.platform.entity.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {

    @Select("SELECT * FROM category WHERE parent_id = #{parentId} AND is_deleted = 0 ORDER BY sort_order ASC")
    List<Category> findByParentId(@Param("parentId") Long parentId);

    @Select("SELECT * FROM category WHERE is_deleted = 0 ORDER BY sort_order ASC")
    List<Category> findAll();
}
