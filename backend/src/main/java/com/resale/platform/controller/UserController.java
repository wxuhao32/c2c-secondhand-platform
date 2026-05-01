package com.resale.platform.controller;

import com.resale.platform.common.Result;
import com.resale.platform.entity.User;
import com.resale.platform.entity.UserAddress;
import com.resale.platform.entity.Favorite;
import com.resale.platform.entity.Goods;
import com.resale.platform.mapper.UserMapper;
import com.resale.platform.mapper.UserAddressMapper;
import com.resale.platform.mapper.FavoriteMapper;
import com.resale.platform.mapper.GoodsMapper;
import com.resale.platform.mapper.OrderMapper;
import com.resale.platform.security.SecurityUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserMapper userMapper;
    private final UserAddressMapper addressMapper;
    private final FavoriteMapper favoriteMapper;
    private final GoodsMapper goodsMapper;
    private final OrderMapper orderMapper;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/info")
    public Result<Map<String, Object>> getUserInfo() {
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) return Result.error(404, "用户不存在");
        Map<String, Object> info = new HashMap<>();
        info.put("userId", user.getUserId());
        info.put("username", user.getUsername());
        info.put("mobile", user.getMobile());
        info.put("email", user.getEmail());
        info.put("nickname", user.getNickname());
        info.put("avatar", user.getAvatar());
        info.put("status", user.getStatus());
        info.put("role", user.getRole());
        info.put("lastLoginTime", user.getLastLoginTime());
        info.put("createTime", user.getCreatedAt());
        return Result.success(info);
    }

    @PutMapping("/info")
    public Result<Void> updateUserInfo(@RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);
        if (user == null) return Result.error(404, "用户不存在");
        if (body.containsKey("nickname")) user.setNickname((String) body.get("nickname"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("avatar")) user.setAvatar((String) body.get("avatar"));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> updatePassword(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return Result.error(400, "原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return Result.success();
    }

    @PostMapping("/avatar")
    public Result<Map<String, String>> updateAvatar() {
        return Result.success(Map.of("url", "/uploads/avatar-placeholder.jpg"));
    }

    @GetMapping("/address")
    public Result<List<UserAddress>> getAddressList() {
        Long userId = getCurrentUserId();
        List<UserAddress> addresses = addressMapper.findByUserId(userId);
        return Result.success(addresses);
    }

    @PostMapping("/address")
    public Result<UserAddress> addAddress(@RequestBody UserAddress address) {
        Long userId = getCurrentUserId();
        address.setUserId(userId);
        address.setIsDeleted(0);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.insert(address);
        return Result.success(address);
    }

    @PutMapping("/address/{id}")
    public Result<Void> updateAddress(@PathVariable Long id, @RequestBody UserAddress address) {
        Long userId = getCurrentUserId();
        UserAddress existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        address.setId(id);
        address.setUserId(userId);
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(address);
        return Result.success();
    }

    @DeleteMapping("/address/{id}")
    public Result<Void> deleteAddress(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        UserAddress existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            return Result.error(403, "无权操作");
        }
        existing.setIsDeleted(1);
        existing.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(existing);
        return Result.success();
    }

    @PutMapping("/address/{id}/default")
    public Result<Void> setDefaultAddress(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        addressMapper.clearDefault(userId);
        UserAddress address = addressMapper.selectById(id);
        if (address != null && address.getUserId().equals(userId)) {
            address.setIsDefault(1);
            address.setUpdatedAt(LocalDateTime.now());
            addressMapper.updateById(address);
        }
        return Result.success();
    }

    @GetMapping("/favorites")
    public Result<List<Map<String, Object>>> getFavorites() {
        Long userId = getCurrentUserId();
        List<Favorite> favorites = favoriteMapper.findByUserId(userId);
        List<Map<String, Object>> result = favorites.stream().map(fav -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", fav.getId());
            map.put("goodsId", fav.getGoodsId());
            map.put("createdAt", fav.getCreatedAt());
            Goods goods = goodsMapper.selectById(fav.getGoodsId());
            if (goods != null) {
                map.put("title", goods.getTitle());
                map.put("price", goods.getPrice());
                map.put("originalPrice", goods.getOriginalPrice());
                map.put("images", goods.getImages());
                map.put("status", goods.getStatus());
            }
            return map;
        }).toList();
        return Result.success(result);
    }

    @PostMapping("/favorites/{productId}")
    public Result<Void> addFavorite(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        int exists = favoriteMapper.countByUserAndGoods(userId, productId);
        if (exists > 0) return Result.error(400, "已收藏");
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(productId);
        favorite.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(favorite);
        goodsMapper.incrementLikeCount(productId);
        return Result.success();
    }

    @DeleteMapping("/favorites/{productId}")
    public Result<Void> removeFavorite(@PathVariable Long productId) {
        Long userId = getCurrentUserId();
        List<Favorite> favorites = favoriteMapper.findByUserId(userId);
        for (Favorite fav : favorites) {
            if (fav.getGoodsId().equals(productId)) {
                favoriteMapper.deleteById(fav.getId());
                goodsMapper.decrementLikeCount(productId);
                break;
            }
        }
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getUserStats() {
        Long userId = getCurrentUserId();
        Map<String, Object> stats = new HashMap<>();
        List<Goods> myGoods = goodsMapper.findBySellerId(userId);
        stats.put("publishedCount", myGoods.size());
        stats.put("onSaleCount", myGoods.stream().filter(g -> g.getStatus() == 1).count());
        stats.put("soldCount", myGoods.stream().filter(g -> g.getStatus() == 3).count());
        stats.put("favoriteCount", favoriteMapper.findByUserId(userId).size());
        stats.put("boughtOrderCount", orderMapper.findByBuyerId(userId).size());
        stats.put("soldOrderCount", orderMapper.findBySellerId(userId).size());
        return Result.success(stats);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        SecurityUser userDetails = (SecurityUser) authentication.getPrincipal();
        return userDetails.getUserId();
    }
}
