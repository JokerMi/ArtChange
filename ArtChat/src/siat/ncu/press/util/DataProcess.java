package siat.ncu.press.util;

import java.util.ArrayList;
import java.util.Arrays;

public class DataProcess {

   public static byte[] pressData =
    {(byte) 0x81, (byte) 0x7f, (byte) 0x14, (byte) 0x06, (byte) 0x94, (byte) 0x00, (byte) 0x3B, (byte) 0x0D,
            (byte) 0xA3, (byte) 0x7F, (byte) 0x66, (byte) 0x32, (byte) 0xF5, (byte) 0x0B, (byte) 0x45, (byte) 0x36};
    
  /*  public static byte[] pressData =
        {(byte) 0x81, (byte) 0x7f, (byte) 0x14, (byte) 0x06};
    */
    private ArrayList<String> totalValue = new ArrayList<String>();
    private static final int SIZE = 4;
    private static final String ZERO="00000000";
    /**
     * @param args
     */
    public static void main(String[] args) {
        String bString = "00001101";  
        System.out.println(binaryString2hexString(bString));  
        DataProcess dp = new DataProcess();
        dp.processReadData(pressData);
        for(String dd:dp.totalValue) {
            System.out.println(dp.totalValue);
        }
    }
    
    /**
     * 将一个字节转换成一个8比特的字符串
     * @param data
     * @return
     */
    public String getBinaryStr(byte data) {
        
        String s = Integer.toBinaryString(data);
        if(s.length() > 8) {
            s = s.substring(s.length() - 8);
        }else if(s.length() < 8) {
            s = ZERO.substring(s.length()) + s;
        }
        return s;
        
        /*for   (int i = 0;i<bs.length;i++)   {
            System.out.println(bs[i]+" ");
            String   s   =   Integer.toBinaryString(bs[i]);
            if   (s.length()   >
            8)   {
                s   =   s.substring(s.length()   -   8);
            }   else   if   (s.length()  
                    <   8)   {
                s   =   ZERO.substring(s.length())   +   s;
            }
            System.out.println(s);
        }*/
    }
    
    protected void processReadData(byte[] data) {
        char low3 = '0';
        char low2 = '0';
        char low1 = '0';
        //将byte类型数据转换成二进制字符串
        String dataStr = "";
        //存放二进制的字符数组
        char[] dataChar; 
        //存放添加了高位的数据段的二进制字符串
        String postDataStr = "0x";
        
        if(data == null) {
            return;
        }else {
            totalValue.clear();
            for(int i = 0;i < data.length;i++) {
                dataStr = getBinaryStr(data[i]);
                dataChar = dataStr.toCharArray();
                /*System.out.println(dataChar.length+" ");
                for(int j = 0;j < dataChar.length;j++){
                    System.out.print(dataChar[j]+" ");
                }*/
                switch (i%SIZE) {
                    case 0:
                        if(dataChar[1] == '0') {
                            low1 = dataChar[5];
                            low2 = dataChar[6];
                            low3 = dataChar[7];
                            totalValue.add(postDataStr);
                            postDataStr = "0x";
                        }else if(dataChar[1] == '1') {
                            totalValue.add(postDataStr);
                            
                            break;
                        }
                        break;
                    case 1:
                        dataChar[0] = low1;
                        
                        String newStr1 = new String(dataChar);
                        System.out.println(newStr1);
                        postDataStr += binaryString2hexString(newStr1);
                        break;
                    case 2:
                        dataChar[0] = low2;
                        String newStr2 = new String(dataChar);
                        System.out.println(newStr2);
                        postDataStr += binaryString2hexString(newStr2);
                        break;
                    case 3:
                        dataChar[0] = low3;
                        String newStr3 = new String(dataChar);
                        System.out.println(newStr3);
                        postDataStr += binaryString2hexString(newStr3);
                        break;
                    default:
                        break;
                }
                System.out.println(postDataStr);
            }
        }
        
    }
    
    public static String binaryString2hexString(String bString) {  
        if (bString == null || bString.equals("") || bString.length() % 8 != 0)  
            return null;  
        StringBuffer tmp = new StringBuffer();  
        int iTmp = 0;  
        for (int i = 0; i < bString.length(); i += 4)  
        {  
            iTmp = 0;  
            for (int j = 0; j < 4; j++)  
            {  
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);  
            }  
            tmp.append(Integer.toHexString(iTmp));  
        }  
        return tmp.toString();  
    }  
    
    
    public byte getToken(byte[] data) {
        byte token = (byte) (data[2] & 0xff);
        System.out.println(" token" + new Byte(token).toString());
        return token;
        // return (byte) (data[2] & 0xff);// 第3个字节表示令牌
    }

    /**
     * 
     * 
     * @param data
     * @return
     */
    public int getCurrentBp(byte[] data) {
        // 包头（2） 令牌（1） 长度（1） 类型（1） 数据（2） 校验和 （1）
        System.out.println(((data[5] & 0xf) << 8) + (data[6] & 0xff)+"");
        return ((data[5] & 0xf) << 8) + (data[6] & 0xff);
    }
}
