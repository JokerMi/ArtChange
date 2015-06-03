package siat.ncu.press.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ParseDeviceDataManager<T> {
	private ParseDeviceDataManager() {// 工具类，外部不能实例化
	}

	/**
	 * 将收到的多条数据根据头标志分割，
	 * 
	 * @param buffer
	 * @return
	 */
	public static List<byte[]> splitBufferData(byte[] buffer, byte[] head) {
		if(buffer == null) {
			return null;
		}
		final List<byte[]> datas = new ArrayList<byte[]>();
		if(head == null || head.length == 0 || buffer.length <= head.length) {
			datas.add(buffer);
			return datas;
		}
		
		int low = 0, high = buffer.length;
		boolean isNewHead = false;
		for(int i=head.length; i<buffer.length; ++i) {
			for(int j=0; j<head.length; ++j) {
				if(i+j>=buffer.length || buffer[i+j] != head[j]) {
					isNewHead = false;
					break;
				}
				isNewHead = true;
			}
			//
			if(isNewHead) {
				high = i;
//				System.out.println("splitBufferData(1) [" + low + ", " + high + ")");
				datas.add(copyOfRange(buffer, low, high));
				low = high;
				i += head.length;
			}
		}
//		System.out.println("splitBufferData(2) [" + low + ", " + buffer.length + ")");
		datas.add(copyOfRange(buffer, low, buffer.length)); //最后一条数据
		return datas;
	}
	
	/**
	 * 将data的[start,end)左闭右区间开部分复制返回
	 * 
	 * @param data
	 * @param start
	 * @param end
	 * @return
	 */
	public static <T> T[] copyOfRange(T[] data, int start, int end,
			Class<T> type) {
		@SuppressWarnings("unchecked")
		T[] buffer = (T[]) Array.newInstance(type, end - start);
		for (int i = start; i < end; i++) {
			buffer[i] = data[i - start];
		}
		return buffer;
	}

	/**
	 * 将data的[start,end)左闭右区间开部分复制返回
	 * 
	 * @param data
	 * @param start
	 * @param end
	 * @return
	 */
	public static byte[] copyOfRange(byte[] data, int start, int end) {
		//CustomLog.i(TAG, "start:" + start + ", end:" + end);
		byte[] buffer = new byte[end - start];
		for (int i = start; i < end; i++) {
			buffer[i - start] = data[i];
		}
		return buffer;
	}

	/**
	 * 将data的[0,length)部分复制返回，如果length大于data长度，
	 * 则后面的部分置为0
	 * 
	 * @param data
	 * @param length
	 * @return
	 */
	public static byte[] copyOf(byte[] data, int length) {
		byte[] buffer = new byte[length];
		if (length < data.length)// 返回[0,length)部分
			return copyOfRange(data, 0, length);
		int i;
		for (i = 0; i < data.length; i++)
			buffer[i] = data[i];
		for (; i < length; i++)
			// 超过的部分设置为0
			buffer[i] = 0;
		return buffer;
	}
}
