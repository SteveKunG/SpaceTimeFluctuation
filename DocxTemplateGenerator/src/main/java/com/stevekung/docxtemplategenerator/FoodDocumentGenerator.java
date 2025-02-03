package com.stevekung.docxtemplategenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;

import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STTblWidth;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STVerticalJc;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FoodDocumentGenerator
{
    public static void main(String[] args) throws IOException
    {
        var data = new ObjectMapper().enable(JsonParser.Feature.ALLOW_COMMENTS).readTree(new File("src/main/resources/food.json"));
        var templateFile = new FileInputStream("src/main/resources/templates/food_template.docx");
        var document = new XWPFDocument(templateFile);

        // Set A4 Paper Size
        var sectPr = document.getDocument().getBody().addNewSectPr();
        var pageSize = sectPr.addNewPgSz();
        pageSize.setW(BigInteger.valueOf(11907)); // A4 width in twips (210mm)
        pageSize.setH(BigInteger.valueOf(16839)); // A4 height in twips (297mm)

        processDocument(document, data);

        var file = new File("output/food_output.docx");
        file.getParentFile().mkdirs();
        var out = new FileOutputStream(file);
        document.write(out);
    }

    private static void processDocument(XWPFDocument document, JsonNode data)
    {
        for (var element : document.getBodyElements())
        {
            if (element instanceof XWPFParagraph paragraph)
            {
                processParagraph(paragraph, data);
            }
            else if (element instanceof XWPFTable table)
            {
                processExpenseTable(table, data);
            }
        }
    }

    private static void processParagraph(XWPFParagraph paragraph, JsonNode data)
    {
        var runFirst = paragraph.getRuns().stream().findFirst();
        var fontFamily = "";
        var fontSize = 11.0;
        var text = paragraph.getText();

        if (runFirst.isPresent())
        {
            fontFamily = runFirst.get().getFontFamily();
            fontSize = runFirst.get().getFontSizeAsDouble();
        }

        text = text.replace("${title}", data.get("title").asText());
        text = text.replace("${food_name}", data.get("food_name").asText());

        updateParagraphText(paragraph, text, fontFamily, fontSize);
    }

    private static void processExpenseTable(XWPFTable table, JsonNode data)
    {
        var originalCell = table.getRow(0).getCell(0);
        var originalColor = originalCell.getColor();
        var fontFamily = originalCell.getParagraphs().getFirst().getRuns().getFirst().getFontFamily();
        var fontSize = originalCell.getParagraphs().getFirst().getRuns().getFirst().getFontSizeAsDouble();
        table.removeRow(0);

        for (var ingredient : data.get("ingredients"))
        {
            var row = table.createRow();
            createCell(row, 0, ingredient.get("number").asText(), originalColor, fontFamily, fontSize, 800, false);
            createCell(row, 1, ingredient.get("name").asText(), originalColor, fontFamily, fontSize, 4000, true);
            createCell(row, 2, ingredient.get("type").asText(), originalColor, fontFamily, fontSize, 2000, true);
        }
    }

    private static void updateParagraphText(XWPFParagraph paragraph, String text, String fontFamily, double fontSize)
    {
        while (!paragraph.getRuns().isEmpty())
        {
            paragraph.removeRun(0);
        }

        var run = paragraph.createRun();
        run.setFontFamily(fontFamily);
        run.setFontSize(fontSize);

        if (text.startsWith("**"))
        {
            run.setBold(true);
            run.setText(text.replace("**", ""));
        }
        else
        {
            run.setText(text);
        }
    }

    private static void createCell(XWPFTableRow row, int pos, String text, String color, String fontFamily, double fontSize, int width, boolean wordWrap)
    {
        var cell = pos < row.getTableCells().size() ? row.getCell(pos) : row.createCell();
        cell.setColor(color);

        var tcPr = cell.getCTTc().addNewTcPr();
        var cellWidth = tcPr.addNewTcW();
        cellWidth.setType(STTblWidth.DXA);
        cellWidth.setW(BigInteger.valueOf(width));

        tcPr.addNewVAlign().setVal(STVerticalJc.TOP);

        // Add borders
        var borders = tcPr.addNewTcBorders();
        addBorder(borders.addNewBottom());

        // เพิ่มการตัดคำด้วยการสร้าง paragraph ใหม่
        var para = cell.addParagraph();
        para.setAlignment(ParagraphAlignment.LEFT);
        para.setWordWrapped(wordWrap);  // เปิดใช้งานการตัดคำ

        updateParagraphText(para, text, fontFamily, fontSize);
        cell.removeParagraph(0);
    }

    private static void addBorder(CTBorder border)
    {
        border.setVal(STBorder.SINGLE);
        border.setSz(BigInteger.valueOf(4));
        border.setColor("000000");
    }
}