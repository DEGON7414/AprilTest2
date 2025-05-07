package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassName: OrderUtils
    功能 提取PDF的頁面
 *每兩頁一組處理（for 迴圈走兩頁一組）
 * 第一頁：圖片跳過
 * 第二頁：
 * 讀取文本（提取資料）
 * @Author 許記源
 * @Create 2025/4/28 下午 02:00
 * @Version 1.0
 */
public class OrderUtils {
    public static List<PDPage> extractReceiptPages(String filePath) throws IOException {
        //1. 建立ArrayList 用來存PDF每一頁
        List<PDPage> receiptPages  = new ArrayList<>();
        //2. 建立File物件 代表要開啟的PDF檔案
        File file = new File(filePath);
        //3.載入檔案
        try (PDDocument document = PDDocument.load(file);){
            //4.取得總頁數
            int totalPages = document.getNumberOfPages();
            //5.取每一頁
            for (int i = 0; i < totalPages; i++) {
                PDPage receiptPage = document.getPage(i);
                receiptPages .add(receiptPage);
            }
        }

        return receiptPages;
    }
}
