package com.shopping.b2c_ecommerce.exception;

public class EmailSendException extends RuntimeException {
    public EmailSendException(String email) {
        super("Failed to send email to: " + email);
    }
}
