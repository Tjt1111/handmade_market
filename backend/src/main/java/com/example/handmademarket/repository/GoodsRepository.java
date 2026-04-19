package com.example.handmademarket.repository;

import com.example.handmademarket.entity.Goods;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GoodsRepository extends JpaRepository<Goods, Long> {

    /** 悲观锁查询商品（SELECT ... FOR UPDATE），用于库存扣减/恢复 */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT g FROM Goods g WHERE g.id = :id")
    Optional<Goods> findByIdForUpdate(@Param("id") Long id);

    long countByCreatorId(Integer creatorId);
}

