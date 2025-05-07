package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.awt.SystemColor.text;

/**
 * ClassName: PDFReader
 * Description:
 * 負責讀取PDF
 * 提取編號 金額
 *
 * @Author 許記源
 * @Create 2025/4/28 下午 01:25
 * @Version 1.0
 */
public class PDFReaderUtils {
    //讀取PDF文本
    public static String extractTextFromPdf(PDDocument document, int startPage, int endPage) throws IOException {
        PDFTextStripper pdfTextStripper = new PDFTextStripper();
        pdfTextStripper.setStartPage(startPage);
        pdfTextStripper.setEndPage(endPage);
        String text1 = pdfTextStripper.getText(document);
        System.gc();

        return text1;

    }

    //提取條碼下的編號
    /*
     * @param text 從PDF提取出來全部的文本
     * @return 條碼編號
     * */
    public static String extractBarcodeNumber(String text) throws IOException {
        //1.檢查提取的文本是否為空
        if (text == null || text.isEmpty()) {
            return null;
        }
        //.2.Pattern定義正則表達式                 // \\d代表數字，{2}表示至少兩位數開頭 [A-Z 或0-9]至少六個字元。
        Pattern compile = Pattern.compile("\\d{2,}[A-Z0-9]{6,}");
        //3.是否有符合的
        Matcher matches = compile.matcher(text);
        //如果有符合回傳第一筆
        if (matches.find()) {
            return matches.group();
        }
        return null;
    }

    //提取實付金額
    public static String paymentAmount(String text) throws IOException {
        //1.檢查是否為空
        if (text == null || text.isEmpty()) {
            return null;
        }
        //2.將整段文字按照行 分隔成陣列lines
        String[] lines = text.split("\\r?\\n");
        //3.一筆一筆取出
        for (String line : lines) {
            //4.去除開頭結尾空白 並將多個空白(\\s)合併成一個 +是多個的意思
            line = line.trim().replaceAll("\\s+", " ");
            // 5.匹配異字「金額」的金字 一個是正常金 一個是部首用的金
            // 中文字沒有全形半形 英數字才有
            if ( line.contains("實付")) {
                //6以:分割
                String[] parts = line.split("[：:]");
                if (parts.length > 1) {
                    //7.取出金額[1]
                    return parts[1].split(" ")[0].trim();
                }
            }
        }
        return null;
    }

    //提取貨號
    public static boolean isItemNumberMatched(String text, String itemNumber) throws IOException {
        //1.檢查是否為空
        if (text == null || text.isEmpty()) {
            return false;
        }
        String cleanedText = text.replaceAll("[\\n\\r]", " ") // 去掉換行符和回車符
                .replaceAll("　", " ")  // 去掉全形空格
                .replaceAll("\\s+", " ") // 去掉多餘的空格
                .toUpperCase(); // 轉為大寫
        // 收集所有匹配的貨號
        List<String> extractedItemNumbers = new ArrayList<>();
        //\\b：單字邊界，避免匹配到一串字裡面的部分
        //[A-Z0-9]{5,}：第一段至少 5 個大寫英數字
        // \\s?：中間可能有空格
        //[A-Z0-9]{2,}：第二段至少 2 個大寫英數字
        Pattern compile = Pattern.compile("\\b([A-Z]+\\d+[A-Z0-9]*)\\s+([A-Z0-9]{1,})\\b");
        Matcher matcher = compile.matcher(cleanedText);

        while (matcher.find()) {
            String firstPart = matcher.group(1);
            String secondPart = matcher.group(2);
            String fullItemNumber = firstPart + secondPart; // 完整貨號組合
            extractedItemNumbers.add(fullItemNumber); // 添加完整貨號
            System.out.println("提取到的所有貨號: " + extractedItemNumbers);

        }

        // 第二種模式：處理包含 & 字符的特殊貨號 (如 AT&T30D U)
        Pattern compile1 = Pattern.compile("(AT&T\\d+[A-Z0-9]*)\\s+([A-Z0-9]{1,})");
        Matcher matcher1 = compile1.matcher(cleanedText);

        while (matcher1.find()) {
            String firstPart1 = matcher1.group(1);  // 例如：AT&T30D
            String secondPart1 = matcher1.group(2); // 例如：U
            String fullItemNumber1 = firstPart1 + secondPart1; // 完整貨號組合
            extractedItemNumbers.add(fullItemNumber1);
            // 列印提取出的所有貨號
            System.out.println("提取到的所有貨號: " + extractedItemNumbers);
            if (!extractedItemNumbers.contains(fullItemNumber1)) {
                extractedItemNumbers.add(fullItemNumber1);
            }
        }


        // 檢查輸入的貨號是否在提取出的貨號中
        String input = itemNumber.replaceAll("\\s", "").toUpperCase(); // 清理空格並轉大寫
        for (String extracted : extractedItemNumbers) {
            System.out.println("輸入的貨號: " + input);
            // 若有匹配，則返回 true
            if (extracted.equals(input)) {
                return true;
            }
        }
        return false;
    }

    //提取買家備註並檢查是否有無統編相關
    public static boolean buyerNote(String text) throws IOException {
        //1.檢查是否為空
        if (text == null || text.isEmpty()) {
            return false;
        }
        //2.將整段文字按照行 分隔成陣列lines
        String[] lines = text.split("\\r?\\n");
        //3.一筆一筆取出
        for (String line : lines) {
            //4.去除開頭結尾空白 並將多個空白(\\s)合併成一個 +是多個的意思
            line = line.trim().replaceAll("\\s+", " ");

            if (line.contains("買家備註")) {
                //6以:分割
                String[] parts = line.split("[：:]");
                if (parts.length > 1) {
                    //7.取出備註
                    String note = parts[1].split(" ")[0].trim();
                    //定義關鍵字
                    String[] keywords = {"統編", "統一編號", "開", "統號", "Taxpayer",
                            "taxpayer", "tax", "Tax", "ID", "id"};
                    //檢查
                    for (String keyword : keywords) {
                        if (note.contains(keyword)) {
                            return true;
                        }
                    }
                    //檢查是否含有8位數
                    if (note.matches(".*\\b\\d{8}\\b.*")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}


