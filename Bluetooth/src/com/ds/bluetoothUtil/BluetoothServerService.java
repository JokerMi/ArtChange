package com.ds.bluetoothUtil;

import java.io.Serializable;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

/**
 * 蓝牙模块服务器端主控制Service
 * @author GuoDong
 *
 */
public class BluetoothServerService extends Service {

	//蓝牙适配器
	private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	
	//蓝牙通讯线程
	private BluetoothCommunThread communThread;
	
	//控制信息广播接收器
	private BroadcastReceiver controlReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (BluetoothTools.ACTION_STOP_SERVICE.equals(action)) {
				//停止后台服务
				if (communThread != null) {
					communThread.isRun = false;
				}
				stopSelf();
				
			} else if (BluetoothTools.ACTION_DATA_TO_SERVICE.equals(action)) {
				//发送数据
				Object data = intent.getSerializableExtra(BluetoothTools.DATA);
				if (communThread != null) {
					communThread.writeObject(data);
				}
				
			}
		}
	};
	
	//接收其他线程消息的Handler
	private Handler serviceHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			case BluetoothTools.MESSAGE_CONNECT_SUCCESS:
				//连接成功
				//开启通讯线程
				communThread = new BluetoothCommunThread(serviceHandler, (BluetoothSocket)msg.obj);
				communThread.start();
				
				//发送连接成功消息
				Intent connSuccIntent = new Intent(BluetoothTools.ACTION_CONNECT_SUCCESS);
				sendBroadcast(connSuccIntent);
				break;
				
			case BluetoothTools.MESSAGE_CONNECT_ERROR:
				//连接错误
				//发送连接错误广播
				Intent errorIntent = new Intent(BluetoothTools.ACTION_CONNECT_ERROR);
				sendBroadcast(errorIntent);
				break;
				
			case BluetoothTools.MESSAGE_READ_OBJECT:
				//读取到数据
				//发送数据广播（包含数据对象）
				Intent dataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_GAME);
				dataIntent.putExtra(BluetoothTools.DATA, (Serializable)msg.obj);
				sendBroadcast(dataIntent);
				
				break;
			}
			
			super.handleMessage(msg);
		}
		
	};
	
	/**
	 * 获取通讯线程
	 * @return
	 */
	public BluetoothCommunThread getBluetoothCommunThread() {
		return communThread;
	}
	
	@Override
	public void onCreate() {
		//ControlReceiver的IntentFilter
		IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(BluetoothTools.ACTION_START_SERVER);
		controlFilter.addAction(BluetoothTools.ACTION_STOP_SERVICE);
		controlFilter.addAction(BluetoothTools.ACTION_DATA_TO_SERVICE);
		
		//注册BroadcastReceiver
		registerReceiver(controlReceiver, controlFilter);
		
		//开启服务器
		bluetoothAdapter.enable();	//打开蓝牙
		//开启蓝牙发现功能（300秒）
		Intent discoveryIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoveryIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		discoveryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(discoveryIntent);
		//开启后台连接线程
		new BluetoothServerConnThread(serviceHandler).start();
		
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		if (communThread != null) {
			communThread.isRun = false;
		}
		unregisterReceiver(controlReceiver);
		super.onDestroy();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

}
