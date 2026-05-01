package com.resale.platform.service;

import com.resale.platform.entity.User;
import com.resale.platform.entity.UserAddress;
import com.resale.platform.entity.Favorite;

import java.util.List;
import java.util.Map;

public interface UserService {

    Map<String, Object> getUserInfo(Long userId);

    void updateUserInfo(Long userId, Map<String, Object> body);

    void updatePassword(Long userId, String oldPassword, String newPassword);

    List<UserAddress> getAddressList(Long userId);

    UserAddress addAddress(Long userId, UserAddress address);

    void updateAddress(Long userId, Long id, UserAddress address);

    void deleteAddress(Long userId, Long id);

    void setDefaultAddress(Long userId, Long id);

    List<Map<String, Object>> getFavorites(Long userId);

    void addFavorite(Long userId, Long productId);

    void removeFavorite(Long userId, Long productId);

    Map<String, Object> getUserStats(Long userId);
}
