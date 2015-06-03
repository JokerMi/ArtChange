package siat.ncu.press.util;

/**
 * 
 * @author joker
 *
 */
public class DataProcessUtil {
    /**
     * byte字节——> 16进制字符串显示（输出的字符串带0x）
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        StringBuilder mBulilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            mBulilder.append(v+"--");
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append("0x0");
            } else {
                stringBuilder.append("0x");
            }
            stringBuilder.append(hv).append("  ");
        }
        return stringBuilder.toString();
    }
    
    /**
     * 16进制字符串（输入的字符串不带0x） ——> byte字节数组
     * @param message
     * @return
     */
    public static byte[] hexStringToBytes(String message) {
        int len = message.length() / 2;
        char[] chars = message.toCharArray();
        String[] hexStr = new String[len];
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; j < len; i += 2, j++) {
            hexStr[j] = "" + chars[i] + chars[i + 1];
            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
        }
        return bytes;
    }
}
