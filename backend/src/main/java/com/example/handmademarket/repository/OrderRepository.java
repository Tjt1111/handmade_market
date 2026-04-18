package com.example.handmademarket.repository;

import com.example.handmademarket.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, String> {

    List<Order> findByBuyerIdOrderByCreateTimeDesc(Integer buyerId);

    List<Order> findBySellerIdOrderByCreateTimeDesc(Integer sellerId);

    List<Order> findAllByOrderByCreateTimeDesc();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.sellerId = :sellerId")
    long countBySellerId(@Param("sellerId") Integer sellerId);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.sellerId = :sellerId AND o.status = :status")
    long countBySellerIdAndStatus(@Param("sellerId") Integer sellerId, @Param("status") Integer status);

    @Query("SELECT COALESCE(SUM(o.amount), 0) FROM Order o WHERE o.sellerId = :sellerId AND o.status >= 1")
    BigDecimal sumAmountBySellerId(@Param("sellerId") Integer sellerId);
}
