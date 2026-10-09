package com.buyer.management.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.buyer.management.dto.BuyerResponse;
import com.buyer.management.dto.BuyerStatisticsResponse;
import com.buyer.management.dto.FileUploadResponse;
import com.buyer.management.service.impl.BuyerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;

    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadBuyers(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {
        
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 5MB");
        }

        String userEmail = authentication.getName();
        return ResponseEntity.ok(buyerService.uploadBuyers(file, userEmail));
    }

    @GetMapping
    public ResponseEntity<Page<BuyerResponse>> getBuyers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            Authentication authentication) {
        
        String userEmail = authentication.getName();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(buyerService.getBuyers(userEmail, search, pageable));
    }

    @GetMapping("/statistics")
    public ResponseEntity<BuyerStatisticsResponse> getStatistics(Authentication authentication) {
        String userEmail = authentication.getName();
        return ResponseEntity.ok(buyerService.getStatistics(userEmail));
    }
}