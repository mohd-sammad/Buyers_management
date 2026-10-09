package com.buyer.management.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BuyerResponse {
    private Long id;
    private String name;
    private String email;
    private String mobile;
    private String address;
    private BigDecimal totalInvoiceAmount;
    private BigDecimal totalAmountPaid;
    private BigDecimal totalAmountDue;
    private LocalDateTime uploadedAt;
}