package com.example.demo.controller;

import com.example.demo.model.ProductOrder;
import com.example.demo.service.ProductOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单控制器
 */
@RestController
@RequestMapping("/api/order")
public class ProductOrderController {
    
    @Autowired
    private ProductOrderService productOrderService;
    
    /**
     * 创建订单（购买商品）
     */
    @PostMapping("/create")
    public Map<String, Object> createOrder(@RequestParam("productId") Integer productId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            
            ProductOrder order = productOrderService.createOrder(productId, currentUser.getUserName());
            if (order == null) {
                result.put("success", false);
                result.put("message", "商品不存在或已被售出");
                return result;
            }
            
            result.put("success", true);
            result.put("message", "订单创建成功");
            result.put("data", order);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "创建订单失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 查询订单详情
     */
    @GetMapping("/detail/{orderId}")
    public Map<String, Object> getOrderDetail(@PathVariable Integer orderId) {
        Map<String, Object> result = new HashMap<>();
        try {
            ProductOrder order = productOrderService.getById(orderId);
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            result.put("success", true);
            result.put("data", order);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 查询我的卖出订单
     */
    @GetMapping("/my/sell")
    public Map<String, Object> listMySellOrders() {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            List<ProductOrder> orders = productOrderService.listBySeller(currentUser.getUserName());
            result.put("success", true);
            result.put("data", orders);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 查询我的买入订单
     */
    @GetMapping("/my/buy")
    public Map<String, Object> listMyBuyOrders() {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            List<ProductOrder> orders = productOrderService.listByBuyer(currentUser.getUserName());
            result.put("success", true);
            result.put("data", orders);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 支付订单（模拟支付）
     */
    @PostMapping("/pay/{orderId}")
    public Map<String, Object> payOrder(@PathVariable Integer orderId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            ProductOrder order = productOrderService.getById(orderId);
            
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            
            // 检查是否是买家
            if (!order.getBuyerName().equals(currentUser.getUserName())) {
                result.put("success", false);
                result.put("message", "无权限支付此订单");
                return result;
            }
            
            boolean success = productOrderService.payOrder(orderId);
            if (success) {
                result.put("success", true);
                result.put("message", "支付成功");
            } else {
                result.put("success", false);
                result.put("message", "支付失败，订单状态异常");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "支付失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 确认收货
     */
    @PostMapping("/confirm/{orderId}")
    public Map<String, Object> confirmReceipt(@PathVariable Integer orderId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            ProductOrder order = productOrderService.getById(orderId);
            
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            
            // 检查是否是买家
            if (!order.getBuyerName().equals(currentUser.getUserName())) {
                result.put("success", false);
                result.put("message", "无权限确认此订单");
                return result;
            }
            
            boolean success = productOrderService.confirmReceipt(orderId);
            if (success) {
                result.put("success", true);
                result.put("message", "确认收货成功");
            } else {
                result.put("success", false);
                result.put("message", "确认失败，订单状态异常");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "确认失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 取消订单
     */
    @PostMapping("/cancel/{orderId}")
    public Map<String, Object> cancelOrder(
            @PathVariable Integer orderId,
            @RequestParam(value = "reason", required = false) String reason) {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            ProductOrder order = productOrderService.getById(orderId);
            
            if (order == null) {
                result.put("success", false);
                result.put("message", "订单不存在");
                return result;
            }
            
            // 检查是否是买家或卖家
            if (!order.getBuyerName().equals(currentUser.getUserName()) && 
                !order.getSellerName().equals(currentUser.getUserName())) {
                result.put("success", false);
                result.put("message", "无权限取消此订单");
                return result;
            }
            
            boolean success = productOrderService.cancelOrder(orderId, reason);
            if (success) {
                result.put("success", true);
                result.put("message", "取消成功");
            } else {
                result.put("success", false);
                result.put("message", "取消失败，订单状态不允许取消");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "取消失败：" + e.getMessage());
        }
        return result;
    }
}
