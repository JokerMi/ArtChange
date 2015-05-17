package siat.ncu.press.util;

import java.util.UUID;

/**
 * 应用相关的常量
 * @author joker
 *
 */
public final class MyContents {

    public interface BlueTools {
        
        public static final String BLUETOOTH_NAME = "Nexus S";
//      public static final String BLUETOOTH_NAME = "HC-06/5";
        
        public static final UUID MUUID = UUID.fromString("0f3561b9-bda5-4672-84ff-ab1f98e349b6");
            
        public static final int MESSAGE_CONNECTION_FIAL = 0;//蓝牙连接失败
        
        public static final int MESSAGE_CONNECTION_SUCCESS = 1;//连接成功
        
        public static final int MESSAGE_GET_OBJECT = 2; //蓝牙之间通信获取信息
        
        public static final String DATA = "data"; 
    }

    
    public interface PressDataRange {
        //单位是kg
        public static final int MIN_NORMAL_PRESS_VALUE = 0; 
        public static final int MAX_NORMAL_PRESS_VALUE = 20;
        public static final int NORMAL_PRESS_VALUE = 100;
    }
    

}
