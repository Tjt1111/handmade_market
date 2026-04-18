package com.example.handmademarket.service;

import com.example.handmademarket.dto.CreateOrderRequest;
import com.example.handmademarket.dto.EvaluationRequest;
import com.example.handmademarket.util.ResponseResult;

public interface OrderService {

    /** 买家：查看我的订单 */
    ResponseResult getBuyerOrders(String username);

    /** 卖家：查看销售订单 */
    ResponseResult getSellerOrders(String username);

    /** 卖家：销售统计 */
    ResponseResult getSellerStats(String username);

    /** 管理员：查看所有订单 */
    ResponseResult getAllOrders();

    /** 创建订单（从购物车结算） */
    ResponseResult createOrder(String username, CreateOrderRequest request);

    /** 订单详情 */
    ResponseResult getOrderDetail(String orderId);

    /** 买家支付订单 */
    ResponseResult payOrder(String username, String orderId, String payType);

    /** 卖家发货 */
    ResponseResult shipOrder(String username, String orderId, String logisticsInfo);

    /** 买家确认收货 */
    ResponseResult confirmReceipt(String username, String orderId);

    /** 取消订单 */
    ResponseResult cancelOrder(String username, String orderId);

    /** 评价订单 */
    ResponseResult evaluateOrder(String username, String orderId, EvaluationRequest request);
}
