package utils;

import DTO.PageGroup;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * ClassName: PDFIOUtils
 * Description:
 * @Author 許記源
 * @Create 2025/5/7 下午 03:46
 * @Version 1.0
 */
public class PDFIOUtils {
    //處理頁面群組並保存為PDF文件
    public static void savePDF(PDDocument document, List<PageGroup> pageGroups, String outputPath) throws IOException {

        try(PDDocument pdDocument = new PDDocument();) {
            for (PageGroup pg : pageGroups) {
                PDPage originalPage1 = document.getPage(pg.getStartPage() - 1);
                PDPage originalPage2 = document.getPage(pg.getEndPage() - 1);
                // 導入頁面以斷開對原始 document 的依賴
                pdDocument.importPage(originalPage1);
                pdDocument.importPage(originalPage2);
            }
                pdDocument.save(outputPath);
        }
    }
    //合併多個PDF
    public static void mergePDF(String outputPath, String... inputPaths) {
        try (PDDocument finalDocument = new PDDocument()) {
            for (String inputPath : inputPaths) {
                File sourceFile = new File(inputPath);
                if (!sourceFile.exists()) {
                    System.out.println("檔案不存在，跳過: " + inputPath);
                    continue;
                }

                // 載入來源文件
                try (PDDocument sourceDocument = PDDocument.load(sourceFile)) {
                    // 使用 PDFMergerUtility 來複製頁面，這比手動 importPage 更穩定
                    PDFMergerUtility merger = new PDFMergerUtility();
                    merger.appendDocument(finalDocument, sourceDocument);
                }
            }

            // 儲存最終合併的文件
            finalDocument.save(outputPath);
            System.out.println("合併完成，輸出: " + outputPath);
        } catch (IOException e) {
            throw new RuntimeException("合併 PDF 時發生錯誤: " + e.getMessage(), e);
        }
    }



}


