package com.senior.candleShopProject.feature.order.generator.Excel;

import com.senior.candleShopProject.datasource.domain.orders.IOrdersReportDataResp;
import org.dhatim.fastexcel.BorderStyle;
import org.dhatim.fastexcel.Workbook;
import org.dhatim.fastexcel.Worksheet;
import org.springframework.stereotype.Component;

import java.io.OutputStream;
import java.util.List;

@Component
public class ExcelGenerators {
    public void generateSaleReport(OutputStream os, List<IOrdersReportDataResp> products, String monthName, String year) {

        String fontName = "TH SarabunPSK";
        try (Workbook wb = new Workbook(os, "Moji's Candle Shop", "1.0")) {
            Worksheet ws = wb.newWorksheet("Report_" + monthName + "_" + year);

            // Report Shop Name (Row 0)
            ws.range(0, 0, 0, 4).merge();
            ws.value(0, 0, "Moji's Candle Shop");
            ws.style(0, 0).fontName(fontName).bold().fontSize(16).horizontalAlignment("center").verticalAlignment("center").set();

            // Topic of Report
            ws.range(1, 0, 1, 4).merge();
            ws.value(1, 0, "รายงานยอดขายสินค้า ประจำเดือน " + monthName + " พ.ศ. " + year);
            ws.style(1, 0).fontName(fontName).horizontalAlignment("center").fontSize(16).bold().set();

            // Column Header
            String[] headers = {"ลำดับที่", "รายการสินค้า", "ราคา/หน่วย (บาท)", "ยอดขายทั้งหมด (หน่วย)", "มูลค่ารวม (บาท)"};
            for (int i = 0; i < headers.length; i++) {
                ws.value(2, i, headers[i]);
                ws.style(2, i)
                        .fontName(fontName)
                        .fontSize(14)
                        .bold()
                        .fillColor("D3D3D3")
                        .bold()
                        .horizontalAlignment("center")
                        .verticalAlignment("center")
                        .borderStyle(BorderStyle.THIN)
                        .set();
                if (i == 0) ws.width(i, 8);
                else ws.width(i, 20);
            }
            ws.freezePane(0, 3);

            // Data Section
            int startRow = 3;
            for (int i = 0; i < products.size(); i++) {
                int currentRow = startRow + i;
                IOrdersReportDataResp p = products.get(i);

                ws.value(currentRow, 0, i + 1);
                ws.value(currentRow, 1, p.getProductName());
                ws.value(currentRow, 2, p.getPricePerUnit() != null ? p.getPricePerUnit() : 0);
                ws.value(currentRow, 3, p.getTotalSales() != null ? p.getTotalSales() : 0);

                int excelRowNum = currentRow + 1;
                ws.formula(currentRow, 4, "C" + excelRowNum + "*D" + excelRowNum);
                ws.rowHeight(currentRow, 22);

                applyDataStyle(ws, currentRow, fontName);
            }

            // Total Amount Section
            int lastDataRow = startRow + products.size();
            int excelStartDataRow = startRow + 1;
            int excelEndDataRow = lastDataRow;

            ws.range(lastDataRow, 0, lastDataRow, 3).merge();
            ws.value(lastDataRow, 0, "รวมทั้งสิ้น");
            ws.range(lastDataRow, 0, lastDataRow, 3)
                    .style()
                    .fillColor("D3D3D3")
                    .borderStyle(BorderStyle.THIN)
                    .set();
            ws.style(lastDataRow, 0)
                    .horizontalAlignment("center")
                    .verticalAlignment("center")
                    .fontName(fontName)
                    .fontSize(14)
                    .bold()
                    .set();
            ws.rowHeight(lastDataRow, 25);

            ws.formula(lastDataRow, 4, "SUM(E" + excelStartDataRow + ":E" + excelEndDataRow + ")");
            ws.style(lastDataRow, 4)
                    .fontName(fontName).fontSize(14).fillColor("D3D3D3").bold().borderStyle(BorderStyle.THIN).horizontalAlignment("right").verticalAlignment("center")
                    .format("#,##0").set();

        } catch (Exception e) {
            System.err.println("Failed to generate excel report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void applyDataStyle(Worksheet ws, int row, String font) {
        for (int col = 0; col <= 4; col++) {
            var style = ws.style(row, col).fontName(font).fontSize(12).borderStyle(BorderStyle.THIN);
            if (col == 1) style.horizontalAlignment("left").verticalAlignment("center");
            else if (col == 2 || col == 4) style.horizontalAlignment("right").verticalAlignment("center").format("#,##0.00");
            else style.horizontalAlignment("center").verticalAlignment("center").format("#,##0");
            style.set();
        }
        ws.rowHeight(0, 25);
        ws.rowHeight(1, 30);
        ws.rowHeight(2, 25);
    }
}