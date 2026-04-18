package com.example.handmademarket.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_evaluation")
public class Evaluation {

    @Id
    @Column(name = "eval_id", length = 30)
    private String evalId;

    @Column(name = "order_id", length = 50)
    private String orderId;

    @Column(name = "evaluator_id")
    private Integer evaluatorId;

    @Column(name = "evaluated_id")
    private Integer evaluatedId;

    @Column(name = "score")
    private Integer score;

    @Column(name = "content", length = 200)
    private String content;

    @Column(name = "images", length = 500)
    private String images;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "status")
    private Integer status;

    // Getters and Setters

    public String getEvalId() { return evalId; }
    public void setEvalId(String evalId) { this.evalId = evalId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public Integer getEvaluatorId() { return evaluatorId; }
    public void setEvaluatorId(Integer evaluatorId) { this.evaluatorId = evaluatorId; }

    public Integer getEvaluatedId() { return evaluatedId; }
    public void setEvaluatedId(Integer evaluatedId) { this.evaluatedId = evaluatedId; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getImages() { return images; }
    public void setImages(String images) { this.images = images; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
