package siat.ncu.press.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.commlibrary.android.utils.CustomLog;

/**
 * 
 * @author tanger
 *
 */
public class HealthDevice {
	/**
	 * 将收到的多条数据根据头标志分割，
	 * 
	 * @param buffer
	 * @return
	 */
	public static List<byte[]> splitBufferData(byte[] buffer, byte[] head) {
		List<byte[]> datas = new ArrayList<byte[]>();
		// 每条独立的数据的前五个字节大小是固定的,最短的数据为6个字节
		int low = 0, high = buffer.length;// 初始假设只有一条数据

		for (int i = 2; i < buffer.length; i++) {
			if (buffer[i - 1] == head[0] && buffer[i] == head[1]) {
				high = i - 1;
				datas.add(ParseDeviceDataManager.copyOfRange(buffer, low, high));
				low = high;
			}
		}
		datas.add(ParseDeviceDataManager.copyOfRange(buffer, low, buffer.length));// 最后一条数据
		return datas;
	}

	/**
	 * 
	 * 持续传递命令，以让蓝牙保持连接
	 * 
	 */
	public static class PersistWriter extends Thread {
		private BluetoothService bluetoothService;
		private static final String TAG = "PC300.PersistWriter";
		private byte[] command;// 传递的命令
		private long sleepTime;// 间隔时间

		public PersistWriter(BluetoothService bluetoothService, byte[] command,
				long sleepTime) {
			this.bluetoothService = bluetoothService;
			this.command = command;
			this.sleepTime = sleepTime;
		}

		@Override
		public void run() {
			while (true) {
				if (bluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
				    CustomLog.i(TAG, "send persist command");
					bluetoothService.write(command);// 电量命令
				}
				try {
					TimeUnit.MILLISECONDS.sleep(sleepTime);// 每隔一段时间发送一次电量查询命令
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
