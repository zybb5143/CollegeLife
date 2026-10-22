package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.CheckinService;
import com.example.demo.service.UserService;
import com.example.demo.service.UserStatusService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CheckinService checkinService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserStatusService userStatusService;
    
    @Autowired
    private Environment environment;

    /**
     * 用户登录（返回 JWT Token）
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam("userName") String userName, @RequestParam("userPassword") String userPassword) {
        Map<String, Object> result = new HashMap<>();
        // 验证密码
        User user = userService.login(userName, userPassword);
        if (user == null) {
            result.put("success", false);
            result.put("message", "用户名或密码错误");
            return result;
        }

        // 生成 JWT Token
        String token = jwtUtil.generateToken(userName, user.getRole());

        // 登录成功后将用户登录状态写入 Redis，供 Spring Security 过滤器判断登录状态（多实例共享）
        userStatusService.saveUserStatus(user.getUserName(), user.getRole());

        result.put("success", true);
        result.put("message", "登录成功");
        result.put("data", user);
        result.put("token", token); // 返回 Token
        result.put("role", user.getRole()); // 返回角色
        return result;
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestParam("userName") String userName, @RequestParam("userPassword") String userPassword, @RequestParam("userEmail") String userEmail, @RequestParam("userGender") String userGender) {
        Map<String, Object> result = new HashMap<>();

        // 检查用户名是否已存在
        if (userService.getUserByUserName(userName) != null) {
            result.put("success", false);
            result.put("message", "用户名已存在");
            return result;
        }

        // 插入新用户
        boolean success = userService.register(userName, userPassword, userEmail, userGender);

        if (success) {
            result.put("success", true);
            result.put("message", "注册成功！");
        } else {
            result.put("success", false);
            result.put("message", "注册失败");
        }
        return result;
    }

    /**
     * 修改用户信息
     */
    @PostMapping("/update")
    public Map<String, Object> update(@RequestParam("userName") String userName, @RequestParam("userPassword") String userPassword, @RequestParam("userEmail") String userEmail, @RequestParam("userGender") String userGender) {
        Map<String, Object> result = new HashMap<>();

        // 检查用户名是否存在
        if (userService.getUserByUserName(userName) == null) {
            result.put("success", false);
            result.put("message", "用户名不存在");
            return result;
        }

        // 更新用户信息
        boolean success = userService.update(userName, userPassword, userEmail, userGender);

        if (success) {
            result.put("success", true);
            result.put("message", "修改成功！");
        } else {
            result.put("success", false);
            result.put("message", "修改失败");
        }
        return result;
    }

    /**
     * 获取当前登录用户信息（从 JWT Token 中获取）
     */
    @GetMapping("/current")
    public Map<String, Object> getCurrentUser() {
        Map<String, Object> result = new HashMap<>();

        // 从 SecurityContext 获取认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getPrincipal() instanceof User) {
            User user = (User) authentication.getPrincipal();
            result.put("success", true);
            result.put("data", user);
        } else {
            result.put("success", false);
            result.put("message", "未登录");
        }
        return result;
    }

    /**
     * 用户退出登录（JWT 无需服务端处理，前端删除 Token 即可）
     */
    @PostMapping("/logout")
    public Map<String, Object> logout() {
        Map<String, Object> result = new HashMap<>();
        // 清除 Redis 中保存的用户登录状态，使当前 Token 立即失效
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            userStatusService.removeUserStatus(((User) authentication.getPrincipal()).getUserName());
        }
        // 前端仍需删除 localStorage 中的 Token
        result.put("success", true);
        result.put("message", "退出登录成功");
        return result;
    }

    /**
     * 用户签到（每 15 分钟可签到一次）
     */
    @PostMapping("/checkin")
    public Map<String, Object> checkin() {
        Map<String, Object> result = new HashMap<>();

        // 从 SecurityContext 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        User currentUser = (User) authentication.getPrincipal();

        int checkinResult = checkinService.checkin(currentUser.getUserName());

        switch (checkinResult) {
            case 0:
                long minutesLeft = checkinService.getMinutesUntilNextCheckin(currentUser.getUserName());
                result.put("success", false);
                result.put("message", String.format("%.0f 分钟内已签到，请等待后再试", minutesLeft));
                break;
            case 1:
                result.put("success", true);
                result.put("message", "签到成功！连续签到" + getContinuousDays(currentUser.getUserName()) + "次");
                break;
            case 2:
                result.put("success", true);
                result.put("message", "签到成功！连续签到中断，重新从第 1 次开始");
                break;
            default:
                result.put("success", false);
                result.put("message", "签到失败");
        }

        return result;
    }

    /**
     * 获取用户签到信息（每 15 分钟可签到一次）
     */
    @GetMapping("/checkin/info")
    public Map<String, Object> getCheckinInfo() {
        Map<String, Object> result = new HashMap<>();

        // 从 SecurityContext 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        User currentUser = (User) authentication.getPrincipal();

        int[] info = checkinService.getCheckinInfo(currentUser.getUserName());
        long minutesUntilNext = checkinService.getMinutesUntilNextCheckin(currentUser.getUserName());

        result.put("success", true);
        result.put("data", new HashMap<String, Object>() {{
            put("continuousDays", info[0]);
            put("hasCheckedInToday", info[1] == 0); // 0 表示不能签到（15 分钟内已签过）
            put("minutesUntilNextCheckin", minutesUntilNext);
            put("lastCheckinTime", currentUser.getLastCheckinDate());
        }});
        return result;
    }

    /**
     * 辅助方法：获取连续签到天数
     */
    private int getContinuousDays(String userName) {
        int[] info = checkinService.getCheckinInfo(userName);
        return info[0];
    }

    /**
     * 获取当前服务器信息（IP和端口）
     */
    @GetMapping("/server-info")
    public Map<String, Object> getServerInfo(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();
        try {
            // 获取本地IP地址
            String localHost = InetAddress.getLocalHost().getHostAddress();
            
            // 获取服务器实际运行端口（从 Spring Environment 中获取）
            String serverPort = environment.getProperty("local.server.port");
            if (serverPort == null) {
                serverPort = String.valueOf(request.getLocalPort());
            }
            final String finalServerPort = serverPort; // 声明为final供内部类使用
            
            // 获取请求的完整URL
            String requestUrl = request.getRequestURL().toString();
            
            result.put("success", true);
            result.put("data", new HashMap<String, Object>() {{
                put("ip", localHost);
                put("port", finalServerPort);
                put("address", localHost + ":" + finalServerPort);
                put("requestUrl", requestUrl);
            }});
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "获取服务器信息失败: " + e.getMessage());
        }
        return result;
    }
}
