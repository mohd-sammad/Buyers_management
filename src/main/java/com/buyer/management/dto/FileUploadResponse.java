package com.buyer.management.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class FileUploadResponse {
	 private boolean success;
	    private String message;
	    private int totalRecords;
	    private int successfulRecords;
	    private int failedRecords;
	    private List<UploadErrorDetail> errors;
}
