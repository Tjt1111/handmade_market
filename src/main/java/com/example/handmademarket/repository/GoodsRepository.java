package com.example.handmademarket.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.handmademarket.entity.Goods;

import jakarta.persistence.LockModeType;

@Repository
public interface GoodsRepository extends JpaRepository<Goods, Long> {

    Page<Goods> findByCategory(String category, Pageable pageable);

    Page<Goods> findByStatus(Integer status, Pageable pageable);

    Page<Goods> findByCreatorIdAndStatus(Long creatorId, Integer status, Pageable pageable);

    @Query("SELECT g FROM Goods g WHERE g.status = 1 AND (g.goodsName LIKE %:keyword% OR g.details LIKE %:keyword%)")
    Page<Goods> searchGoods(@Param("keyword") String keyword, Pageable pageable);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Goods g WHERE g.id = :id")
    Optional<Goods> findByIdForUpdate(@Param("id") Long id);

    @Query("SELECT g FROM Goods g WHERE g.status = 1 AND (g.goodsName LIKE %:keyword% OR g.details LIKE %:keyword%)")
    java.util.List<Goods> searchByKeyword(@Param("keyword") String keyword);

    long countByCreatorId(Long creatorId);

}