package com.example.demo.service;

import com.example.demo.mapper.RedPacketAmountMapper;
import com.example.demo.mapper.RedPacketMapper;
import com.example.demo.mapper.RedPacketRecordMapper;
import com.example.demo.model.RedPacket;
import com.example.demo.model.RedPacketRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/**
 * 红包服务类（纯MySQL版本，用于性能对比）
 */
@Service("redPacketMySqlService")
public class RedPacketMySqlService {

    @Autowired
    private RedPacketMapper redPacketMapper;

    @Autowired
    private RedPacketAmountMapper redPacketAmountMapper;

    @Autowired
    private RedPacketRecordMapper redPacketRecordMapper;

    /**
     * 发布红包
     */
    @Transactional
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

        try {
            // 生成红包金额列表（二倍均值法）
            List<BigDecimal> amountList = generateRedPacketAmounts(totalAmount, totalCount);

            // 创建红包实体
            RedPacket redPacket = new RedPacket();
            redPacket.setPacketNo(packetNo);
            redPacket.setTotalAmount(totalAmount);
            redPacket.setTotalCount(totalCount);
            redPacket.setPublisher(publisher);
            redPacket.setStatus(0);
            redPacket.setExpireTime(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)); // 24小时后过期

            // 插入红包主表
            redPacketMapper.insertRedPacket(redPacket);

            // 批量插入红包金额明细
            redPacketAmountMapper.batchInsertAmounts(packetNo, amountList);

            result.put("success", true);
            result.put("packetNo", packetNo);
            result.put("message", "红包发布成功");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "红包发布失败：" + e.getMessage());
            throw e;
        }

        return result;
    }

    /**
     * 抢红包（使用数据库事务保证一致性）
     */
    @Transactional
    public Map<String, Object> grabRedPacket(String packetNo, String userName) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 检查是否已抢过
            int count = redPacketRecordMapper.countByPacketNoAndUserName(packetNo, userName);
            if (count > 0) {
                result.put("success", false);
                result.put("message", "已经抢过红包了");
                return result;
            }

            // 加锁查询第一个未抢的金额行（SELECT ... FOR UPDATE），行锁持有至事务提交，
            // 保证并发下不会有两个线程抢到同一行，从根本上避免超发
            Map<String, Object> slot = redPacketAmountMapper.selectFirstUngrabbedForUpdate(packetNo);
            if (slot == null || slot.get("id") == null) {
                result.put("success", false);
                result.put("message", "红包已被抢完");
                return result;
            }
            Long amountId = ((Number) slot.get("id")).longValue();
            BigDecimal amount = new BigDecimal(slot.get("amount").toString());

            // 按主键 id 精确标记该行为已抢
            redPacketAmountMapper.markAsGrabbedById(amountId);

            // 原子扣减红包主表剩余数量和金额（避免先查后写的丢失更新）
            redPacketMapper.decrementRemain(packetNo, amount);
            // 若剩余数量为 0，更新状态为已结束
            redPacketMapper.finishIfEmpty(packetNo);

            // 保存抢红包记录
            RedPacketRecord record = new RedPacketRecord();
            record.setPacketNo(packetNo);
            record.setUserName(userName);
            record.setAmount(amount);
            redPacketRecordMapper.insertRecord(record);

            result.put("success", true);
            result.put("amount", amount);
            result.put("message", "恭喜抢到 " + amount + " 元");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "抢红包失败：" + e.getMessage());
            throw e; // 抛出异常触发事务回滚
        }

        return result;
    }

    /**
     * 查询红包详情
     */
    public Map<String, Object> getRedPacketDetail(String packetNo) {
        Map<String, Object> result = new HashMap<>();

        RedPacket redPacket = redPacketMapper.selectByPacketNo(packetNo);
        if (redPacket == null) {
            result.put("success", false);
            result.put("message", "红包不存在");
            return result;
        }

        result.put("success", true);
        result.put("data", redPacket);
        return result;
    }

    /**
     * 查询用户的抢红包记录
     */
    public List<RedPacketRecord> getUserGrabRecords(String userName) {
        return redPacketRecordMapper.selectByUserName(userName);
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
}
