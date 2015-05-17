package com.ds.bluetooth;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.bluetoothUtil.BluetoothClientService;
import com.ds.bluetoothUtil.BluetoothTools;
import com.ds.bluetoothUtil.TransmitBean;
import com.ds.jokerbluetooth.R;

public class ClientActivity extends Activity {

	private TextView serversText;
	private EditText chatEditText;
	private EditText sendEditText;
	private Button sendBtn;
	private Button startSearchBtn;
	private Button selectDeviceBtn;
	
	private List<BluetoothDevice> deviceList = new ArrayList<BluetoothDevice>();
	
	//广播接收器
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if (BluetoothTools.ACTION_NOT_FOUND_SERVER.equals(action)) {
				//未发现设备
				serversText.append("not found device\r\n");
				
			} else if (BluetoothTools.ACTION_FOUND_DEVICE.equals(action)) {
				//获取到设备对象
				BluetoothDevice device = (BluetoothDevice)intent.getExtras().get(BluetoothTools.DEVICE);
				deviceList.add(device);
				serversText.append(device.getName() + "\r\n");
				
			} else if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)) {
				//连接成功
				serversText.append("连接成功");
				sendBtn.setEnabled(true);
				
			} else if (BluetoothTools.ACTION_DATA_TO_GAME.equals(action)) {
				//接收数据
				TransmitBean data = (TransmitBean)intent.getExtras().getSerializable(BluetoothTools.DATA);
				String msg = "from remote " + new Date().toLocaleString() + " :\r\n" + data.getMsg() + "\r\n";
				chatEditText.append(msg);
			
			} 
		}
	};
	
	
	@Override
	protected void onStart() {
		//清空设备列表
		deviceList.clear();
		
		//开启后台service
		Intent startService = new Intent(ClientActivity.this, BluetoothClientService.class);
		startService(startService);
		
		//注册BoradcasrReceiver
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothTools.ACTION_NOT_FOUND_SERVER);
		intentFilter.addAction(BluetoothTools.ACTION_FOUND_DEVICE);
		intentFilter.addAction(BluetoothTools.ACTION_DATA_TO_GAME);
		intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);
		
		registerReceiver(broadcastReceiver, intentFilter);
		
		super.onStart();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.client);
		
		serversText = (TextView)findViewById(R.id.clientServersText);
		chatEditText = (EditText)findViewById(R.id.clientChatEditText);
		sendEditText = (EditText)findViewById(R.id.clientSendEditText);
		sendBtn = (Button)findViewById(R.id.clientSendMsgBtn);
		startSearchBtn = (Button)findViewById(R.id.startSearchBtn);
		selectDeviceBtn = (Button)findViewById(R.id.selectDeviceBtn);
		
		sendBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//发送消息
				if ("".equals(sendEditText.getText().toString().trim())) {
					Toast.makeText(ClientActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
				} else {
					//发送消息
					TransmitBean data = new TransmitBean();
					data.setMsg(sendEditText.getText().toString());
					Intent sendDataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_SERVICE);
					sendDataIntent.putExtra(BluetoothTools.DATA, data);
					sendBroadcast(sendDataIntent);
				}
			}
		});
		
		startSearchBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//开始搜索
				Intent startSearchIntent = new Intent(BluetoothTools.ACTION_START_DISCOVERY);
				sendBroadcast(startSearchIntent);
			}
		});
		
		selectDeviceBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//选择第一个设备
				Intent selectDeviceIntent = new Intent(BluetoothTools.ACTION_SELECTED_DEVICE);
				selectDeviceIntent.putExtra(BluetoothTools.DEVICE, deviceList.get(0));
				sendBroadcast(selectDeviceIntent);
			}
		});
	}

	@Override
	protected void onStop() {
		//关闭后台Service
		Intent startService = new Intent(BluetoothTools.ACTION_STOP_SERVICE);
		sendBroadcast(startService);
		
		unregisterReceiver(broadcastReceiver);
		super.onStop();
	}
}
