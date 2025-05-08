package test;

import DTO.PageGroup;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import utils.PDFComparetor;
import utils.PDFIOUtils;
import utils.PDFReaderUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ClassName: NotPostTest
 * Description:
 * @Author 許記源
 * @Create 2025/5/7 下午 01:40
 * @Version 1.0
 */
public class NotPostTest {
    public static void main(String[] args) {
        //輸出檔案的路徑
        String fileOutPath1 = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\不用統編且小於800.pdf";
        String fileOutPath2 = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\不用統編且大於等於800.pdf";
        String fileOutPath3 = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\需用統編且小於800.pdf";
        String fileOutPath4 = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\需用統編且大於等於800.pdf";
        //輸出合併檔案的路徑
        String mergedFilePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\合併結果.pdf";

        // 建立儲存已產生檔案路徑的列表
        List<String> generatedFiles = new ArrayList<>();
        //來源檔案
//        String filePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\export_combined.pdf";
        String filePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\蝦皮店到店 - 隔日到貨_環亞電訊&AsiaWiF_0507_0822.pdf";

        //四個不同條件的頁面群組
        List<PageGroup> noTaxIdlessthan800 = new ArrayList<>();
        List<PageGroup> noTaxIdMorethan800 = new ArrayList<>();
        List<PageGroup> wantTaxIdlessthan800 = new ArrayList<>();
        List<PageGroup> wantTaxIdMorethan800 = new ArrayList<>();
        try (PDDocument sourceDocument = PDDocument.load(new File(filePath));) {
            int totalPages = sourceDocument.getNumberOfPages();
            for (int i = 0; i < totalPages - 1; i += 2) {
                String tableText = PDFReaderUtils.extractTextFromPdf(sourceDocument, i + 1, i + 2);
                String transportMethod = PDFReaderUtils.transportMethod(tableText);
                //如果不是郵局才執行
                if (!PDFReaderUtils.isPost(transportMethod)) {
                    //判斷是否需要統編
                    boolean isTaxId = PDFReaderUtils.buyerNote(tableText);
                    //判斷金額是否小於800
                    String money = PDFReaderUtils.paymentAmount(tableText);
                    boolean isLessThan800 = Integer.parseInt(money) < 800;
                    //提取貨號
                    List<String> itemNumbers = PDFReaderUtils.extractItemNumber(tableText);
                    //獲得第一個字母
                    //預設值
                    char firstLetter = 'X';
                    if (!itemNumbers.isEmpty() && itemNumbers.get(0).length() > 0) {
                        firstLetter = itemNumbers.get(0).charAt(0);
                    }
                    PageGroup pages = new PageGroup(i + 1, i + 2, String.valueOf(firstLetter), tableText);

                    if (!isTaxId && isLessThan800) {//不用統編且小於800
                        noTaxIdlessthan800.add(pages);
                    } else if (!isTaxId && !isLessThan800) {//不用統編且大於等於800
                        noTaxIdMorethan800.add(pages);

                    } else if (isTaxId && isLessThan800) { //要統編且小於800

                        wantTaxIdlessthan800.add(pages);
                    } else if (isTaxId && !isLessThan800) {//要統編且大於等於800
                        wantTaxIdMorethan800.add(pages);
                    }
                }
            }
            noTaxIdlessthan800.sort(Comparator.comparing(PageGroup::getFirstItemLetter));
            noTaxIdMorethan800.sort(Comparator.comparing(PageGroup::getFirstItemLetter));
            wantTaxIdlessthan800.sort(Comparator.comparing(PageGroup::getFirstItemLetter));
            wantTaxIdMorethan800.sort(Comparator.comparing(PageGroup::getFirstItemLetter));


            if (!noTaxIdlessthan800.isEmpty()) {
                PDFIOUtils.savePDF(sourceDocument, noTaxIdlessthan800, fileOutPath1);
                generatedFiles.add(fileOutPath1);

                System.out.println("已保存文件: " + fileOutPath1);
            } else {
                System.out.println("無符合「不用統編且小於800」條件的檔案");
            }

            if (!noTaxIdMorethan800.isEmpty()) {
                PDFIOUtils.savePDF(sourceDocument, noTaxIdMorethan800, fileOutPath2);
                generatedFiles.add(fileOutPath2);
                System.out.println("已保存文件: " + fileOutPath2);
            } else {
                System.out.println("無符合「不用統編且大於等於800」條件的檔案");
            }
            if (!wantTaxIdlessthan800.isEmpty()) {
                PDFIOUtils.savePDF(sourceDocument, wantTaxIdlessthan800, fileOutPath3);
                generatedFiles.add(fileOutPath3);

                System.out.println("已保存文件: " + fileOutPath3);
            } else {
                System.out.println("無符合「需用統編且小於800」條件的檔案");
            }
            if (!wantTaxIdMorethan800.isEmpty()) {
                PDFIOUtils.savePDF(sourceDocument, wantTaxIdMorethan800, fileOutPath4);
                generatedFiles.add(fileOutPath4);
                System.out.println("已保存文件: " + fileOutPath4);
            } else {
                System.out.println("無符合「需用統編且大於等於800」條件的檔案");
            }
            //合併PDF
            PDFIOUtils.mergePDF(mergedFilePath, fileOutPath1, fileOutPath2, fileOutPath3, fileOutPath4);

            boolean b = PDFComparetor.comparePDF(filePath, mergedFilePath);
            if (b) {
                System.out.println("兩者相同");
            } else {
                System.out.println("兩者不同");
            }


        } catch (IOException e) {
            throw new RuntimeException("處理PDF時發生錯誤: " + e.getMessage(), e);
        }


    }
}

