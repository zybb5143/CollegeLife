package com.example.demo.mapper;

import com.example.demo.model.SecondHandProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SecondHandProductMapper {
    
    // 查询所有在售商品（分页）
    List<SecondHandProduct> listOnSaleProducts(@Param("offset") int offset, @Param("limit") int limit);
    
    // 根据用户名查询商品
    List<SecondHandProduct> listByUserName(@Param("userName") String userName);
    
    // 根据 ID 查询商品
    SecondHandProduct getById(@Param("productId") Integer productId);
    
    // 发布商品
    int insert(SecondHandProduct product);
    
    // 更新商品
    int update(SecondHandProduct product);
    
    // 删除商品
    int deleteById(@Param("productId") Integer productId);
    
    // 增加浏览次数
    int incrementViewCount(@Param("productId") Integer productId);
    
    // 增加想要人数
    int incrementWantCount(@Param("productId") Integer productId);
    
    // 更新商品状态
    int updateStatus(@Param("productId") Integer productId, @Param("status") String status);
}
