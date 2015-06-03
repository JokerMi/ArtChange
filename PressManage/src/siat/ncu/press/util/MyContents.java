package siat.ncu.press.util;

import java.util.UUID;

/**
 * 应用相关的常量
 * @author joker
 *
 */
public final class MyContents {

    public interface BlueTools {
        
        public static final String BLUETOOTH_NAME = "SIAT_PM_2";
        public static final String BLUETOOTH_PIN = "1234";
//      public static final String BLUETOOTH_NAME = "HC-06/5";
        
        public static final UUID MUUID = UUID.fromString("0f3561b9-bda5-4672-84ff-ab1f98e349b6");
            
        public static final int MESSAGE_CONNECTION_FIAL = 0;//蓝牙连接失败
        
        public static final int MESSAGE_CONNECTION_SUCCESS = 1;//连接成功
        
        public static final int MESSAGE_GET_OBJECT = 2; //蓝牙之间通信获取信息
        
        public static final String DATA = "data"; 
    }

    
    public interface PressDataContents {
        //单位是kg
        public static final int MIN_NORMAL_PRESS_VALUE = 0; 
        public static final int MAX_NORMAL_PRESS_VALUE = 20;  //最大压力值
        public static final double COMMON_DENOMINATOR = 4194303; //求压力值的方程式分母
        public static final double MOLECULE = 8388608;//求压力值的方程式分子
        public static final double MAX_ELECTRIC_VALUE = 4.2;//电量最大值
        public static final double MIN_ELECTRIC_VALUE = 0;//电量最小值
        public static final double ELECTRIC_DENOMINATOR = 1023; //求电量值的方程式分母
        public static final int NORMAL_PRESS_VALUE = 100;
    }
    

}
