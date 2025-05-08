package test;

import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFCloneUtility;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import utils.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
        //外部參數 特別貨號
        String specialNumber = "BUJ10G09D";

        // 1. 提前旋轉圖片，僅執行一次
        File picFile = new File(imagePath);
        double degree = 270;
        File rotatedImage = null;
        try {
            rotatedImage = PicInsert.rotateImage(picFile, degree);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 2. 初始化最終輸出檔案
        try (PDDocument finalDocument = new PDDocument()) {
            // 分批處理，每批處理20頁
            int batchSize = 20;
            int processedCount = 0;
            List<File> tempFiles = new ArrayList<>();

            // 3. 逐頁處理
            try (PDDocument sourceDocument = PDDocument.load(new File(filePath))) {
                int totalPages = sourceDocument.getNumberOfPages();
                // 確保頁數為偶數
                if (totalPages % 2 != 0) {
                    System.out.println("警告：PDF頁數不是偶數，最後一頁可能無法正確處理");
                }
                //FOR迴圈內不要new
                for (int i = 0; i < totalPages - 1; i += 2) {
                    //建立一個複製頁面 在此作操作 避免本來的檔案關閉
                    try (PDDocument batchDocument = new PDDocument()) {

                        PDPage page1 = sourceDocument.getPage(i);
                        PDPage page2 = sourceDocument.getPage(i + 1);
                        batchDocument.importPage(page1);
                        batchDocument.importPage(page2);

                        String tableText = PDFReaderUtils.extractTextFromPdf(batchDocument, 2, 2);
                        String barcodeNumber = PDFReaderUtils.extractBarcodeNumber(tableText);
                        String payment = PDFReaderUtils.paymentAmount(tableText);
                        System.out.println("條碼編號是: " + barcodeNumber);
                        System.out.println("實付金額是: " + payment);
                        List<String> itemNumbers = PDFReaderUtils.extractItemNumber(tableText);
                        boolean isItemNumber = PDFReaderUtils.isItemNumberMatched(itemNumbers, itemNumber);
                        boolean isTaxId = PDFReaderUtils.buyerNote(tableText);
                        boolean isSpecialNumber = PDFReaderUtils.isItemNumberMatched(itemNumbers, specialNumber);

                        if (isItemNumber) {
                            EasyTextAddUtils.addBill(batchDocument, 1, true);
                        }
                        if (isSpecialNumber) {
                            EasyTextAddUtils.addBoth(batchDocument, 1, true);
                        }

                        if (isTaxId) {
                            EasyTextAddUtils.addTaxId(batchDocument, 1, true);
                        }

                        if (!isItemNumber && !isTaxId && !isSpecialNumber) {
                            TextAppenderUtils.addText(batchDocument, 1, date, barcodeNumber, payment);
                            PDPage targetPage = batchDocument.getPage(1);
                            PicInsert.insertPic(batchDocument, targetPage, rotatedImage);
                        }

                        // 儲存 temp PDF
                        File tempFile = new File("temp_" + i + ".pdf");
                        batchDocument.save(tempFile);
                        tempFiles.add(tempFile);

                        processedCount++;
                        System.out.println("已處理 " + processedCount + " 筆資料");
                    }

                }
                // 合併所有 temp PDF
                PDFMergerUtility merger = new PDFMergerUtility();
                merger.setDestinationFileName(outputFilePath);
                for (File tempFile : tempFiles) {
                    merger.addSource(tempFile);
                }
                merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
                System.out.println("已成功建立最終PDF，處理筆數：" + processedCount);
            } catch (IOException e) {
                throw new RuntimeException("處理PDF時發生錯誤: " + e.getMessage(), e);
            }

            // 刪除旋轉圖片
            rotatedImage.delete();

            // 刪除 temp 檔案
            for (File temp : tempFiles) {
                temp.delete();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}