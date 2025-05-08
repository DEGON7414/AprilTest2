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
 * ClassName: FinalTest
 * Description:
 * @Author 許記源
 * @Create 2025/5/8 下午 01:02
 * @Version 1.0
 */
public class SortFirst {
    public static void main(String[] args) {
// 輸入檔案路徑
        String filePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\蝦皮店到店 - 隔日到貨_環亞電訊&AsiaWiF_0507_0822.pdf";
        // 輸出檔案路徑
        String fileOutPath1 = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\不用統編且小於800.pdf";
        String fileOutPath2 = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\不用統編且大於等於800.pdf";
        String fileOutPath3 = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\需用統編且小於800.pdf";
        String fileOutPath4 = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\需用統編且大於等於800.pdf";
        String mergedFilePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\合併結果.pdf";
        // 圖片及字型路徑
        String imagePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\receipt_signature.bmp";
        String fontPath = "C:\\Users\\cxhil\\Downloads\\Fonts_Kai\\TW-Kai-98_1.ttf";
        // 外部參數
        String date = "2025年5月2日";
        //要查找的貨號
        String itemNumber = "Mobifone07DU";
        //指定貨號
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
            //取得總頁數
            int totalPages = sourceDocument.getNumberOfPages();
            for (int i = 0; i < totalPages - 1; i += 2) {
                String tableText = PDFReaderUtils.extractTextFromPdf(sourceDocument, i + 1, i + 2);
                String transportMethod = PDFReaderUtils.transportMethod(tableText);

                // 僅處理非郵局頁面
                if (!PDFReaderUtils.isPost(transportMethod)) {
                    // 執行 ReceiptTest 的頁面處理
                    ReceiptUtils.createReceipt(sourceDocument, i + 1, tableText, date, itemNumber, markNumber, font, rotatedImage);

                    // 分組邏輯
                    boolean isTaxId = PDFReaderUtils.buyerNote(tableText);
                    String money = PDFReaderUtils.paymentAmount(tableText);
                    boolean isLessThan800 = Integer.parseInt(money) < 800;
                    List<String> itemNumbers = PDFReaderUtils.extractItemNumber(tableText);
                    char firstLetter = itemNumbers.isEmpty() || itemNumbers.get(0).isEmpty() ? 'X' : itemNumbers.get(0).charAt(0);
                    PageGroup pages = new PageGroup(i + 1, i + 2, String.valueOf(firstLetter), tableText);

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
