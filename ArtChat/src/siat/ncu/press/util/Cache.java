package siat.ncu.press.util;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import siat.ncu.press.util.MyContents.BlueTools;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * �������ݵ�����SharedPreferences
 * 
 */
public class Cache {
	public static final String PC300 = "PC300";
	public static final String BENECHECK = "BeneCheck";// �ٽ�Ѫ���豸
	public static final String GMPUA = "GmpUa";// ��Һ������
	public static final String PressDevice = BlueTools.BLUETOOTH_NAME; //

	private static final String USER_ID = "user_id";
	public static final String ITEM = "ITEM";
	public static final String BP = "bp";
	public static final String TEMP = "temp";
	public static final String BO = "bo";
	public static final String GLU = "glu";
	public static final String UA = "ua";
	public static final String CHOL = "chol";
	public static final String URINE = "urine";

	Context context;
	private SharedPreferences sharedPrefrences;
	private Editor editor;

	public Cache(Context context) {
		this.context = context;
		sharedPrefrences = context.getSharedPreferences("padhealth",
				Context.MODE_PRIVATE);
		editor = sharedPrefrences.edit();
	}

	/**
	 * ���浱ǰ�û�id
	 * 
	 * @param idCard
	 */
	public void saveUserId(String idCard) {
		editor.putString(USER_ID, idCard);
		editor.commit();// �ύ
	}

	/**
	 * ��ȡ��ǰ�û�id
	 * 
	 * @return
	 */
	public String getUserId() {
		return sharedPrefrences.getString(USER_ID, null);
	}

	/**
	 * �����豸��ַ
	 * 
	 * @param device
	 * @param address
	 */
	public void saveDeviceAddress(String device, String address) {
		editor.putString(device, address);
		editor.commit();// �ύ
	}

	/**
	 * ��ȡ�豸�ĵ�ַ
	 * 
	 * @param device
	 * @return
	 */
	public String getDeviceAddress(String device) {
		return sharedPrefrences.getString(device, null);
	}

	/**
	 * ���浱ǰ�û���item��Ŀ��������
	 * 
	 * @param item
	 * @param dataMap
	 */
	public void saveItem(String item, Map<String, String> dataMap) {
		JSONObject json = new JSONObject(dataMap);
		saveItem(item, json);
	}

	/***
	 * ���浱ǰ�û�item��������
	 * 
	 * @param item
	 * @param json
	 */
	public void saveItem(String item, JSONObject json) {
		String id = getUserId();
		editor.putString(id + item, json.toString());
		editor.commit();// �ύ

	}

	/**
	 * ��ȡ����ĵ�ǰ�û��Ĳ�������
	 * 
	 * @param item
	 * @return
	 */
	public JSONObject getItem(String item) {
		String id = getUserId();
		String itemString = sharedPrefrences.getString(id + item, null);
		if (null == itemString)
			return null;
		try {
			return new JSONObject(itemString);
		} catch (JSONException e) {
			e.printStackTrace();
			return null;
		}
	}

	/***
	 * ���浱ǰ�û���Ϣ
	 * 
	 * @param userInfo
	 */
	public void saveUserInfo(JSONObject userInfo) {
		editor.putString("userInfo", userInfo.toString());
		editor.commit();// �ύ
	}

	/***
	 * ���浱ǰ�û���Ϣ
	 * 
	 * @param userInfo
	 */
	public JSONObject getUserInfo() {
		JSONObject json = new JSONObject();
		try {
			json = new JSONObject(sharedPrefrences.getString("userInfo", "{}"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}

	/***
	 * ��ȡ��ǰ�û�����
	 * 
	 * @return
	 */
	public String getUserName() {
		String userName = "";
		JSONObject json = getUserInfo();
		try {
			userName = json.getString("name");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return userName;

	}

}
