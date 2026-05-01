package com.resale.platform.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.resale.platform.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM orders WHERE buyer_id = #{buyerId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<Order> findByBuyerId(@Param("buyerId") Long buyerId);

    @Select("SELECT * FROM orders WHERE seller_id = #{sellerId} AND is_deleted = 0 ORDER BY created_at DESC")
    List<Order> findBySellerId(@Param("sellerId") Long sellerId);

    @Select("SELECT * FROM orders WHERE buyer_id = #{buyerId} AND status = #{status} AND is_deleted = 0 ORDER BY created_at DESC")
    List<Order> findByBuyerIdAndStatus(@Param("buyerId") Long buyerId, @Param("status") Integer status);

    @Select("SELECT * FROM orders WHERE order_no = #{orderNo} AND is_deleted = 0")
    Order findByOrderNo(@Param("orderNo") String orderNo);
}
