package com.examp.genifit.entity;

public enum RefundStatus {
    NOT_ELIGIBLE,   // Không đủ điều kiện hoàn tiền
    ELIGIBLE,       // Đủ điều kiện hoàn tiền
    PENDING,        // Đang chờ xử lý hoàn tiền
    COMPLETED,      // Đã hoàn tiền
    REJECTED        // Từ chối hoàn tiền
}