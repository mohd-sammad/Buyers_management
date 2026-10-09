package com.buyer.management.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
	 @NotBlank(message = "Name is required")
	    @Size(min = 2, message = "Name must be at least 2 characters")
	    private String name;

	    @NotBlank(message = "Email is required")
	    @Email(message = "Invalid email format")
	    private String email;

	    @NotBlank(message = "Mobile is required")
	    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid 10-digit Indian mobile number")
	    private String mobile;

	    @NotBlank(message = "Password is required")
	    @Size(min = 8, message = "Password must be at least 8 characters")
	    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]+$", 
	             message = "Password must contain uppercase, lowercase, number, and special character")
	    private String password;

	    @NotBlank(message = "Confirm password is required")
	    private String confirmPassword;
}
