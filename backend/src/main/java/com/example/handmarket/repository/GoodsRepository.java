package com.example.handmarket.repository;

import com.example.handmarket.entity.Goods;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GoodsRepository extends JpaRepository<Goods, Long> {
    List<Goods> findBySellerId(Long sellerId);
    List<Goods> findByCategoryId(Long categoryId);
    List<Goods> findByStatus(String status);
}