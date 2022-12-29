package com.hector.excel.constructor.app.service.impl;

import com.hector.excel.constructor.app.dto.DivDTO;
import com.hector.excel.constructor.app.dto.Locations;
import com.hector.excel.constructor.app.dto.VeterinariaWrapper;
import com.hector.excel.constructor.app.service.ReadExcelService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReadExcelServiceImpl implements ReadExcelService {
    private static final String FILE_LOCATION = "E:/Esponja/nichos 2.0/veterinarias/excels/%s/%s.xlsx";
    private static final List<VeterinariaWrapper> veterinarias = new ArrayList<>();
    private static final String COMUNA = "ANTOFAGASTA";

    private static final Integer NAME_COLUMN_NUMBER = 13;
    private static final Integer PHONE_COLUMN_NUMBER = 14;
    private static final Integer RATING_COLUMN_NUMBER = 16;
    private static final Integer REVIEW_COLUMN_NUMBER = 17;
    private static final Integer FULL_ADDRESS_COLUMN_NUMBER = 8;

    @Override
    public String readExcel(Locations locations) throws IOException {
        if (Objects.isNull(locations))
            return "bad request";

        if (Objects.isNull(locations.getComuna()) || Objects.isNull(locations.getRegion()))
            return "bad request";

        String overrideFileLocation = String.format(FILE_LOCATION, locations.getRegion(), locations.getComuna());
        FileInputStream file = new FileInputStream(overrideFileLocation);
        Workbook workbook = new XSSFWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        for (Row row : sheet) {
            if (row.getRowNum() == 0){
                continue;
            }
            Cell cellName = row.getCell(NAME_COLUMN_NUMBER);
            Cell cellPhone = row.getCell(PHONE_COLUMN_NUMBER);
            Cell cellRating = row.getCell(RATING_COLUMN_NUMBER);
            Cell cellReview = row.getCell(REVIEW_COLUMN_NUMBER);
            Cell cellFullAddress = row.getCell(FULL_ADDRESS_COLUMN_NUMBER);

            VeterinariaWrapper veterinariaWrapper = VeterinariaWrapper.builder()
                    .name(cellName.getStringCellValue())
                    .phone(cellPhone.getStringCellValue())
                    .rating(cellRating == null ? 0 : cellRating.getNumericCellValue())
                    .quantityReviews(cellReview == null ? 0: cellReview.getNumericCellValue())
                    .fullAddress(cellFullAddress.getStringCellValue())
                    .build();
            veterinarias.add(veterinariaWrapper);
        }
        List<VeterinariaWrapper> veterinariaWrappers = filterVeterinariasByComuna(locations.getComuna());
        List<DivDTO> divDTO = parseDataToHtml(veterinariaWrappers);
        StringBuilder stringBuilder = new StringBuilder();

        for (DivDTO dto : divDTO) {
            stringBuilder
                    .append("<div class=\"block-container\">")
                    .append(dto.getTitleAsH3())
                    .append("[su_row][su_column size=\"1/2\" center=\"no\" class=\"\"]\n")
                    .append(dto.getList())
                    .append("[/su_column] [su_column size=\"1/2\" center=\"no\" class=\"\"]\n")
                    .append(dto.getGoogleMaps())
                    .append("[/su_column][/su_row]\n")
                    .append(dto.getButtons())
                    .append("</div>");
        }

        return stringBuilder.toString();
    }

    private List<VeterinariaWrapper> filterVeterinariasByComuna(String comuna) {
        String comunaUpperCase = comuna.toUpperCase(Locale.ROOT);
        return veterinarias
                .stream()
                .filter(veterinaria -> deleteAcentos(veterinaria.getName()).toUpperCase(Locale.ROOT).contains(comunaUpperCase) ||
                        deleteAcentos(veterinaria.getFullAddress()).toUpperCase(Locale.ROOT).contains(comunaUpperCase))
                .collect(Collectors.toList());
    }

    private List<DivDTO> parseDataToHtml(List<VeterinariaWrapper> veterinarias){
        return veterinarias
                .stream()
                .map(this::createDivDTOFormatted)
                .collect(Collectors.toList());
    }

    private DivDTO createDivDTOFormatted(VeterinariaWrapper wrapper){
        return DivDTO.builder()
                .googleMaps(this.getEmbedGoogleMap(wrapper.getName()))
                .titleAsH3(this.titleAsH3(wrapper.getName()))
                .list(createHtmlList(wrapper))
                .buttons(this.createButtons(wrapper))
                .build();
    }

    private String getEmbedGoogleMap(String name){
        String overrideName = name.replace(" ", "%20");
        String embedHtmlGoogleMap ="<div class=\"mapouter\"><div class=\"gmap_canvas\"><iframe width=\"400\" height=\"300\" id=\"gmap_canvas\" src=\"https://maps.google.com/maps?q=%s&t=&z=13&ie=UTF8&iwloc=&output=embed\" frameborder=\"0\" scrolling=\"no\" marginheight=\"0\" marginwidth=\"0\"></iframe><a href=\"https://fmovies-online.net\">fmovies</a><br><style>.mapouter{position:relative;text-align:right;height:300px;width:400px;}</style><a href=\"https://www.embedgooglemap.net\">google map websites</a><style>.gmap_canvas {overflow:hidden;background:none!important;height:300px;width:400px;}</style></div></div>";
        return String.format(embedHtmlGoogleMap, overrideName);
    }

    private String titleAsH3(String name){
        String overrideName = cleanH3(name);
        return String.format("<center><h3>\uD83D\uDC36 %s \uD83D\uDC31</h3></center>\n", overrideName);
    }

    private String createHtmlList(VeterinariaWrapper wrapper){
        return String.format(
                "[su_list icon=\"icon: thumbs-o-up\" icon_color=\"#37ff6d\"]\n" +
                    "<ul>\n" +
                    " \t<li><strong>Numero de teléfono</strong>: %s</li>\n" +
                    " \t<li><strong>Dirección</strong>: %s</li>\n" +
                    " \t<li><strong>Nota media</strong>: %s / 5.0</li>\n" +
                    " \t<li><strong>Precios</strong>: Preguntar Antes</li>\n" +
                    " \t<li><strong>Horario</strong>: De lunes a domingo entre 00:00 y 24:00 horas. Abren todos los días</li>\n" +
                    "</ul>\n" +
                "[/su_list]\n", wrapper.getPhone(), wrapper.getFullAddress(), wrapper.getRating());
    }

    private String cleanH3(String name){
        return name
                .replace("Clínica ", "")
                .replace("Clinica ", "")
                .replace("CLINICA ", "")
                .replace("CLÍNICA ", "")
                .replace("Veterinaria ", "")
                .replace("VETERINARIA ", "");
    }

    private String createButtons(VeterinariaWrapper wrapper){
        String first = "[su_row][su_column size=\"1/2\" center=\"no\" class=\"\"]\n\n";
        String second = "[/su_column] [su_column size=\"1/2\" center=\"no\" class=\"\"]\n\n";
        String last = "[/su_column][/su_row]\n\n";

        String phoneFormatter = wrapper.getPhone().replace("+", "").replace(" ", "");
        String telButton = String.format("[su_button url=\"tel:%s\" target=\"blank\" style=\"flat\" background=\"#41056f\" size=\"10\" wide=\"yes\" center=\"yes\" radius=\"10\" icon=\"icon: phone\" rel=\"nofollow\"] Llamar [/su_button]"
        , phoneFormatter);
        String whatsAppButton = String.format("[su_button url=\"https://api.whatsapp.com/send/?phone=%s\" target=\"blank\" style=\"flat\" background=\"#41056f\" size=\"10\" wide=\"yes\" center=\"yes\" radius=\"10\" icon=\"icon: phone\" rel=\"nofollow\"] Enviar un WhatsApp[/su_button]"
        , phoneFormatter);

        return first + telButton + second + whatsAppButton + last;
    }

    private String deleteAcentos(String text){
        return text
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U");
    }
}
