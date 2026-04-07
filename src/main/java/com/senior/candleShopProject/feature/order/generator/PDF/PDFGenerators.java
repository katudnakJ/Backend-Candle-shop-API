package com.senior.candleShopProject.feature.order.generator.PDF;

import com.senior.candleShopProject.common.utils.Constants;
import com.senior.candleShopProject.datasource.domain.orders.IReceiptInformationResp;
import com.senior.candleShopProject.datasource.domain.orders.IReceiptOrderItemResp;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.text.Normalizer;

import java.io.*;
import java.security.KeyStore;
import java.util.List;

import static com.senior.candleShopProject.common.utils.DateTimeFormatter.formatDateTimeNowToString;

@Slf4j
@Component
public class PDFGenerators {

    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final int FIRST_PAGE_ITEMS = 15;
    private static final int SUBSEQUENT_PAGE_ITEMS = 20;

    private byte[] fontRegBytes;
    private byte[] fontBoldBytes;

    @PostConstruct
    public void init() throws Exception {

        try (InputStream regIs = getClass().getResourceAsStream("/fonts/Sarabun-Regular.ttf");
             InputStream boldIs = getClass().getResourceAsStream("/fonts/Sarabun-Bold.ttf")) {
            if (regIs != null) fontRegBytes = regIs.readAllBytes();
            if (boldIs != null) fontBoldBytes = boldIs.readAllBytes();
        }
    }

    private void drawGrayBackground(PDPageContentStream content, float x, float y, float width, float height) throws IOException {
        content.setNonStrokingColor(200, 200, 200);
        content.addRect(x, y, width, height);
        content.fill();
        content.setNonStrokingColor(0, 0, 0);
    }

    private void drawLine(PDPageContentStream content, float x, float y, float width) throws IOException {
        content.setStrokingColor(200, 200, 200);
        content.setLineWidth(1f);
        content.moveTo(x, y);
        content.lineTo(x + width, y);
        content.stroke();
        content.setStrokingColor(0, 0, 0);
    }

    private void text(PDPageContentStream content, PDType0Font font, String text, int fontSize, float x, float y) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.newLineAtOffset(x, y);

        String normalizedText = Normalizer.normalize(text != null ? text : "", Normalizer.Form.NFC);

        content.showText(text != null ? normalizedText : "");
        content.endText();
    }

    private void textRightAligned(PDPageContentStream content, PDType0Font font, String text, int fontSize, float xRight, float y) throws IOException {
        float stringWidth = font.getStringWidth(text != null ? text : "") / 1000 * fontSize;
        text(content, font, text, fontSize, xRight - stringWidth, y);
    }

    private void drawHeader(PDPageContentStream content, PDType0Font fontBold, boolean isFirstPage) throws IOException {
        String title = "ใบเสร็จรับเงิน / ใบแจ้งหนี้";
        float titleWidth = fontBold.getStringWidth(title) / 1000 * 20;
        text(content, fontBold, title, 20, (PAGE_WIDTH - titleWidth) / 2, 780);
        text(content, fontBold, "ร้าน Moji's Candle Shop", 12, MARGIN, 745);

        if (isFirstPage) {
            textRightAligned(content, fontBold, "ข้อมูลใบเสร็จ", 12, PAGE_WIDTH - MARGIN, 745);
            drawLine(content, MARGIN, 735, PAGE_WIDTH - (MARGIN * 2));
        }
    }

    private void drawInfoSection(PDPageContentStream content, PDType0Font fontBold, PDType0Font fontReg, IReceiptInformationResp info) throws IOException {
        float leftX = MARGIN;
        float rightLabelX = PAGE_WIDTH / 2 + 40;
        float rightValueX = PAGE_WIDTH - MARGIN;
        float y = 700;
        float gap = 18;

        text(content, fontBold, "ข้อมูลผู้ขาย", 11, leftX, y);
        text(content, fontReg, "ชื่อ: " + info.getSellerFirstName() + " " + info.getSellerLastName(), 11, leftX, y - gap);
        text(content, fontReg, "เบอร์โทรศัพท์: " + info.getSellerPhone(), 11, leftX, y - gap * 2);
        String sellerAddr = info.getSellerDeliveryAddress() + " ต." + info.getSellerSubDistrict() + " อ." + info.getSellerDistrict() + " จ." + info.getSellerProvince() + " " + info.getSellerPostcode();
        text(content, fontReg, "ที่อยู่: " + sellerAddr, 11, leftX, y - gap * 3);

        text(content, fontReg, "เลขที่", 11, rightLabelX, y);
        textRightAligned(content, fontReg, info.getPaymentReceiptNumber(), 11, rightValueX, y);
        text(content, fontReg, "วันที่ออกใบเสร็จ", 11, rightLabelX, y - gap);
        textRightAligned(content, fontReg, formatDateTimeNowToString("dd/MM/yyyy", Constants.TIME_ZONE_BANGKOK), 11, rightValueX, y - gap);
        text(content, fontReg, "เลขอ้างอิง", 11, rightLabelX, y - gap * 2);
        textRightAligned(content, fontReg, info.getOrderNumber(), 11, rightValueX, y - gap * 2);

        float customerY = y - 90;
        text(content, fontBold, "ข้อมูลลูกค้า", 11, leftX, customerY);
        text(content, fontReg, "ชื่อ: " + info.getCustomerFirstName() + " " + info.getCustomerLastName(), 11, leftX, customerY - gap);
        text(content, fontReg, "เบอร์โทรศัพท์: " + info.getCustomerPhone(), 11, leftX, customerY - gap * 2);
        String custAddr = info.getCustomerDeliveryAddress() + " ต." + info.getCustomerSubDistrict() + " อ." + info.getCustomerDistrict() + " จ." + info.getCustomerProvince() + " " + info.getCustomerPostcode();
        text(content, fontReg, "ที่อยู่: " + custAddr, 11, leftX, customerY - gap * 3);
    }

    private float drawOrderItemsList(PDPageContentStream content, PDType0Font bold, PDType0Font reg, List<IReceiptOrderItemResp> items, int startIndex, float startY) throws IOException {
        float y = startY;
        drawLine(content, MARGIN, y, PAGE_WIDTH - (MARGIN * 2));
        y -= 15;

        text(content, bold, "#", 11, MARGIN, y);
        text(content, bold, "รายการสินค้า", 11, MARGIN + 30, y);
        textRightAligned(content, bold, "จำนวน", 11, 380, y);
        textRightAligned(content, bold, "ราคาต่อหน่วย", 11, 470, y);
        textRightAligned(content, bold, "ยอดรวม(บาท)", 11, 550, y);

        y -= 10;
        drawLine(content, MARGIN, y, PAGE_WIDTH - (MARGIN * 2));
        y -= 20;

        int index = startIndex;
        for (IReceiptOrderItemResp item : items) {
            text(content, reg, String.valueOf(index++), 11, MARGIN, y);
            text(content, reg, item.getProductName(), 11, MARGIN + 30, y);
            textRightAligned(content, reg, String.format("%,d", item.getQuantity()), 11, 380, y);
            textRightAligned(content, reg, item.getPricePerUnit(), 11, 470, y);
            textRightAligned(content, reg, item.getSubTotalPrice(), 11, 550, y);
            y -= 20;
        }

        drawLine(content, MARGIN, y + 10, PAGE_WIDTH - (MARGIN * 2));
        return y + 10;
    }

    private void drawSummary(PDPageContentStream content, PDType0Font bold, PDType0Font reg, IReceiptInformationResp info, float tableBottomY) throws IOException {
        float summaryHeight = 90;
        float footerHeight = 40;
        float y = tableBottomY - 20 - summaryHeight;
        if (y < footerHeight + 30) {
            y = footerHeight + 20;
        }

        text(content, bold, "หมายเหตุ", 11, MARGIN, y + 50);
        text(content, reg, "ราคาดังกล่าวยังไม่รวมภาษีมูลค่าเพิ่ม (VAT) และ", 10, MARGIN, y + 32);
        text(content, reg, "ภาษีหัก ณ ที่จ่าย", 10, MARGIN, y + 20);

        text(content, bold, "รวมเป็นเงิน", 12, 380, y + 50);
        textRightAligned(content, reg, String.format("%,.0f", info.getOrderTotalAmount()), 12, 550, y + 50);

        text(content, bold, "ค่าจัดส่ง", 12, 380, y + 30);
        float shipping = info.getOrderNetAmount().subtract(info.getOrderTotalAmount()).floatValue();
        textRightAligned(content, reg, String.format("%,.0f", shipping), 12, 550, y + 30);

        drawGrayBackground(content, 370, y - 5, 190, 25);
        text(content, bold, "รวมราคาสุทธิ(บาท)", 13, 380, y);
        textRightAligned(content, bold, String.format("%,.0f", info.getOrderNetAmount()), 13, 550, y);
    }

    private void drawFooter(PDPageContentStream content, PDType0Font reg) throws IOException {
        String footerText = "ขอขอบคุณที่อุดหนุนค่ะ ( Thank you )";
        float width = reg.getStringWidth(footerText) / 1000 * 12;
        text(content, reg, footerText, 12, (PAGE_WIDTH - width) / 2, 50);
    }

    public byte[] generateReceiptPDFWithSignature(IReceiptInformationResp info, List<IReceiptOrderItemResp> items) throws Exception {

        byte[] pdfBytes;

        try (PDDocument document = new PDDocument()) {
            PDType0Font fontReg = PDType0Font.load(document, new ByteArrayInputStream(fontRegBytes), true);
            PDType0Font fontBold = PDType0Font.load(document, new ByteArrayInputStream(fontBoldBytes), true);

            int totalItems = items.size();
            int currentIndex = 0;
            int pageNumber = 1;

            while (currentIndex < totalItems) {
                PDPage page = new PDPage(PDRectangle.A4);
                document.addPage(page);

                try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                    boolean isFirstPage = (pageNumber == 1);
                    drawHeader(content, fontBold, isFirstPage);

                    float currentY;
                    if (isFirstPage) {
                        drawInfoSection(content, fontBold, fontReg, info);
                        int limit = Math.min(currentIndex + FIRST_PAGE_ITEMS, totalItems);
                        List<IReceiptOrderItemResp> pageItems = items.subList(currentIndex, limit);
                        currentY = drawOrderItemsList(content, fontBold, fontReg, pageItems, currentIndex + 1, 520);
                        currentIndex = limit;
                    } else {
                        int limit = Math.min(currentIndex + SUBSEQUENT_PAGE_ITEMS, totalItems);
                        List<IReceiptOrderItemResp> pageItems = items.subList(currentIndex, limit);
                        currentY = drawOrderItemsList(content, fontBold, fontReg, pageItems, currentIndex + 1, 730);
                        currentIndex = limit;
                    }

                    if (currentIndex >= totalItems) {
                        drawSummary(content, fontBold, fontReg, info, currentY);
                        drawFooter(content, fontReg);
                    }
                }
                pageNumber++;
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            document.save(out);
            pdfBytes = out.toByteArray();

        } catch (Exception e) {
            log.error("PDF Generation failed: ", e);
            throw new RuntimeException(e);
        }

        return pdfBytes;
    }
}