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
	 * 与设备配对 参考源码：platform/packages/apps/Settings.git
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
	 * 与设备解除配对 参考源码：platform/packages/apps/Settings.git
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

	// 取消用户输入
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

	// 取消配对
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
			// 取得所有方法
			Method[] hideMethod = clsShow.getMethods();
			int i = 0;
			for (; i < hideMethod.length; i++) {
				CustomLog.e("method name", hideMethod[i].getName() + ";and the i is:"
						+ i);
			}
			// 取得所有常量
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

		if (!BluetoothAdapter.checkBluetoothAddress(strAddr)) { // 检查蓝牙地址是否有效
			CustomLog.d("mylog", "devAdd un effient!");
		}

		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(strAddr);

		if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
			try {
				CustomLog.d("mylog", "NOT BOND_BONDED");
				setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
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
				setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
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
