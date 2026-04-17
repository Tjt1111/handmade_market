package com.example.handmarket.controller;

import com.example.handmarket.entity.Goods;
import com.example.handmarket.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/goods")
@CrossOrigin(origins = "http://localhost:5173")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping
    public List<Goods> getAllGoods() {
        return goodsService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Goods> getGoodsById(@PathVariable Long id) {
        Optional<Goods> goods = goodsService.findById(id);
        return goods.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Goods createGoods(@RequestBody Goods goods) {
        return goodsService.save(goods);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Goods> updateGoods(@PathVariable Long id, @RequestBody Goods goodsDetails) {
        Optional<Goods> goods = goodsService.findById(id);
        if (goods.isPresent()) {
            goodsDetails.setId(id);
            return ResponseEntity.ok(goodsService.save(goodsDetails));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGoods(@PathVariable Long id) {
        goodsService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}