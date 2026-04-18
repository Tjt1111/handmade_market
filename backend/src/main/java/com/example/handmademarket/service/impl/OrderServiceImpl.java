package com.example.handmademarket.service.impl;

import com.example.handmademarket.dto.CreateOrderRequest;
import com.example.handmademarket.dto.EvaluationRequest;
import com.example.handmademarket.entity.*;
import com.example.handmademarket.repository.*;
import com.example.handmademarket.service.OrderService;
import com.example.handmademarket.util.ResponseResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderGoodsRepository orderGoodsRepository;
    private final GoodsRepository goodsRepository;
    private final UserRepository userRepository;
    private final EvaluationRepository evaluationRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderGoodsRepository orderGoodsRepository,
                            GoodsRepository goodsRepository,
                            UserRepository userRepository,
                            EvaluationRepository evaluationRepository) {
        this.orderRepository = orderRepository;
        this.orderGoodsRepository = orderGoodsRepository;
        this.goodsRepository = goodsRepository;
        this.userRepository = userRepository;
        this.evaluationRepository = evaluationRepository;
    }

    // 状态映射：DB integer -> 前端 string
    private String statusToString(Integer status) {
        if (status == null) return "pending";
        return switch (status) {
            case 0 -> "pending";
            case 1 -> "paid";
            case 2 -> "paid";     // 待发货也算已支付
            case 3 -> "shipped";
            case 4 -> "completed";
            case 5 -> "cancelled";
            default -> "pending";
        };
    }

    // 生成订单号：HM + 日期 + 4位随机数
    private String generateOrderId() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomPart = String.format("%04d", new Random().nextInt(10000));
        return "HM" + datePart + randomPart;
    }

    // 根据 username 获取 userId
    private User getUserByUsername(String username) {
        return userRepository.findByUserAccount(username)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    @Override
    public ResponseResult getBuyerOrders(String username) {
        User user = getUserByUsername(username);
        List<Order> orders = orderRepository.findByBuyerIdOrderByCreateTimeDesc(
                user.getUser_id().intValue());

        List<Map<String, Object>> result = orders.stream().map(order -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", order.getOrderId());
            map.put("no", order.getOrderId());
            map.put("time", order.getCreateTime() != null
                    ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "");
            map.put("status", statusToString(order.getStatus()));

            // 获取订单商品
            List<OrderGoods> goodsList = orderGoodsRepository.findByOrderId(order.getOrderId());
            if (!goodsList.isEmpty()) {
                OrderGoods firstItem = goodsList.get(0);
                map.put("name", firstItem.getGoodsName());
                map.put("quantity", firstItem.getNum());
                map.put("price", firstItem.getPrice());

                // 获取商品图片
                Optional<Goods> goods = goodsRepository.findById(firstItem.getGoodsId().longValue());
                map.put("image", goods.map(Goods::getImageUrl).orElse(""));
            } else {
                map.put("name", "");
                map.put("quantity", 0);
                map.put("price", BigDecimal.ZERO);
                map.put("image", "");
            }

            map.put("total", order.getAmount());
            // 检查是否已评价
            map.put("commented", evaluationRepository.existsByOrderId(order.getOrderId()));

            return map;
        }).collect(Collectors.toList());

        return ResponseResult.ok(result);
    }

    @Override
    public ResponseResult getSellerOrders(String username) {
        User user = getUserByUsername(username);
        List<Order> orders = orderRepository.findBySellerIdOrderByCreateTimeDesc(
                user.getUser_id().intValue());

        List<Map<String, Object>> result = orders.stream().map(order -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", order.getOrderId());
            map.put("no", order.getOrderId());
            map.put("status", statusToString(order.getStatus()));
            map.put("amount", order.getAmount());
            map.put("time", order.getCreateTime() != null
                    ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
            map.put("address", order.getDeliveryAddress());

            // 获取买家信息
            if (order.getBuyerId() != null) {
                userRepository.findById(order.getBuyerId().longValue())
                        .ifPresent(buyer -> map.put("buyer", buyer.getUserName() != null ? buyer.getUserName() : buyer.getUserAccount()));
            }

            // 获取订单商品
            List<OrderGoods> goodsList = orderGoodsRepository.findByOrderId(order.getOrderId());
            if (!goodsList.isEmpty()) {
                OrderGoods firstItem = goodsList.get(0);
                map.put("name", firstItem.getGoodsName());
                map.put("quantity", firstItem.getNum());
                map.put("price", firstItem.getPrice());
                Optional<Goods> goods = goodsRepository.findById(firstItem.getGoodsId().longValue());
                map.put("image", goods.map(Goods::getImageUrl).orElse(""));
            } else {
                map.put("name", "");
                map.put("quantity", 0);
                map.put("price", BigDecimal.ZERO);
                map.put("image", "");
            }

            return map;
        }).collect(Collectors.toList());

        return ResponseResult.ok(result);
    }

    @Override
    public ResponseResult getSellerStats(String username) {
        User user = getUserByUsername(username);
        Integer sellerId = user.getUser_id().intValue();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSales", orderRepository.sumAmountBySellerId(sellerId));
        stats.put("totalOrders", orderRepository.countBySellerId(sellerId));
        stats.put("pendingOrders", orderRepository.countBySellerIdAndStatus(sellerId, 0)
                + orderRepository.countBySellerIdAndStatus(sellerId, 1));
        // 在售商品数量
        long goodsCount = goodsRepository.countByCreatorId(sellerId);
        stats.put("totalGoods", goodsCount);

        return ResponseResult.ok(stats);
    }

    @Override
    public ResponseResult getAllOrders() {
        List<Order> orders = orderRepository.findAllByOrderByCreateTimeDesc();

        List<Map<String, Object>> result = orders.stream().map(order -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", order.getOrderId());
            map.put("no", order.getOrderId());
            map.put("amount", order.getAmount());
            map.put("status", statusToString(order.getStatus()));
            map.put("time", order.getCreateTime() != null
                    ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) : "");
            map.put("address", order.getDeliveryAddress());

            // 买家信息
            if (order.getBuyerId() != null) {
                userRepository.findById(order.getBuyerId().longValue())
                        .ifPresent(buyer -> map.put("buyer", buyer.getUserName() != null ? buyer.getUserName() : buyer.getUserAccount()));
            }
            // 卖家信息
            if (order.getSellerId() != null) {
                userRepository.findById(order.getSellerId().longValue())
                        .ifPresent(seller -> map.put("seller", seller.getUserName() != null ? seller.getUserName() : seller.getUserAccount()));
            }

            return map;
        }).collect(Collectors.toList());

        return ResponseResult.ok(result);
    }

    @Override
    @Transactional
    public ResponseResult createOrder(String username, CreateOrderRequest request) {
        User buyer = getUserByUsername(username);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            return ResponseResult.fail("订单商品不能为空");
        }
        if (request.getDeliveryAddress() == null || request.getDeliveryAddress().isBlank()) {
            return ResponseResult.fail("收货地址不能为空");
        }

        // 按卖家分组创建订单（不同卖家的商品拆分为不同订单）
        // 先获取所有商品信息
        Map<Integer, List<CreateOrderRequest.OrderItemDTO>> sellerItemsMap = new LinkedHashMap<>();
        Map<Integer, Goods> goodsMap = new HashMap<>();

        for (CreateOrderRequest.OrderItemDTO item : request.getItems()) {
            Goods goods = goodsRepository.findById(item.getGoodsId().longValue())
                    .orElseThrow(() -> new RuntimeException("商品不存在: " + item.getGoodsId()));
            goodsMap.put(item.getGoodsId(), goods);

            // 这里需要从Goods实体获取卖家ID（creator_id），当前Goods实体中没有这个字段
            // 暂时使用 goods.getId() 作为 sellerId 的占位
            Integer sellerId = goods.getCreatorId() != null ? goods.getCreatorId() : 0;
            sellerItemsMap.computeIfAbsent(sellerId, k -> new ArrayList<>()).add(item);
        }

        List<String> orderIds = new ArrayList<>();

        for (Map.Entry<Integer, List<CreateOrderRequest.OrderItemDTO>> entry : sellerItemsMap.entrySet()) {
            Integer sellerId = entry.getKey();
            List<CreateOrderRequest.OrderItemDTO> items = entry.getValue();

            // 计算总金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            for (CreateOrderRequest.OrderItemDTO item : items) {
                Goods goods = goodsMap.get(item.getGoodsId());
                BigDecimal price = goods.getPrice() != null ? BigDecimal.valueOf(goods.getPrice()) : BigDecimal.ZERO;
                totalAmount = totalAmount.add(price.multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            // 创建订单
            Order order = new Order();
            String orderId = generateOrderId();
            order.setOrderId(orderId);
            order.setBuyerId(buyer.getUser_id().intValue());
            order.setSellerId(sellerId);
            order.setOrderType(1); // 普通订单
            order.setAmount(totalAmount);
            order.setDeliveryAddress(request.getDeliveryAddress());
            order.setPayType(request.getPayType());
            order.setRemark(request.getRemark());
            order.setCreateTime(LocalDateTime.now());
            order.setStatus(0); // 待支付

            orderRepository.save(order);

            // 创建订单商品
            for (CreateOrderRequest.OrderItemDTO item : items) {
                Goods goods = goodsMap.get(item.getGoodsId());
                OrderGoods og = new OrderGoods();
                og.setOrderId(orderId);
                og.setGoodsId(item.getGoodsId());
                og.setGoodsName(goods.getTitle());
                og.setPrice(goods.getPrice() != null ? BigDecimal.valueOf(goods.getPrice()) : BigDecimal.ZERO);
                og.setNum(item.getQuantity());
                og.setCreateTime(LocalDateTime.now());
                orderGoodsRepository.save(og);
            }

            orderIds.add(orderId);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("orderIds", orderIds);
        data.put("message", "订单创建成功");
        return ResponseResult.ok(data);
    }

    @Override
    public ResponseResult getOrderDetail(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", order.getOrderId());
        map.put("no", order.getOrderId());
        map.put("status", statusToString(order.getStatus()));
        map.put("amount", order.getAmount());
        map.put("address", order.getDeliveryAddress());
        map.put("createTime", order.getCreateTime());
        map.put("payTime", order.getPayTime());
        map.put("deliveryTime", order.getDeliveryTime());
        map.put("receiveTime", order.getReceiveTime());
        map.put("logisticsInfo", order.getLogisticsInfo());
        map.put("remark", order.getRemark());

        // 订单商品
        List<OrderGoods> goodsList = orderGoodsRepository.findByOrderId(orderId);
        List<Map<String, Object>> items = goodsList.stream().map(og -> {
            Map<String, Object> itemMap = new LinkedHashMap<>();
            itemMap.put("goodsId", og.getGoodsId());
            itemMap.put("name", og.getGoodsName());
            itemMap.put("price", og.getPrice());
            itemMap.put("quantity", og.getNum());
            Optional<Goods> goods = goodsRepository.findById(og.getGoodsId().longValue());
            itemMap.put("image", goods.map(Goods::getImageUrl).orElse(""));
            return itemMap;
        }).collect(Collectors.toList());
        map.put("items", items);

        return ResponseResult.ok(map);
    }

    @Override
    @Transactional
    public ResponseResult payOrder(String username, String orderId, String payType) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getBuyerId().equals(user.getUser_id().intValue())) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            return ResponseResult.fail("订单状态不允许支付");
        }

        order.setStatus(1); // 已支付
        order.setPayTime(LocalDateTime.now());
        if (payType != null && !payType.isBlank()) {
            order.setPayType(payType);
        }
        orderRepository.save(order);

        return ResponseResult.ok("支付成功");
    }

    @Override
    @Transactional
    public ResponseResult shipOrder(String username, String orderId, String logisticsInfo) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getSellerId().equals(user.getUser_id().intValue())) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (order.getStatus() != 1) {
            return ResponseResult.fail("订单状态不允许发货");
        }

        order.setStatus(3); // 已发货
        order.setDeliveryTime(LocalDateTime.now());
        if (logisticsInfo != null && !logisticsInfo.isBlank()) {
            order.setLogisticsInfo(logisticsInfo);
        }
        orderRepository.save(order);

        return ResponseResult.ok("发货成功");
    }

    @Override
    @Transactional
    public ResponseResult confirmReceipt(String username, String orderId) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getBuyerId().equals(user.getUser_id().intValue())) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (order.getStatus() != 3) {
            return ResponseResult.fail("订单状态不允许确认收货");
        }

        order.setStatus(4); // 已完成
        order.setReceiveTime(LocalDateTime.now());
        orderRepository.save(order);

        return ResponseResult.ok("确认收货成功");
    }

    @Override
    @Transactional
    public ResponseResult cancelOrder(String username, String orderId) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        Integer userId = user.getUser_id().intValue();
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            return ResponseResult.fail("无权操作此订单");
        }
        if (order.getStatus() != 0) {
            return ResponseResult.fail("只有待支付订单可以取消");
        }

        order.setStatus(5); // 已取消
        orderRepository.save(order);

        return ResponseResult.ok("订单已取消");
    }

    @Override
    @Transactional
    public ResponseResult evaluateOrder(String username, String orderId, EvaluationRequest request) {
        User user = getUserByUsername(username);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!order.getBuyerId().equals(user.getUser_id().intValue())) {
            return ResponseResult.fail("只有买家可以评价");
        }
        if (order.getStatus() != 4) {
            return ResponseResult.fail("只有已完成的订单可以评价");
        }
        if (evaluationRepository.existsByOrderId(orderId)) {
            return ResponseResult.fail("该订单已评价");
        }

        if (request.getScore() == null || request.getScore() < 1 || request.getScore() > 5) {
            return ResponseResult.fail("评分必须在1-5之间");
        }

        Evaluation eval = new Evaluation();
        eval.setEvalId(String.valueOf(System.currentTimeMillis()));
        eval.setOrderId(orderId);
        eval.setEvaluatorId(user.getUser_id().intValue());
        eval.setEvaluatedId(order.getSellerId());
        eval.setScore(request.getScore());
        eval.setContent(request.getContent());
        eval.setImages(request.getImages());
        eval.setCreateTime(LocalDateTime.now());
        eval.setStatus(0); // 正常

        evaluationRepository.save(eval);

        return ResponseResult.ok("评价成功");
    }
}
