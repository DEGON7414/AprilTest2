package DTO;

/**
 * ClassName: PageGroup
 * Description:
 * 資料容器 暫存 分組後的資料
 * @Author 許記源
 * @Create 2025/5/7 下午 02:30
 * @Version 1.0
 */
public class PageGroup {
    private int startPage;
    private int endPage;
    private String firstItemLetter;
    private String rawText;

    public PageGroup(int startPage, int endPage, String firstItemLetter, String rawText) {
        this.startPage = startPage;
        this.endPage = endPage;
        this.firstItemLetter = firstItemLetter;
        this.rawText = rawText;
    }

    public int getStartPage() {
        return startPage;
    }

    public void setStartPage(int startPage) {
        this.startPage = startPage;
    }

    public int getEndPage() {
        return endPage;
    }

    public void setEndPage(int endPage) {
        this.endPage = endPage;
    }

    public String getFirstItemLetter() {
        return firstItemLetter;
    }

    public void setFirstItemLetter(String firstItemLetter) {
        this.firstItemLetter = firstItemLetter;
    }

    public String getRawText() {
        return rawText;
    }

    public void setRawText(String rawText) {
        this.rawText = rawText;
    }
}
