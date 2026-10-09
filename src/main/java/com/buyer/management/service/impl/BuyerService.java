package com.buyer.management.service.impl;
import com.buyer.management.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
public interface BuyerService {
    FileUploadResponse uploadBuyers(MultipartFile file, String userEmail);
    Page<BuyerResponse> getBuyers(String userEmail, String search, Pageable pageable);
    BuyerStatisticsResponse getStatistics(String userEmail);
}
