package com.resale.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resale.platform.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    User selectByUsername(String username);

    User selectByMobile(String mobile);

    User selectByEmail(String email);

    User selectByWechatOpenid(String openid);

    @Select("SELECT id AS userId, username, password, nickname, avatar, mobile, email, role, is_deleted AS isDeleted, created_at AS createdAt, updated_at AS updatedAt, last_login_time AS lastLoginTime, last_login_ip AS lastLoginIp FROM users WHERE is_deleted = 0 ORDER BY created_at DESC")
    List<User> selectAll();

    @Select("SELECT id AS userId, username, password, nickname, avatar, mobile, email, role, is_deleted AS isDeleted, created_at AS createdAt, updated_at AS updatedAt, last_login_time AS lastLoginTime, last_login_ip AS lastLoginIp FROM users WHERE role = #{role} AND is_deleted = 0 ORDER BY created_at DESC")
    List<User> selectByRole(@Param("role") String role);

    @Update("UPDATE users SET last_login_time = #{lastLoginTime}, last_login_ip = #{lastLoginIp} WHERE id = #{userId}")
    void updateLoginInfo(@Param("userId") Long userId, @Param("lastLoginTime") java.time.LocalDateTime lastLoginTime, @Param("lastLoginIp") String lastLoginIp);
}
