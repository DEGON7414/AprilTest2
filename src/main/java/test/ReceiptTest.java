package test;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import utils.*;

import java.io.File;
import java.io.IOException;
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
          String filePath = "C:\\Users\\cxhil\\Downloads\\test_商城_郵局_0531_0810.pdf";

        //輸出檔案
        String outputFilePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\export_combined.pdf";
        //來源圖片
        String imagePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\receipt_signature.bmp";
        //外部參數 date
        String date = "2025年5月2日";
        //外部參數 貨號
        String itemNumber = "BUIIJ10G 30D";

        try {
            //1.呼叫extractReceiptPages 抓取檔案中的每一頁
            List<PDPage> pdPages = OrderUtils.extractReceiptPages(filePath);
            //2.確保是偶數
            if (pdPages.size() % 2 != 0) {
                System.out.println("警告：PDF頁數不是偶數，最後一頁可能無法正確處理");
            }
            //3. 載入考題檔案
            try (PDDocument sourceDocument = PDDocument.load(new File(filePath));
            ) {//計算已處理的頁數
                int processedCount = 0;
                // 遍歷每兩頁一組
                for (int i = 0; i < pdPages.size() - 1; i += 2) {
                    //貨號判斷開關
                    boolean isItemNumber = false;
                    //統編判斷開關
                    boolean isTaxId = false;
                    //2. 從第二頁（表格頁）提取文本
                    String tableText = PDFReaderUtils.extractTextFromPdf(sourceDocument, i + 2, i + 2); // 提取第二頁文本

                    //3. 提取條碼編號
                    String barcodeNumber = PDFReaderUtils.extractBarcodeNumber(tableText);
                    System.out.println("條碼編號是: " + barcodeNumber);

                    //4. 提取實付金額
                    String payment = PDFReaderUtils.paymentAmount(tableText);
                    System.out.println("實付金額是: " + payment);

                    //判斷貨號是否相同
                    boolean itemNumberMatched = PDFReaderUtils.isItemNumberMatched(tableText, itemNumber);
                    //判斷買家備註中有無統編相關備註
                    boolean isBuyerNote = PDFReaderUtils.buyerNote(tableText);
                    //貨號相同則不印收據 印字
                    if (itemNumberMatched) {
                        System.out.println("找到了");
                        System.out.println("已處理 " + processedCount + " 筆資料");
                        System.out.println("-----------------");
                        isItemNumber = true;
                        EasyTextAddUtils.addBill(sourceDocument, i + 1, isItemNumber);

                        processedCount++;
                    }
                    //備註若有統編則不印收據 印字
                    if (isBuyerNote) {
                        System.out.println("有統編相關備註");
                        System.out.println("已處理 " + processedCount + " 筆資料");
                        System.out.println("-----------------");
                        isTaxId = true;
                        EasyTextAddUtils.addTaxId(sourceDocument, i + 1, isTaxId);
                        processedCount++;

                    }if(!isItemNumber && !isTaxId ){
                        //5.寫入
                        TextAppenderUtils.addText(sourceDocument, i + 1, date, barcodeNumber, payment);
                        //6載入圖片並旋轉
                        File picFile = new File(imagePath);
                        double degree = 270;
                        File rotatedImage = PicInsert.rotateImage(picFile, degree);

                        // 接著插入圖片
                        //選取頁數插入
                        PDPage targetPage = sourceDocument.getPage(i + 1);
                        PicInsert.insertPic(sourceDocument, targetPage, rotatedImage);
                        processedCount++;
                        System.out.println("已處理 " + processedCount + " 筆資料");
                        System.out.println("-----------------");
                    }
                }
                // 保存合併後的文檔
                sourceDocument.save(outputFilePath);
                System.out.println("已成功創建包含所有 " + processedCount + " 筆收據的PDF文件：" + outputFilePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("處理PDF時發生錯誤: " + e.getMessage(), e);

        }
    }
}