package com.hector.excel.constructor.app.controller;

import com.hector.excel.constructor.app.dto.Locations;
import com.hector.excel.constructor.app.service.ReadExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ExcelController {

    private final ReadExcelService readExcelService;

    @GetMapping("/")
    public String test(@RequestBody Locations locations) throws IOException {
        return readExcelService.readExcel(locations);
    }

    @GetMapping("/test")
    public String test2(){
        File folder = new File("E:/Esponja/nichos 2.0/veterinarias/excels/arica");
        File[] files = folder.listFiles();
        List<File> collect = Arrays.stream(files)
                .filter(File::isFile)
                .filter(file -> !file.getName().contains("$"))
                .peek(file -> System.out.println(file.getName()))
                .collect(Collectors.toList());
        return "";
    }
}
