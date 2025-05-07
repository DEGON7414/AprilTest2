package utils;

/**
 * ClassName: AmountToChinese
 * Description:
 *  數字轉換大寫中文數字
 * @Author 許記源
 * @Create 2025/4/28 下午 03:33
 * @Version 1.0
 */
public class AmountToChinese {
    public static String covertAmountToChinese(int amount) {
        //1. 中文數字對照表 0-9
        String[] chineseNumbers = {"零", "壹", "貳", "參", "肆", "伍", "陸", "柒", "捌", "玖"};
        //2.中文數字單位
        String[] units = {"仟萬", "佰萬", "拾萬", "萬", "仟", "佰", "拾", ""}; // 固定8位
        //3.錯誤處裡
        if (amount < 0 || amount > 99999999) {
            return "金額超出範圍";
        }
        //4.將金額轉為八位數字串，不足前面補0
        String amountStr = String.format("%08d", amount);
        //result 接結果
        StringBuilder result = new StringBuilder();
        // 是否開始輸出（略過萬位之前的0）
        boolean started = false;
        //從左到右處理
        for (int i = 0; i < 8; i++) {
            //取得當前位的數字 -0是因為本來是char char做減法 會轉成整數型別(int)
            int digit = amountStr.charAt(i) - '0';
            //對應的單位
            String unit = units[i];

            // 只要萬以下，強制開始輸出
            if (i >= 3) {
                started = true;
            }

            // 如果該位不是 0 或已經開始輸出，就印出該位
            if (digit != 0 || started) {
                result.append(chineseNumbers[digit]).append(unit);
                started = true;
            }
        }
        //補上元
        return result.toString() + "元";
    }
}