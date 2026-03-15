package org.example.demoapp.mapper;


import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.example.demoapp.entity.Product;

import java.util.List;

@Mapper
public interface ProductMapper {

    Product selectById(Long id);
    List<Product> selectByUserId(Long userId);
    List<Product> selectAll();
    int insert(Product product);
    int update(Product product);
    int deleteById(Long id);

    int updateStatus(@Param("id") Long id,@Param("status") Integer status);
    // List<Product> selectByCategory(String category); // 数据库已删除category字段，暂时禁用

}
