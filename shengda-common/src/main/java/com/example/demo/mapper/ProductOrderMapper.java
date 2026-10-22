package com.example.demo.mapper;

import com.example.demo.model.ProductOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductOrderMapper {
    
    // 创建订单
    int insert(ProductOrder order);
    
    // 根据订单 ID 查询
    ProductOrder getById(@Param("orderId") Integer orderId);
    
    // 根据订单编号查询
    ProductOrder getByOrderNo(@Param("orderNo") String orderNo);
    
    // 查询卖家订单
    List<ProductOrder> listBySeller(@Param("sellerName") String sellerName);
    
    // 查询买家订单
    List<ProductOrder> listByBuyer(@Param("buyerName") String buyerName);
    
    // 更新订单状态
    int updateStatus(@Param("orderId") Integer orderId, @Param("status") String status);
    
    // 更新支付时间
    int updatePaymentTime(@Param("orderId") Integer orderId);
    
    // 更新完成时间
    int updateCompleteTime(@Param("orderId") Integer orderId);
    
    // 取消订单
    int cancelOrder(@Param("orderId") Integer orderId, @Param("cancelReason") String cancelReason);
}
