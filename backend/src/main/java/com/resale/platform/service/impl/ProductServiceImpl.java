package com.resale.platform.service.impl;

import com.resale.platform.common.BusinessException;
import com.resale.platform.common.ExceptionEnum;
import com.resale.platform.entity.Goods;
import com.resale.platform.mapper.GoodsMapper;
import com.resale.platform.service.CacheService;
import com.resale.platform.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final GoodsMapper goodsMapper;
    private final CacheService cacheService;

    private static final String PRODUCT_CACHE_KEY = "product:";
    private static final String PRODUCT_LIST_CACHE_KEY = "product:list:";

    @Override
    public List<Goods> getProductList(Long categoryId, String keyword, Integer status) {
        String cacheKey = PRODUCT_LIST_CACHE_KEY + (keyword != null ? keyword : "") + ":" + (categoryId != null ? categoryId : "") + ":" + (status != null ? status : "");
        List<Goods> cached = cacheService.get(cacheKey, List.class);
        if (cached != null) return cached;

        List<Goods> products;
        if (keyword != null && !keyword.isEmpty()) {
            products = goodsMapper.searchByKeyword(keyword);
        } else if (categoryId != null) {
            products = goodsMapper.findByCategoryId(categoryId);
        } else {
            products = goodsMapper.findOnSale();
        }

        cacheService.set(cacheKey, products, 5, TimeUnit.MINUTES);
        return products;
    }

    @Override
    public Goods getProductDetail(Long id) {
        String cacheKey = PRODUCT_CACHE_KEY + id;
        Goods cached = cacheService.get(cacheKey, Goods.class);
        if (cached != null) return cached;

        Goods goods = goodsMapper.selectById(id);
        if (goods == null || goods.getIsDeleted() == 1) {
            throw new BusinessException(ExceptionEnum.NOT_FOUND);
        }
        goodsMapper.incrementViewCount(id);
        goods.setViewCount(goods.getViewCount() + 1);

        cacheService.set(cacheKey, goods, 10, TimeUnit.MINUTES);
        return goods;
    }

    @Override
    @Transactional
    public Goods publishProduct(Long userId, Map<String, Object> body) {
        Goods goods = new Goods();
        goods.setSellerId(userId);
        goods.setTitle((String) body.get("title"));
        goods.setDescription((String) body.get("description"));
        goods.setPrice(new BigDecimal(body.get("price").toString()));
        goods.setOriginalPrice(body.get("originalPrice") != null ? new BigDecimal(body.get("originalPrice").toString()) : null);
        goods.setCategoryId(body.get("categoryId") != null ? Long.valueOf(body.get("categoryId").toString()) : null);
        goods.setImages(body.get("images") != null ? body.get("images").toString() : null);
        goods.setStatus(1);
        goods.setViewCount(0);
        goods.setLikeCount(0);
        goods.setIsDeleted(0);
        goods.setCreatedAt(LocalDateTime.now());
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.insert(goods);

        cacheService.deleteByPattern(PRODUCT_LIST_CACHE_KEY + "*");
        return goods;
    }

    @Override
    @Transactional
    public Goods updateProduct(Long userId, Long id, Map<String, Object> body) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null || goods.getIsDeleted() == 1) {
            throw new BusinessException(ExceptionEnum.NOT_FOUND);
        }
        if (!goods.getSellerId().equals(userId)) {
            throw new BusinessException(ExceptionEnum.FORBIDDEN);
        }
        if (body.containsKey("title")) goods.setTitle((String) body.get("title"));
        if (body.containsKey("description")) goods.setDescription((String) body.get("description"));
        if (body.containsKey("price")) goods.setPrice(new BigDecimal(body.get("price").toString()));
        if (body.containsKey("originalPrice"))
            goods.setOriginalPrice(body.get("originalPrice") != null ? new BigDecimal(body.get("originalPrice").toString()) : null);
        if (body.containsKey("categoryId"))
            goods.setCategoryId(body.get("categoryId") != null ? Long.valueOf(body.get("categoryId").toString()) : null);
        if (body.containsKey("images")) goods.setImages(body.get("images").toString());
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);

        cacheService.delete(PRODUCT_CACHE_KEY + id);
        cacheService.deleteByPattern(PRODUCT_LIST_CACHE_KEY + "*");
        return goods;
    }

    @Override
    @Transactional
    public void deleteProduct(Long userId, Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException(ExceptionEnum.NOT_FOUND);
        }
        if (!goods.getSellerId().equals(userId)) {
            throw new BusinessException(ExceptionEnum.FORBIDDEN);
        }
        goods.setIsDeleted(1);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);

        cacheService.delete(PRODUCT_CACHE_KEY + id);
        cacheService.deleteByPattern(PRODUCT_LIST_CACHE_KEY + "*");
    }

    @Override
    public List<Goods> getMyProducts(Long userId) {
        return goodsMapper.findBySellerId(userId);
    }

    @Override
    @Transactional
    public void updateProductStatus(Long userId, Long id, Integer status) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null || goods.getIsDeleted() == 1) {
            throw new BusinessException(ExceptionEnum.NOT_FOUND);
        }
        if (!goods.getSellerId().equals(userId)) {
            throw new BusinessException(ExceptionEnum.FORBIDDEN);
        }
        goods.setStatus(status);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);

        cacheService.delete(PRODUCT_CACHE_KEY + id);
        cacheService.deleteByPattern(PRODUCT_LIST_CACHE_KEY + "*");
    }
}
