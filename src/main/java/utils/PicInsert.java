package utils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.util.Matrix;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * ClassName: PicInsert
 * Description:
 * 插入圖片到 PDF 中的方法
 * 旋轉圖片
 *
 * @Author 許記源
 * @Create 2025/4/30 下午 03:05
 * @Version 1.0
 */
public class PicInsert {
    // 插入圖片到 PDF 中的方法
    public static boolean insertPic(PDDocument document, PDPage page, File picFile) {
        try {   //獲取頁面寬 高度
            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            //讀取圖片文件並且創建PDImageXObject 物件
            PDImageXObject image = PDImageXObject.createFromFile(picFile.getAbsolutePath(), document);
            //讀取圖片寬高
            float imageWidth = image.getWidth();
            float imageHeight = image.getHeight();

            // 縮放比例
            float scaleFactor = Math.min(pageHeight * 0.4f / imageWidth, pageWidth * 0.4f / imageHeight);
            float drawWidth = imageWidth * scaleFactor;
            float drawHeight = imageHeight * scaleFactor;

            // 設定圖片在右下角（有邊距）
            //Y軸減是往左
            //X軸 加是向下
            float margin = 10;
            float x = pageWidth - drawWidth - margin - 5;
            float y = margin + 45;
            // 5. 創建 PDPageContentStream 用於繪製圖片
            try (PDPageContentStream contentStream = new PDPageContentStream(
                    document, // PDF 文檔。
                    page, // PDPage 物件，代表當前頁面，圖片將會被繪製在這一頁上。
                    PDPageContentStream.AppendMode.APPEND,//是一個模式參數，表示將要向頁面內容流中追加內容，而不是覆蓋掉已有的內容。
                    true //`true` 是一個布林值，表示開啟自動關閉 `contentStream` 資源
            )) {
                //繪製圖片到頁面
                contentStream.drawImage(image, x, y, drawWidth, drawHeight);
            }

            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 旋轉圖片的方法
    public static File rotateImage(File picFile, double degree) throws IOException {

        // 加載原始圖片
        BufferedImage originalImage = ImageIO.read(picFile);
        //取得原始寬高
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // 計算旋轉後的寬高
        // 根據旋轉角度（degree），計算旋轉後的寬度和高度。這裡使用了正弦和餘弦函數來確定旋轉後的邊長。
        double sin = Math.abs(Math.sin(Math.toRadians(degree)));// 計算正弦值
        double cos = Math.abs(Math.cos(Math.toRadians(degree)));// 計算餘弦值
        int newWidth = (int) Math.floor(width * cos + height * sin);// 旋轉後的寬度
        int newHeight = (int) Math.floor(height * cos + width * sin); // 旋轉後的高度

        // 創建新的空白圖片，尺寸為旋轉後的尺寸
        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, originalImage.getType());
        // 使用 Graphics2D 來繪製圖像
        Graphics2D g2d = rotatedImage.createGraphics();

        // 設定旋轉的中心點為新圖像的中心
        //使用 AffineTransform 來設定旋轉變換，將旋轉中心設置為圖片的中心。
        AffineTransform at = new AffineTransform();
        at.translate((newWidth - width) / 2, (newHeight - height) / 2);// 平移，確保旋轉後的圖片居中
        at.rotate(Math.toRadians(degree), width / 2, height / 2);// 執行旋轉，旋轉中心為原始圖片的中心

        // 執行旋轉
        // at：旋轉變換的設定，TYPE_BICUBIC：雙三次插值方法進行旋轉
        AffineTransformOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
        rotatedImage = op.filter(originalImage, rotatedImage);

        // 裁剪黑邊
        rotatedImage = cropToVisible(rotatedImage);

        // 保存旋轉後的圖片
        File outputFile = new File("rotated_image.jpg");
        ImageIO.write(rotatedImage, "JPG", outputFile);
        // 釋放 Graphics2D 物件佔用的資源
        g2d.dispose();

        return outputFile;
    }

    /**
     * 裁剪掉多餘的黑邊
     *
     * @param img 旋轉後的圖片
     * @return 去除黑邊後的圖片
     */
    private static BufferedImage cropToVisible(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();

        // 設定最小和最大坐標，遍歷圖片查找黑邊範圍
        int top = height, left = width, bottom = 0, right = 0;//初始化

        // 查找最上、最下、最左、最右的非黑邊區域
        for (int y = 0; y < height; y++) {// 遍歷每一行（y軸）
            for (int x = 0; x < width; x++) { // 遍歷每一列（x軸）
                if ((img.getRGB(x, y) & 0xFF000000) != 0) { // 如果像素不是完全透明
                    if (y < top) top = y; // 更新最上方的非透明像素位置
                    if (y > bottom) bottom = y;// 更新最下方的非透明像素位置
                    if (x < left) left = x;// 更新最左邊的非透明像素位置
                    if (x > right) right = x;  // 更新最右邊的非透明像素位置
                }
            }
        }

        // 根據找到的邊界裁剪圖片
        BufferedImage croppedImage = img.getSubimage(left, top, right - left, bottom - top);
        return croppedImage;
    }


}
