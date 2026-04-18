package com.example.handmademarket.util;

import com.example.handmademarket.entity.*;
import com.example.handmademarket.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 应用启动时初始化测试数据
 * 注意：仅用于测试环境，生产环境应删除或禁用
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final GoodsRepository goodsRepository;
    private final OrderRepository orderRepository;
    private final OrderGoodsRepository orderGoodsRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                          GoodsRepository goodsRepository,
                          OrderRepository orderRepository,
                          OrderGoodsRepository orderGoodsRepository,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.goodsRepository = goodsRepository;
        this.orderRepository = orderRepository;
        this.orderGoodsRepository = orderGoodsRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========== 初始化测试数据 ==========");

        // 创建测试用户
        createTestUsers();
        
        // 创建测试商品
        createTestGoods();
        
        // 创建测试订单
        createTestOrders();

        System.out.println("========== 测试数据初始化完成 ==========\n");
    }

    private void createTestUsers() {
        // 创建买家
        if (!userRepository.existsByUserAccount("testuser")) {
            User buyer = new User();
            buyer.setUserAccount("testuser");
            buyer.setPassword(passwordEncoder.encode("123456"));
            buyer.setEmail("testuser@example.com");
            buyer.setUserName("测试用户");
            buyer.setRole("1");  // 1-消费者
            buyer.setPhone("13800138000");
            userRepository.save(buyer);
            System.out.println("✓ 创建买家: testuser (密码: 123456)");
        }

        // 创建卖家
        if (!userRepository.existsByUserAccount("seller1")) {
            User seller = new User();
            seller.setUserAccount("seller1");
            seller.setPassword(passwordEncoder.encode("123456"));
            seller.setEmail("seller1@example.com");
            seller.setUserName("卖家1");
            seller.setRole("2");  // 2-创作者
            seller.setPhone("13800138001");
            userRepository.save(seller);
            System.out.println("✓ 创建卖家: seller1 (密码: 123456)");
        }

        // 创建另一个卖家
        if (!userRepository.existsByUserAccount("seller2")) {
            User seller2 = new User();
            seller2.setUserAccount("seller2");
            seller2.setPassword(passwordEncoder.encode("123456"));
            seller2.setEmail("seller2@example.com");
            seller2.setUserName("卖家2");
            seller2.setRole("2");  // 2-创作者
            seller2.setPhone("13800138002");
            userRepository.save(seller2);
            System.out.println("✓ 创建卖家: seller2 (密码: 123456)");
        }
    }

    private void createTestGoods() {
        // 创建商品1
        if (goodsRepository.findById(1L).isEmpty()) {
            Goods goods1 = new Goods();
            goods1.setTitle("青花瓷茶杯");
            goods1.setDescription("精美的手工青花瓷茶杯，传统工艺");
            goods1.setCategory("茶具");
            goods1.setPrice(198.0);
            goods1.setImageUrl("https://picsum.photos/id/30/200/200");
            goods1.setStatus(1);  // 已上架
            goods1.setCreatorId(1);  // seller1
            goodsRepository.save(goods1);
            System.out.println("✓ 创建商品: 青花瓷茶杯");
        }

        // 创建商品2
        if (goodsRepository.findById(2L).isEmpty()) {
            Goods goods2 = new Goods();
            goods2.setTitle("手工编织包");
            goods2.setDescription("用天然植物纤维手工编织的环保包包");
            goods2.setCategory("手工制品");
            goods2.setPrice(299.0);
            goods2.setImageUrl("https://picsum.photos/id/31/200/200");
            goods2.setStatus(1);
            goods2.setCreatorId(2);  // seller2
            goodsRepository.save(goods2);
            System.out.println("✓ 创建商品: 手工编织包");
        }

        // 创建商品3
        if (goodsRepository.findById(3L).isEmpty()) {
            Goods goods3 = new Goods();
            goods3.setTitle("黄杨木雕");
            goods3.setDescription("传统黄杨木雕工艺品");
            goods3.setCategory("木雕");
            goods3.setPrice(399.0);
            goods3.setImageUrl("https://picsum.photos/id/32/200/200");
            goods3.setStatus(1);
            goods3.setCreatorId(1);  // seller1
            goodsRepository.save(goods3);
            System.out.println("✓ 创建商品: 黄杨木雕");
        }
    }

    private void createTestOrders() {
        // 创建待支付订单
        if (orderRepository.findById("HM202412010001").isEmpty()) {
            Order order1 = new Order();
            order1.setOrderId("HM202412010001");
            order1.setBuyerId(1);  // testuser
            order1.setSellerId(1);  // seller1
            order1.setGoodsId(1);
            order1.setOrderType(1);
            order1.setAmount(new BigDecimal("198.00"));
            order1.setDeliveryAddress("北京市朝阳区xxx街道xxx号");
            order1.setPayType("模拟支付宝");
            order1.setCreateTime(LocalDateTime.now().minusDays(1));
            order1.setStatus(0);  // 待支付
            orderRepository.save(order1);

            OrderGoods og1 = new OrderGoods();
            og1.setOrderId("HM202412010001");
            og1.setGoodsId(1);
            og1.setGoodsName("青花瓷茶杯");
            og1.setPrice(new BigDecimal("198.00"));
            og1.setNum(1);
            og1.setCreateTime(LocalDateTime.now());
            orderGoodsRepository.save(og1);
            System.out.println("✓ 创建订单: HM202412010001 (待支付)");
        }

        // 创建已支付订单
        if (orderRepository.findById("HM202412010002").isEmpty()) {
            Order order2 = new Order();
            order2.setOrderId("HM202412010002");
            order2.setBuyerId(1);
            order2.setSellerId(2);  // seller2
            order2.setGoodsId(2);
            order2.setOrderType(1);
            order2.setAmount(new BigDecimal("299.00"));
            order2.setDeliveryAddress("上海市浦东新区xxx路xxx号");
            order2.setPayType("模拟支付宝");
            order2.setCreateTime(LocalDateTime.now().minusDays(2));
            order2.setPayTime(LocalDateTime.now().minusDays(2).plusHours(1));
            order2.setStatus(1);  // 已支付
            orderRepository.save(order2);

            OrderGoods og2 = new OrderGoods();
            og2.setOrderId("HM202412010002");
            og2.setGoodsId(2);
            og2.setGoodsName("手工编织包");
            og2.setPrice(new BigDecimal("299.00"));
            og2.setNum(1);
            og2.setCreateTime(LocalDateTime.now());
            orderGoodsRepository.save(og2);
            System.out.println("✓ 创建订单: HM202412010002 (已支付)");
        }

        // 创建已发货订单
        if (orderRepository.findById("HM202412010003").isEmpty()) {
            Order order3 = new Order();
            order3.setOrderId("HM202412010003");
            order3.setBuyerId(1);
            order3.setSellerId(1);
            order3.setGoodsId(3);
            order3.setOrderType(1);
            order3.setAmount(new BigDecimal("399.00"));
            order3.setDeliveryAddress("广州市天河区xxx街xxx号");
            order3.setPayType("模拟支付宝");
            order3.setCreateTime(LocalDateTime.now().minusDays(5));
            order3.setPayTime(LocalDateTime.now().minusDays(5).plusHours(1));
            order3.setDeliveryTime(LocalDateTime.now().minusDays(4));
            order3.setLogisticsInfo("中通快递，单号：2024120100001");
            order3.setStatus(3);  // 已发货
            orderRepository.save(order3);

            OrderGoods og3 = new OrderGoods();
            og3.setOrderId("HM202412010003");
            og3.setGoodsId(3);
            og3.setGoodsName("黄杨木雕");
            og3.setPrice(new BigDecimal("399.00"));
            og3.setNum(1);
            og3.setCreateTime(LocalDateTime.now());
            orderGoodsRepository.save(og3);
            System.out.println("✓ 创建订单: HM202412010003 (已发货)");
        }

        // 创建已完成订单
        if (orderRepository.findById("HM202412010004").isEmpty()) {
            Order order4 = new Order();
            order4.setOrderId("HM202412010004");
            order4.setBuyerId(1);
            order4.setSellerId(2);
            order4.setGoodsId(2);
            order4.setOrderType(1);
            order4.setAmount(new BigDecimal("299.00"));
            order4.setDeliveryAddress("北京市西城区xxx街xxx号");
            order4.setPayType("模拟支付宝");
            order4.setCreateTime(LocalDateTime.now().minusDays(10));
            order4.setPayTime(LocalDateTime.now().minusDays(10).plusHours(1));
            order4.setDeliveryTime(LocalDateTime.now().minusDays(9));
            order4.setReceiveTime(LocalDateTime.now().minusDays(7));
            order4.setStatus(4);  // 已完成
            orderRepository.save(order4);

            OrderGoods og4 = new OrderGoods();
            og4.setOrderId("HM202412010004");
            og4.setGoodsId(2);
            og4.setGoodsName("手工编织包");
            og4.setPrice(new BigDecimal("299.00"));
            og4.setNum(1);
            og4.setCreateTime(LocalDateTime.now());
            orderGoodsRepository.save(og4);
            System.out.println("✓ 创建订单: HM202412010004 (已完成)");
        }
    }
}
