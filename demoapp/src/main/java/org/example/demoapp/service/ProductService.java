package org.example.demoapp.service;

import org.example.demoapp.entity.Product;
import org.example.demoapp.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {
    @Autowired
    private ProductMapper productMapper;

    public Product createProduct(Product product){
        product.setStatus(1);
        product.setCreateTime(LocalDateTime.now());
        product.setUpdateTime(LocalDateTime.now());

        productMapper.insert(product);
        return product;
    }

    public Product getProductById(Long id){
        return productMapper.selectById(id);
    }

    public List<Product> getUserProducts(Long userId){
        return productMapper.selectByUserId(userId);
    }

    public List<Product> getAllProducts(){
        return productMapper.selectAll();
    }

    public boolean updateProduct(Product product) {
        product.setUpdateTime(LocalDateTime.now());
        return productMapper.update(product) > 0;
    }

    public boolean deleteProduct(Long id) {
        return productMapper.deleteById(id) > 0;
    }

    public boolean disableProduct(Long id) {
        return productMapper.updateStatus(id, 3) > 0;
    }


    public boolean toggleProductStatus(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            return false;
        }
        int currentStatus = product.getStatus();
        int newStatus;
        if (currentStatus == Product.STATUS_ON_SALE) {
            newStatus = Product.STATUS_OFF_SHELF;
        } else if (currentStatus == Product.STATUS_OFF_SHELF) {
            newStatus = Product.STATUS_ON_SALE;
        } else {
            // 已售状态不能切换
            return false;
        }
        return productMapper.updateStatus(id, newStatus) > 0;
    }
}
