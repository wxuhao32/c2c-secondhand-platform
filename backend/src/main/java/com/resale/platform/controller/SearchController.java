package com.resale.platform.controller;

import com.resale.platform.common.Result;
import com.resale.platform.entity.Goods;
import com.resale.platform.mapper.GoodsMapper;
import com.resale.platform.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final GoodsMapper goodsMapper;

    private static final List<String> HOT_KEYWORDS = List.of(
            "iPhone", "MacBook", "相机", "Nike", "沙发", "自行车", "Java", "羽绒服"
    );

    @GetMapping
    public Result<Map<String, Object>> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false, defaultValue = "1") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {
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
    public Result<List<String>> getHotKeywords() {
        return Result.success(HOT_KEYWORDS);
    }

    @GetMapping("/suggest")
    public Result<List<String>> getSearchSuggestions(@RequestParam String keyword) {
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
    public Result<List<String>> getSearchHistory() {
        return Result.success(new ArrayList<>());
    }

    @DeleteMapping("/history")
    public Result<Void> clearSearchHistory() {
        return Result.success();
    }
}
