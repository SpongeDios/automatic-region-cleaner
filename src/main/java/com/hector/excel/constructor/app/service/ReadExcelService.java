package com.hector.excel.constructor.app.service;

import com.hector.excel.constructor.app.dto.Locations;

import java.io.IOException;

public interface ReadExcelService {
    String readExcel(Locations locations) throws IOException;
}
