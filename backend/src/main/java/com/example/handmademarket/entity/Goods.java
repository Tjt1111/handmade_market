package com.example.handmademarket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_goods")
public class Goods {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goods_id")
    private Long id;

    @Column(name = "creator_id")
    private Integer creatorId;

    @Column(name = "goods_name", length = 50)
    private String title;

    @Column(name = "price")
    private Double price;

    @Column(name = "reserve_price")
    private Double reservePrice;

    @Column(name = "material", length = 50)
    private String material;

    @Column(name = "size", length = 50)
    private String size;

    @Column(name = "style", length = 50)
    private String style;

    @Column(name = "delivery_cycle")
    private Integer deliveryCycle;

    @Column(name = "details", length = 500)
    private String description;
    private String category;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "audit_time")
    private LocalDateTime auditTime;

    @Column(name = "auditor_id")
    private Integer auditorId;

    @Column(name = "status")
    private Integer status;

    @Column(name = "audit_remark", length = 200)
    private String auditRemark;

    @Column(name = "images")
    private String imageUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCreatorId() { return creatorId; }
    public void setCreatorId(Integer creatorId) { this.creatorId = creatorId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Double getReservePrice() { return reservePrice; }
    public void setReservePrice(Double reservePrice) { this.reservePrice = reservePrice; }

    public String getMaterial() { return material; }
    public void setMaterial(String material) { this.material = material; }

    public String getSize() { return size; }
    public void setSize(String size) { this.size = size; }

    public String getStyle() { return style; }
    public void setStyle(String style) { this.style = style; }

    public Integer getDeliveryCycle() { return deliveryCycle; }
    public void setDeliveryCycle(Integer deliveryCycle) { this.deliveryCycle = deliveryCycle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }

    public LocalDateTime getAuditTime() { return auditTime; }
    public void setAuditTime(LocalDateTime auditTime) { this.auditTime = auditTime; }

    public Integer getAuditorId() { return auditorId; }
    public void setAuditorId(Integer auditorId) { this.auditorId = auditorId; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getAuditRemark() { return auditRemark; }
    public void setAuditRemark(String auditRemark) { this.auditRemark = auditRemark; }
}
