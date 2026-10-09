package com.buyer.management.dto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data @AllArgsConstructor
public class UploadErrorDetail {
    private int row;
    private String error;
}