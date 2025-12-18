package com.titanbank.user.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileUpdateRequest {

    @Size(min = 1, max = 100, message = "First name must be between 1 and 100 characters")
    private String firstName;

    @Size(min = 1, max = 100, message = "Last name must be between 1 and 100 characters")
    private String lastName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Invalid phone number format")
    private String phoneNumber;

    @Size(max = 255, message = "Address line 1 is too long")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 is too long")
    private String addressLine2;

    @Size(max = 100, message = "City name is too long")
    private String city;

    @Size(max = 50, message = "State name is too long")
    private String state;

    @Size(max = 20, message = "Postal code is too long")
    private String postalCode;

    @Size(max = 50, message = "Country name is too long")
    private String country;

    @Size(max = 50, message = "Employment status is too long")
    private String employmentStatus;

    private BigDecimal annualIncome;
}