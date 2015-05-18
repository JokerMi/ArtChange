package siat.ncu.press.main;




import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import siat.ncu.press.bean.PressInfo;
import siat.ncu.press.util.BluetoothCommunicateThread;
import siat.ncu.press.util.BluetoothService;
import siat.ncu.press.util.Cache;
import siat.ncu.press.util.MyContents.BlueTools;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.commlibrary.android.utils.CustomLog;
import com.commlibrary.android.utils.ToastManager;

public class MainActivity extends PressBaseActivity {

    private LinearLayout btoothLineLay;
    private ImageView btoothState;
    private TextView btoothName;
    private LinearLayout chartLineLay;
    
    private BluetoothCommunicateThread btoothCommunicateThread;
    
    private XYSeries xyseries;//����
    private XYSeries xyseries_up;//��ƽ����
    private XYSeries xyseries_down;//��ƽ����
    private XYMultipleSeriesDataset dataset;
    private GraphicalView chartview;
    private XYMultipleSeriesRenderer renderer;
    private XYSeriesRenderer datarenderer;
    private XYSeriesRenderer datarenderer_up;
    private XYSeriesRenderer datarenderer_down;
    private Context context;
   
	private int i=0;
	private Thread pressThread;
    private int addX ;
    private double addY ;
    int X_MAX=1000;//x�����ֵ
	//��׼����ֵ
	private double value=30 ;//ƽ�����м�ֵ
	private double bound=30;//������ �����Լ������趨
	private int boundTimes = 2; //Y����ʾ�ķ�Χ
	private double normalTimes = 1.5; //������С�߱���
	
	private String btName = BlueTools.BLUETOOTH_NAME;// ��������
    private String btMac = null;// ��������
	private boolean isAsyn = true;
	private Cache cache;
	private boolean pressDrawflag = true;
	
	private int sampleRateInt;
	private ArrayList<PressInfo> mArrayList;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); //��title
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.activity_main);
		
		initView();
		initDatas();
		initEvent();
	}
	public void initView() {
	    btoothLineLay  = (LinearLayout)findViewById(R.id.linearLayout_btooth);
	    btoothLineLay.setOnClickListener(myClickListener);
	    
	    btoothState = (ImageView)findViewById(R.id.iV_btstatue);
	    btoothName = (TextView)findViewById(R.id.tV_btDeviceNm);
	    
	    chartLineLay = (LinearLayout)findViewById(R.id.linearLayout_chat);
        chartLineLay.setBackgroundColor(Color.BLACK);
	}
	public OnClickListener myClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.linearLayout_btooth:
                    if(isConnected()) {
                        Toast.makeText(MainActivity.this, "����������", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startDeviceListActivity();
                    break;

                default:
                    break;
            }
        }
    };
    
    public void startDeviceListActivity(){
        Intent serverIntent = new Intent(this, DeviceScanActivity.class);
        startActivityForResult(serverIntent,
                DeviceScanActivity.REQUEST_CONNECT_DEVICE);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        CustomLog.i(TAG, "onActivityResult() resultCode=" + resultCode);
        switch (requestCode) {
            case DeviceScanActivity.REQUEST_CONNECT_DEVICE:
                if(resultCode == DeviceScanActivity.RESULT_OK) {
                    String address = data.getExtras().getString(
                            DeviceScanActivity.EXTRA_DEVICE_ADDRESS);
                    connectPressDevice(address);
                }
                break;
        }
    }
    public void connectPressDevice(String address) {
        btMac = address;
        BluetoothDevice device = mBluetoothService
                .getRemoteDeviceByAddress(address);
        btName = device.getName();
        mBluetoothService.connect(device);
    }
    public Handler btoothHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BluetoothService.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                    btoothName.setText(btName);
                    btoothState.setBackgroundResource(R.drawable.id_bluetooth_connect);
                    ToastManager.showToast(context, "�����ӵ�    " + btName);
                    break;
                case BluetoothService.STATE_CONNECTING:
                    btoothName.setText(R.string.connecting);
                    btoothState.setBackgroundResource(R.drawable.id_bluetooth_unconnect);
                    break;
                case BluetoothService.STATE_NONE:
                    btoothName.setText(R.string.unconnect);
                    btoothState.setBackgroundResource(R.drawable.id_bluetooth_unconnect);
                    break;
                }
                break;

            case BluetoothService.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                new String(writeBuf);
                break;
            case BluetoothService.MESSAGE_READ:

                byte[] readBuf = (byte[]) msg.obj;
                CustomLog.v(TAG, "handleMessage() readBuf=" + Arrays.toString(readBuf));
                try {
                    processReadData(readBuf);
                    if(pressDrawflag) {
                        pressThread.start();
                        pressDrawflag = false;
                    }
                }
                catch (Exception e) {
                    CustomLog.v("txl", "��ȡ�쳣  " + e.getMessage());
                }
                
                break;
            case BluetoothService.MESSAGE_TOAST:
                ToastManager.showToast(context, msg.getData().getString(BluetoothService.TOAST));
                break;
            case BluetoothService.MESSAGE_DEVICE:
                btName = msg.getData().getString(BluetoothService.DEVICE_NAME);
                btMac = msg.getData().getString(BluetoothService.DEVICE_ADDRESS);
                cache.saveDeviceAddress(Cache.PressDevice, btMac);// �����ַ,�Ա��´��Զ�����
                break;
//          case Uploader.MESSAGE_UPLOADE_RESULT:
//              Bundle bundler = msg.getData();
//              String item = bundler.getString(Cache.ITEM);
//              int status = bundler.getInt(Uploader.STUTAS);
//              if (Cache.BP.equals(item)) {
//                  setImageView(hBpImageView, status);
//                  setImageView(lBpImageView, status);
//                  setImageView(pulseImageView, status);
//              }
//              if (Cache.BO.equals(item))
//                  setImageView(boImageView, status);
//              if (Cache.TEMP.equals(item))
//                  setImageView(tempImageView, status);
//              break;
            }
        };
    };

    public void initDatas() {
        Intent mIntent = getIntent();
        sampleRateInt = Integer.valueOf(mIntent.getStringExtra("sampleRate"));
        mArrayList = mIntent.getParcelableArrayListExtra("pressinfo");
        
        context = MainActivity.this;
        cache = new Cache(context);
        mBluetoothService = BluetoothService.getService(btoothHandler, false);// �첽��ʽ
        pressThread=new Thread(PressRunnable); 
        initChatDatas();
    }
    
    protected void processReadData(byte[] readBuf) {
        BigDecimal b = new BigDecimal(((Math.random()) * bound * 4) - (2 * bound)); //ת��
        addY = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        //double+double��תһ��
        b = new BigDecimal(addY + value); //������λС�� ��������
        addY = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
    
    public void initChatDatas(){
      //No.1 �趨����Ⱦ��������
        renderer = new XYRenderer("ѹ��ͼ", "ʱ��(ms)", "ѹ��(kg)", 100, X_MAX, -1, 1, Color.GRAY, Color.LTGRAY, 16, 16, 15,
                15, 10, 10, true);
        dataset = new XYMultipleSeriesDataset();
        datarenderer = new XYSeriesRenderer();
        //      datarenderer.setDisplayChartValues(true);
        xyseries = new XYSeries("ʵ��ѹ��");
        //2
        xyseries.add(100, 0);//������һ�������������renderer
        //3
        dataset.addSeries(0, xyseries);
        datarenderer.setColor(Color.GREEN);
        datarenderer.setPointStyle(PointStyle.POINT);
        //4
        renderer.addSeriesRenderer(datarenderer);
        //��Ⱦ��ʱ���ֹ�û��������������ź��ƶ�������Ȼ���ױ���
        renderer.setPanEnabled(false);
        renderer.setExternalZoomEnabled(false);
        renderer.setClickEnabled(false);
        renderer.setZoomEnabled(false, false);
        //5
        context = getApplicationContext();
        chartview = ChartFactory.getLineChartView(context, dataset, renderer);
        chartLineLay.addView(chartview, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        //���ð�ť ������¼�ߴ�ı�׼(value)����ֵ(bound),Ȼ������������ʾ������

        //����������� �ֱ����µ�XYSeriesRendererҪ��Ȼ�ᱨ��
        xyseries_up = new XYSeries("���ѹ��");
        xyseries_down = new XYSeries("��Сѹ��");
        xyseries_up.add(100, value + normalTimes * bound);
        xyseries_up.add(X_MAX, value + normalTimes * bound);
        xyseries_down.add(100, value - normalTimes * bound);
        xyseries_down.add(X_MAX, value - normalTimes * bound);
        dataset.addSeries(1, xyseries_up);
        dataset.addSeries(2, xyseries_down);

        datarenderer_up = new XYSeriesRenderer();
        datarenderer_down = new XYSeriesRenderer();
        datarenderer_up.setColor(Color.RED);
        datarenderer_down = datarenderer_up;
        renderer.addSeriesRenderer(datarenderer_up);
        renderer.addSeriesRenderer(datarenderer_down);
        renderer.setYAxisMin(value - (boundTimes * bound));// Y��Сֵ
        renderer.setYAxisMax(value + (boundTimes * bound));// Y��Сֵ
        //����chart����ͼ��Χ  ����//1x->start 2max 3y->start 4max 
        renderer.setRange(new double[]
        {100d, (double) X_MAX, value - (boundTimes * bound), value + (boundTimes * bound)});
        //  chartview.repaint();
    }
	public void initEvent() {
	    connectPressDevice();
//	    setVisibility();
	}
	
	/**
     * ���ü���button����ʾ������
     * 
     * @param status
     */
    private void setVisibility() {
        int status;
        if (isConnected()) {
            status = View.VISIBLE;// ����ʱ���ÿɼ�
            btoothName.setText(btName);
            btoothState.setBackgroundResource(R.drawable.id_bluetooth_connect);
        } else {
            status = View.GONE;// δ����ʱ���ò��ɼ�
            btoothName.setText("δ����");
            btoothState.setBackgroundResource(R.drawable.id_bluetooth_unconnect);
        }
    }
    /**
     * ���ӵ��豸
     */
    private void connectPressDevice() {
        if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {// ����״̬������
            String address = cache.getDeviceAddress(Cache.PressDevice);
            btMac = address;
            BluetoothDevice device = mBluetoothService
                    .getBondedDeviceByAddress(address);
            if (device != null) {
                btName = device.getName();
                mBluetoothService.connect(device);
            }
        }
    }
	protected void onResume() {
	    if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
	        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	       }
	       super.onResume();
	};
	//handler����UI����
	Handler chatHandler=new Handler() {
		@Override
		public void handleMessage(Message msg) {
//			Bundle b=msg.getData();
//			double x=b.getDouble("part_X");
//			double y=b.getDouble("part_Y");
			chartview.invalidate();
			super.handleMessage(msg);
//			 chartview.repaint();
			//part_id1.setText(x);
			//part_size1.setText(y);
			 
            //���費�ܱ� 1��������� 2���Դ���� 3�������
            
            
            
            /*if(i*2>X_MAX)//�ӳ�X_MAX�������Ч��
            {
                X_MAX*=2;//��2���ٶ��ӳ� �������ó�speed
                renderer.setXAxisMax(X_MAX);// ����X���ֵ
                dataset.removeSeries(xyseries_up);
                dataset.removeSeries(xyseries_down);
                xyseries_up.add(X_MAX/2 , value+bound);
                xyseries_up.add(X_MAX, value+bound);
                xyseries_down.add(X_MAX/2, value-bound);
                xyseries_down.add(X_MAX,value-bound);
                dataset.addSeries(1,xyseries_up);
                dataset.addSeries(2,xyseries_down);
//                datarenderer_up = new XYSeriesRenderer();
//                datarenderer_down = new XYSeriesRenderer();
//                datarenderer_up.setColor(Color.RED);
//                datarenderer_down=datarenderer_up;
                renderer.addSeriesRenderer(datarenderer_up);
                renderer.addSeriesRenderer(datarenderer_down);
            }*/
		}
	};
	  Runnable PressRunnable = new Runnable() {
	        BigDecimal b;
	        public void run() {
	            try {
	                while (true)
	                {
	                    i = i + 100;
	                    if (i > 1100) {
	                        i = 100;
	                        xyseries.clear();
	                    }
	                    Thread.sleep(100);
	                    addX = i;
	                    //���ú���һ����Ҫ���ӵĽڵ�
	                  /*  b = new BigDecimal(((Math.random()) * bound * 4) - (2 * bound)); //ת��
	                    addY = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
	                    //double+double��תһ��
	                    b = new BigDecimal(addY + value); //������λС�� ��������
	                    addY = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();*/

	                    dataset.removeSeries(xyseries);
	                    xyseries.add(addX, addY);
	                    dataset.addSeries(0, xyseries);
	                    //����hanlder
	                    Message message = chatHandler.obtainMessage();
	                    message.what = 1;//��־���ĸ��̴߳�����  
	                    chatHandler.sendMessage(message);//����message��Ϣ  
	                }
	            }
	            catch (InterruptedException e)
	            {
	                e.printStackTrace();
	            }
	        }
	    };
   /* Runnable PressRunnable = new Runnable() {
        BigDecimal b;
        public void run() {
            try {
                while (true)
                {
                    i = i + 100;
                    if (i > 1100) {
                        i = 100;
                        xyseries.clear();
                    }
                    Thread.sleep(100);
                    addX = i;
                    //���ú���һ����Ҫ���ӵĽڵ�
                    b = new BigDecimal(((Math.random()) * bound * 4) - (2 * bound)); //ת��
                    addY = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                    //double+double��תһ��
                    b = new BigDecimal(addY + value); //������λС�� ��������
                    addY = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();

                    dataset.removeSeries(xyseries);
                    xyseries.add(addX, addY);
                    dataset.addSeries(0, xyseries);
                    //����hanlder
                    Message message = chatHandler.obtainMessage();
                    //                 Bundle bundle=new Bundle();  
                    //                 bundle.putDouble("part_X", addX);  
                    //                 bundle.putDouble("part_Y", addY); 
                    //                 message.setData(bundle);//bundle��ֵ����ʱ��Ч�ʵ�  
                    message.what = 1;//��־���ĸ��̴߳�����  
                    chatHandler.sendMessage(message);//����message��Ϣ  
                    //chartview.repaint();
                    //                 chartview.postInvalidate();
                     //���費�ܱ� 1��������� 2���Դ���� 3�������
                     dataset.removeSeries(xyseries);
                     xyseries.add(addX, addY); 
                     dataset.addSeries(0,xyseries);
                     //chartview.repaint();
                     chartview.postInvalidate();
                     if(i*2>X_MAX)//�ӳ�X_MAX�������Ч��
                     {
                    	 X_MAX*=2;//��2���ٶ��ӳ� �������ó�speed
                    	 renderer.setXAxisMax(X_MAX);// ����X���ֵ
                    	 dataset.removeSeries(xyseries_up);
                    	 dataset.removeSeries(xyseries_down);
                    	 xyseries_up.add(X_MAX/2 , value+bound);
                         xyseries_up.add(X_MAX, value+bound);
                         xyseries_down.add(X_MAX/2, value-bound);
                         xyseries_down.add(X_MAX,value-bound);
                         dataset.addSeries(1,xyseries_up);
                         dataset.addSeries(2,xyseries_down);
                    //   datarenderer_up = new XYSeriesRenderer();
                    //   datarenderer_down = new XYSeriesRenderer();
                    //   datarenderer_up.setColor(Color.RED);
                    //   datarenderer_down=datarenderer_up;
                         renderer.addSeriesRenderer(datarenderer_up);
                         renderer.addSeriesRenderer(datarenderer_down);
                     }

                }
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    };*/
		@Override
		protected void onDestroy() {
			pressThread.interrupt();
			System.out.println("destory");
			super.onDestroy();
		}
}