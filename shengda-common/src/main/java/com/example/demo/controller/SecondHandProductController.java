package com.example.demo.controller;

import com.example.demo.model.SecondHandProduct;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.SecondHandProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 二手商品控制器
 */
@RestController
@RequestMapping("/api/secondhand")
public class SecondHandProductController {
    
    @Autowired
    private SecondHandProductService secondHandProductService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";
    
    /**
     * 查询所有在售商品
     */
    @GetMapping("/list")
    public Map<String, Object> listProducts(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<SecondHandProduct> products = secondHandProductService.listOnSaleProducts(page, size);
            result.put("success", true);
            result.put("data", products);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 查询我的商品
     */
    @GetMapping("/my")
    public Map<String, Object> listMyProducts() {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            List<SecondHandProduct> products = secondHandProductService.listByUserName(currentUser.getUserName());
            result.put("success", true);
            result.put("data", products);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 查询商品详情
     */
    @GetMapping("/detail/{productId}")
    public Map<String, Object> getProductDetail(@PathVariable Integer productId) {
        Map<String, Object> result = new HashMap<>();
        try {
            SecondHandProduct product = secondHandProductService.getById(productId);
            if (product == null) {
                result.put("success", false);
                result.put("message", "商品不存在");
                return result;
            }
            result.put("success", true);
            result.put("data", product);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 发布商品
     */
    @PostMapping("/create")
    public Map<String, Object> createProduct(
            @RequestParam("productName") String productName,
            @RequestParam("productDescription") String productDescription,
            @RequestParam("productCondition") String productCondition,
            @RequestParam("price") String price,
            @RequestParam(value = "originalPrice", required = false) String originalPrice,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            
            SecondHandProduct product = new SecondHandProduct();
            product.setUserName(currentUser.getUserName());
            product.setProductName(productName);
            product.setProductDescription(productDescription);
            product.setProductCondition(productCondition);
            product.setPrice(new java.math.BigDecimal(price));
            if (originalPrice != null && !originalPrice.isEmpty()) {
                product.setOriginalPrice(new java.math.BigDecimal(originalPrice));
            }
            product.setCategory(category);
            
            // 处理图片上传
            if (images != null && images.length > 0) {
                StringBuilder imageUrls = new StringBuilder();
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        String imageUrl = uploadFile(image);
                        if (imageUrls.length() > 0) {
                            imageUrls.append(",");
                        }
                        imageUrls.append(imageUrl);
                    }
                }
                product.setProductImages(imageUrls.toString());
            }
            
            boolean success = secondHandProductService.createProduct(product);
            if (success) {
                result.put("success", true);
                result.put("message", "发布成功");
                result.put("productId", product.getProductId());
            } else {
                result.put("success", false);
                result.put("message", "发布失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发布失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 更新商品
     */
    @PostMapping("/update")
    public Map<String, Object> updateProduct(
            @RequestParam("productId") Integer productId,
            @RequestParam("productName") String productName,
            @RequestParam("productDescription") String productDescription,
            @RequestParam("productCondition") String productCondition,
            @RequestParam("price") String price,
            @RequestParam(value = "originalPrice", required = false) String originalPrice,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "images", required = false) MultipartFile[] images) {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            SecondHandProduct existingProduct = secondHandProductService.getById(productId);
            if (existingProduct == null) {
                result.put("success", false);
                result.put("message", "商品不存在");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            if (!existingProduct.getUserName().equals(currentUser.getUserName())) {
                result.put("success", false);
                result.put("message", "无权限修改此商品");
                return result;
            }
            
            existingProduct.setProductName(productName);
            existingProduct.setProductDescription(productDescription);
            existingProduct.setProductCondition(productCondition);
            existingProduct.setPrice(new java.math.BigDecimal(price));
            if (originalPrice != null && !originalPrice.isEmpty()) {
                existingProduct.setOriginalPrice(new java.math.BigDecimal(originalPrice));
            }
            existingProduct.setCategory(category);
            
            // 处理图片上传
            if (images != null && images.length > 0) {
                StringBuilder imageUrls = new StringBuilder();
                for (MultipartFile image : images) {
                    if (!image.isEmpty()) {
                        String imageUrl = uploadFile(image);
                        if (imageUrls.length() > 0) {
                            imageUrls.append(",");
                        }
                        imageUrls.append(imageUrl);
                    }
                }
                if (imageUrls.length() > 0) {
                    existingProduct.setProductImages(imageUrls.toString());
                }
            }
            
            boolean success = secondHandProductService.updateProduct(existingProduct);
            if (success) {
                result.put("success", true);
                result.put("message", "更新成功");
            } else {
                result.put("success", false);
                result.put("message", "更新失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "更新失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 删除商品
     */
    @PostMapping("/delete/{productId}")
    public Map<String, Object> deleteProduct(@PathVariable Integer productId) {
        Map<String, Object> result = new HashMap<>();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof com.example.demo.model.User)) {
                result.put("success", false);
                result.put("message", "请先登录");
                return result;
            }
            
            SecondHandProduct product = secondHandProductService.getById(productId);
            if (product == null) {
                result.put("success", false);
                result.put("message", "商品不存在");
                return result;
            }
            
            com.example.demo.model.User currentUser = (com.example.demo.model.User) authentication.getPrincipal();
            // 检查是否是卖家或管理员
            if (!product.getUserName().equals(currentUser.getUserName()) && !"admin".equals(currentUser.getRole())) {
                result.put("success", false);
                result.put("message", "无权限删除此商品");
                return result;
            }
            
            boolean success = secondHandProductService.deleteProduct(productId);
            if (success) {
                result.put("success", true);
                result.put("message", "删除成功");
            } else {
                result.put("success", false);
                result.put("message", "删除失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "删除失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 增加想要人数
     */
    @PostMapping("/want/{productId}")
    public Map<String, Object> incrementWantCount(@PathVariable Integer productId) {
        Map<String, Object> result = new HashMap<>();
        try {
            boolean success = secondHandProductService.incrementWantCount(productId);
            if (success) {
                result.put("success", true);
                result.put("message", "已加入想要");
            } else {
                result.put("success", false);
                result.put("message", "操作失败");
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "操作失败：" + e.getMessage());
        }
        return result;
    }
    
    /**
     * 文件上传
     */
    private String uploadFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return "";
        }
        
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : "";
        String newFilename = UUID.randomUUID().toString() + suffix;
        
        Path path = Paths.get(UPLOAD_DIR + newFilename);
        Files.createDirectories(path.getParent());
        Files.write(path, file.getBytes());
        
        return "/uploads/" + newFilename;
    }
}
