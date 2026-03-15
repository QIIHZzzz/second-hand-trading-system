package org.example.demoapp.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.demoapp.entity.Order;

import java.util.List;

@Mapper
public interface OrderMapper {

    int insert(Order order);
    Order selectById(Long id);
    List<Order> selectByBuyerId(@Param("buyerId") Long buyerId);
    int updateStatus(@Param("id") Long id,
                     @Param("status") Integer status);
    int updateToPaid(@Param("id") Long id);
}
