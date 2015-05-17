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
 * ����-���û���
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
		//����-��Ϩ��
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
	 * �����豸
	 * 
	 * @param address
	 */
	protected void connectDevice(String address) {
		if(mBluetoothService == null) {
			mBluetoothService = BluetoothService.getService(mMeasureHandler, true); // �첽��ʽ
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
	 * ע��-���������豸-�㲥
	 */
	protected void registerFindDeviceReceiver() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
		intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
		intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		// ע��㲥�����������ղ������������
		registerReceiver(mReceiver, intentFilter);
	}
	
	/**
	 * ע��-���������豸-�㲥
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
	 * �������豸
	 * @param device
	 */
	protected void onFoundDevice(BluetoothDevice device) {
	}
	
	/**
	 * �������
	 */
	protected void onFoundDeviceFinished() {
	}
	
	/**
	 * �Ƿ����������豸
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
					//����״̬����
					changeConnectonStatus(msg.arg1);
					break;
				}
				case BluetoothService.MESSAGE_WRITE:
					break;
				case BluetoothService.MESSAGE_READ:
				{
					//��ȡ״̬
					byte[] readBuf = (byte[]) msg.obj;
					CustomLog.v(TAG, "MeasureBaseActivity ... handleMessage() " + Arrays.toString(readBuf));
					parseData(readBuf); //��������
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
	 * ����״̬�ı�
	 * @param connectState
	 */
	protected void changeConnectonStatus(int connectState) {
	}

	/**
	 * ��������
	 */
	protected void parseData(byte[] data) {
	}
	
	@Override
	protected void onDestroy() {
		unRegisterFindDeviceReceiver();
		
		super.onDestroy();
	}
}
