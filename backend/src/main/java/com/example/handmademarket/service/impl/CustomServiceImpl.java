package com.example.handmademarket.service.impl;

import com.example.handmademarket.dto.CustomRequest;
import com.example.handmademarket.entity.Custom;
import com.example.handmademarket.entity.Order;
import com.example.handmademarket.entity.User;
import com.example.handmademarket.repository.CustomRepository;
import com.example.handmademarket.repository.OrderRepository;
import com.example.handmademarket.repository.UserRepository;
import com.example.handmademarket.service.CustomService;
import com.example.handmademarket.util.ResponseResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CustomServiceImpl implements CustomService {

    private final CustomRepository customRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    public CustomServiceImpl(CustomRepository customRepository,
                             UserRepository userRepository,
                             OrderRepository orderRepository) {
        this.customRepository = customRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUserAccount(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    private String generateOrderId() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "HM" + datePart + randomPart;
    }

    private String customStatusToChinese(Integer status) {
        if (status == null) return "待匹配";
        return switch (status) {
            case 0 -> "待匹配";
            case 1 -> "沟通中";
            case 2 -> "已接单";
            case 3 -> "已完成";
            case 4 -> "已取消";
            case 5 -> "已拒绝";
            default -> "未知";
        };
    }

    private Map<String, Object> buildCustomMap(Custom custom) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("customId", custom.getCustomId());
        map.put("customDesc", custom.getCustomDesc());
        map.put("referenceImages", custom.getReferenceImages());
        map.put("quantity", custom.getQuantity());
        map.put("isWholesale", custom.getIsWholesale());
        map.put("budget", custom.getBudget());
        map.put("finalUnitPrice", custom.getFinalUnitPrice());
        map.put("finalTotalPrice", custom.getFinalTotalPrice());
        map.put("cycle", custom.getCycle());
        map.put("category", custom.getCategory());
        map.put("style", custom.getStyle());
        map.put("contact", custom.getContact());
        map.put("deliverContent", custom.getDeliverContent());
        map.put("status", custom.getStatus());
        map.put("statusText", customStatusToChinese(custom.getStatus()));
        map.put("remark", custom.getRemark());
        map.put("submitTime", custom.getSubmitTime() != null
                ? custom.getSubmitTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        map.put("acceptTime", custom.getAcceptTime() != null
                ? custom.getAcceptTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);
        map.put("finishTime", custom.getFinishTime() != null
                ? custom.getFinishTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : null);

        // 消费者信息
        if (custom.getConsumerId() != null) {
            userRepository.findById(custom.getConsumerId().longValue())
                    .ifPresent(u -> {
                        map.put("consumerName", u.getUserName() != null ? u.getUserName() : u.getUserAccount());
                        map.put("consumerId", u.getUserId());
                    });
        }
        // 创作者信息
        if (custom.getCreatorId() != null) {
            userRepository.findById(custom.getCreatorId().longValue())
                    .ifPresent(u -> {
                        map.put("creatorName", u.getUserName() != null ? u.getUserName() : u.getUserAccount());
                        map.put("creatorId", u.getUserId());
                    });
        }
        return map;
    }

    // ==================== 需求方操作 ====================

    @Override
    @Transactional
    public ResponseResult submitCustom(String username, CustomRequest request) {
        User user = getUserByUsername(username);

        if (request.getCustomDesc() == null || request.getCustomDesc().isBlank()) {
            return ResponseResult.fail("定制需求描述不能为空");
        }
        if (request.getBudget() == null || request.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseResult.fail("预算金额必须大于0");
        }
        if (request.getCycle() == null || request.getCycle() <= 0) {
            return ResponseResult.fail("期望周期必须大于0");
        }
        if (request.getCategory() == null || request.getCategory().isBlank()) {
            return ResponseResult.fail("定制品类不能为空");
        }

        Custom custom = new Custom();
        custom.setConsumerId(user.getUserId().intValue());
        custom.setCustomDesc(request.getCustomDesc());
        custom.setReferenceImages(request.getReferenceImages());
        custom.setQuantity(request.getQuantity() != null ? request.getQuantity() : 1);
        custom.setIsWholesale(request.getIsWholesale() != null ? request.getIsWholesale() : false);
        custom.setBudget(request.getBudget());
        custom.setCycle(request.getCycle());
        custom.setCategory(request.getCategory());
        custom.setStyle(request.getStyle());
        custom.setContact(request.getContact());
        custom.setSubmitTime(LocalDateTime.now());
        custom.setStatus(0); // 待匹配

        // 自动匹配：根据品类查找擅长该领域的用户（不区分身份，所有用户均可接单）
        List<User> allUsers = userRepository.findAll();
        Integer currentUserId = user.getUserId().intValue();
        List<String> matchedCreatorIds = allUsers.stream()
                .filter(u -> !currentUserId.equals(u.getUserId().intValue())) // 排除自己
                .filter(u -> u.getSpecialty() != null && u.getSpecialty().contains(request.getCategory()))
                .map(u -> String.valueOf(u.getUserId()))
                .collect(Collectors.toList());

        if (!matchedCreatorIds.isEmpty()) {
            custom.setMatchCreators(String.join(",", matchedCreatorIds));
        }

        customRepository.save(custom);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("customId", custom.getCustomId());
        data.put("matchedCreators", matchedCreatorIds.size());
        data.put("message", matchedCreatorIds.isEmpty()
                ? "定制需求已提交，暂未匹配到合适的创作者，请等待"
                : "定制需求已提交，已匹配到 " + matchedCreatorIds.size() + " 位创作者");
        return ResponseResult.ok("定制需求提交成功", data);
    }

    @Override
    public ResponseResult getMyCustoms(String username) {
        User user = getUserByUsername(username);
        List<Custom> customs = customRepository.findByConsumerIdOrderBySubmitTimeDesc(
                user.getUserId().intValue());
        List<Map<String, Object>> result = customs.stream()
                .map(this::buildCustomMap).collect(Collectors.toList());
        return ResponseResult.ok(result);
    }

    @Override
    @Transactional
    public ResponseResult cancelCustom(String username, Integer customId) {
        User user = getUserByUsername(username);
        Custom custom = customRepository.findById(customId)
                .orElseThrow(() -> new RuntimeException("定制需求不存在"));

        if (!custom.getConsumerId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("无权操作此定制需求");
        }
        if (custom.getStatus() >= 3) {
            return ResponseResult.fail("该定制需求已完成或已取消，无法再取消");
        }

        custom.setStatus(4); // 已取消
        custom.setRemark("需求方主动取消");
        customRepository.save(custom);
        return ResponseResult.ok("定制需求已取消");
    }

    @Override
    @Transactional
    public ResponseResult confirmCustom(String username, Integer customId, String deliveryAddress) {
        User user = getUserByUsername(username);
        Custom custom = customRepository.findById(customId)
                .orElseThrow(() -> new RuntimeException("定制需求不存在"));

        if (!custom.getConsumerId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("无权操作此定制需求");
        }
        if (custom.getStatus() != 2) {
            return ResponseResult.fail("该定制需求状态不允许确认（需为已接单状态）");
        }
        if (custom.getDeliverContent() == null || custom.getDeliverContent().isBlank()) {
            return ResponseResult.fail("接单方尚未交付作品，无法确认");
        }
        if (deliveryAddress == null || deliveryAddress.isBlank()) {
            return ResponseResult.fail("收货地址不能为空");
        }

        // 标记定制需求完成
        custom.setStatus(3); // 已完成
        custom.setFinishTime(LocalDateTime.now());
        customRepository.save(custom);

        // 自动生成定制订单（orderType=2）
        BigDecimal totalPrice = custom.getFinalTotalPrice();
        if (totalPrice == null) {
            // fallback: finalUnitPrice * quantity
            BigDecimal unitPrice = custom.getFinalUnitPrice() != null ? custom.getFinalUnitPrice() : custom.getBudget();
            totalPrice = unitPrice.multiply(BigDecimal.valueOf(custom.getQuantity()));
        }

        // 定金 = 总价的30%
        BigDecimal deposit = totalPrice.multiply(new BigDecimal("0.3"))
                .setScale(2, java.math.RoundingMode.CEILING);
        BigDecimal balance = totalPrice.subtract(deposit);

        Order order = new Order();
        String orderId = generateOrderId();
        order.setOrderId(orderId);
        order.setBuyerId(custom.getConsumerId());
        order.setSellerId(custom.getCreatorId());
        order.setCustomId(custom.getCustomId());
        order.setOrderType(2); // 定制订单
        order.setAmount(totalPrice);
        order.setDeposit(deposit);
        order.setBalance(balance);
        order.setDeliveryAddress(deliveryAddress);
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(0); // 待支付（定金）
        order.setPaymentStatus(0);
        order.setRemark("定制需求#" + customId + " 自动生成订单");
        orderRepository.save(order);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("customId", customId);
        data.put("orderId", orderId);
        data.put("totalPrice", totalPrice);
        data.put("deposit", deposit);
        data.put("balance", balance);
        data.put("message", "定制确认成功，已生成定制订单，请支付定金");
        return ResponseResult.ok("定制确认成功", data);
    }

    // ==================== 接单方操作 ====================

    @Override
    public ResponseResult getAvailableCustoms(String username) {
        User user = getUserByUsername(username);
        String userId = String.valueOf(user.getUserId());

        // 查找 status=0(待匹配) 的定制需求
        List<Custom> allPending = customRepository.findByStatusOrderBySubmitTimeDesc(0);

        // 过滤：matchCreators 包含当前创作者ID，或 matchCreators 为空（开放接单）
        List<Map<String, Object>> result = allPending.stream()
                .filter(c -> {
                    String match = c.getMatchCreators();
                    return match == null || match.isEmpty() || match.contains(userId);
                })
                .map(this::buildCustomMap)
                .collect(Collectors.toList());

        return ResponseResult.ok(result);
    }

    @Override
    public ResponseResult getMyAcceptedCustoms(String username) {
        User user = getUserByUsername(username);
        List<Custom> customs = customRepository.findByCreatorIdOrderBySubmitTimeDesc(
                user.getUserId().intValue());
        List<Map<String, Object>> result = customs.stream()
                .map(this::buildCustomMap).collect(Collectors.toList());
        return ResponseResult.ok(result);
    }

    @Override
    @Transactional
    public ResponseResult acceptCustom(String username, Integer customId, BigDecimal quotedPrice) {
        User user = getUserByUsername(username);
        Custom custom = customRepository.findById(customId)
                .orElseThrow(() -> new RuntimeException("定制需求不存在"));

        if (custom.getStatus() != 0) {
            return ResponseResult.fail("该定制需求已被接单或已取消");
        }

        if (quotedPrice == null || quotedPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return ResponseResult.fail("报价必须大于0");
        }

        custom.setCreatorId(user.getUserId().intValue());
        custom.setFinalUnitPrice(quotedPrice);
        custom.setStatus(2); // 已接单
        custom.setAcceptTime(LocalDateTime.now());
        customRepository.save(custom);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("customId", customId);
        data.put("quotedPrice", quotedPrice);
        data.put("totalPrice", quotedPrice.multiply(BigDecimal.valueOf(custom.getQuantity())));
        data.put("message", "接单成功，请按时完成定制作品");
        return ResponseResult.ok("接单成功", data);
    }

    @Override
    @Transactional
    public ResponseResult rejectCustom(String username, Integer customId, String reason) {
        User user = getUserByUsername(username);
        Custom custom = customRepository.findById(customId)
                .orElseThrow(() -> new RuntimeException("定制需求不存在"));

        if (custom.getStatus() != 0) {
            return ResponseResult.fail("该定制需求状态不允许拒绝");
        }

        // 从匹配列表中移除当前创作者
        String userId = String.valueOf(user.getUserId());
        if (custom.getMatchCreators() != null) {
            List<String> creators = new ArrayList<>(Arrays.asList(custom.getMatchCreators().split(",")));
            creators.remove(userId);
            custom.setMatchCreators(creators.isEmpty() ? null : String.join(",", creators));
        }

        // 如果只有一个匹配创作者且被拒绝了，标记为已拒绝
        if (custom.getMatchCreators() == null || custom.getMatchCreators().isEmpty()) {
            // 不自动拒绝，保持待匹配状态让其他创作者可以接单
        }

        customRepository.save(custom);
        return ResponseResult.ok("已拒绝该定制需求");
    }

    @Override
    @Transactional
    public ResponseResult deliverCustom(String username, Integer customId, String deliverContent) {
        User user = getUserByUsername(username);
        Custom custom = customRepository.findById(customId)
                .orElseThrow(() -> new RuntimeException("定制需求不存在"));

        if (!custom.getCreatorId().equals(user.getUserId().intValue())) {
            return ResponseResult.fail("无权操作此定制需求");
        }
        if (custom.getStatus() != 2) {
            return ResponseResult.fail("该定制需求状态不允许交付（需为已接单状态）");
        }
        if (deliverContent == null || deliverContent.isBlank()) {
            return ResponseResult.fail("交付内容不能为空");
        }

        custom.setDeliverContent(deliverContent);
        customRepository.save(custom);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("customId", customId);
        data.put("deliverContent", deliverContent);
        data.put("message", "作品已交付，等待消费者确认");
        return ResponseResult.ok("交付成功", data);
    }

    // ==================== 通用操作 ====================

    @Override
    public ResponseResult getCustomDetail(Integer customId) {
        Custom custom = customRepository.findById(customId)
                .orElseThrow(() -> new RuntimeException("定制需求不存在"));
        return ResponseResult.ok(buildCustomMap(custom));
    }

    @Override
    public ResponseResult getAllCustoms() {
        List<Custom> customs = customRepository.findAllByOrderBySubmitTimeDesc();
        List<Map<String, Object>> result = customs.stream()
                .map(this::buildCustomMap).collect(Collectors.toList());
        return ResponseResult.ok(result);
    }
}
