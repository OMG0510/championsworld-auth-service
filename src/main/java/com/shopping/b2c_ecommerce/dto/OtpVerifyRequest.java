package com.shopping.b2c_ecommerce.dto;

import lombok.Data;

@Data
public class OtpVerifyRequest {
    private String email;
    private String mobile;
    private String otp;
}
