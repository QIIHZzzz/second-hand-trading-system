package org.example.demoapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.demoapp.entity.CartItem;

import java.util.List;

@Mapper
public interface CartItemMapper {
    CartItem selectById(@Param("id") Long id);
    List<CartItem> selectByCartId(@Param("cartId") Long cartId);
    CartItem selectByCartIdAndProductId(@Param("cartId") Long cartId, @Param("productId") Long productId);
    int insert(CartItem item);
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);
    int deleteById(@Param("id") Long id);
    int deleteByCartId(@Param("cartId") Long cartId);
}