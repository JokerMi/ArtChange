package siat.ncu.press.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;

import com.commlibrary.android.utils.CustomLog;

/**
 * 负责提供蓝牙连接服务
 * 
 */
public class BluetoothService {
    public static String sPin = "6666";
	private final BluetoothAdapter mBtAdapter;// 蓝牙适配器
	private final Handler handler;// 传递信息给界面的handler
	private int state;// 蓝牙适配器的状态
	private ConnectDeviceThread connectThread = null;// 连接线程
	private ConnectReadThread connectedThread = null;// 蓝牙通道管理线程

//	private static final String UUIDS = "00001101-0000-1000-8000-00805F9B34FB";//外部通用的蓝牙设备连接
	
	private static final UUID MY_UUID = MyContents.BlueTools.MUUID;// 一般手机之间建立蓝牙连接的UUID

	// 控制信息和状态
	public static final int STATE_NONE       = 0x401180; // 空闲状态
	public static final int STATE_CONNECTING = 0x401181; // 正在连接
	public static final int STATE_CONNECTED  = 0x401182; // 已经建立蓝牙连
	public static final int STATE_FOUNDING   = 0x401183; //查找设备中

	public static final int MESSAGE_DEVICE = 0x401183;// 传递蓝牙设备名称和地址
	public static final int MESSAGE_STATE_CHANGE = 0x401184;// 适配器状态改变信号
	public static final int MESSAGE_READ = 0x401185;// 读入数据信号
	public static final int MESSAGE_WRITE = 0x401186;// 写入数据信号
	public static final int MESSAGE_TOAST = 0x401187;// 显示给用户的提示的信号

	public static final String TOAST       = "toast";// handler传递过去的关键字
	public static final String DEVICE_NAME = "device_name";
	public static final String DEVICE_ADDRESS = "device_address";

	private boolean isAsyn = true;// 异步方式传递数据 
	private static BluetoothService lastInstance = null;
	private boolean isStop = false;

	// 调试信息
	private static final String TAG = "txl";
	private static final boolean DEBUG = true;

	private BluetoothService(Handler handler, boolean isAsyn) {
		this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter.getState() == BluetoothAdapter.STATE_OFF) {
			mBtAdapter.enable();// 打开蓝牙		
		}
		this.handler = handler;
		this.isAsyn = isAsyn;
		this.state = STATE_NONE;
	}

	public static BluetoothService getService(Handler handler, boolean isAsyn) {
		if (lastInstance != null) {
			if (lastInstance.handler == handler) {
				return lastInstance;// 返回之前建立的实例
			}
			else {
				lastInstance.stop();// handler不同表示要连接不同的设备，需要把上次连接断掉
			}
			CustomLog.i(TAG, "Stop lastInstance:" + lastInstance.handler.getClass());
		}
		CustomLog.i(TAG, "新建 BluetoothService:" + handler.getClass());
		lastInstance = new BluetoothService(handler, isAsyn);
		return lastInstance;
	}

	/**
	 * 根据设备名字找蓝牙
	 * 
	 * @param name
	 * @return
	 */
	public BluetoothDevice getBondedDeviceByName(String name) {
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		for (BluetoothDevice d : pairedDevices) {
			String n = d.getName();
			if (name.equals(n)) {
				return d;
			}
		}
		return null;
	}

	/**
	 * 根据设备地址找已经绑定的蓝牙
	 * 
	 * @param name
	 * @return
	 */
	public BluetoothDevice getBondedDeviceByAddress(String address) {
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		for (BluetoothDevice d : pairedDevices) {
			if (d.getAddress().equals(address)) {
				return d;
			}
		}
		return null;
	}

	/**
	 * 根据地址名，获取设备
	 * 
	 * @param address
	 * @return
	 */
	public BluetoothDevice getRemoteDeviceByAddress(String address) {
		return mBtAdapter.getRemoteDevice(address);
	}
	
	/**
	 * 查找设备
	 */
	public void doFindDevice() {
		openBluetooth();
		if (mBtAdapter.isDiscovering()) {
			return;
//			mBtAdapter.cancelDiscovery();
		}
		new Thread() {
			public void run() {
				setState(STATE_FOUNDING);
				boolean founDevice = false;
				int i = 0;
				while(!founDevice && i<100 && !isStop) {
					founDevice = mBtAdapter.startDiscovery();
					CustomLog.v("txl", "BluetoothService ... doFoundDevice() " + i
							+ ", founDevice=" + founDevice);
					
					i++;
					
					SystemClock.sleep(500);
				}
				setState(STATE_NONE);
			};
		}
		.start();
	}
	
	/**
	 * 
	 */
	public void openBluetooth() {
		if (mBtAdapter != null && mBtAdapter.getState() == BluetoothAdapter.STATE_OFF) {
			boolean isEnable = mBtAdapter.enable();
		}
	}
	
	/**
	 * 停止-查找设备
	 */
	public void stopFindDevice() {
		mBtAdapter.cancelDiscovery();
	}

	/**
	 * 关闭蓝牙
	 */
	public static void close() {
		if (lastInstance != null) {
			lastInstance.stop();
			lastInstance.mBtAdapter.disable();// 关闭蓝牙
		}
	}
	
	/**
	 * 关闭所有蓝牙连接和处理线程
	 */
	public synchronized void stop() {
		if (DEBUG)
			CustomLog.d(TAG, "stop");
		isStop = true;
		if (connectedThread != null) {
			connectedThread.shutdown();
			connectedThread.cancel();
			connectedThread = null;
		}
		if (connectThread != null) {
			connectThread.cancel();
			connectThread = null;
		}
		setState(STATE_NONE);
	}

	/**
	 * 以同步方式获取状态
	 * 
	 * @return
	 */
	public synchronized int getState() {
		return this.state;
	}

	/**
	 * 
	 * @param address
	 * @param commandBettery
	 */
	public synchronized void connect(final String address) {
		setState(STATE_CONNECTING);
		stopFindDevice();
		
		new Thread() {
			public void run() {
				if(mBtAdapter == null) {
					connectionFailed();
					return;
				}
				int i = 0;
				while(!mBtAdapter.isEnabled() && i<100 && !isStop) {
					openBluetooth();
					i++;
					
					SystemClock.sleep(250);
				}
				
				if(!mBtAdapter.isEnabled()) {
					connectionFailed();
				}
				else {
					final BluetoothDevice device = getRemoteDeviceByAddress(address);
					CustomLog.v("txl", "BluetoothService ... connect() address:" + address
							+ ", device=" + device
							+ ", deviceState=" + device.getBondState()
							+ ", i=" + i);
					if(device.getBondState() != BluetoothDevice.BOND_BONDED) {
						boolean setPinResult = BluetoothUtils.setPin(device.getClass(), device, sPin); // 手机和蓝牙采集器配对
						boolean createBondResult = BluetoothUtils.createBond(device.getClass(), device);
//						boolean cancelPairingResult = BluetoothUtils.cancelPairingUserInput(device.getClass(), device);
						
						CustomLog.v("txl", "BluetoothService ... connect() "
								+ "setPinResult:" + setPinResult
								+ ", createBondResult=" + createBondResult
//								+ ", cancelPairingResult=" + cancelPairingResult
								);
						SystemClock.sleep(500);
					}
					
					connect(device);
				}
			};
		}
		.start();
	}
	
	/**
	 * 连接蓝牙设备
	 * 
	 * @param device
	 */
	public synchronized void connect(BluetoothDevice device) {
		if (state == STATE_CONNECTING) {// 取消正在建立的连接
			if (connectThread != null) {
				connectThread.cancel();
				connectThread = null;
			}
		}
		else if (state == STATE_CONNECTED) {// 断开已经建立好连接
			if (connectedThread != null) {
				connectedThread.shutdown();// 先关闭流
				connectedThread.cancel();
				connectedThread = null;
			}
		}
		try {
			connectThread = new ConnectDeviceThread(device);
			connectThread.start();// 开始建立连接
			setState(STATE_CONNECTING);
		} 
		catch (SecurityException e) {
			connectionFailed();
			e.printStackTrace();
			CustomLog.e(TAG, "connect error=" + e.getMessage());
		} 
		catch (IllegalArgumentException e) {
			connectionFailed();
			e.printStackTrace();
			CustomLog.e(TAG, "connect error=" + e.getMessage());
		} 
		catch (NoSuchMethodException e) {
			connectionFailed();
			e.printStackTrace();
			CustomLog.e(TAG, "connect error=" + e.getMessage());
		}
		catch (IllegalAccessException e) {
			connectionFailed();
			e.printStackTrace();
			CustomLog.e(TAG, "connect error=" + e.getMessage());
		} 
		catch (InvocationTargetException e) {
			connectionFailed();
			e.printStackTrace();
			CustomLog.e(TAG, "connect error=" + e.getMessage());
		}
		finally {
			
		}
	}

	/**
	 * 开启蓝牙已连接线程，管理蓝牙通道的读写
	 * 
	 * @param socket
	 * @param device
	 */
	private void connected(BluetoothSocket socket, BluetoothDevice device) {
		
		if (state == STATE_CONNECTING) {// 取消正在建立的连接
			if (connectThread != null) {
				connectThread.cancel();
				connectThread = null;
			}
		}
		if (state == STATE_CONNECTED) {// 断开已经建立好连接
			if (connectedThread != null) {
				connectedThread.shutdown();
				connectedThread.cancel();
				connectedThread = null;
			}
		}
		connectedThread = new ConnectReadThread(socket);
		connectedThread.start();// 开启管理蓝牙通道的线程
		Message msg = handler.obtainMessage(MESSAGE_DEVICE);
		Bundle bundle = new Bundle();
		bundle.putString(DEVICE_NAME, device.getName());
		bundle.putString(DEVICE_ADDRESS, device.getAddress());
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	/**
	 * 设置状态信息
	 * 
	 * @param state
	 */
	private void setState(int state) {
		if (DEBUG)
			CustomLog.d(TAG, "setState() " + this.state + " -> " + state);
		this.state = state;
		// 将状态改变信息传递给UI界面
		handler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	/**
	 * 处理连接建立失败，通知界面
	 */
	private void connectionFailed() {
		Message msg = handler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, "设备连接失败");
		msg.setData(bundle);
		handler.sendMessage(msg);
		setState(STATE_NONE);// 设置状态为空闲
	}

	/**
	 * 处理连接端口，通知Activity
	 */
	public void connectionLost() {
		Message msg = handler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, "设备连接断开");
		msg.setData(bundle);
		handler.sendMessage(msg);
		if (connectedThread != null) {
			connectedThread.shutdown();
			connectedThread.cancel();
			connectedThread = null;
		}
		setState(STATE_NONE);
	}

	/**
	 * 往蓝牙通道中写入数据
	 * 
	 * @param buffer
	 */
	public void write(byte[] out) {
		ConnectReadThread ct = null;
		synchronized (this) {// 以同步方式取得连接管理线程的引用
			if (state != STATE_CONNECTED)
				return;
			ct = connectedThread;
		}
		ct.write(out);
	}

	/**
	 * 建立连接的线程
	 * 
	 */
	private class ConnectDeviceThread extends Thread {
		private BluetoothDevice device;
		private BluetoothSocket socket;
		
		private boolean isConnected;
		private int remainConnetTime = 10;

		@SuppressLint("NewApi")
		public ConnectDeviceThread(BluetoothDevice device) throws NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
			this.device = device;
			if (mBtAdapter.getState() == BluetoothAdapter.STATE_OFF) {
				mBtAdapter.enable();// 打开蓝牙
			}
//			BluetoothSocket tempSocket = null;
//			int sdkVersion = Build.VERSION.SDK_INT;
//			Method method = null;
//			try {
//				if (false || sdkVersion >= 10) {// 10以上的使用不安全连接
//					tempSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
//					method = device.getClass().getDeclaredMethod(
//							"createInsecureRfcommSocket",
//							new Class[] { int.class });
//				} else {
//					tempSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
//					method = device.getClass().getMethod("createRfcommSocket",
//							new Class[] { int.class });
//				}
//				tempSocket = (BluetoothSocket) method.invoke(device, 1);
//				
//				tempSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
//			} 
//			catch (IOException e) {
//				CustomLog.e(TAG, "create() failed error=" + e.getMessage());
//			}
//			socket = tempSocket;
			
			initSocket();
			isConnected = false;
		}

		@Override
		public void run() {
			CustomLog.i(TAG, "BEGIN connectThread");
			setName("ConnectDeviceThread");
//			try {
				// 绑定没有配对过的蓝牙
//				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//					BluetoothDevice.class.getMethod("createBond", new Class[0])
//					.invoke(device, new Object[0]);
					
//					Method setPinMethod = BluetoothDevice.class.getDeclaredMethod("setPin",
//						     new Class[]
//						     {byte[].class});
//					Boolean setPinResult = (Boolean) setPinMethod.invoke(device,
//							new Object[]{sPin.getBytes()});
					//
//					Method createBondMethod = BluetoothDevice.class.getMethod("createBond");
//					Boolean createBondResult = (Boolean)createBondMethod.invoke(device);
//				}
//				try {
//					socket.connect();// 建立连接
//				} 
//				catch (IOException connectException) {// 连接异常
//					CustomLog.e(TAG, "connect failure error=" + connectException.getMessage());
//					connectionFailed();
//					try {
//						socket.close();
//					} catch (IOException closeException) {// 关闭异常
//						CustomLog.e(TAG, "Can't close socket error=" + closeException.getMessage());
//					}
//					return;// 没有连接成功，返回
//				}
//			} catch (Exception bondException) {// 绑定异常
//				CustomLog.e(TAG, "createBond error=" + bondException.getMessage());
//				return;// 没有连接成功，返回
//			}
			
			while (!isStop && !isConnected && remainConnetTime > 0) {
				connect2Device();
			}
			CustomLog.e(TAG, "ConnectDeviceThread run() isConnected=" + isConnected
					+ ", remainConnetTime=" + remainConnetTime);
			if(isStop || !isConnected) {
				connectionFailed();
				return;
			}
			// 连接建立成功，连接线程不再需要了
			synchronized (BluetoothService.this) {
				connectThread = null;
			}
			connected(socket, device);
		}
		
		/**
		 * 
		 */
		private void connect2Device() {
			try {
				remainConnetTime--;
				//
				initSocket();
				socket.connect();// 建立连接
				
				isConnected = true;
			} 
			catch (IOException connectException) {// 连接异常
				CustomLog.e(TAG, "connect failure error=" + connectException.getMessage());
//				connectionFailed();
				try {
					socket.close();
					socket = null;
				} 
				catch (IOException closeException) {// 关闭异常
					CustomLog.e(TAG, "Can't close socket error=" + closeException.getMessage());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		/**
		 * 
		 */
		private void initSocket() {
			if(socket != null) {
				return;
			}
			BluetoothSocket tempSocket = null;
			int sdkVersion = Build.VERSION.SDK_INT;
			Method method = null;
			try {
//				if (false || sdkVersion >= 10) {// 10以上的使用不安全连接
//					tempSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
//					method = device.getClass().getDeclaredMethod(
//							"createInsecureRfcommSocket",
//							new Class[] { int.class });
//				} else {
//					tempSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
//					method = device.getClass().getMethod("createRfcommSocket",
//							new Class[] { int.class });
//				}
//				tempSocket = (BluetoothSocket) method.invoke(device, 1);
			    Method m = device.getClass().getMethod( "createRfcommSocket", new Class[]{int.class});
			    tempSocket = (BluetoothSocket) m.invoke( device, Integer.valueOf( 1));
				
//				tempSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
			} 
			catch (Exception e) {
				CustomLog.e(TAG, "create() failed error=" + e.getMessage());
			}
			socket = tempSocket;
		}

		public void cancel() {
			try {
				if(socket != null) {
					socket.close();
					socket = null;
				}
			} catch (IOException e) {
				CustomLog.e(TAG, "close socket error=" + e.getMessage());
			}
		}
	}
	
	/**
	 * 管理连接好的通道的线程，处理i/o操作
	 * 
	 */
	private class ConnectReadThread extends Thread {
		private BluetoothSocket socket;
		private InputStream inStream = null;
		private OutputStream outStream = null;
		private boolean stop = false;

		public ConnectReadThread(BluetoothSocket socket) {
			CustomLog.v(TAG, "create ConnectReadThread");
			this.socket = socket;
			try {
				inStream = socket.getInputStream();
				outStream = socket.getOutputStream();
			} 
			catch (IOException e) {
				CustomLog.e(TAG, "sockets stream not created error=" + e.getMessage());
			}
		}

		@Override
		public void run() {
			setState(STATE_CONNECTED);// 更新状态
			while (!stop && isAsyn) {// 异步方式下，不停的读数据
				read();
			}
			while (!stop && !isAsyn) {
				synchronized (this) {
					try {
						wait();// 先睡觉,等写了数据叫我起来哈
						read();
					} catch (InterruptedException e) {
						CustomLog.e(TAG, "wait() Interrupted error=" + e.getMessage());
					}
				}
			}
		}

		/**
		 * 读取数据
		 */
		private void read() {
			if(stop) {
				return;
			}
			try {
				CustomLog.v(TAG, "BluetoothService ... ConnectReadThread read(1) start stop=" + stop);
				int tryTime = 2;
				while (!stop 
						&& inStream.available() < 1 
						&& (tryTime--) >= 0) {
					ConnectReadThread.sleep(500);
				}
				CustomLog.v(TAG, "BluetoothService ... ConnectReadThread read(1.1) "
						+ ", tryTime=" + tryTime
						+ ", available=" + inStream.available());
//				if (tryTime < 0) {
				if (stop || (tryTime < 0 && inStream.available() <= 0)) {
//					ConnectReadThread.sleep(500);
					return;
				}
				byte[] buffer = new byte[256]; //
				CustomLog.v(TAG, "BluetoothService ... ConnectReadThread read(1.2)");
				int bytes;
				CustomLog.v(TAG, "BluetoothService ... ConnectReadThread read(1.3) available=" + inStream.available());
				bytes = inStream.read(buffer);
				
				CustomLog.v(TAG, "BluetoothService ... ConnectReadThread read(2) count=" + bytes
//						+ ", buffer=" + Arrays.toString(buffer)
						);
				bytes = Math.min(bytes, buffer.length);
				if (bytes > 0) {
					byte[] contend = new byte[bytes];// 只传递有效数据内容
					for (int i = 0; i < bytes; i++) {
						contend[i] = buffer[i];
					}
					handler.obtainMessage(MESSAGE_READ, bytes, -1, contend)
							.sendToTarget();// 传递给界面更新数据
				}
				
			} catch (IOException e) {// 读数据异常
				CustomLog.e(TAG, "Can't read from socket error=" + e.getMessage());
				connectionLost();// 连接断开
			} 
			catch (InterruptedException e) {
				CustomLog.e(TAG, "sleep interrupted error=" + e.getMessage());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 往流中写入数据
		 * 
		 * @param buffer
		 */
		public synchronized void write(byte[] buffer) {
			CustomLog.v("txl", "BluetoothService ... write() buffer=" + Arrays.toString(buffer));
			try {
				outStream.write(buffer);
				outStream.flush();
				handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
						.sendToTarget();// 传递状态
				if (!isAsyn) {// 同步方式下，写完数据后马上唤醒读
					notify();
				}
			} 
			catch (IOException e) {
				connectionLost();
				CustomLog.e("txl", "BluetoothService ... write() error=" + e.getMessage());
			}
			catch (Exception e) {
				CustomLog.e("txl", "BluetoothService ... write() error=" + e.getMessage());
			}
		}

		/**
		 * 关闭已经建立连接
		 */
		public void cancel() {
			try {
				if(socket != null) {
					socket.close();
					socket = null;
				}
			} catch (IOException e) {
				CustomLog.e(TAG, "close socket error=" + e.getMessage());
			}
		}

		/**
		 * 关闭流
		 */
		public void shutdown() {
			stop = true;
			try {
				if (inStream != null) {
					inStream.close();
					inStream = null;
				}
				
				if (outStream != null) {
					outStream.close();
					outStream = null;
				}
			} catch (IOException e) {
				CustomLog.e(TAG, "close() of stream failed. error=" + e.getMessage());
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

}
