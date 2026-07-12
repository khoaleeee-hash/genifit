package com.examp.genifit.dto.request;

import com.examp.genifit.entity.PaymentTransaction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "Request khởi tạo thanh toán cho 1 gói subscription")
public class PaymentRequestDto {

    @NotNull(message = "PlanId không được để trống")
    @Schema(description = "ID gói muốn thanh toán", example = "2", required = true)
    private Integer planId;

    @NotNull(message = "Phương thức thanh toán không được để trống")
    @Schema(
            description = "Phương thức thanh toán",
            example = "VNPAY",
            allowableValues = {"VNPAY", "MOMO"},
            required = true
    )
    private PaymentTransaction.PaymentMethod paymentMethod;
}