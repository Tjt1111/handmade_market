package com.example.handmarket.service;

import com.example.handmarket.entity.Goods;
import com.example.handmarket.repository.GoodsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GoodsService {

    @Autowired
    private GoodsRepository goodsRepository;

    public Goods save(Goods goods) {
        return goodsRepository.save(goods);
    }

    public Optional<Goods> findById(Long id) {
        return goodsRepository.findById(id);
    }

    public List<Goods> findAll() {
        return goodsRepository.findAll();
    }

    public List<Goods> findBySellerId(Long sellerId) {
        return goodsRepository.findBySellerId(sellerId);
    }

    public List<Goods> findByCategoryId(Long categoryId) {
        return goodsRepository.findByCategoryId(categoryId);
    }

    public void deleteById(Long id) {
        goodsRepository.deleteById(id);
    }
}