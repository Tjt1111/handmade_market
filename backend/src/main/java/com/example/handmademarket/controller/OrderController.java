package com.example.handmademarket.controller;

import com.example.handmademarket.dto.CreateOrderRequest;
import com.example.handmademarket.dto.EvaluationRequest;
import com.example.handmademarket.service.OrderService;
import com.example.handmademarket.util.JwtUtil;
import com.example.handmademarket.util.ResponseResult;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;
    private final JwtUtil jwtUtil;

    public OrderController(OrderService orderService, JwtUtil jwtUtil) {
        this.orderService = orderService;
        this.jwtUtil = jwtUtil;
    }

    // 从 Authorization header 中提取用户名，如果没有则使用测试用户
    private String extractUsername(String authHeader) {
        // 测试模式：如果没有 token，使用默认测试用户
        if (authHeader == null || authHeader.isBlank()) {
            return "testuser";  // 默认测试用户
        }
        
        if (!authHeader.startsWith("Bearer ")) {
            return "testuser";  // 默认测试用户
        }
        
        String token = authHeader.substring(7);
        if (!jwtUtil.validateToken(token)) {
            return "testuser";  // token 无效时也用测试用户
        }
        return jwtUtil.getUsernameFromToken(token);
    }

    /** 买家：获取我的订单列表 */
    @GetMapping("/user/orders")
    public ResponseEntity<ResponseResult> getBuyerOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.getBuyerOrders(username));
    }

    /** 卖家：获取销售订单列表 */
    @GetMapping("/seller/orders")
    public ResponseEntity<ResponseResult> getSellerOrders(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.getSellerOrders(username));
    }

    /** 卖家：获取销售统计 */
    @GetMapping("/seller/stats")
    public ResponseEntity<ResponseResult> getSellerStats(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.getSellerStats(username));
    }

    /** 管理员：获取所有订单 */
    @GetMapping("/admin/orders")
    public ResponseEntity<ResponseResult> getAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    /** 创建订单（从购物车结算） */
    @PostMapping("/orders")
    public ResponseEntity<ResponseResult> createOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody CreateOrderRequest request) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.createOrder(username, request));
    }

    /** 买家支付订单 */
    @PutMapping("/orders/{orderId}/pay")
    public ResponseEntity<ResponseResult> payOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, String> body) {
        String username = extractUsername(authHeader);
        String payType = body != null ? body.get("payType") : null;
        return ResponseEntity.ok(orderService.payOrder(username, orderId, payType));
    }

    /** 卖家发货 */
    @PutMapping("/orders/{orderId}/ship")
    public ResponseEntity<ResponseResult> shipOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId,
            @RequestBody(required = false) Map<String, String> body) {
        String username = extractUsername(authHeader);
        String logisticsInfo = body != null ? body.get("logisticsInfo") : null;
        return ResponseEntity.ok(orderService.shipOrder(username, orderId, logisticsInfo));
    }

    /** 买家确认收货 */
    @PutMapping("/orders/{orderId}/confirm")
    public ResponseEntity<ResponseResult> confirmReceipt(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.confirmReceipt(username, orderId));
    }

    /** 取消订单 */
    @PutMapping("/orders/{orderId}/cancel")
    public ResponseEntity<ResponseResult> cancelOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.cancelOrder(username, orderId));
    }

    /** 评价订单 */
    @PostMapping("/orders/{orderId}/evaluate")
    public ResponseEntity<ResponseResult> evaluateOrder(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String orderId,
            @RequestBody EvaluationRequest request) {
        String username = extractUsername(authHeader);
        return ResponseEntity.ok(orderService.evaluateOrder(username, orderId, request));
    }
}
