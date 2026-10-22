package com.example.demo.controller;

import com.example.demo.model.Tie;
import com.example.demo.model.User;
import com.example.demo.model.UserFile;
import com.example.demo.ai.AIService;
import com.example.demo.ai.AIServiceFactory;
import com.example.demo.service.ExcelExportService;
import com.example.demo.service.TieService;
import com.example.demo.service.UserFileService;
import com.example.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/tie")
public class TieController {

    @Autowired
    private TieService tieService;

    @Autowired
    private UserFileService userFileService;

    @Autowired
    private ExcelExportService excelExportService;

    @Autowired
    private AIServiceFactory aiServiceFactory;

    @Autowired
    private UserService userService;

    @Autowired
    private Environment environment;

    @PostMapping("/create")
    public Map<String, Object> createTie(@RequestParam("tieTitle") String tieTitle,
                                         @RequestParam("tieContent") String tieContent,
                                         @RequestParam(value = "file", required = false) MultipartFile file,
                                         @RequestParam(value = "tieType", defaultValue = "academic") String tieType,
                                         @RequestParam(value = "productId", required = false) Integer productId) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从 JWT Token 获取当前登录用户，不依赖前端传 userName（防止伪造他人发帖）
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
                result.put("success", false);
                result.put("message", "发帖失败：登录状态失效，请重新登录");
                return result;
            }
            String userName = ((User) authentication.getPrincipal()).getUserName();

            // 验证用户是否存在
            if (!userService.validateUser(userName)) {
                result.put("success", false);
                result.put("message", "发帖失败：用户不存在，请先注册或登录");
                return result;
            }
            String filePath = "";

            // 如果有文件，先保存文件获取路径
            if (file != null && !file.isEmpty()) {
                Tie tempTie = new Tie();
                tempTie.setTieTitle(tieTitle);
                tempTie.setTieContent(tieContent);
                tempTie.setUserName(userName);
                filePath = saveFile(file, tempTie, userName);
            }

            // 创建帖子
            boolean success = tieService.createTie(tieTitle, tieContent, userName, filePath, tieType, productId);
            if (!success) {
                result.put("success", false);
                result.put("message", "发帖失败：数据库插入失败");
                return result;
            }

            // AI 审核：与学习无关的帖子标记为待审核，帖子列表中不再展示
            Integer tieId = tieService.getLatestTieId(userName);
            boolean passed = aiServiceFactory.getCurrentService().isLearningRelated(tieTitle + "\n" + tieContent);
            if (!passed) {
                tieService.markTieAsPending(tieId);
                System.out.println("帖子 ID " + tieId + " 审核不通过，已标记为待审核");
            }

            result.put("success", true);
            result.put("tieId", tieId);
            result.put("message", passed ? "发帖成功！" : "发帖成功！内容与学习无关，已进入待审核");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "发帖失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 编辑帖子（仅作者本人可修改标题与内容）
     */
    @PostMapping("/edit")
    public Map<String, Object> editTie(@RequestBody Map<String, Object> body) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 从 JWT 获取当前登录用户，禁止越权修改他人帖子
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
                result.put("success", false);
                result.put("message", "编辑失败：登录状态失效，请重新登录");
                return result;
            }
            String userName = ((User) authentication.getPrincipal()).getUserName();

            Object tieIdObj = body.get("tieId");
            if (tieIdObj == null) {
                result.put("success", false);
                result.put("message", "编辑失败：缺少帖子ID");
                return result;
            }
            Integer tieId = ((Number) tieIdObj).intValue();
            String tieTitle = (String) body.get("tieTitle");
            String tieContent = (String) body.get("tieContent");

            if (tieTitle == null || tieTitle.trim().isEmpty()
                    || tieContent == null || tieContent.trim().isEmpty()) {
                result.put("success", false);
                result.put("message", "编辑失败：标题和内容不能为空");
                return result;
            }

            // 校验帖子是否存在及作者身份
            Tie tie = tieService.getTieById(tieId);
            if (tie == null) {
                result.put("success", false);
                result.put("message", "编辑失败：帖子不存在");
                return result;
            }
            if (!userName.equals(tie.getUserName())) {
                result.put("success", false);
                result.put("message", "编辑失败：只能修改本人发布的帖子");
                return result;
            }

            boolean success = tieService.updateTie(tieId, tieTitle.trim(), tieContent.trim());
            result.put("success", success);
            result.put("message", success ? "修改成功" : "修改失败：数据库更新失败");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "编辑失败：" + e.getMessage());
        }
        return result;
    }

    /**
     * 保存上传的文件（支持所有类型），并自动添加到个人网盘
     */
    private String saveFile(MultipartFile file, Tie tempTie, String userName) {
        try {
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uuidFileName = UUID.randomUUID() + extension;

            String uploadPath = System.getProperty("user.dir") + "/src/main/resources/static/uploads/";
            File destDir = new File(uploadPath);
            if (!destDir.exists()) {
                destDir.mkdirs();
            }

            File destFile = new File(uploadPath + uuidFileName);
            file.transferTo(destFile);

            // 添加到个人网盘
            try {
                UserFile userFile = new UserFile();
                userFile.setUserName(userName);
                userFile.setFileName(originalFileName);
                userFile.setFilePath(uuidFileName);
                userFile.setFileSize(file.getSize());
                userFile.setFileType(file.getContentType());
                userFileService.uploadFileDirectly(userFile);
            } catch (Exception e) {
                // 网盘添加失败不影响发帖，记录日志即可
                System.err.println("添加到个人网盘失败：" + e.getMessage());
            }

            return "/uploads/" + uuidFileName;
        } catch (IOException e) {
            throw new RuntimeException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户的所有帖子
     */
    @GetMapping("/list")
    public List<Tie> getUserTies(@RequestParam("userName") String userName) {
        try {
            return tieService.getTiesByUserName(userName);
        } catch (Exception e) {
            throw new RuntimeException("查询失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有帖子
     * 返回结果中直接携带处理本次请求的后端实例端口/地址，方便演示 nginx 负载均衡效果
     */
    @GetMapping("/all")
    public Map<String, Object> getAllTies(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            List<Tie> ties = tieService.getAllTies();

            // 获取当前实例实际运行端口
            String serverPort = environment.getProperty("local.server.port");
            if (serverPort == null) {
                serverPort = String.valueOf(request.getLocalPort());
            }
            String localHost;
            try {
                localHost = InetAddress.getLocalHost().getHostAddress();
            } catch (Exception e) {
                localHost = "unknown";
            }

            result.put("success", true);
            result.put("port", serverPort);
            result.put("address", localHost + ":" + serverPort);
            result.put("data", ties);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "查询失败：" + e.getMessage());
            result.put("data", new java.util.ArrayList<Tie>());
        }
        return result;
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/detail")
    public Tie getTieDetail(@RequestParam("tieId") Integer tieId) {
        try {
            Tie tie = tieService.getTieById(tieId);
            if (tie != null) {
                return tie;
            } else {
                throw new RuntimeException("帖子不存在");
            }
        } catch (Exception e) {
            throw new RuntimeException("查询失败：" + e.getMessage());
        }
    }

    /**
     * 手动触发导出最新 10 个帖子到 Excel
     */
    @PostMapping("/export")
    public String exportLatestTies() {
        try {
            String filePath = excelExportService.exportLatestTies();
            return "导出成功！文件已保存至：" + filePath;
        } catch (Exception e) {
            return "导出失败：" + e.getMessage();
        }
    }

    /**
     * 智能搜索（基于AI关键词提取）
     */
    @GetMapping("/smart-search")
    public List<Tie> smartSearch(@RequestParam("keyword") String keyword) {
        try {
            // 1. 用 AI 提取关键词
            String keywords = aiServiceFactory.getCurrentService().extractKeywords(keyword);
            System.out.println("提取的关键词：" + keywords);
            
            // 2. 获取所有帖子
            List<Tie> allTies = tieService.getAllTies();
            
            // 3. 计算每个帖子的匹配分数并排序
            return allTies.stream()
                .map(tie -> {
                    int score = calculateRelevanceScore(tie, keywords);
                    return new Object() {
                        Tie tie;
                        int score;
                        { this.tie = tie; this.score = score; }
                    };
                })
                .filter(obj -> obj.score > 0)
                .sorted((a, b) -> Integer.compare(b.score, a.score))
                .limit(10)
                .map(obj -> obj.tie)
                .collect(java.util.stream.Collectors.toList());
                
        } catch (Exception e) {
            throw new RuntimeException("搜索失败：" + e.getMessage());
        }
    }

    /**
     * 计算帖子与关键词的相关性分数
     */
    private int calculateRelevanceScore(Tie tie, String keywords) {
        if (keywords == null || keywords.trim().isEmpty()) {
            return 0;
        }
        
        String[] keywordArray = keywords.split(",");
        String content = (tie.getTieTitle() + " " + tie.getTieContent()).toLowerCase();
        
        int score = 0;
        for (String kw : keywordArray) {
            String trimmedKw = kw.trim().toLowerCase();
            if (!trimmedKw.isEmpty() && content.contains(trimmedKw)) {
                score++;
            }
        }
        
        return score;
    }

    /**
     * 获取当前AI服务商信息
     */
    @GetMapping("/ai-provider")
    public Map<String, String> getCurrentAIProvider() {
        AIService currentService = aiServiceFactory.getCurrentService();
        return Map.of(
            "provider", aiServiceFactory.getCurrentProvider(),
            "name", currentService.getProviderName()
        );
    }
}
