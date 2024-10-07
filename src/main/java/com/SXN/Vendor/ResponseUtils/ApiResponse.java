package com.SXN.Vendor.ResponseUtils;

import com.SXN.Vendor.Entity.VendorIdDetails;
import lombok.*;

@Getter
@Setter
@ToString
//@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private T data;
    private String message;
    private String error;
    private String status;
    private int statusCode;

    public ApiResponse(T data, String message, String error, String status, int statusCode) {
        this.data = data;
        this.message = message;
        this.error = error;
        this.status = status;
        this.statusCode = statusCode;
    }

    // Getters and Setters
}
