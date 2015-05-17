package com.ds.jokerbluetooth;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Writer;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;

import com.ds.bluetoothUtil.TransmitBean;
import com.ds.jokerUtil.BlueTools;
	
/**
 * 蓝牙通信线程
 * @author funan
 *
 */
public class BluetoothCommunicateThread extends Thread {
    protected ObjectInputStream mInputStream;
    protected ObjectOutputStream mOutputStream;
    protected BluetoothSocket mBluetoothSocket;
    protected Handler mHandler;
    public boolean flag = true;   //运行标志位
    
    public BluetoothCommunicateThread() {
    }
    
    public BluetoothCommunicateThread(BluetoothSocket mSocket, Handler m) {
        mBluetoothSocket = mSocket;
        mHandler = m;
        try {
            mOutputStream =new ObjectOutputStream(mBluetoothSocket.getOutputStream());
            mInputStream = new ObjectInputStream(mBluetoothSocket.getInputStream());
        }
        catch (IOException e) {
            e.printStackTrace();
            mHandler.obtainMessage(BlueTools.MESSAGE_CONNECTION_FIAL).sendToTarget();
        }
    }
    public void run() {
        
        while(true) {
            if(!flag) {
                break;
            }
            try {
                Object info = (TransmitBean)mInputStream.readObject();
                mHandler.obtainMessage(BlueTools.MESSAGE_GET_OBJECT, info).sendToTarget();
            }
            catch (Exception e) {
                e.printStackTrace();
                mHandler.obtainMessage(BlueTools.MESSAGE_CONNECTION_FIAL).sendToTarget();
                return;
            }
        }
        
        
        if(mInputStream != null) {
            try {
                mInputStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(mOutputStream != null) {
            try {
                mOutputStream.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(mBluetoothSocket != null) {
            try {
                mBluetoothSocket.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    public void Writer(Object o) {
        if(mOutputStream != null) {
            try {
                mOutputStream.flush();
                mOutputStream.writeObject(o);
                mOutputStream.flush();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
