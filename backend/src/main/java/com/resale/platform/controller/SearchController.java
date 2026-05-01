package com.resale.platform.controller;

import com.resale.platform.common.Result;
import com.resale.platform.entity.Goods;
import com.resale.platform.mapper.GoodsMapper;
import com.resale.platform.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "搜索", description = "商品搜索、热搜词、搜索建议等接口")
public class SearchController {

    private final GoodsMapper goodsMapper;
    private final ProductService productService;

    private static final List<String> HOT_KEYWORDS = List.of(
            "iPhone", "MacBook", "相机", "Nike", "沙发", "自行车", "Java", "羽绒服"
    );

    @GetMapping
    @Operation(summary = "搜索商品", description = "按关键词、分类搜索商品，支持排序和分页")
    public Result<Map<String, Object>> searchProducts(
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "排序方式") @RequestParam(required = false) String sort,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "20") Integer size) {
        List<Goods> products;
        if (keyword != null && !keyword.isEmpty()) {
            products = goodsMapper.searchByKeyword(keyword);
        } else if (categoryId != null) {
            products = goodsMapper.findByCategoryId(categoryId);
        } else {
            products = goodsMapper.findOnSale();
        }

        if ("price_asc".equals(sort)) {
            products.sort((a, b) -> a.getPrice().compareTo(b.getPrice()));
        } else if ("price_desc".equals(sort)) {
            products.sort((a, b) -> b.getPrice().compareTo(a.getPrice()));
        } else if ("views".equals(sort)) {
            products.sort((a, b) -> b.getViewCount().compareTo(a.getViewCount()));
        }

        int total = products.size();
        int fromIndex = (page - 1) * size;
        int toIndex = Math.min(fromIndex + size, total);
        List<Goods> paged = fromIndex < total ? products.subList(fromIndex, toIndex) : new ArrayList<>();

        Map<String, Object> result = new HashMap<>();
        result.put("list", paged);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("keyword", keyword);
        return Result.success(result);
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热搜词", description = "获取热门搜索关键词")
    public Result<List<String>> getHotKeywords() {
        return Result.success(HOT_KEYWORDS);
    }

    @GetMapping("/suggest")
    @Operation(summary = "搜索建议", description = "根据输入关键词提供搜索建议")
    public Result<List<String>> getSearchSuggestions(
            @Parameter(description = "输入关键词") @RequestParam String keyword) {
        List<String> suggestions = new ArrayList<>();
        List<Goods> allGoods = goodsMapper.findOnSale();
        for (Goods goods : allGoods) {
            if (goods.getTitle().toLowerCase().contains(keyword.toLowerCase())) {
                suggestions.add(goods.getTitle());
                if (suggestions.size() >= 10) break;
            }
        }
        return Result.success(suggestions);
    }

    @GetMapping("/history")
    @Operation(summary = "获取搜索历史", description = "获取用户搜索历史（占位接口）")
    public Result<List<String>> getSearchHistory() {
        return Result.success(new ArrayList<>());
    }

    @DeleteMapping("/history")
    @Operation(summary = "清除搜索历史", description = "清除用户搜索历史")
    public Result<Void> clearSearchHistory() {
        return Result.success();
    }
}
