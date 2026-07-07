package com.examp.genifit.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment_transactions")
@Data
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer transactionId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;

    // Mã đơn hàng do backend tự sinh, gửi lên VNPay/MoMo
    // Phải unique, dùng để đối soát khi callback về
    @Column(unique = true, nullable = false)
    private String orderCode;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;  // VNPAY, MOMO

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;         // PENDING, SUCCESS, FAILED, CANCELLED

    private BigDecimal amount;                // số tiền thực tế thanh toán

    // Mã giao dịch do VNPay/MoMo trả về sau khi thanh toán xong
    // Null nếu chưa thanh toán
    private String gatewayTransactionId;

    // Raw response từ VNPay/MoMo lưu lại để debug hoặc đối soát
    @Column(columnDefinition = "TEXT")
    private String gatewayResponse;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public enum PaymentMethod { VNPAY, MOMO }

    public enum PaymentStatus { PENDING, SUCCESS, FAILED, CANCELLED }
}