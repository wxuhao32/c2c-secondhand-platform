package com.resale.platform.service.impl;

import com.resale.platform.common.BusinessException;
import com.resale.platform.common.ExceptionEnum;
import com.resale.platform.entity.*;
import com.resale.platform.mapper.*;
import com.resale.platform.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final UserAddressMapper addressMapper;
    private final FavoriteMapper favoriteMapper;
    private final GoodsMapper goodsMapper;
    private final OrderMapper orderMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Map<String, Object> getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ExceptionEnum.USER_NOT_FOUND);
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
        return info;
    }

    @Override
    @Transactional
    public void updateUserInfo(Long userId, Map<String, Object> body) {
        User user = userMapper.selectById(userId);
        if (user == null) throw new BusinessException(ExceptionEnum.USER_NOT_FOUND);
        if (body.containsKey("nickname")) user.setNickname((String) body.get("nickname"));
        if (body.containsKey("email")) user.setEmail((String) body.get("email"));
        if (body.containsKey("avatar")) user.setAvatar((String) body.get("avatar"));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ExceptionEnum.OLD_PASSWORD_ERROR);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    public List<UserAddress> getAddressList(Long userId) {
        return addressMapper.findByUserId(userId);
    }

    @Override
    @Transactional
    public UserAddress addAddress(Long userId, UserAddress address) {
        address.setUserId(userId);
        address.setIsDeleted(0);
        address.setCreatedAt(LocalDateTime.now());
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.insert(address);
        return address;
    }

    @Override
    @Transactional
    public void updateAddress(Long userId, Long id, UserAddress address) {
        UserAddress existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(ExceptionEnum.FORBIDDEN);
        }
        address.setId(id);
        address.setUserId(userId);
        address.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(address);
    }

    @Override
    @Transactional
    public void deleteAddress(Long userId, Long id) {
        UserAddress existing = addressMapper.selectById(id);
        if (existing == null || !existing.getUserId().equals(userId)) {
            throw new BusinessException(ExceptionEnum.FORBIDDEN);
        }
        existing.setIsDeleted(1);
        existing.setUpdatedAt(LocalDateTime.now());
        addressMapper.updateById(existing);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long userId, Long id) {
        addressMapper.clearDefault(userId);
        UserAddress address = addressMapper.selectById(id);
        if (address != null && address.getUserId().equals(userId)) {
            address.setIsDefault(1);
            address.setUpdatedAt(LocalDateTime.now());
            addressMapper.updateById(address);
        }
    }

    @Override
    public List<Map<String, Object>> getFavorites(Long userId) {
        List<Favorite> favorites = favoriteMapper.findByUserId(userId);
        return favorites.stream().map(fav -> {
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
    }

    @Override
    @Transactional
    public void addFavorite(Long userId, Long productId) {
        int exists = favoriteMapper.countByUserAndGoods(userId, productId);
        if (exists > 0) throw new BusinessException(400, "已收藏");
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(productId);
        favorite.setCreatedAt(LocalDateTime.now());
        favoriteMapper.insert(favorite);
        goodsMapper.incrementLikeCount(productId);
    }

    @Override
    @Transactional
    public void removeFavorite(Long userId, Long productId) {
        List<Favorite> favorites = favoriteMapper.findByUserId(userId);
        for (Favorite fav : favorites) {
            if (fav.getGoodsId().equals(productId)) {
                favoriteMapper.deleteById(fav.getId());
                goodsMapper.decrementLikeCount(productId);
                break;
            }
        }
    }

    @Override
    public Map<String, Object> getUserStats(Long userId) {
        Map<String, Object> stats = new HashMap<>();
        List<Goods> myGoods = goodsMapper.findBySellerId(userId);
        stats.put("publishedCount", myGoods.size());
        stats.put("onSaleCount", myGoods.stream().filter(g -> g.getStatus() == 1).count());
        stats.put("soldCount", myGoods.stream().filter(g -> g.getStatus() == 3).count());
        stats.put("favoriteCount", favoriteMapper.findByUserId(userId).size());
        stats.put("boughtOrderCount", orderMapper.findByBuyerId(userId).size());
        stats.put("soldOrderCount", orderMapper.findBySellerId(userId).size());
        return stats;
    }
}
