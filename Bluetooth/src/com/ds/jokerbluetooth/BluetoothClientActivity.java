package com.ds.jokerbluetooth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ds.bluetoothUtil.TransmitBean;
import com.ds.jokerUtil.BlueTools;
import com.ds.jokerUtil.PingBluetoothUtils;

public class BluetoothClientActivity extends Activity implements OnClickListener{
    
    protected Button mButton;
    protected TextView mTextView;
    protected ListView mListView;
    protected ProgressBar mProgressBar;
    private EditText mEditText;
    private Button mSendButton;
    private EditText mSendEditText;
   
    
    protected BluetoothAdapter mBlueAdapter;
    protected BluetoothDevice mBlueDevice;
//    protected BluetoothSocket mClientSocket;
    protected UUID mUuid;
    
    protected ArrayAdapter<String> mAdapter;
    protected ArrayList<String> mArrayList;
    protected ArrayList<BluetoothDevice> mArrayDevice;
    protected boolean tFlag;
    
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_client);
        initData();
        initView();
        initEvent();
    }
    
    private BroadcastReceiver disBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
//                mTextView.setVisibility(View.VISIBLE);
//                mTextView.setText("没有查找到设备");
//                mListView.setVisibility(View.GONE);
                mProgressBar.setVisibility(View.GONE);
            }else if(BluetoothDevice.ACTION_FOUND.equals(action)){
                mBlueDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(mArrayList.indexOf(mBlueDevice.getName()) == -1) {
                    mArrayList.add(mBlueDevice.getName());
                    mArrayDevice.add(mBlueDevice);
                }
                mAdapter = new ArrayAdapter<String>(BluetoothClientActivity.this, android.R.layout.simple_list_item_1, mArrayList);
                mListView.setAdapter(mAdapter);
                mTextView.setVisibility(View.GONE);
                mListView.setVisibility(View.VISIBLE);
            }else if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                
            }
        }
    };
    
    protected void initData() {
        mUuid = BlueTools.MUUID;
        tFlag = true;
        mArrayList = new ArrayList<String>();
        mArrayDevice = new ArrayList<BluetoothDevice>();
        mBlueAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBlueAdapter != null) {
            mBlueAdapter.enable();
        }else {
            Toast.makeText(BluetoothClientActivity.this, "本机没有蓝牙", Toast.LENGTH_LONG).show();
        }
        
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mFilter.addAction(BluetoothDevice.ACTION_FOUND);
        mFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        registerReceiver(disBroadcast, mFilter);         
        
    }
    protected void initView() {
        mTextView = (TextView) findViewById(R.id.clientServersText);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mEditText =(EditText) findViewById(R.id.clientChatEditText);
        mSendEditText = (EditText) findViewById(R.id.clientSendEditText);
        
        mButton = (Button) findViewById(R.id.startSearchBtn);
        mSendButton = (Button) findViewById(R.id.clientSendMsgBtn);
        mButton.setOnClickListener(this);
        mSendButton.setOnClickListener(this);
        
        mListView = (ListView) findViewById(R.id.bluetoothList);
        mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                System.out.println("position :" + position);
                 ClientThread mClientThread = new ClientThread(mArrayDevice.get(position));
                 new Thread(mClientThread).start();
            }
        });
    }
    
    protected void initEvent() {
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.startSearchBtn:
                mProgressBar.setVisibility(View.VISIBLE);
                mBlueAdapter.startDiscovery();
                break;
            case R.id.clientSendMsgBtn:
                sendMsg();
                break;  
            default:
                break;
        }
    }
    public void sendMsg() {
        if("".equals(mSendEditText.getText().toString())) {
            Toast.makeText(BluetoothClientActivity.this, "发送的信息不能为空", Toast.LENGTH_LONG).show();
            return; 
        }
        TransmitBean mBean = new TransmitBean();
        mBean.setMsg(mSendEditText.getText().toString());
        if(mCommunicateThread != null) {
            Toast.makeText(BluetoothClientActivity.this, "数据发送成功", Toast.LENGTH_LONG).show();
            mCommunicateThread.Writer(mBean);
        }else {
            Toast.makeText(BluetoothClientActivity.this, "数据发送失败", Toast.LENGTH_LONG).show();
            return; 
        }
    }
    protected class ClientThread implements Runnable {
        private BluetoothSocket mClientSocket;
        private BluetoothDevice mDevice;
        
        public ClientThread(BluetoothDevice mDevice) {
            this.mDevice = mDevice;
        }
        
        public void run() {
                try {
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mClientSocket = mDevice.createRfcommSocketToServiceRecord(mUuid);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mClientSocket.connect();
                }
                catch (IOException e) {
                    e.printStackTrace();
                    try {
                        mClientSocket.close();
                    }
                    catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    MyHandler.obtainMessage(BlueTools.MESSAGE_CONNECTION_FIAL).sendToTarget();
                    return;
                }
                
                MyHandler.obtainMessage(BlueTools.MESSAGE_CONNECTION_SUCCESS, mClientSocket).sendToTarget();
            }
        }
    private BluetoothCommunicateThread mCommunicateThread;
    protected Handler MyHandler = new Handler() {
       public void handleMessage(Message msg) {
                switch (msg.what) {
                    case BlueTools.MESSAGE_CONNECTION_SUCCESS:
                        mTextView.setText("蓝牙通信连接成功");
                        mTextView.setVisibility(View.VISIBLE);
                        mListView.setVisibility(View.GONE);
                        mCommunicateThread = new BluetoothCommunicateThread((BluetoothSocket)msg.obj, MyHandler);
                        mCommunicateThread.start();
                        break;
                    case BlueTools.MESSAGE_CONNECTION_FIAL:
                        Toast.makeText(BluetoothClientActivity.this, "蓝牙通信连接失败", Toast.LENGTH_LONG).show();
                        break;
                    case BlueTools.MESSAGE_GET_OBJECT:
                        TransmitBean info = (TransmitBean)msg.obj;
                        mEditText.setText("From Server : "+info.getMsg()+" by "+new Date().toLocaleString()+"\n");
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
        unregisterReceiver(disBroadcast);
        if(mCommunicateThread != null) {
            mCommunicateThread.flag = false;
        }
    }
    public static boolean pair(String strPsw, BluetoothDevice device) {
        boolean result = false;
        String strAddr = device.getAddress();
 
        if (!BluetoothAdapter.checkBluetoothAddress(strAddr))
        { // 检查蓝牙地址是否有效
            Log.d("mylog", "devAdd un effient!");
        }
 
        if (device.getBondState() != BluetoothDevice.BOND_BONDED)
        {
            try
            {
                Log.d("mylog", "NOT BOND_BONDED");
                PingBluetoothUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
                PingBluetoothUtils.createBond(device.getClass(), device);
//                remoteDevice = device; // 配对完毕就把这个设备对象传给全局的remoteDevice
                result = true;
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
 
                Log.d("mylog", "setPiN failed!");
                e.printStackTrace();
            } //
 
        }
        else
        {
            Log.d("mylog", "HAS BOND_BONDED");
            try
            {
                PingBluetoothUtils.createBond(device.getClass(), device);
                PingBluetoothUtils.setPin(device.getClass(), device, strPsw); // 手机和蓝牙采集器配对
                PingBluetoothUtils.createBond(device.getClass(), device);
//                remoteDevice = device; // 如果绑定成功，就直接把这个设备对象传给全局的remoteDevice
                result = true;
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                Log.d("mylog", "setPiN failed!");
                e.printStackTrace();
            }
        }
        return result;
    }
}
