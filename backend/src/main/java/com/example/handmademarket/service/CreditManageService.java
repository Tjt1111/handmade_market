package com.example.handmademarket.service;

import com.example.handmademarket.entity.CreditRecord;
import com.example.handmademarket.entity.UserCredit;
import com.example.handmademarket.repository.CreditRecordRepository;
import com.example.handmademarket.repository.UserCreditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CreditManageService {

    @Autowired
    private UserCreditRepository userCreditRepository;

    @Autowired
    private CreditRecordRepository creditRecordRepository;

    // 查询全部用户信用列表（后台管理员查看）
    public List<UserCredit> getAllUserCredit() {
        return userCreditRepository.findAll();
    }

    // 管理员信用加减分+自动日志记录
    @Transactional
    public void adjustUserCredit(Long userId, Long adminId, Integer scoreChange, String reason) {
        // 1. 获取用户现有信用数据
        UserCredit credit = userCreditRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("该用户信用数据不存在"));

        // 2. 计算并更新最新分数
        int finalScore = credit.getCreditScore() + scoreChange;
        credit.setCreditScore(finalScore);

        // 3. 自动判定更新信用等级与状态
        if (finalScore >= 120) {
            credit.setCreditLevel("优秀");
        } else if (finalScore >= 80) {
            credit.setCreditLevel("良好");
        } else if (finalScore >= 60) {
            credit.setCreditLevel("一般");
        } else {
            credit.setCreditLevel("黑名单");
            credit.setStatus(0); // 拉黑封禁
        }

        credit.setUpdateTime(new Date());
        userCreditRepository.save(credit);

        // 4. 永久留存管理员操作记录，可追溯
        CreditRecord record = new CreditRecord();
        record.setUserId(userId);
        record.setAdminId(adminId);
        record.setScoreChange(scoreChange);
        record.setReason(reason);
        record.setCreateTime(new Date());
        creditRecordRepository.save(record);
    }
}
