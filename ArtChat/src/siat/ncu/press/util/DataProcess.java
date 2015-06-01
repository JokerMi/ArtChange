package siat.ncu.press.util;

import java.sql.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import android.R.array;
import android.test.MoreAsserts;

import siat.ncu.press.util.MyContents.PressDataContents;

public class DataProcess {

  /* public static byte[] pressData =
    {       (byte) 0x81, (byte) 0x7f, (byte) 0x14, (byte) 0x06, (byte) 0x94, (byte) 0x00, (byte) 0x3B, (byte) 0x0D,
            (byte) 0xA3, (byte) 0x7F, (byte) 0x66, (byte) 0x32, (byte) 0xF5, (byte) 0x0B, (byte) 0x45, (byte) 0x36,
            (byte) 0x81, (byte) 0x7f, (byte) 0x14, (byte) 0x06, (byte) 0x94, (byte) 0x00, (byte) 0x3B, (byte) 0x0D,
            (byte) 0xA3, (byte) 0x7F, (byte) 0x66, (byte) 0x32, (byte) 0xF5, (byte) 0x0B, (byte) 0x45, (byte) 0x36};*/
   
   
   public static byte[] pressData =
       {       (byte) 0x81, (byte) 0x7f, (byte) 0x14, (byte) 0x06, (byte) 0x94, (byte) 0x00, (byte) 0x3B, (byte) 0x0D,
               (byte) 0xA3, (byte) 0x7F, (byte) 0x66, (byte) 0x32, (byte) 0xF5, (byte) 0x0B, (byte) 0x45, (byte) 0x36,
               (byte) 0x81, (byte) 0x7f, (byte) 0x14, (byte) 0x06, (byte) 0x94, (byte) 0x00, (byte) 0x3B, (byte) 0x0D,
               (byte) 0xA3, (byte) 0x7F, (byte) 0x66, (byte) 0x32, (byte) 0xF5, (byte) 0x0B, (byte) 0x45, (byte) 0x36};
    
  /*  public static byte[] pressData =
        {(byte) 0x81, (byte) 0x7f, (byte) 0x14, (byte) 0x06};
    */
    private ArrayList<String> resultValue = new ArrayList<String>();
    private static final int SIZE = 4;
    private static final String ZERO="00000000";
    /**
     * @param args
     */
    public static void main(String[] args) {
        String bString = "00001101";  
        System.out.println(binaryString2hexString(bString));  
        DataProcess dp = new DataProcess();
        dp.resultValue =  dp.processReadData(pressData);
       /* for(int i = 0;i <dp.totalValue.size();i ++){
            if("0x".equals(dp.totalValue.get(i))) {
                dp.totalValue.remove(i);
            }
        }*/
       
        System.out.println(dp.resultValue + "  ");
        System.out.println(Arrays.toString(pressData));
    }
    
    /**
     * ��ȡѹ��ֵ��������λС��
     * @param value
     * @return
     */
    public static double countPressValue(int value) {
        
        DecimalFormat df = new DecimalFormat("#.#####");
        double pressValue = PressDataContents.MAX_NORMAL_PRESS_VALUE *(value - PressDataContents.MOLECULE)/PressDataContents.COMMON_DENOMINATOR;
        pressValue = Double.parseDouble(df.format(pressValue));
        return pressValue;
    }
    /**
     * ��ȡ����ֵ������1λС��
     * @param value
     * @return
     */
    public static double countElectricValue(int value) {
        DecimalFormat df = new DecimalFormat("#.#");
        double electricValue = (PressDataContents.MAX_ELECTRIC_VALUE * value)/PressDataContents.ELECTRIC_DENOMINATOR;
        electricValue = Double.parseDouble(df.format(electricValue));
        return electricValue;
    }
    /**
     * ��һ���ֽ�ת����һ��8���ص��ַ���
     * @param data
     * @return
     */
    public static String getBinaryStr(byte data) {
        
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
    
    public static ArrayList<String> processReadData(byte[] data) {
        ArrayList<String> totalValue = new ArrayList<String>();
        char low3 = '0';
        char low2 = '0';
        char low1 = '0';
        //��byte��������ת���ɶ������ַ���
        String dataStr = "";
        //��Ŷ����Ƶ��ַ�����
        char[] dataChar; 
        //�������˸�λ�����ݶεĶ������ַ���
        String postDataStr = "0x";
        
        if(data == null) {
            return null;
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
                            low1 = dataChar[5];
                            low2 = dataChar[6];
                            low3 = dataChar[7];
                            postDataStr = "";
                            //Ϊ���ж��ǵ���ֵ����0x
                            if(dataChar[1] == '1' ) {
                                postDataStr = "0x";   
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
                        totalValue.add(postDataStr);
                        break;
                    default:
                        break;
                }
            }
            return totalValue;
        }
        
    }
    
    /**
     * ���M�u�ַ����D����16�M���ַ���
     * @param bString
     * @return
     */
    public static String binaryString2hexString(String bString) {  
        if (bString == null || bString.equals("") || bString.length() % 8 != 0)  
            return null;  
        StringBuffer tmp = new StringBuffer();  
        int iTmp = 0;  
        for (int i = 0; i < bString.length(); i += 4) {  
            iTmp = 0;  
            for (int j = 0; j < 4; j++) {  
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
        // return (byte) (data[2] & 0xff);// ��3���ֽڱ�ʾ����
    }

    /**
     * 
     * 
     * @param data
     * @return
     */
    public int getCurrentBp(byte[] data) {
        // ��ͷ��2�� ���ƣ�1�� ���ȣ�1�� ���ͣ�1�� ���ݣ�2�� У��� ��1��
        System.out.println(((data[5] & 0xf) << 8) + (data[6] & 0xff)+"");
        return ((data[5] & 0xf) << 8) + (data[6] & 0xff);
    }

    /**
     * byte�ֽڡ���> 16�����ַ�����ʾ��������ַ�����0x��
     * @param src
     * @return
     */
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append("0x");
        StringBuilder mBulilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            mBulilder.append(v+"--");
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append("0");
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    
    /**
     * 16�����ַ�����������ַ�������0x�� ����> byte�ֽ�����
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
