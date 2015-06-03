package siat.ncu.press.util;

import java.util.UUID;

/**
 * Ӧ����صĳ���
 * @author joker
 *
 */
public final class MyContents {

    public interface BlueTools {
        
        public static final String BLUETOOTH_NAME = "SIAT_PM_2";
        public static final String BLUETOOTH_PIN = "1234";
//      public static final String BLUETOOTH_NAME = "HC-06/5";
        
        public static final UUID MUUID = UUID.fromString("0f3561b9-bda5-4672-84ff-ab1f98e349b6");
            
        public static final int MESSAGE_CONNECTION_FIAL = 0;//��������ʧ��
        
        public static final int MESSAGE_CONNECTION_SUCCESS = 1;//���ӳɹ�
        
        public static final int MESSAGE_GET_OBJECT = 2; //����֮��ͨ�Ż�ȡ��Ϣ
        
        public static final String DATA = "data"; 
    }

    
    public interface PressDataContents {
        //��λ��kg
        public static final int MIN_NORMAL_PRESS_VALUE = 0; 
        public static final int MAX_NORMAL_PRESS_VALUE = 20;  //���ѹ��ֵ
        public static final double COMMON_DENOMINATOR = 4194303; //��ѹ��ֵ�ķ���ʽ��ĸ
        public static final double MOLECULE = 8388608;//��ѹ��ֵ�ķ���ʽ����
        public static final double MAX_ELECTRIC_VALUE = 4.2;//�������ֵ
        public static final double MIN_ELECTRIC_VALUE = 0;//������Сֵ
        public static final double ELECTRIC_DENOMINATOR = 1023; //�����ֵ�ķ���ʽ��ĸ
        public static final int NORMAL_PRESS_VALUE = 100;
    }
    

}
