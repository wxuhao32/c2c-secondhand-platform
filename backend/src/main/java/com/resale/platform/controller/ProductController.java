package com.resale.platform.controller;

import com.resale.platform.common.Result;
import com.resale.platform.entity.Goods;
import com.resale.platform.entity.Category;
import com.resale.platform.mapper.GoodsMapper;
import com.resale.platform.mapper.CategoryMapper;
import com.resale.platform.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final GoodsMapper goodsMapper;
    private final CategoryMapper categoryMapper;

    @GetMapping
    public Result<List<Goods>> getProductList(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        List<Goods> products;
        if (keyword != null && !keyword.isEmpty()) {
            products = goodsMapper.searchByKeyword(keyword);
        } else if (categoryId != null) {
            products = goodsMapper.findByCategoryId(categoryId);
        } else if (status != null) {
            products = goodsMapper.findOnSale();
        } else {
            products = goodsMapper.findOnSale();
        }
        return Result.success(products);
    }

    @GetMapping("/{id}")
    public Result<Goods> getProductDetail(@PathVariable Long id) {
        Goods goods = goodsMapper.selectById(id);
        if (goods == null || goods.getIsDeleted() == 1) {
            return Result.error(404, "商品不存在");
        }
        goodsMapper.incrementViewCount(id);
        goods.setViewCount(goods.getViewCount() + 1);
        return Result.success(goods);
    }

    @PostMapping
    public Result<Goods> publishProduct(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
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
        return Result.success(goods);
    }

    @PutMapping("/{id}")
    public Result<Goods> updateProduct(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        Goods goods = goodsMapper.selectById(id);
        if (goods == null || goods.getIsDeleted() == 1) {
            return Result.error(404, "商品不存在");
        }
        if (!goods.getSellerId().equals(userId)) {
            return Result.error(403, "无权修改此商品");
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
        return Result.success(goods);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        Goods goods = goodsMapper.selectById(id);
        if (goods == null) {
            return Result.error(404, "商品不存在");
        }
        if (!goods.getSellerId().equals(userId)) {
            return Result.error(403, "无权删除此商品");
        }
        goods.setIsDeleted(1);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);
        return Result.success();
    }

    @GetMapping("/my")
    public Result<List<Goods>> getMyProducts() {
        Long userId = getCurrentUserId();
        List<Goods> products = goodsMapper.findBySellerId(userId);
        return Result.success(products);
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateProductStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Long userId = getCurrentUserId();
        Goods goods = goodsMapper.selectById(id);
        if (goods == null || goods.getIsDeleted() == 1) {
            return Result.error(404, "商品不存在");
        }
        if (!goods.getSellerId().equals(userId)) {
            return Result.error(403, "无权操作此商品");
        }
        Integer status = body.get("status");
        if (status != null) {
            goods.setStatus(status);
            goods.setUpdatedAt(LocalDateTime.now());
            goodsMapper.updateById(goods);
        }
        return Result.success();
    }

    @GetMapping("/categories")
    public Result<List<Category>> getCategories() {
        List<Category> categories = categoryMapper.findAll();
        return Result.success(categories);
    }

    @GetMapping("/category/{categoryId}")
    public Result<List<Goods>> getProductsByCategory(@PathVariable Long categoryId) {
        List<Goods> products = goodsMapper.findByCategoryId(categoryId);
        return Result.success(products);
    }

    @PostMapping("/upload")
    public Result<Map<String, String>> uploadImage() {
        return Result.success(Map.of("url", "/uploads/placeholder.jpg"));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
