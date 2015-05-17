package siat.ncu.press.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import com.commlibrary.android.utils.CustomLog;

/**
 * 
 * @author tanger
 * 
 */
public class BluetoothUtils {
	/**
	 * ���豸��� �ο�Դ�룺platform/packages/apps/Settings.git
	 * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
	 */
	static public boolean createBond(Class btClass, BluetoothDevice btDevice) {
		Method createBondMethod;
		try {
			createBondMethod = btClass.getMethod("createBond");
			Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
			
			return returnValue.booleanValue();
		} 
		catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		catch (IllegalArgumentException e) {
			e.printStackTrace();
		} 
		catch (IllegalAccessException e) {
			e.printStackTrace();
		} 
		catch (InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return false;
	}

	/**
	 * ���豸������ �ο�Դ�룺platform/packages/apps/Settings.git
	 * /Settings/src/com/android/settings/bluetooth/CachedBluetoothDevice.java
	 */
	static public boolean removeBond(Class btClass, BluetoothDevice btDevice)
			throws Exception {
		Method removeBondMethod = btClass.getMethod("removeBond");
		Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice);
		return returnValue.booleanValue();
	}

	static public boolean setPin(Class btClass, BluetoothDevice btDevice,
			String str) {
		try {
			Method removeBondMethod = btClass.getDeclaredMethod("setPin",
					new Class[] { byte[].class });
			Boolean returnValue = (Boolean) removeBondMethod.invoke(btDevice,
					new Object[] { str.getBytes() });
			CustomLog.e("returnValue", "" + returnValue);
		} 
		catch (SecurityException e) {
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return true;

	}

	// ȡ���û�����
	static public boolean cancelPairingUserInput(Class btClass,
			BluetoothDevice device) {
		try {
			Method createBondMethod = btClass.getMethod("cancelPairingUserInput");
			Boolean returnValue = (Boolean) createBondMethod.invoke(device);
			
			return returnValue.booleanValue();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}

	// ȡ�����
	static public boolean cancelBondProcess(Class btClass,
			BluetoothDevice device)

	throws Exception {
		Method createBondMethod = btClass.getMethod("cancelBondProcess");
		Boolean returnValue = (Boolean) createBondMethod.invoke(device);
		return returnValue.booleanValue();
	}

	/**
	 * 
	 * @param clsShow
	 */
	static public void printAllInform(Class clsShow) {
		try {
			// ȡ�����з���
			Method[] hideMethod = clsShow.getMethods();
			int i = 0;
			for (; i < hideMethod.length; i++) {
				CustomLog.e("method name", hideMethod[i].getName() + ";and the i is:"
						+ i);
			}
			// ȡ�����г���
			Field[] allFields = clsShow.getFields();
			for (i = 0; i < allFields.length; i++) {
				CustomLog.e("Field name", allFields[i].getName());
			}
		} 
		catch (SecurityException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} 
		catch (IllegalArgumentException e) {
			// throw new RuntimeException(e.getMessage());
			e.printStackTrace();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 
	 * @param strAddr
	 * @param strPsw
	 * @return
	 */
	public static boolean pair(String strAddr, String strPsw) {
		boolean result = false;
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		bluetoothAdapter.cancelDiscovery();

		if (!bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.enable();
		}

		if (!BluetoothAdapter.checkBluetoothAddress(strAddr)) { // ���������ַ�Ƿ���Ч
			CustomLog.d("mylog", "devAdd un effient!");
		}

		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(strAddr);

		if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
			try {
				CustomLog.d("mylog", "NOT BOND_BONDED");
				setPin(device.getClass(), device, strPsw); // �ֻ��������ɼ������
				createBond(device.getClass(), device);
				result = true;
			} 
			catch (Exception e) {
				e.printStackTrace();
			} //

		} 
		else {
			try {
				createBond(device.getClass(), device);
				setPin(device.getClass(), device, strPsw); // �ֻ��������ɼ������
				createBond(device.getClass(), device);
				result = true;
			} 
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;
	}
}
