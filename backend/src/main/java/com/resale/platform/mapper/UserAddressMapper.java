package com.resale.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resale.platform.entity.UserAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddress> {

    @Select("SELECT * FROM user_address WHERE user_id = #{userId} AND is_deleted = 0 ORDER BY is_default DESC, created_at DESC")
    List<UserAddress> findByUserId(@Param("userId") Long userId);

    @Update("UPDATE user_address SET is_default = 0 WHERE user_id = #{userId}")
    void clearDefault(@Param("userId") Long userId);
}
