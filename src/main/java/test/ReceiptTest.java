package test;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import org.apache.pdfbox.text.PDFTextStripper;
import utils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ClassName: ReceiptTest
 * Description:
 *
 * @Author 許記源
 * @Create 2025/5/2 上午 09:22
 * @Version 1.0
 */
public class ReceiptTest {
    public static void main(String[] args) {
        //來源檔案
//        String filePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\考題.pdf";
        String filePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\test_商城_郵局_0531_0810.pdf";

        //輸出檔案
        String outputFilePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\export_combined.pdf";
        //來源圖片
        String imagePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\receipt_signature.bmp";
        //外部參數 date
        String date = "2025年5月2日";
        //外部參數 貨號
        String itemNumber = "Mobifone07DU";
        //外部參數 指定貨號
        String markNumber = "BUJ10G09D";

        //指定字形
        File font = new File("C:\\Users\\cxhil\\Downloads\\Fonts_Kai\\TW-Kai-98_1.ttf");

        // 1. 提前旋轉圖片，僅執行一次
        File picFile = new File(imagePath);
        double degree = 270;
        File rotatedImage = null;
        try {
            rotatedImage = PicInsert.rotateImage(picFile, degree);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // 初始化計數器
        int processedCount = 0;
        try (PDDocument sourceDocument = PDDocument.load(new File(filePath))) {
            PDType0Font pdffont = PDType0Font.load(sourceDocument, font);

            //提取文本
            int totalPages = sourceDocument.getNumberOfPages();
            if (totalPages % 2 != 0) {
                System.out.println("警告：PDF頁數不是偶數，最後一頁可能無法正確處理");
            }

            // 2. 逐頁處理資料(只處理奇數索引，對應偶數頁)
            for (int i = 0; i < totalPages; i += 2) {
                //提取文本
                String tableText = PDFReaderUtils.extractTextFromPdf(sourceDocument, i+1, i+2);
                int pageNum = i + 1; // 實際頁碼
                // 檢查頁碼是否有效
                if (pageNum > totalPages) {
                    break;
                }
                if (tableText == null) {
                    System.out.println("警告：找不到頁面 " + pageNum + " 的文本內容");
                    continue;
                }
                // 處理文本
                String barcodeNumber = PDFReaderUtils.extractBarcodeNumber(tableText);
                String payment = PDFReaderUtils.paymentAmount(tableText);
                System.out.println("處理頁面 " + pageNum + " - 條碼編號: " + barcodeNumber + ", 實付金額: " + payment);

                List<String> itemNumbers = PDFReaderUtils.extractItemNumber(tableText);
                boolean isItemNumber = PDFReaderUtils.isItemNumberMatched(itemNumbers, itemNumber);
                boolean isTaxId = PDFReaderUtils.buyerNote(tableText);
                boolean isSpecialNumber = PDFReaderUtils.isItemNumberMatched(itemNumbers, markNumber);

                // 根據條件處理頁面
                if (isItemNumber) {
                    EasyTextAddUtils.addBill(sourceDocument, i + 1, true,pdffont);
                }
                if (isSpecialNumber) {
                    EasyTextAddUtils.addBoth(sourceDocument, i + 1, true,pdffont);
                }
                if (isTaxId) {
                    EasyTextAddUtils.addTaxId(sourceDocument, i + 1, true,pdffont);
                }
                if (!isItemNumber && !isTaxId && !isSpecialNumber) {
                    TextAppenderUtils.addText(sourceDocument, i + 1, date, barcodeNumber, payment, pdffont);
                    PDPage targetPage = sourceDocument.getPage(i+1);
                    PicInsert.insertPic(sourceDocument, targetPage, rotatedImage);
                }

                processedCount++;
                System.out.println("已處理 " + processedCount + " 組");
            }
            // 儲存處理後的文件
            sourceDocument.save(outputFilePath);
            System.out.println("處理完成，共處理 " + processedCount + " 筆資料");

        } catch (IOException e) {
            System.err.println("處理PDF時發生錯誤: " + e.getMessage());
            e.printStackTrace();
        }

        // 刪除旋轉圖片
        rotatedImage.delete();

    }
}