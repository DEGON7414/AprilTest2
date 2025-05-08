package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * ClassName: ReceiptUtils
 * Description:
 * 頁面處理與添加圖片
 * @Author 許記源
 * @Create 2025/5/8 下午 03:59
 * @Version 1.0
 */
public class ReceiptUtils {
    public static void createReceipt(PDDocument document, int pageIndex,
                                     String tableText, String date,
                                     String itemNumber, String markNumber,
                                     PDType0Font font, File rotatedImage) throws IOException {
        //提取貨號
        List<String> itemNumbers = PDFReaderUtils.extractItemNumber(tableText);
        boolean isItemNumber = PDFReaderUtils.isItemNumberMatched(itemNumbers, itemNumber);
        //提取備註並判斷是否需要統編
        boolean isTaxId = PDFReaderUtils.buyerNote(tableText);
        boolean isSpecialNumber = PDFReaderUtils.isItemNumberMatched(itemNumbers, markNumber);
        String barcodeNumber = PDFReaderUtils.extractBarcodeNumber(tableText);
        String payment = PDFReaderUtils.paymentAmount(tableText);

        System.out.println("處理頁面 " + pageIndex + " - 條碼編號: " + barcodeNumber + ", 實付金額: " + payment);

        if (isItemNumber) {
            EasyTextAddUtils.addBill(document, pageIndex, true, font);
        }
        if (isSpecialNumber) {
            EasyTextAddUtils.addBoth(document, pageIndex, true, font);
        }
        if (isTaxId) {
            EasyTextAddUtils.addTaxId(document, pageIndex, true, font);
        }
        if (!isItemNumber && !isTaxId && !isSpecialNumber) {
            TextAppenderUtils.addText(document, pageIndex, date, barcodeNumber, payment, font);
            PDPage targetPage = document.getPage(pageIndex - 1); // 頁面索引從 0 開始
            PicInsert.insertPic(document, targetPage, rotatedImage);
        }
    }

}



