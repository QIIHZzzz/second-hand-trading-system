package org.example.demoapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.demoapp.entity.Cart;

@Mapper
public interface CartMapper {
    Cart selectByUserId(@Param("userId") Long userId);
    int insert(Cart cart);
    int deleteByUserId(@Param("userId") Long userId);
}