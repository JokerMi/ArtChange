package com.ds.bluetooth;

import java.util.Date;

import android.app.Activity;
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

import com.ds.bluetoothUtil.BluetoothServerService;
import com.ds.bluetoothUtil.BluetoothTools;
import com.ds.bluetoothUtil.TransmitBean;
import com.ds.jokerbluetooth.R;

public class ServerActivity extends Activity {

	private TextView serverStateTextView;
	private EditText msgEditText;
	private EditText sendMsgEditText;
	private Button sendBtn;
	
	//广播接收器
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (BluetoothTools.ACTION_DATA_TO_GAME.equals(action)) {
				//接收数据
				TransmitBean data = (TransmitBean)intent.getExtras().getSerializable(BluetoothTools.DATA);
				String msg = "from remote " + new Date().toLocaleString() + " :\r\n" + data.getMsg() + "\r\n";
				msgEditText.append(msg);
			} else if (BluetoothTools.ACTION_CONNECT_SUCCESS.equals(action)) {
				//连接成功
				serverStateTextView.setText("连接成功");
				sendBtn.setEnabled(true);
			}
			
		}
	};
	
	@Override
	protected void onStart() {
		//开启后台service
		Intent startService = new Intent(ServerActivity.this, BluetoothServerService.class);
		startService(startService);
		
		//注册BoradcasrReceiver
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BluetoothTools.ACTION_DATA_TO_GAME);
		intentFilter.addAction(BluetoothTools.ACTION_CONNECT_SUCCESS);
		
		registerReceiver(broadcastReceiver, intentFilter);
		super.onStart();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server);
		
		serverStateTextView = (TextView)findViewById(R.id.serverStateText);
		serverStateTextView.setText("正在连接...");
		
		msgEditText = (EditText)findViewById(R.id.serverEditText);
		
		sendMsgEditText = (EditText)findViewById(R.id.serverSendEditText);
		
		sendBtn = (Button)findViewById(R.id.serverSendMsgBtn);
		sendBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if ("".equals(sendMsgEditText.getText().toString().trim())) {
					Toast.makeText(ServerActivity.this, "输入不能为空", Toast.LENGTH_SHORT).show();
				} else {
					//发送消息
					TransmitBean data = new TransmitBean();
					data.setMsg(sendMsgEditText.getText().toString());
					Intent sendDataIntent = new Intent(BluetoothTools.ACTION_DATA_TO_SERVICE);
					sendDataIntent.putExtra(BluetoothTools.DATA, data);
					sendBroadcast(sendDataIntent);
				}
			}
		});
		
		sendBtn.setEnabled(false);
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
