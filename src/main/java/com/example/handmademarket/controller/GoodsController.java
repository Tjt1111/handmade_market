package com.example.handmademarket.controller;

import com.example.handmademarket.dto.CreateGoodsRequest;
import com.example.handmademarket.util.ResponseResult;
import com.example.handmademarket.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @GetMapping("/list")
    public ResponseEntity<ResponseResult> listGoods() {
        return ResponseEntity.ok(goodsService.listGoods());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ResponseResult> getGoodsByCategory(@PathVariable String category) {
        return ResponseEntity.ok(goodsService.getGoodsByCategory(category));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ResponseResult> getGoodsByStatus(@PathVariable Integer status) {
        return ResponseEntity.ok(goodsService.getGoodsByStatus(status));
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseResult> searchGoods(@RequestParam String keyword) {
        return ResponseEntity.ok(goodsService.searchGoods(keyword));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseResult> getGoods(@PathVariable Long id) {
        return ResponseEntity.ok(goodsService.getGoods(id));
    }

    @PostMapping
    public ResponseEntity<ResponseResult> createGoods(
            @RequestBody CreateGoodsRequest request,
            @RequestHeader("X-User-Id") Long creatorId) {
        return ResponseEntity.ok(goodsService.createGoods(request, creatorId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseResult> updateGoods(
            @PathVariable Long id,
            @RequestBody CreateGoodsRequest request) {
        return ResponseEntity.ok(goodsService.updateGoods(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseResult> offlineGoods(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long creatorId) {
        return ResponseEntity.ok(goodsService.offlineGoods(id, creatorId));
    }

    @PutMapping("/{id}/audit")
    public ResponseEntity<ResponseResult> auditGoods(
            @PathVariable Long id,
            @RequestParam Integer status,
            @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(goodsService.auditGoods(id, status, reason));
    }
}

