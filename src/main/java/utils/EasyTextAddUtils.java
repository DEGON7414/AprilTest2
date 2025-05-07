package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType0Font;

import java.io.File;
import java.io.IOException;

/**
 * ClassName: EasyTextAddUtils
 * Description:
 * 寫入 需要發票
 * 寫入 需要統一編號
 *
 * @Author 許記源
 * @Create 2025/5/6 下午 02:33
 * @Version 1.0
 */
public class EasyTextAddUtils {
    public static void addBill(PDDocument document, int pageIndex, boolean needBill ) throws IOException {
        if(needBill){
            //===================== 1. 獲取 PDF 頁面相關資訊 =====================
            //1.獲取PDF頁面(pageIndex)
            PDPage page = document.getPage(pageIndex);
            //取的頁面的邊界
            PDRectangle mediaBox = page.getMediaBox();
            //頁面的寬高
            float width = mediaBox.getWidth();
            float height = mediaBox.getHeight();
            //===================== 2. 載入自定字型檔案 =====================
            //自定義字形
            File file = new File("C:\\Users\\cxhil\\Downloads\\Fonts_Kai\\TW-Kai-98_1.ttf");
            //PDType0Font載入指定字形 第一個參數是指定文件 第二個是字形路徑
            PDType0Font font = PDType0Font.load(document, file);

            //===================== 4. 開始寫入內容 =====================
            //PDPageContentStream 使用此類來處理PDF上內容流 可以用來繪製文本或線條
            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document,// PDF 文件物件
                    page,// 要寫入的頁面
                    PDPageContentStream.AppendMode.APPEND,// 追加模式（不覆蓋原有內容）
                    true, // 是否保留原有內容（是）
                    true// 是否壓縮內容流（是）
            )) {
                String text = "需要發票";
                int fontSize = 24;
                contentStream.beginText();// 開始輸入文字模式
                contentStream.setFont(font, fontSize);// 設定字型與字體大小
                float textWidth = font.getStringWidth(text) / 1000 * fontSize;

                float x = (width - textWidth) / 2;    // 讓文字置中
                float y = height - 200;               // 頁面上方往下 100 單位
                contentStream.newLineAtOffset(x, y); // 設定起始座標
                contentStream.showText(text); // 寫入標題文字
                contentStream.endText();// 結束輸入文字模式

            }
        }

    }
    //添加統編
    public static void addTaxId(PDDocument document,int pageIndex, boolean needTaxId ) throws IOException {
        if(needTaxId){
            //===================== 1. 獲取 PDF 頁面相關資訊 =====================
            //1.獲取PDF頁面(pageIndex)
            PDPage page = document.getPage(pageIndex);
            //取的頁面的邊界
            PDRectangle mediaBox = page.getMediaBox();
            //頁面的寬高
            float width = mediaBox.getWidth();
            float height = mediaBox.getHeight();
            //===================== 2. 載入自定字型檔案 =====================
            //自定義字形
            File file = new File("C:\\Users\\cxhil\\Downloads\\Fonts_Kai\\TW-Kai-98_1.ttf");
            //PDType0Font載入指定字形 第一個參數是指定文件 第二個是字形路徑
            PDType0Font font = PDType0Font.load(document, file);

            //===================== 4. 開始寫入內容 =====================
            //PDPageContentStream 使用此類來處理PDF上內容流 可以用來繪製文本或線條
            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document,// PDF 文件物件
                    page,// 要寫入的頁面
                    PDPageContentStream.AppendMode.APPEND,// 追加模式（不覆蓋原有內容）
                    true, // 是否保留原有內容（是）
                    true// 是否壓縮內容流（是）
            )) {
                String text = "需要統一編號";
                int fontSize = 24;
                contentStream.beginText();// 開始輸入文字模式
                contentStream.setFont(font, fontSize);// 設定字型與字體大小
                float textWidth = font.getStringWidth(text) / 1000 * fontSize;

                float x = (width - textWidth) / 2;    // 讓文字置中
                float y = height - 250;
                contentStream.newLineAtOffset(x, y); // 設定起始座標
                contentStream.showText(text); // 寫入標題文字
                contentStream.endText();// 結束輸入文字模式

            }
        }

    }

}
