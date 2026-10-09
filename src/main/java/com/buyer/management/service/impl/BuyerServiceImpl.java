package com.buyer.management.service.impl;

import com.buyer.management.dto.*;
import com.buyer.management.entity.Buyer;
import com.buyer.management.entity.User;
import com.buyer.management.repository.BuyerRepository;
import com.buyer.management.repository.UserRepository;
import com.buyer.management.service.impl.BuyerService;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BuyerServiceImpl implements BuyerService {

    private final BuyerRepository buyerRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public FileUploadResponse uploadBuyers(MultipartFile file, String userEmail) {
        List<UploadErrorDetail> errors = new ArrayList<>();
        int totalRecords = 0;
        int successfulRecords = 0;

        User user = userRepository.findByEmailOrMobile(userEmail, userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            List<String[]> rows = new ArrayList<>();
            String fileName = file.getOriginalFilename();
            
            if (fileName != null && fileName.toLowerCase().endsWith(".csv")) {
                // Process CSV
                try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                    String[] nextLine;
                    while ((nextLine = reader.readNext()) != null) {
                        rows.add(nextLine);
                    }
                }
            } else if (fileName != null && (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx"))) {
                // Process Excel
                try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
                    Sheet sheet = workbook.getSheetAt(0);
                    for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                        Row row = sheet.getRow(i);
                        if (row == null) continue;
                        String[] rowData = new String[7];
                        for (int j = 0; j < 7; j++) {
                            Cell cell = row.getCell(j, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
                            rowData[j] = getCellValueAsString(cell);
                        }
                        rows.add(rowData);
                    }
                }
            } else {
                throw new IllegalArgumentException("Invalid file type. Only CSV, XLS, and XLSX are allowed.");
            }

            // Process rows (skip index 0 which is the header)
            for (int i = 1; i < rows.size(); i++) {
                totalRecords++;
                String[] data = rows.get(i);
                
                if (data.length < 7) {
                    errors.add(new UploadErrorDetail(i + 1, "Missing columns"));
                    continue;
                }

                String name = data[0].trim();
                String email = data[1].trim();
                String mobile = data[2].trim();
                String address = data[3].trim();
                String invoiceStr = data[4].trim();
                String paidStr = data[5].trim();
                String dueStr = data[6].trim();

                // 1. Validation
                if (name.isEmpty()) { errors.add(new UploadErrorDetail(i + 1, "Name is required")); continue; }
                if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) { errors.add(new UploadErrorDetail(i + 1, "Invalid email format")); continue; }
                if (!mobile.matches("^[6-9]\\d{9}$")) { errors.add(new UploadErrorDetail(i + 1, "Invalid 10-digit Indian mobile number")); continue; }
                
                BigDecimal invoice, paid, due;
                try {
                    invoice = new BigDecimal(invoiceStr);
                    paid = new BigDecimal(paidStr);
                    due = new BigDecimal(dueStr);
                } catch (NumberFormatException e) {
                    errors.add(new UploadErrorDetail(i + 1, "Invalid numeric format for amounts"));
                    continue;
                }

                // 2. Duplicate Check (Email + Mobile for this specific user)
                if (buyerRepository.existsByEmailAndMobileAndUploadedByEmail(email, mobile, userEmail)) {
                    errors.add(new UploadErrorDetail(i + 1, "Duplicate record (Email + Mobile already exists)"));
                    continue;
                }

                // 3. Save to Database
                Buyer buyer = Buyer.builder()
                        .name(name).email(email).mobile(mobile).address(address)
                        .totalInvoiceAmount(invoice).totalAmountPaid(paid).totalAmountDue(due)
                        .uploadedBy(user).build();
                
                buyerRepository.save(buyer);
                successfulRecords++;
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to process file: " + e.getMessage());
        }

        return FileUploadResponse.builder()
                .success(true)
                .message("File processed successfully")
                .totalRecords(totalRecords)
                .successfulRecords(successfulRecords)
                .failedRecords(errors.size())
                .errors(errors)
                .build();
    }

    @Override
    public Page<BuyerResponse> getBuyers(String userEmail, String search, Pageable pageable) {
        String searchQuery = search == null ? "" : search.trim();
        Page<Buyer> buyers = buyerRepository.findByUploadedByEmailAndSearch(userEmail, searchQuery, pageable);
        
        return buyers.map(b -> BuyerResponse.builder()
                .id(b.getId())
                .name(b.getName())
                .email(b.getEmail())
                .mobile(b.getMobile())
                .address(b.getAddress())
                .totalInvoiceAmount(b.getTotalInvoiceAmount())
                .totalAmountPaid(b.getTotalAmountPaid())
                .totalAmountDue(b.getTotalAmountDue())
                .uploadedAt(b.getCreatedAt())
                .build());
    }

    @Override
    public BuyerStatisticsResponse getStatistics(String userEmail) {
        Map<String, Object> stats = buyerRepository.getStatisticsByEmail(userEmail);
        
        return BuyerStatisticsResponse.builder()
                .totalBuyers(((Number) stats.get("totalBuyers")).longValue())
                .totalInvoiceAmount((BigDecimal) stats.get("totalInvoice"))
                .totalAmountPaid((BigDecimal) stats.get("totalPaid"))
                .totalAmountDue((BigDecimal) stats.get("totalDue"))
                .build();
    }

    // Helper method to safely read Excel cell values
    private String getCellValueAsString(Cell cell) {
        if (cell.getCellType() == CellType.NUMERIC) {
            // Check if it's a whole number to avoid ".0" at the end
            double numericValue = cell.getNumericCellValue();
            if (numericValue == Math.floor(numericValue) && !Double.isInfinite(numericValue)) {
                return String.valueOf((long) numericValue);
            }
            return String.valueOf(numericValue);
        } else if (cell.getCellType() == CellType.STRING) {
            return cell.getStringCellValue();
        } else if (cell.getCellType() == CellType.BOOLEAN) {
            return String.valueOf(cell.getBooleanCellValue());
        }
        return "";
    }
}