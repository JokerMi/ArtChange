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
 * �����ṩ�������ӷ���
 * 
 */
public class BluetoothService {
    public static String sPin = "6666";
	private final BluetoothAdapter mBtAdapter;// ����������
	private final Handler handler;// ������Ϣ�������handler
	private int state;// ������������״̬
	private ConnectDeviceThread connectThread = null;// �����߳�
	private ConnectReadThread connectedThread = null;// ����ͨ�������߳�

//	private static final String UUIDS = "00001101-0000-1000-8000-00805F9B34FB";//�ⲿͨ�õ������豸����
	
	private static final UUID MY_UUID = MyContents.BlueTools.MUUID;// һ���ֻ�֮�佨���������ӵ�UUID

	// ������Ϣ��״̬
	public static final int STATE_NONE       = 0x401180; // ����״̬
	public static final int STATE_CONNECTING = 0x401181; // ��������
	public static final int STATE_CONNECTED  = 0x401182; // �Ѿ�����������
	public static final int STATE_FOUNDING   = 0x401183; //�����豸��

	public static final int MESSAGE_DEVICE = 0x401183;// ���������豸���ƺ͵�ַ
	public static final int MESSAGE_STATE_CHANGE = 0x401184;// ������״̬�ı��ź�
	public static final int MESSAGE_READ = 0x401185;// ���������ź�
	public static final int MESSAGE_WRITE = 0x401186;// д�������ź�
	public static final int MESSAGE_TOAST = 0x401187;// ��ʾ���û�����ʾ���ź�

	public static final String TOAST       = "toast";// handler���ݹ�ȥ�Ĺؼ���
	public static final String DEVICE_NAME = "device_name";
	public static final String DEVICE_ADDRESS = "device_address";

	private boolean isAsyn = true;// �첽��ʽ�������� 
	private static BluetoothService lastInstance = null;
	private boolean isStop = false;

	// ������Ϣ
	private static final String TAG = "txl";
	private static final boolean DEBUG = true;

	private BluetoothService(Handler handler, boolean isAsyn) {
		this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBtAdapter.getState() == BluetoothAdapter.STATE_OFF) {
			mBtAdapter.enable();// ������		
		}
		this.handler = handler;
		this.isAsyn = isAsyn;
		this.state = STATE_NONE;
	}

	public static BluetoothService getService(Handler handler, boolean isAsyn) {
		if (lastInstance != null) {
			if (lastInstance.handler == handler) {
				return lastInstance;// ����֮ǰ������ʵ��
			}
			else {
				lastInstance.stop();// handler��ͬ��ʾҪ���Ӳ�ͬ���豸����Ҫ���ϴ����Ӷϵ�
			}
			CustomLog.i(TAG, "Stop lastInstance:" + lastInstance.handler.getClass());
		}
		CustomLog.i(TAG, "�½� BluetoothService:" + handler.getClass());
		lastInstance = new BluetoothService(handler, isAsyn);
		return lastInstance;
	}

	/**
	 * �����豸����������
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
	 * �����豸��ַ���Ѿ��󶨵�����
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
	 * ���ݵ�ַ������ȡ�豸
	 * 
	 * @param address
	 * @return
	 */
	public BluetoothDevice getRemoteDeviceByAddress(String address) {
		return mBtAdapter.getRemoteDevice(address);
	}
	
	/**
	 * �����豸
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
	 * ֹͣ-�����豸
	 */
	public void stopFindDevice() {
		mBtAdapter.cancelDiscovery();
	}

	/**
	 * �ر�����
	 */
	public static void close() {
		if (lastInstance != null) {
			lastInstance.stop();
			lastInstance.mBtAdapter.disable();// �ر�����
		}
	}
	
	/**
	 * �ر������������Ӻʹ����߳�
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
	 * ��ͬ����ʽ��ȡ״̬
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
						boolean setPinResult = BluetoothUtils.setPin(device.getClass(), device, sPin); // �ֻ��������ɼ������
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
	 * ���������豸
	 * 
	 * @param device
	 */
	public synchronized void connect(BluetoothDevice device) {
		if (state == STATE_CONNECTING) {// ȡ�����ڽ���������
			if (connectThread != null) {
				connectThread.cancel();
				connectThread = null;
			}
		}
		else if (state == STATE_CONNECTED) {// �Ͽ��Ѿ�����������
			if (connectedThread != null) {
				connectedThread.shutdown();// �ȹر���
				connectedThread.cancel();
				connectedThread = null;
			}
		}
		try {
			connectThread = new ConnectDeviceThread(device);
			connectThread.start();// ��ʼ��������
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
	 * ���������������̣߳���������ͨ���Ķ�д
	 * 
	 * @param socket
	 * @param device
	 */
	private void connected(BluetoothSocket socket, BluetoothDevice device) {
		
		if (state == STATE_CONNECTING) {// ȡ�����ڽ���������
			if (connectThread != null) {
				connectThread.cancel();
				connectThread = null;
			}
		}
		if (state == STATE_CONNECTED) {// �Ͽ��Ѿ�����������
			if (connectedThread != null) {
				connectedThread.shutdown();
				connectedThread.cancel();
				connectedThread = null;
			}
		}
		connectedThread = new ConnectReadThread(socket);
		connectedThread.start();// ������������ͨ�����߳�
		Message msg = handler.obtainMessage(MESSAGE_DEVICE);
		Bundle bundle = new Bundle();
		bundle.putString(DEVICE_NAME, device.getName());
		bundle.putString(DEVICE_ADDRESS, device.getAddress());
		msg.setData(bundle);
		handler.sendMessage(msg);
	}

	/**
	 * ����״̬��Ϣ
	 * 
	 * @param state
	 */
	private void setState(int state) {
		if (DEBUG)
			CustomLog.d(TAG, "setState() " + this.state + " -> " + state);
		this.state = state;
		// ��״̬�ı���Ϣ���ݸ�UI����
		handler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
	}

	/**
	 * �������ӽ���ʧ�ܣ�֪ͨ����
	 */
	private void connectionFailed() {
		Message msg = handler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, "�豸����ʧ��");
		msg.setData(bundle);
		handler.sendMessage(msg);
		setState(STATE_NONE);// ����״̬Ϊ����
	}

	/**
	 * �������Ӷ˿ڣ�֪ͨActivity
	 */
	public void connectionLost() {
		Message msg = handler.obtainMessage(MESSAGE_TOAST);
		Bundle bundle = new Bundle();
		bundle.putString(TOAST, "�豸���ӶϿ�");
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
	 * ������ͨ����д������
	 * 
	 * @param buffer
	 */
	public void write(byte[] out) {
		ConnectReadThread ct = null;
		synchronized (this) {// ��ͬ����ʽȡ�����ӹ����̵߳�����
			if (state != STATE_CONNECTED)
				return;
			ct = connectedThread;
		}
		ct.write(out);
	}

	/**
	 * �������ӵ��߳�
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
				mBtAdapter.enable();// ������
			}
//			BluetoothSocket tempSocket = null;
//			int sdkVersion = Build.VERSION.SDK_INT;
//			Method method = null;
//			try {
//				if (false || sdkVersion >= 10) {// 10���ϵ�ʹ�ò���ȫ����
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
				// ��û����Թ�������
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
//					socket.connect();// ��������
//				} 
//				catch (IOException connectException) {// �����쳣
//					CustomLog.e(TAG, "connect failure error=" + connectException.getMessage());
//					connectionFailed();
//					try {
//						socket.close();
//					} catch (IOException closeException) {// �ر��쳣
//						CustomLog.e(TAG, "Can't close socket error=" + closeException.getMessage());
//					}
//					return;// û�����ӳɹ�������
//				}
//			} catch (Exception bondException) {// ���쳣
//				CustomLog.e(TAG, "createBond error=" + bondException.getMessage());
//				return;// û�����ӳɹ�������
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
			// ���ӽ����ɹ��������̲߳�����Ҫ��
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
				socket.connect();// ��������
				
				isConnected = true;
			} 
			catch (IOException connectException) {// �����쳣
				CustomLog.e(TAG, "connect failure error=" + connectException.getMessage());
//				connectionFailed();
				try {
					socket.close();
					socket = null;
				} 
				catch (IOException closeException) {// �ر��쳣
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
//				if (false || sdkVersion >= 10) {// 10���ϵ�ʹ�ò���ȫ����
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
	 * �������Ӻõ�ͨ�����̣߳�����i/o����
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
			setState(STATE_CONNECTED);// ����״̬
			while (!stop && isAsyn) {// �첽��ʽ�£���ͣ�Ķ�����
				read();
			}
			while (!stop && !isAsyn) {
				synchronized (this) {
					try {
						wait();// ��˯��,��д�����ݽ���������
						read();
					} catch (InterruptedException e) {
						CustomLog.e(TAG, "wait() Interrupted error=" + e.getMessage());
					}
				}
			}
		}

		/**
		 * ��ȡ����
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
					byte[] contend = new byte[bytes];// ֻ������Ч��������
					for (int i = 0; i < bytes; i++) {
						contend[i] = buffer[i];
					}
					handler.obtainMessage(MESSAGE_READ, bytes, -1, contend)
							.sendToTarget();// ���ݸ������������
				}
				
			} catch (IOException e) {// �������쳣
				CustomLog.e(TAG, "Can't read from socket error=" + e.getMessage());
				connectionLost();// ���ӶϿ�
			} 
			catch (InterruptedException e) {
				CustomLog.e(TAG, "sleep interrupted error=" + e.getMessage());
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * ������д������
		 * 
		 * @param buffer
		 */
		public synchronized void write(byte[] buffer) {
			CustomLog.v("txl", "BluetoothService ... write() buffer=" + Arrays.toString(buffer));
			try {
				outStream.write(buffer);
				outStream.flush();
				handler.obtainMessage(MESSAGE_WRITE, -1, -1, buffer)
						.sendToTarget();// ����״̬
				if (!isAsyn) {// ͬ����ʽ�£�д�����ݺ����ϻ��Ѷ�
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
		 * �ر��Ѿ���������
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
		 * �ر���
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
