package com.example.demo.service;

import com.example.demo.mapper.RedPacketRecordMapper;
import com.example.demo.model.RedPacketRecord;
import com.example.demo.util.RedisDistributedLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.Executor;

/**
 * 红包服务类（使用 Redis 分布式锁保证抢红包操作的原子性）
 */
@Service
public class RedPacketService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RedPacketRecordMapper redPacketRecordMapper;

    @Autowired
    private RedisDistributedLock redisDistributedLock;

    @Autowired
    @Qualifier("redPacketRecordExecutor")
    private Executor redPacketRecordExecutor;

    private static final String RED_PACKET_KEY = "red_packet:";
    private static final String RED_PACKET_RECORD_KEY = "red_packet_record:";
    private static final String RED_PACKET_LOCK_KEY = "lock:red_packet:";

    /** 用户级分布式锁过期时间（毫秒），防止持有者宕机导致死锁 */
    private static final long LOCK_EXPIRE_MS = 3000;

    /** 获锁最长等待时间（毫秒），0 表示抢不到立即失败（防重复抢） */
    private static final long LOCK_WAIT_MS = 0;

    /**
     * 发布红包
     */
    public Map<String, Object> publishRedPacket(String publisher, String packetNo, BigDecimal totalAmount, Integer totalCount) {
        Map<String, Object> result = new HashMap<>();

        if (totalCount > 10000) {
            result.put("success", false);
            result.put("message", "红包个数不能超过10000个");
            return result;
        }

        if (packetNo == null || packetNo.trim().isEmpty()) {
            result.put("success", false);
            result.put("message", "红包编号不能为空");
            return result;
        }

        // 生成红包金额列表（二倍均值法）
        List<BigDecimal> amountList = generateRedPacketAmounts(totalAmount, totalCount);

        // 存储红包信息到 Redis
        String packetKey = RED_PACKET_KEY + packetNo;
        redisTemplate.opsForHash().put(packetKey, "packetNo", packetNo);
        redisTemplate.opsForHash().put(packetKey, "publisher", publisher);
        redisTemplate.opsForHash().put(packetKey, "totalAmount", totalAmount.toString());
        redisTemplate.opsForHash().put(packetKey, "totalCount", String.valueOf(totalCount));
        redisTemplate.opsForHash().put(packetKey, "remainCount", String.valueOf(totalCount));
        redisTemplate.opsForHash().put(packetKey, "remainAmount", totalAmount.toString());
        redisTemplate.opsForHash().put(packetKey, "status", "0");
        // 注意：红包数据永不过期，不设置expire

        // 将金额列表存入 Redis List
        String amountListKey = packetKey + ":amounts";
        for (BigDecimal amount : amountList) {
            redisTemplate.opsForList().rightPush(amountListKey, amount.toString());
        }
        // 注意：红包数据永不过期，不设置expire

        result.put("success", true);
        result.put("packetNo", packetNo);
        result.put("message", "红包发布成功");
        return result;
    }

    /**
     * 抢红包
     */
    public Map<String, Object> grabRedPacket(String packetNo, String userName) {
        Map<String, Object> result = new HashMap<>();

        String packetKey = RED_PACKET_KEY + packetNo;
        String amountListKey = packetKey + ":amounts";
        String recordKey = RED_PACKET_RECORD_KEY + packetNo;
        // 用户级分布式锁：lock:red_packet:{packetNo}:{userName}
        String lockKey = RED_PACKET_LOCK_KEY + packetNo + ":" + userName;

        // 获取用户锁：保证同一用户对同一红包的“查重-领取-扣减”串行执行，防止重复抢
        String token = redisDistributedLock.tryLock(lockKey, LOCK_EXPIRE_MS, LOCK_WAIT_MS);
        if (token == null) {
            result.put("success", false);
            result.put("message", "操作太频繁，请稍后再试");
            return result;
        }

        try {
            // 检查红包是否存在
            if (!redisTemplate.hasKey(packetKey)) {
                result.put("success", false);
                result.put("message", "红包不存在");
                return result;
            }

            // 检查是否已抢过（用户锁保护下，查重-记录整体原子）
            if (redisTemplate.opsForSet().isMember(recordKey, userName)) {
                result.put("success", false);
                result.put("message", "已经抢过红包了");
                return result;
            }

            // 从列表中弹出一个金额（原子操作，天然防止超发）
            Object amountObj = redisTemplate.opsForList().leftPop(amountListKey);
            if (amountObj == null) {
                result.put("success", false);
                result.put("message", "红包已被抢完");
                return result;
            }

            BigDecimal amount = new BigDecimal(amountObj.toString());

            // 更新剩余数量（原子自减）
            redisTemplate.opsForHash().increment(packetKey, "remainCount", -1);

            // 更新剩余金额（原子自减，不同用户并发也不会丢失更新）
            redisTemplate.opsForHash().increment(packetKey, "remainAmount", amount.negate().doubleValue());

            // 记录抢红包用户（仅用于防重复，不存储详细记录）
            redisTemplate.opsForSet().add(recordKey, userName);

            // 异步保存抢红包记录到MySQL（提交到独立线程池，避免占用锁）
            redPacketRecordExecutor.execute(() -> saveGrabRecordToMySQL(packetNo, userName, amount));

            result.put("success", true);
            result.put("amount", amount);
            result.put("message", "恭喜抢到 " + amount + " 元");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "抢红包失败：" + e.getMessage());
            System.err.println("[Redis抢红包] 异常: " + e.getMessage());
        } finally {
            // 释放用户锁：仅当锁属于当前 token 时才删除
            redisDistributedLock.unlock(lockKey, token);
        }

        return result;
    }

    /**
     * 查询红包详情
     */
    public Map<String, Object> getRedPacketDetail(String packetNo) {
        Map<String, Object> result = new HashMap<>();
        String packetKey = RED_PACKET_KEY + packetNo;

        Map<Object, Object> packetInfo = redisTemplate.opsForHash().entries(packetKey);
        if (packetInfo.isEmpty()) {
            result.put("success", false);
            result.put("message", "红包不存在");
            return result;
        }

        result.put("success", true);
        result.put("data", packetInfo);
        return result;
    }

    /**
     * 查询用户的抢红包记录（从MySQL查询）
     */
    public List<Map<String, Object>> getUserGrabRecords(String userName) {
        // Redis只存储防重标记，不存储详细记录，所以返回空列表
        // 实际应该从MySQL查询，这里由Controller层调用MySQL服务
        return new ArrayList<>();
    }

    /**
     * 生成红包金额列表（二倍均值法）
     */
    private List<BigDecimal> generateRedPacketAmounts(BigDecimal totalAmount, Integer totalCount) {
        List<BigDecimal> amountList = new ArrayList<>();
        BigDecimal remainAmount = totalAmount;
        Random random = new Random();

        for (int i = 0; i < totalCount - 1; i++) {
            // 计算当前可抢的最大金额（剩余平均金额的2倍）
            BigDecimal maxAmount = remainAmount.divide(BigDecimal.valueOf(totalCount - i), 2, RoundingMode.DOWN)
                    .multiply(BigDecimal.valueOf(2));

            // 随机生成金额（最小0.01元）
            double randomAmount = random.nextDouble() * maxAmount.doubleValue();
            if (randomAmount < 0.01) {
                randomAmount = 0.01;
            }

            BigDecimal amount = new BigDecimal(randomAmount).setScale(2, RoundingMode.DOWN);
            if (amount.compareTo(remainAmount) > 0) {
                amount = remainAmount;
            }

            amountList.add(amount);
            remainAmount = remainAmount.subtract(amount);
        }

        // 最后一个红包等于剩余金额
        amountList.add(remainAmount.setScale(2, RoundingMode.DOWN));

        // 打乱顺序
        Collections.shuffle(amountList);

        return amountList;
    }

    /**
     * 保存抢红包记录到MySQL（由 redPacketRecordExecutor 线程池异步执行）
     */
    private void saveGrabRecordToMySQL(String packetNo, String userName, BigDecimal amount) {
        try {
            RedPacketRecord record = new RedPacketRecord();
            record.setPacketNo(packetNo);
            record.setUserName(userName);
            record.setAmount(amount);
            redPacketRecordMapper.insertRecord(record);
            System.out.println("[异步] 用户 " + userName + " 抢到红包 " + packetNo + "，金额：" + amount + " (已保存到MySQL)");
        } catch (Exception e) {
            System.err.println("[异步] 保存抢红包记录到MySQL失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
