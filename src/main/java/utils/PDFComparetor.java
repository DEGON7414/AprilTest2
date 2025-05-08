package utils;

import org.apache.pdfbox.pdmodel.PDDocument;

import java.io.File;
import java.io.IOException;

/**
 * ClassName: PDFComparetor
 * Description:比較兩個PDF檔案頁數
 * 提取PDF頁數
 * 比較
 * @Author 許記源
 * @Create 2025/5/8 上午 09:35
 * @Version 1.0
 */
public class PDFComparetor {
    //獲取頁數
    public static int getPageCount(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("PDF doesn't exist: " + filePath);
        }
        try (PDDocument document = PDDocument.load(file)) {
            return document.getNumberOfPages();
        }
    }
    //判斷兩者是否相等
    public static  boolean comparePDF(String sourceFilePath, String newFilePath) throws IOException {
        int pageCount1=getPageCount(sourceFilePath);
        int pageCount2=getPageCount(newFilePath);
        return pageCount1 == pageCount2 ;
    }
}
