package com.hector.excel.constructor.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VeterinariaWrapper {
    private String name;
    private String phone;
    private Double rating;
    private Double quantityReviews;
    private String fullAddress;
}
