package test;

import DTO.PageGroup;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ClassName: ReceiptFirst
 * Description:先全都打印收據再去做分類排序輸出
 * @Author 許記源
 * @Create 2025/5/8 下午 04:08
 * @Version 1.0
 */
public class ReceiptFirstTest {
    public static void main(String[] args) {
        // 輸入檔案路徑
        String filePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\蝦皮店到店 - 隔日到貨_環亞電訊&AsiaWiF_0507_0822.pdf";
        // 輸出檔案路徑
        String fileOutPath1 = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\result\\不用統編且小於800.pdf";
        String fileOutPath2 = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\result\\不用統編且大於等於800.pdf";
        String fileOutPath3 = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\result\\需用統編且小於800.pdf";
        String fileOutPath4 = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\result\\需用統編且大於等於800.pdf";
        String mergedFilePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\result\\合併結果.pdf";
        // 圖片及字型路徑
        String imagePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\receipt_signature.bmp";
        String fontPath = "C:\\Users\\cxhil\\Downloads\\Fonts_Kai\\TW-Kai-98_1.ttf";
        // 外部參數
        String date = "2025年5月2日";
        String itemNumber = "Mobifone07DU";
        String markNumber = "BUJ10G09D";

        // 儲存已產生檔案
        List<String> generatedFiles = new ArrayList<>();
        // 四個分組
        List<PageGroup> noTaxIdLessThan800 = new ArrayList<>();
        List<PageGroup> noTaxIdMoreThan800 = new ArrayList<>();
        List<PageGroup> wantTaxIdLessThan800 = new ArrayList<>();
        List<PageGroup> wantTaxIdMoreThan800 = new ArrayList<>();

        try (PDDocument sourceDocument = PDDocument.load(new File(filePath))) {
            // 載入字型
            PDType0Font font = PDType0Font.load(sourceDocument, new File(fontPath));
            // 旋轉圖片
            File rotatedImage = PicInsert.rotateImage(new File(imagePath), 270);
            // 第一階段：收據處理
            int totalPages = sourceDocument.getNumberOfPages();
            //取得運送方式文本
            String totalText = PDFReaderUtils.extractTextFromPdf(sourceDocument, totalPages, totalPages);
            String transportMethod = PDFReaderUtils.transportMethod(totalText);

            //判斷是否為偶數
            if (totalPages % 2 != 0) {
                System.out.println("警告：PDF頁數不是偶數，最後一頁可能無法正確處理");
            }
            //初始化計數
            int processedCount = 0;
            // 僅處理非郵局頁面
            if (!PDFReaderUtils.isPost(transportMethod)) {
                for (int i = 0; i < totalPages - 1; i += 2) {
                    String tableText = PDFReaderUtils.extractTextFromPdf(sourceDocument, i + 1, i + 2);
                    ReceiptUtils.createReceipt(sourceDocument, i + 1, tableText, date, itemNumber, markNumber, font, rotatedImage);
                    processedCount++;
                    System.out.println("已處理 " + processedCount + " 組");
                }
            }
            // 第二階段：分類排序
            for (int i = 0; i < totalPages - 1; i += 2) {
                //取得文本
                String tableText = PDFReaderUtils.extractTextFromPdf(sourceDocument, i + 1, i + 2);
                //是否有備註要統編
                boolean isTaxId = PDFReaderUtils.buyerNote(tableText);
                //提取錢
                String money = PDFReaderUtils.paymentAmount(tableText);
                //判斷是否小於800
                boolean isLessThan800 = Integer.parseInt(money) < 800;
                //提取貨號
                List<String> itemNumbers = PDFReaderUtils.extractItemNumber(tableText);
                //初始化 首字母
                char firstLetter = 'X';
                //首字母不為空
                if (!itemNumbers.isEmpty() && itemNumbers.get(0).length() > 0) {
                    //get第一個字串中的第一個字元
                    firstLetter = itemNumbers.get(0).charAt(0);
                }
                // 創建 PageGroup 物件，儲存頁面資訊：
                // - i + 1：起始頁碼（實際頁碼，從 1 開始）
                // - i + 2：結束頁碼（每組處理兩頁）
                // - String.valueOf(firstLetter)：貨號首字母（用於後續排序）
                // - tableText：頁面提取的文本內容（用於後續處理或儲存）
                PageGroup pages = new PageGroup(i + 1, i + 2, String.valueOf(firstLetter), tableText);
                //根據條件分組
                if (!isTaxId && isLessThan800) {
                    noTaxIdLessThan800.add(pages);
                } else if (!isTaxId && !isLessThan800) {
                    noTaxIdMoreThan800.add(pages);
                } else if (isTaxId && isLessThan800) {
                    wantTaxIdLessThan800.add(pages);
                } else if (isTaxId && !isLessThan800) {
                    wantTaxIdMoreThan800.add(pages);
                }

            }

            // 排序
            noTaxIdLessThan800.sort(Comparator.comparing(PageGroup::getFirstItemLetter));
            noTaxIdMoreThan800.sort(Comparator.comparing(PageGroup::getFirstItemLetter));
            wantTaxIdLessThan800.sort(Comparator.comparing(PageGroup::getFirstItemLetter));
            wantTaxIdMoreThan800.sort(Comparator.comparing(PageGroup::getFirstItemLetter));

            // 儲存分組 PDF
            if (!noTaxIdLessThan800.isEmpty()) {
                PDFIOUtils.savePDF(sourceDocument, noTaxIdLessThan800, fileOutPath1);
                generatedFiles.add(fileOutPath1);
                System.out.println("已保存文件: " + fileOutPath1);
            } else {
                System.out.println("無符合「不用統編且小於800」條件的檔案");
            }
            if (!noTaxIdMoreThan800.isEmpty()) {
                PDFIOUtils.savePDF(sourceDocument, noTaxIdMoreThan800, fileOutPath2);
                generatedFiles.add(fileOutPath2);
                System.out.println("已保存文件: " + fileOutPath2);
            } else {
                System.out.println("無符合「不用統編且大於等於800」條件的檔案");
            }
            if (!wantTaxIdLessThan800.isEmpty()) {
                PDFIOUtils.savePDF(sourceDocument, wantTaxIdLessThan800, fileOutPath3);
                generatedFiles.add(fileOutPath3);
                System.out.println("已保存文件: " + fileOutPath3);
            } else {
                System.out.println("無符合「需用統編且小於800」條件的檔案");
            }
            if (!wantTaxIdMoreThan800.isEmpty()) {
                PDFIOUtils.savePDF(sourceDocument, wantTaxIdMoreThan800, fileOutPath4);
                generatedFiles.add(fileOutPath4);
                System.out.println("已保存文件: " + fileOutPath4);
            }

            // 合併 PDF
            PDFIOUtils.mergePDF(mergedFilePath, fileOutPath1, fileOutPath2, fileOutPath3, fileOutPath4);
            System.out.println("已合併文件: " + mergedFilePath);

            // 比對原始與合併檔案
            boolean isSame = PDFComparetor.comparePDF(filePath, mergedFilePath);
            System.out.println(isSame ? "兩者相同" : "兩者不同");

            // 刪除旋轉圖片
            if (rotatedImage != null) {
                rotatedImage.delete();
            }

        } catch (IOException e) {
            throw new RuntimeException("處理PDF時發生錯誤: " + e.getMessage(), e);
        }
    }
}
