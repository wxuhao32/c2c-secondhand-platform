package com.resale.platform.service;

import com.resale.platform.entity.Goods;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ProductService {

    List<Goods> getProductList(Long categoryId, String keyword, Integer status);

    Goods getProductDetail(Long id);

    Goods publishProduct(Long userId, Map<String, Object> body);

    Goods updateProduct(Long userId, Long id, Map<String, Object> body);

    void deleteProduct(Long userId, Long id);

    List<Goods> getMyProducts(Long userId);

    void updateProductStatus(Long userId, Long id, Integer status);
}
