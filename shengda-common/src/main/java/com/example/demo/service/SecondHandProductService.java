package com.example.demo.service;

import com.example.demo.mapper.SecondHandProductMapper;
import com.example.demo.model.SecondHandProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 二手商品服务
 */
@Service
public class SecondHandProductService {
    
    @Autowired
    private SecondHandProductMapper secondHandProductMapper;
    
    /**
     * 查询所有在售商品（分页）
     */
    public List<SecondHandProduct> listOnSaleProducts(int page, int size) {
        int offset = (page - 1) * size;
        return secondHandProductMapper.listOnSaleProducts(offset, size);
    }
    
    /**
     * 根据用户名查询商品
     */
    public List<SecondHandProduct> listByUserName(String userName) {
        return secondHandProductMapper.listByUserName(userName);
    }
    
    /**
     * 根据 ID 查询商品
     */
    public SecondHandProduct getById(Integer productId) {
        SecondHandProduct product = secondHandProductMapper.getById(productId);
        if (product != null) {
            // 增加浏览次数
            secondHandProductMapper.incrementViewCount(productId);
        }
        return product;
    }
    
    /**
     * 发布商品
     */
    @Transactional
    public boolean createProduct(SecondHandProduct product) {
        product.setStatus("on_sale");
        product.setViewCount(0);
        product.setWantCount(0);
        return secondHandProductMapper.insert(product) > 0;
    }
    
    /**
     * 更新商品
     */
    @Transactional
    public boolean updateProduct(SecondHandProduct product) {
        return secondHandProductMapper.update(product) > 0;
    }
    
    /**
     * 删除商品
     */
    @Transactional
    public boolean deleteProduct(Integer productId) {
        return secondHandProductMapper.deleteById(productId) > 0;
    }
    
    /**
     * 增加想要人数
     */
    @Transactional
    public boolean incrementWantCount(Integer productId) {
        return secondHandProductMapper.incrementWantCount(productId) > 0;
    }
    
    /**
     * 更新商品状态
     */
    @Transactional
    public boolean updateStatus(Integer productId, String status) {
        return secondHandProductMapper.updateStatus(productId, status) > 0;
    }
}
