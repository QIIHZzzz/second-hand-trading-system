package org.example.demoapp.service;

import org.example.demoapp.entity.Order;
import org.example.demoapp.entity.Product;
import org.example.demoapp.mapper.OrderMapper;
import org.example.demoapp.mapper.ProductMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private ProductMapper productMapper;

//创建订单
    @Transactional
    public Order createOrder(Order order){
        //验证基本信息
        if (order == null) {
            throw new RuntimeException("订单信息不能为空");
        }
        if (order.getProductId() == null) {
            throw new RuntimeException("商品ID不能为空");
        }
        if (order.getBuyerId() == null) {
            throw new RuntimeException("买家ID不能为空");
        }

        //验证商品信息
        Product product = productMapper.selectById(order.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在，ID: " + order.getProductId());
        }
        if (product.getStatus() != 1) {
            throw new RuntimeException("商品已下架或已售出，无法购买");
        }

        //生成订单号
        String orderNumber = generateOrderNumber();
        order.setOrderNumber(orderNumber);

        //保存下单信息
        order.setProductTitle(product.getTitle());
        order.setProductPrice(product.getPrice());

        //设置价格
        if (order.getTotalAmount() == null) {
            order.setTotalAmount(product.getPrice());
        } else {
            if (order.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("订单金额必须大于0");
            }
        }

        //设置订单状态和时间
        order.setStatus(Order.STATUS_PENDING_PAYMENT); // 1=待支付
        LocalDateTime now = LocalDateTime.now();
        order.setCreateTime(now);
        order.setUpdateTime(now);

        //保存订单到数据库
        int inserResult = orderMapper.insert(order);
        if (inserResult <= 0 ){
            throw new RuntimeException("订单保存失败！");
        }

        //更新商品状态
        int updateProductResult = productMapper.updateStatus(order.getProductId(),2);
        if (updateProductResult <= 0){
            throw new RuntimeException("商品状态更新失败，商品可能已出售！");
        }

        return order;
    }

//生成订单号
    private String generateOrderNumber(){
        String timestamp = String.valueOf(System.currentTimeMillis());
        String randomStr = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD" + timestamp + randomStr;
    }

//查询订单详情
    public Order getOrderById(Long id){
        if (id == null || id<= 0){
            throw new RuntimeException("订单号ID不合法！");
        }
        return orderMapper.selectById(id);
    }

//查询买家所有的订单
    public List<Order> getBuyerOrders(Long buyerId){
        if (buyerId == null || buyerId <= 0){
            throw new RuntimeException("买家ID不合法！");
        }
        return orderMapper.selectByBuyerId(buyerId);
    }

//支付订单
    @Transactional
    public boolean payOrder(Long orderId){
        if (orderId == null || orderId <= 0) {
            throw new RuntimeException("订单ID不合法");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在，ID: " + orderId);
        }

        if (order.getStatus() != Order.STATUS_PENDING_PAYMENT) {
            throw new RuntimeException("订单状态不可支付，当前状态: " + order.getStatus());
        }

        int result = orderMapper.updateToPaid(orderId);
        return result > 0;
    }

    @Transactional
    public boolean cancelOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new RuntimeException("订单ID不合法");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != Order.STATUS_PENDING_PAYMENT) {
            throw new RuntimeException("订单状态不可取消");
        }

        // 恢复商品状态
        productMapper.updateStatus(order.getProductId(), 1); // 1=在售

        // 更新订单状态为已取消
        int result = orderMapper.updateStatus(orderId, Order.STATUS_CANCELLED);
        return result > 0;
    }

    //完成订单（卖家确认）
    @Transactional
    public boolean completeOrder(Long orderId) {
        if (orderId == null || orderId <= 0) {
            throw new RuntimeException("订单ID不合法");
        }

        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        if (order.getStatus() != Order.STATUS_PAID) {
            throw new RuntimeException("只有已支付订单可以完成");
        }

        int result = orderMapper.updateStatus(orderId, Order.STATUS_COMPLETED);
        return result > 0;
    }
}
