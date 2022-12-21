package com.hector.excel.constructor.app.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DivDTO {
    private String titleAsH3;
    private String googleMaps;
    private String list;
    private String buttons;
}
