package test;

import DTO.PageGroup;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.font.PDType0Font;
import utils.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * ClassName: FinalTest
 * Description:
 * @Author 許記源
 * @Create 2025/5/8 下午 01:02
 * @Version 1.0
 */
public class FinalTest {
    public static void main(String[] args) {
        // 輸入與輸出檔案路徑
        String inputFilePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\test_商城_郵局_0531_0810.pdf";
        String intermediateFilePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\export_combined.pdf";
        String mergedFilePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\合併結果.pdf";
        String imagePath = "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\receipt_signature.bmp";
        String date = "2025年5月2日";
        String itemNumber = "Mobifone07DU";
        String markNumber = "BUJ10G09D";
        //指定字形
        File font = new File("C:\\Users\\cxhil\\Downloads\\Fonts_Kai\\TW-Kai-98_1.ttf");
        List<String> outputPaths = Arrays.asList(
                "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\不用統編且小於800.pdf",
                "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\不用統編且大於等於800.pdf",
                "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\需用統編且小於800.pdf",
                "C:\\Users\\cxhil\\Desktop\\kevin0427考題\\需用統編且大於等於800.pdf"
        );

        List<File> tempFiles = new ArrayList<>();
        File rotatedImage = null;
        try {
            // 第一階段：處理並生成中間 PDF
            rotatedImage = processAndAnnotatePDF(inputFilePath, intermediateFilePath, imagePath, date, itemNumber, markNumber, tempFiles);

            // 第二階段：分類並合併 PDF
            classifyAndMergePDF(intermediateFilePath, mergedFilePath, outputPaths);

            // 比較原始與合併 PDF
            boolean isIdentical = PDFComparetor.comparePDF(inputFilePath, mergedFilePath);
            System.out.println(isIdentical ? "原始與合併PDF相同" : "原始與合併PDF不同");

        } catch (IOException e) {
            throw new RuntimeException("處理PDF時發生錯誤: " + e.getMessage(), e);
        } finally {
            // 清理臨時檔案與旋轉圖片
            if (rotatedImage != null && rotatedImage.exists()) {
                rotatedImage.delete();
            }
            for (File tempFile : tempFiles) {
                if (tempFile.exists()) {
                    tempFile.delete();
                }
            }
        }
    }

    private static File processAndAnnotatePDF(String inputFilePath, String outputFilePath, String imagePath,
                                              String date, String itemNumber, String markNumber, List<File> tempFiles)
            throws IOException {
        // 提前旋轉圖片
        File picFile = new File(imagePath);
        double degree = 270;
        File rotatedImage = PicInsert.rotateImage(picFile, degree);

        try (PDDocument finalDocument = new PDDocument()) {
            int batchSize = 20;
            int processedCount = 0;

            try (PDDocument sourceDocument = PDDocument.load(new File(inputFilePath))) {
                int totalPages = sourceDocument.getNumberOfPages();
                if (totalPages % 2 != 0) {
                    System.out.println("警告：PDF頁數不是偶數，最後一頁可能無法正確處理");
                }

                for (int i = 0; i < totalPages - 1; i += 2) {
                    try (PDDocument batchDocument = new PDDocument()) {
                        PDPage page1 = sourceDocument.getPage(i);
                        PDPage page2 = sourceDocument.getPage(i + 1);
                        batchDocument.importPage(page1);
                        batchDocument.importPage(page2);

                        String tableText = PDFReaderUtils.extractTextFromPdf(batchDocument, 2, 2);
                        String barcodeNumber = PDFReaderUtils.extractBarcodeNumber(tableText);
                        String payment = PDFReaderUtils.paymentAmount(tableText);
                        System.out.println("條碼編號: " + barcodeNumber + ", 實付金額: " + payment);
                        List<String> itemNumbers = PDFReaderUtils.extractItemNumber(tableText);
                        boolean isItemNumber = PDFReaderUtils.isItemNumberMatched(itemNumbers, itemNumber);
                        boolean isTaxId = PDFReaderUtils.buyerNote(tableText);
                        boolean isSpecialNumber = PDFReaderUtils.isItemNumberMatched(itemNumbers, markNumber);

//                        if (isItemNumber) {
//                            EasyTextAddUtils.addBill(batchDocument, 1, true, font );
//                        }
//                        if (isSpecialNumber) {
//                            EasyTextAddUtils.addBoth(batchDocument, 1, true,font);
//                        }
//                        if (isTaxId) {
//                            EasyTextAddUtils.addTaxId(batchDocument, 1, true,font);
//                        }
                        if (!isItemNumber && !isTaxId && !isSpecialNumber) {
//                            TextAppenderUtils.addText(batchDocument, 1, date, barcodeNumber, payment,font);
                            PDPage targetPage = batchDocument.getPage(1);
                            PicInsert.insertPic(batchDocument, targetPage, rotatedImage);
                        }

                        File tempFile = new File("temp_" + i + ".pdf");
                        batchDocument.save(tempFile);
                        tempFiles.add(tempFile);
                        processedCount++;
                        System.out.println("已處理 " + processedCount + " 筆資料");
                    }
                }

                // 合併所有臨時 PDF
                PDFMergerUtility merger = new PDFMergerUtility();
                merger.setDestinationFileName(outputFilePath);
                for (File tempFile : tempFiles) {
                    merger.addSource(tempFile);
                }
                merger.mergeDocuments(MemoryUsageSetting.setupTempFileOnly());
                System.out.println("已生成中間PDF，處理筆數：" + processedCount);
            }
        }
        return rotatedImage;
    }

    private static void classifyAndMergePDF(String inputFilePath, String mergedFilePath, List<String> outputPaths)
            throws IOException {
        List<PageGroup> noTaxIdLessThan800 = new ArrayList<>();
        List<PageGroup> noTaxIdMoreThan800 = new ArrayList<>();
        List<PageGroup> wantTaxIdLessThan800 = new ArrayList<>();
        List<PageGroup> wantTaxIdMoreThan800 = new ArrayList<>();
        List<String> generatedFiles = new ArrayList<>();

        try (PDDocument sourceDocument = PDDocument.load(new File(inputFilePath))) {
            int totalPages = sourceDocument.getNumberOfPages();
            for (int i = 0; i < totalPages - 1; i += 2) {
                String tableText = PDFReaderUtils.extractTextFromPdf(sourceDocument, i + 1, i + 2);
                String transportMethod = PDFReaderUtils.transportMethod(tableText);
                if (!PDFReaderUtils.isPost(transportMethod)) {
                    boolean isTaxId = PDFReaderUtils.buyerNote(tableText);
                    String money = PDFReaderUtils.paymentAmount(tableText);
                    boolean isLessThan800 = Integer.parseInt(money) < 800;
                    List<String> itemNumbers = PDFReaderUtils.extractItemNumber(tableText);
                    char firstLetter = !itemNumbers.isEmpty() && itemNumbers.get(0).length() > 0
                            ? itemNumbers.get(0).charAt(0) : 'X';
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

            // 保存分類ilibre>保存分類 PDF
            if (!noTaxIdLessThan800.isEmpty()) {
                PDFIOUtils.savePDF(sourceDocument, noTaxIdLessThan800, outputPaths.get(0));
                generatedFiles.add(outputPaths.get(0));
                System.out.println("已保存: " + outputPaths.get(0));
            }
            if (!noTaxIdMoreThan800.isEmpty()) {
                PDFIOUtils.savePDF(sourceDocument, noTaxIdMoreThan800, outputPaths.get(1));
                generatedFiles.add(outputPaths.get(1));
                System.out.println("已保存: " + outputPaths.get(1));
            }
            if (!wantTaxIdLessThan800.isEmpty()) {
                PDFIOUtils.savePDF(sourceDocument, wantTaxIdLessThan800, outputPaths.get(2));
                generatedFiles.add(outputPaths.get(2));
                System.out.println("已保存: " + outputPaths.get(2));
            }
            if (!wantTaxIdMoreThan800.isEmpty()) {
                PDFIOUtils.savePDF(sourceDocument, wantTaxIdMoreThan800, outputPaths.get(3));
                generatedFiles.add(outputPaths.get(3));
                System.out.println("已保存: " + outputPaths.get(3));
            }

            // 合併 PDF
            PDFIOUtils.mergePDF(mergedFilePath, outputPaths.get(0), outputPaths.get(1),
                    outputPaths.get(2), outputPaths.get(3));
        }
    }
}
