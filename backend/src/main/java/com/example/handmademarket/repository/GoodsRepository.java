package com.example.handmademarket.repository;

import com.example.handmademarket.entity.Goods;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsRepository extends JpaRepository<Goods, Long> {

    long countByCreatorId(Integer creatorId);
}
