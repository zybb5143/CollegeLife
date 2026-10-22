package com.example.demo.service;

import com.example.demo.mapper.ProductOrderMapper;
import com.example.demo.mapper.SecondHandProductMapper;
import com.example.demo.model.ProductOrder;
import com.example.demo.model.SecondHandProduct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * 订单服务
 */
@Service
public class ProductOrderService {
    
    @Autowired
    private ProductOrderMapper productOrderMapper;
    
    @Autowired
    private SecondHandProductMapper secondHandProductMapper;
    
    /**
     * 创建订单
     */
    @Transactional
    public ProductOrder createOrder(Integer productId, String buyerName) {
        // 查询商品信息
        SecondHandProduct product = secondHandProductMapper.getById(productId);
        if (product == null || !"on_sale".equals(product.getStatus())) {
            return null;
        }
        
        // 检查不能购买自己的商品
        if (product.getUserName().equals(buyerName)) {
            return null;
        }
        
        // 创建订单
        ProductOrder order = new ProductOrder();
        order.setOrderNo(generateOrderNo());
        order.setProductId(productId);
        order.setSellerName(product.getUserName());
        order.setBuyerName(buyerName);
        order.setProductName(product.getProductName());
        order.setProductImage(getFirstImage(product.getProductImages()));
        order.setPrice(product.getPrice());
        order.setStatus("pending_payment");
        
        productOrderMapper.insert(order);

        return order;
    }
    
    /**
     * 根据订单 ID 查询
     */
    public ProductOrder getById(Integer orderId) {
        return productOrderMapper.getById(orderId);
    }
    
    /**
     * 根据订单编号查询
     */
    public ProductOrder getByOrderNo(String orderNo) {
        return productOrderMapper.getByOrderNo(orderNo);
    }
    
    /**
     * 查询卖家订单
     */
    public List<ProductOrder> listBySeller(String sellerName) {
        return productOrderMapper.listBySeller(sellerName);
    }
    
    /**
     * 查询买家订单
     */
    public List<ProductOrder> listByBuyer(String buyerName) {
        return productOrderMapper.listByBuyer(buyerName);
    }
    
    /**
     * 支付订单（模拟支付）
     */
    @Transactional
    public boolean payOrder(Integer orderId) {
        ProductOrder order = productOrderMapper.getById(orderId);
        if (order == null || !"pending_payment".equals(order.getStatus())) {
            return false;
        }
        
        // 更新订单支付状态
        productOrderMapper.updatePaymentTime(orderId);
        
        // 更新商品状态为已售出
        secondHandProductMapper.updateStatus(order.getProductId(), "sold");
        
        return true;
    }
    
    /**
     * 确认收货
     */
    @Transactional
    public boolean confirmReceipt(Integer orderId) {
        ProductOrder order = productOrderMapper.getById(orderId);
        if (order == null || !"paid".equals(order.getStatus())) {
            return false;
        }
        
        productOrderMapper.updateCompleteTime(orderId);
        return true;
    }
    
    /**
     * 取消订单
     */
    @Transactional
    public boolean cancelOrder(Integer orderId, String cancelReason) {
        ProductOrder order = productOrderMapper.getById(orderId);
        if (order == null) {
            return false;
        }
        
        // 只有待支付的订单可以直接取消
        if ("pending_payment".equals(order.getStatus())) {
            productOrderMapper.cancelOrder(orderId, cancelReason);
            return true;
        }
        
        return false;
    }
    
    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "ORD" + System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "").substring(0, 4).toUpperCase();
    }
    
    /**
     * 获取第一张图片
     */
    private String getFirstImage(String images) {
        if (images == null || images.isEmpty()) {
            return "";
        }
        String[] imageArray = images.split(",");
        return imageArray[0];
    }
}
