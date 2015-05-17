package com.ds.jokerbluetooth;

import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.bluetoothUtil.TransmitBean;
import com.ds.jokerUtil.BlueTools;

public class BluetoothServerActivity extends Activity {
    
    private TextView serverStateTextView;
    private EditText msgEditText;
    private EditText sendMsgEditText;
    private Button sendBtn;
    
    private BluetoothAdapter mBlueAdapter;
    private BluetoothServerSocket mServerSocket;
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_server);
        initData();
        initView();
        initEvent();
    }
    
    
    
    protected void initData() {
       mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
       mBlueAdapter.enable();
        
    }
    protected void initView() {
        serverStateTextView = (TextView)findViewById(R.id.serverStateText);
        serverStateTextView.setText("正在连接...");
        
        msgEditText = (EditText)findViewById(R.id.serverEditText);
        
        sendMsgEditText = (EditText)findViewById(R.id.serverSendEditText);
        
        sendBtn = (Button)findViewById(R.id.serverSendMsgBtn);
        sendBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if("".equals(sendMsgEditText.getText())) {
                    Toast.makeText(BluetoothServerActivity.this, "发送的信息不能为空", Toast.LENGTH_LONG).show();
                    return;
                }
                TransmitBean m = new TransmitBean();
                m.setMsg(sendMsgEditText.getText().toString());
                if(mCommunicateThread != null) {
                    mCommunicateThread.Writer(m);
                }else {
                    Toast.makeText(BluetoothServerActivity.this, "蓝牙通信没有连接", Toast.LENGTH_LONG).show();
                }
            }
        });
        
    }
    
    private ServerThread mServerThread;
    
    protected void initEvent() {
        if(mBlueAdapter != null) {
            mServerThread = new ServerThread();
            new Thread(mServerThread).start();
        }
    }
    
    public class ServerThread implements Runnable {
        public void run() {
            try {
                Thread.sleep(2000);//休眠2秒钟从而有时间让adapter开启
                mServerSocket = mBlueAdapter.listenUsingRfcommWithServiceRecord("server", BlueTools.MUUID);
                BluetoothSocket mSocket = mServerSocket.accept();
                if(mSocket != null) {
                    serverHandler.obtainMessage(BlueTools.MESSAGE_CONNECTION_SUCCESS, mSocket).sendToTarget();
                   }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
                serverHandler.obtainMessage(BlueTools.MESSAGE_CONNECTION_FIAL).sendToTarget();
                return;
            }finally {
                try {
                    mServerSocket.close();
                }
                catch (IOException e)  {
                    e.printStackTrace();
                }
            }
        }
        public void stop() {
           stop();
        }
    }
    private BluetoothCommunicateThread mCommunicateThread;
    
    public Handler serverHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BlueTools.MESSAGE_CONNECTION_SUCCESS:
                    serverStateTextView.setText("蓝牙通信连接成功");
                    mCommunicateThread = new BluetoothCommunicateThread((BluetoothSocket)msg.obj, serverHandler);
                    mCommunicateThread.start();
                    break;
                case BlueTools.MESSAGE_CONNECTION_FIAL:
                    Toast.makeText(BluetoothServerActivity.this, "服务器：socket连接失败", Toast.LENGTH_LONG).show();
                    break;
                case BlueTools.MESSAGE_GET_OBJECT:
                    TransmitBean info = (TransmitBean)msg.obj;
                    msgEditText.setText("From Client : "+info.getMsg()+" by "+new Date().toLocaleString()+"\n");
                    break;
                default:
                    break;
            }
     };
};
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBlueAdapter.disable();
        if(mCommunicateThread != null) {
            mCommunicateThread.flag = false;
        }
    }
    
}
