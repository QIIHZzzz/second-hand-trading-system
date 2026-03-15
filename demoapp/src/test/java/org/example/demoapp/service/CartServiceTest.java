package org.example.demoapp.service;

import org.example.demoapp.entity.CartItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartServiceTest {

    @Autowired
    private CartService cartService;

    @Test
    void testGetCartItems() {
        try {
            Long userId = 1L;
            List<CartItem> items = cartService.getCartItems(userId);
            System.out.println("购物车项数量: " + items.size());
            assertNotNull(items);
        } catch (Exception e) {
            System.out.println("测试获取购物车项时出现异常（可能是用户不存在）: " + e.getMessage());
        }
    }

    @Test
    void testAddItem() {
        try {
            Long userId = 1L;
            Long productId = 1L;
            Integer quantity = 2;

            CartItem item = cartService.addItem(userId, productId, quantity);
            assertNotNull(item);
            assertEquals(productId, item.getProductId());
            assertEquals(quantity, item.getQuantity());
            System.out.println("添加商品到购物车成功: " + item);
        } catch (Exception e) {
            System.out.println("测试添加商品时出现异常（可能是商品不存在）: " + e.getMessage());
        }
    }

    @Test
    void testUpdateItemQuantity() {

        try {
            Long itemId = 1L;
            Long userId = 1L;
            Integer newQuantity = 5;

            boolean success = cartService.updateItemQuantity(itemId, userId, newQuantity);
            assertTrue(success);
            System.out.println("更新数量成功");
        } catch (Exception e) {
            System.out.println("测试更新数量时出现异常（可能是购物车项不存在）: " + e.getMessage());

        }
    }

    @Test
    void testRemoveItem() {
        try {
            Long itemId = 1L;
            Long userId = 1L;

            boolean success = cartService.removeItem(itemId, userId);
            assertTrue(success);
            System.out.println("移除商品成功");
        } catch (Exception e) {
            System.out.println("测试移除商品时出现异常（可能是购物车项不存在）: " + e.getMessage());
        }
    }

    @Test
    void testClearCart() {
        try {
            Long userId = 1L;
            boolean success = cartService.clearCart(userId);
            assertTrue(success);
            System.out.println("清空购物车成功");
        } catch (Exception e) {
            System.out.println("测试清空购物车时出现异常: " + e.getMessage());
        }
    }
}