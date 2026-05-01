package com.resale.platform.controller;

import com.resale.platform.common.AuditLog;
import com.resale.platform.common.Result;
import com.resale.platform.entity.Category;
import com.resale.platform.entity.Goods;
import com.resale.platform.mapper.CategoryMapper;
import com.resale.platform.security.SecurityUser;
import com.resale.platform.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品发布、查询、修改、删除等接口")
public class ProductController {

    private final ProductService productService;
    private final CategoryMapper categoryMapper;

    @GetMapping
    @Operation(summary = "获取商品列表", description = "支持按分类、关键词、状态筛选")
    public Result<List<Goods>> getProductList(
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "商品状态") @RequestParam(required = false) Integer status) {
        List<Goods> products = productService.getProductList(categoryId, keyword, status);
        return Result.success(products);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取商品详情", description = "根据ID获取商品详细信息")
    public Result<Goods> getProductDetail(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        Goods goods = productService.getProductDetail(id);
        return Result.success(goods);
    }

    @PostMapping
    @Operation(summary = "发布商品", description = "卖家发布新商品")
    @AuditLog(module = "商品", action = "发布", description = "卖家发布新商品")
    public Result<Goods> publishProduct(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        Goods goods = productService.publishProduct(userId, body);
        return Result.success(goods);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新商品", description = "卖家修改自己的商品信息")
    @AuditLog(module = "商品", action = "修改", description = "卖家修改商品信息")
    public Result<Goods> updateProduct(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        Goods goods = productService.updateProduct(userId, id, body);
        return Result.success(goods);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除商品", description = "卖家删除自己的商品（软删除）")
    @AuditLog(module = "商品", action = "删除", description = "卖家删除商品")
    public Result<Void> deleteProduct(
            @Parameter(description = "商品ID") @PathVariable Long id) {
        Long userId = getCurrentUserId();
        productService.deleteProduct(userId, id);
        return Result.success();
    }

    @GetMapping("/my")
    @Operation(summary = "获取我的商品", description = "获取当前用户发布的所有商品")
    public Result<List<Goods>> getMyProducts() {
        Long userId = getCurrentUserId();
        List<Goods> products = productService.getMyProducts(userId);
        return Result.success(products);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新商品状态", description = "上架/下架商品")
    @AuditLog(module = "商品", action = "状态变更", description = "卖家更新商品状态")
    public Result<Void> updateProductStatus(
            @Parameter(description = "商品ID") @PathVariable Long id,
            @RequestBody Map<String, Integer> body) {
        Long userId = getCurrentUserId();
        productService.updateProductStatus(userId, id, body.get("status"));
        return Result.success();
    }

    @GetMapping("/categories")
    @Operation(summary = "获取商品分类", description = "获取所有商品分类列表")
    public Result<List<Category>> getCategories() {
        List<Category> categories = categoryMapper.findAll();
        return Result.success(categories);
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "按分类获取商品", description = "根据分类ID获取商品列表")
    public Result<List<Goods>> getProductsByCategory(
            @Parameter(description = "分类ID") @PathVariable Long categoryId) {
        List<Goods> products = productService.getProductList(categoryId, null, null);
        return Result.success(products);
    }

    @PostMapping("/upload")
    @Operation(summary = "上传商品图片", description = "上传商品图片（占位接口）")
    public Result<Map<String, String>> uploadImage() {
        return Result.success(Map.of("url", "/uploads/placeholder.jpg"));
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
