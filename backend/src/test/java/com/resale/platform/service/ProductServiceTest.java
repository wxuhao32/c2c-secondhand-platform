package com.resale.platform.service;

import com.resale.platform.entity.Goods;
import com.resale.platform.mapper.GoodsMapper;
import com.resale.platform.service.impl.ProductServiceImpl;
import com.resale.platform.service.impl.LocalCacheServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("商品服务单元测试")
class ProductServiceTest {

    @Mock
    private GoodsMapper goodsMapper;

    private CacheService cacheService;

    @InjectMocks
    private ProductServiceImpl productService;

    private Goods testGoods;

    @BeforeEach
    void setUp() {
        cacheService = new LocalCacheServiceImpl();
        productService = new ProductServiceImpl(goodsMapper, cacheService);

        testGoods = new Goods();
        testGoods.setId(1L);
        testGoods.setSellerId(100L);
        testGoods.setTitle("测试商品");
        testGoods.setDescription("测试描述");
        testGoods.setPrice(new BigDecimal("99.99"));
        testGoods.setStatus(1);
        testGoods.setViewCount(10);
        testGoods.setLikeCount(5);
        testGoods.setIsDeleted(0);
        testGoods.setCreatedAt(LocalDateTime.now());
        testGoods.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @DisplayName("获取商品详情 - 正常情况")
    void getProductDetail_success() {
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        Goods result = productService.getProductDetail(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("测试商品", result.getTitle());
        verify(goodsMapper).incrementViewCount(1L);
    }

    @Test
    @DisplayName("获取商品详情 - 商品不存在")
    void getProductDetail_notFound() {
        when(goodsMapper.selectById(999L)).thenReturn(null);

        assertThrows(com.resale.platform.common.BusinessException.class,
                () -> productService.getProductDetail(999L));
    }

    @Test
    @DisplayName("获取商品详情 - 已删除商品")
    void getProductDetail_deleted() {
        testGoods.setIsDeleted(1);
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        assertThrows(com.resale.platform.common.BusinessException.class,
                () -> productService.getProductDetail(1L));
    }

    @Test
    @DisplayName("发布商品 - 正常情况")
    void publishProduct_success() {
        when(goodsMapper.insert(any(Goods.class))).thenReturn(1);

        Map<String, Object> body = Map.of(
                "title", "新商品",
                "description", "新商品描述",
                "price", "199.99"
        );

        Goods result = productService.publishProduct(100L, body);

        assertNotNull(result);
        assertEquals(100L, result.getSellerId());
        assertEquals("新商品", result.getTitle());
        assertEquals(1, result.getStatus());
        verify(goodsMapper).insert(any(Goods.class));
    }

    @Test
    @DisplayName("删除商品 - 正常情况")
    void deleteProduct_success() {
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(goodsMapper.updateById(any(Goods.class))).thenReturn(1);

        productService.deleteProduct(100L, 1L);

        verify(goodsMapper).updateById(argThat(goods -> goods.getIsDeleted() == 1));
    }

    @Test
    @DisplayName("删除商品 - 非本人商品")
    void deleteProduct_forbidden() {
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);

        assertThrows(com.resale.platform.common.BusinessException.class,
                () -> productService.deleteProduct(999L, 1L));
    }

    @Test
    @DisplayName("更新商品状态 - 正常情况")
    void updateProductStatus_success() {
        when(goodsMapper.selectById(1L)).thenReturn(testGoods);
        when(goodsMapper.updateById(any(Goods.class))).thenReturn(1);

        productService.updateProductStatus(100L, 1L, 0);

        verify(goodsMapper).updateById(argThat(goods -> goods.getStatus() == 0));
    }

    @Test
    @DisplayName("获取我的商品列表")
    void getMyProducts_success() {
        when(goodsMapper.findBySellerId(100L)).thenReturn(List.of(testGoods));

        List<Goods> result = productService.getMyProducts(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(100L, result.get(0).getSellerId());
    }
}
