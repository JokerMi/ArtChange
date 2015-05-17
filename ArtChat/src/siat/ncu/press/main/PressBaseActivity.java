package siat.ncu.press.main;

import java.util.Arrays;

import siat.ncu.press.util.BluetoothService;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.WindowManager;

import com.commlibrary.android.utils.CustomLog;
import com.commlibrary.android.utils.ToastManager;

/**
 * 检测的-公用基类
 * 
 * @author tanger
 *
 */
public abstract class PressBaseActivity extends Activity {
	public static final String TAG = "txl";
	
	protected String mMeasureCardNo; //
	
	protected String mLatitude;
	protected String mLongitude;
	//
	protected BluetoothService mBluetoothService;
	protected boolean isHaveMeasureData;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//设置-不熄屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//
		initBaseMeasureDatas();
	}
	
	private void initBaseMeasureDatas() {
		//
		//
		registerFindDeviceReceiver();
	}
	
	/**
	 * 连接设备
	 * 
	 * @param address
	 */
	protected void connectDevice(String address) {
		if(mBluetoothService == null) {
			mBluetoothService = BluetoothService.getService(mMeasureHandler, true); // 异步方式
		}
		
		if(TextUtils.isEmpty(address)) {
			doFoundDevice();
		}
		else {
			mBluetoothService.connect(address);
		}
	}
	
	/**
	 * 
	 */
	protected void doFoundDevice() {
		CustomLog.v("txl", "MeasureBaseActivity ... doFoundDevice()");
		mBluetoothService.doFindDevice();
	}
	
	protected void doStopFoundDevice() {
		mBluetoothService.stopFindDevice();
	}
	
	/**
	 * 注册-搜索蓝牙设备-广播
	 */
	protected void registerFindDeviceReceiver() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		// 注册广播接收器，接收并处理搜索结果
		registerReceiver(mReceiver, intentFilter);
	}
	
	/**
	 * 注销-搜索蓝牙设备-广播
	 */
	protected void unRegisterFindDeviceReceiver() {
		unregisterReceiver(mReceiver);
	}
	
	/**
	 * 
	 */
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// When discovery finds a device
				// Get the BluetoothDevice object from the Intent
				final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				onFoundDevice(device);
			} 
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				// When discovery is finished
				onFoundDeviceFinished();
			}
		}
	};
	
	/**
	 * 搜索到设备
	 * @param device
	 */
	protected void onFoundDevice(BluetoothDevice device) {
	}
	
	/**
	 * 搜索完成
	 */
	protected void onFoundDeviceFinished() {
	}
	
	/**
	 * 是否连接蓝牙设备
	 * @return
	 */
	protected boolean isConnected(){
		if(mBluetoothService != null 
				&& mBluetoothService.getState() == BluetoothService.STATE_CONNECTED){
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 */
	protected final Handler mMeasureHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case BluetoothService.MESSAGE_STATE_CHANGE:
				{
					//连接状态更改
					changeConnectonStatus(msg.arg1);
					break;
				}
				case BluetoothService.MESSAGE_WRITE:
					break;
				case BluetoothService.MESSAGE_READ:
				{
					//读取状态
					byte[] readBuf = (byte[]) msg.obj;
					CustomLog.v(TAG, "MeasureBaseActivity ... handleMessage() " + Arrays.toString(readBuf));
					parseData(readBuf); //解析数据
					break;
				}
				case BluetoothService.MESSAGE_TOAST:
					ToastManager.showToast(getApplicationContext(), msg.getData().getString(BluetoothService.TOAST));
					break;
				case BluetoothService.MESSAGE_DEVICE:
					String deviceName = msg.getData().getString(BluetoothService.DEVICE_NAME);
					String deviceAddress = msg.getData().getString(
							BluetoothService.DEVICE_ADDRESS);
					CustomLog.v(TAG, "MeasureBaseActivity ... handleMessage() deviceName=" + deviceName
							+ ", deviceAddress=" + deviceAddress);
					break;
			}
		}
	};
	
	
	/**
	 * 连接状态改变
	 * @param connectState
	 */
	protected void changeConnectonStatus(int connectState) {
	}

	/**
	 * 解析数据
	 */
	protected void parseData(byte[] data) {
	}
	
	@Override
	protected void onDestroy() {
		unRegisterFindDeviceReceiver();
		
		super.onDestroy();
	}
}
