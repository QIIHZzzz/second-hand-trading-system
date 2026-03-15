package org.example.demoapp.service;

import org.example.demoapp.entity.Cart;
import org.example.demoapp.entity.CartItem;
import org.example.demoapp.entity.Product;
import org.example.demoapp.mapper.CartMapper;
import org.example.demoapp.mapper.CartItemMapper;
import org.example.demoapp.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private CartItemMapper cartItemMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 获取用户购物车项列表
     */
    public List<CartItem> getCartItems(Long userId) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("用户ID不合法");
        }

        Cart cart = getOrCreateCart(userId);
        if (cart == null) {
            throw new RuntimeException("购物车不存在");
        }

        return cartItemMapper.selectByCartId(cart.getId());
    }

    /**
     * 添加商品到购物车
     */
    @Transactional
    public CartItem addItem(Long userId, Long productId, Integer quantity) {
        // 参数验证
        if (userId == null || userId <= 0) {
            throw new RuntimeException("用户ID不合法");
        }
        if (productId == null || productId <= 0) {
            throw new RuntimeException("商品ID不合法");
        }
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("商品数量必须大于0");
        }

        // 获取或创建购物车
        Cart cart = getOrCreateCart(userId);

        // 验证商品信息
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new RuntimeException("商品不存在，ID: " + productId);
        }
        if (product.getStatus() != Product.STATUS_ON_SALE) {
            throw new RuntimeException("商品已下架或已售出，无法加入购物车");
        }

        // 检查购物车中是否已存在该商品
        CartItem existingItem = cartItemMapper.selectByCartIdAndProductId(cart.getId(), productId);
        LocalDateTime now = LocalDateTime.now();

        if (existingItem != null) {
            // 已存在，更新数量并更新价格快照
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            existingItem.setPriceAtAdd(product.getPrice()); // 更新为最新价格
            existingItem.setProductTitle(product.getTitle()); // 更新标题快照
            existingItem.setProductImageUrl(product.getImageUrl()); // 更新图片快照
            existingItem.setUpdateTime(now);

            cartItemMapper.updateQuantity(existingItem.getId(), existingItem.getQuantity());
            return existingItem;
        } else {
            // 新商品，创建购物车项
            CartItem newItem = new CartItem();
            newItem.setCartId(cart.getId());
            newItem.setProductId(productId);
            newItem.setProductTitle(product.getTitle());
            newItem.setProductImageUrl(product.getImageUrl());
            newItem.setPriceAtAdd(product.getPrice());
            newItem.setQuantity(quantity);
            newItem.setCreateTime(now);
            newItem.setUpdateTime(now);

            cartItemMapper.insert(newItem);
            return newItem;
        }
    }

    /**
     * 更新购物车项数量
     */
    @Transactional
    public boolean updateItemQuantity(Long itemId, Long userId, Integer quantity) {
        // 参数验证
        if (itemId == null || itemId <= 0) {
            throw new RuntimeException("购物车项ID不合法");
        }
        if (userId == null || userId <= 0) {
            throw new RuntimeException("用户ID不合法");
        }
        if (quantity == null || quantity <= 0) {
            throw new RuntimeException("商品数量必须大于0");
        }

        // 验证购物车项是否存在且属于该用户
        CartItem item = cartItemMapper.selectById(itemId);
        if (item == null) {
            throw new RuntimeException("购物车项不存在，ID: " + itemId);
        }

        Cart cart = cartMapper.selectByUserId(userId);
        if (cart == null || !cart.getId().equals(item.getCartId())) {
            throw new RuntimeException("无权修改此购物车项");
        }

        // 更新数量
        int result = cartItemMapper.updateQuantity(itemId, quantity);
        return result > 0;
    }

    /**
     * 从购物车移除商品
     */
    @Transactional
    public boolean removeItem(Long itemId, Long userId) {
        // 参数验证
        if (itemId == null || itemId <= 0) {
            throw new RuntimeException("购物车项ID不合法");
        }
        if (userId == null || userId <= 0) {
            throw new RuntimeException("用户ID不合法");
        }

        // 验证购物车项是否存在且属于该用户
        CartItem item = cartItemMapper.selectById(itemId);
        if (item == null) {
            throw new RuntimeException("购物车项不存在，ID: " + itemId);
        }

        Cart cart = cartMapper.selectByUserId(userId);
        if (cart == null || !cart.getId().equals(item.getCartId())) {
            throw new RuntimeException("无权删除此购物车项");
        }

        // 删除购物车项
        int result = cartItemMapper.deleteById(itemId);
        return result > 0;
    }

    /**
     * 清空购物车
     */
    @Transactional
    public boolean clearCart(Long userId) {
        if (userId == null || userId <= 0) {
            throw new RuntimeException("用户ID不合法");
        }

        Cart cart = cartMapper.selectByUserId(userId);
        if (cart == null) {
            // 用户没有购物车，视为清空成功
            return true;
        }

        int result = cartItemMapper.deleteByCartId(cart.getId());
        return result >= 0; // 即使删除0条也视为成功（购物车本来就是空的）
    }

    /**
     * 获取或创建用户的购物车
     */
    private Cart getOrCreateCart(Long userId) {
        Cart cart = cartMapper.selectByUserId(userId);
        if (cart == null) {
            // 创建新购物车
            cart = new Cart();
            cart.setUserId(userId);
            LocalDateTime now = LocalDateTime.now();
            cart.setCreateTime(now);
            cart.setUpdateTime(now);
            cartMapper.insert(cart);
        }
        return cart;
    }
}