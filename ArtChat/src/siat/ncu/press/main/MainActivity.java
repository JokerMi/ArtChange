package siat.ncu.press.main;


import java.io.IOException;
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
import siat.ncu.press.util.DataProcess;
import siat.ncu.press.util.FileUtil;
import siat.ncu.press.util.XYRenderer;
import siat.ncu.press.util.MyContents.BlueTools;
import android.R.integer;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnHoverListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.commlibrary.android.utils.CustomLog;
import com.commlibrary.android.utils.ToastManager;

public class MainActivity extends PressBaseActivity {

    private LinearLayout topLineLay;
    private LinearLayout btoothLinLay;
    private LinearLayout saveLinLay;
    private LinearLayout cleanLinLay;
    private ImageView btoothState;
    private TextView btoothName;
    private LinearLayout chartLineLay;
    private TextView voltageTv;
    
    private BluetoothCommunicateThread btoothCommunicateThread;
    
    private XYSeries xyseries;//数据
    private XYSeries xyseries_up;//上平行线
    private XYSeries xyseries_down;//下平行线
    private XYMultipleSeriesDataset dataset;
    private GraphicalView chartview;
    private XYMultipleSeriesRenderer renderer;
    private XYSeriesRenderer datarenderer;
    private XYSeriesRenderer datarenderer_up;
    private XYSeriesRenderer datarenderer_down;
    private Context context;
   
    private double currentStep;
    private Thread pressThread;
    private double addX ;
    private double addY ;
    private int X_MIN = 0;
    private int X_MAX=10;//x轴最大值
    //标准和阈值
    private double value=30 ;//平行线中间值
    private double bound=30;//上下限 可以自己随意设定
    private double boundTimes = 1.2; //Y轴显示的范围
    private double normalTimes = 1.5; //最大和最小线倍数
    
    private String btName = BlueTools.BLUETOOTH_NAME;// 蓝牙名称
    private String btMac = null;// 蓝牙名称
    private boolean isAsyn = true;
    private Cache cache;
    private boolean pressFirstDrawflag = true;
    private boolean isThreadPause = false;
    
    private double sampleRateDouble; //采样频率
    private double baseTime = 1; //基础时间1s
    private double xGap;         //x轴的步长，等于1除以采样频率
    private double timeInterval;   //时间间隔，用于线程睡眠，等于1000毫秒除以采样频率
    private double time = 1000;   //1000毫秒
    private int currentStandardLength ; //当前的X，绘制横线时使用
    private int currentVerticalX;    //当前的X，绘制竖线时使用
    private ArrayList<PressInfo> mArrayList;
    private ArrayList<Double> oldX;
    private ArrayList<Double> oldY;
    private int max_Interval = 30; //能画出的最大的时间间隔
    private int loopAmount;
    private PressRunnable mPressRunnable;
    private boolean isAppend = true;
    private boolean sdCardExist = false;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); //无title
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        
        initView();
        initDatas();
        initEvent();
    }
    public void initView() {
        topLineLay  = (LinearLayout)findViewById(R.id.linLay_top);
        cleanLinLay = (LinearLayout)findViewById(R.id.linearLayout_cleanData);
        saveLinLay =(LinearLayout)findViewById(R.id.linearLayout_save);
        btoothLinLay = (LinearLayout)findViewById(R.id.linearLayout_btooth);
        voltageTv = (TextView)findViewById(R.id.tV_voltage);
        saveLinLay.setOnClickListener(myClickListener);
        cleanLinLay.setOnClickListener(myClickListener);
        btoothLinLay.setOnClickListener(myClickListener);
        
        btoothState = (ImageView)findViewById(R.id.iV_btstatue);
        btoothName = (TextView)findViewById(R.id.tV_btDeviceNm);
        
        chartLineLay = (LinearLayout)findViewById(R.id.linearLayout_chat);
        chartLineLay.setBackgroundColor(Color.BLACK);
    }
    public OnClickListener myClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.linearLayout_cleanData:
                    //清除保存的数据，包括内存和sd卡数据
                    if(cleanSaveData()){
                        ToastManager.showToast(context, R.string.clean_success);
                    }else {
                        ToastManager.showToast(context, R.string.clean_fail);
                    }
                    break;
                case R.id.linearLayout_save:
                    if(resultArrayList == null || resultArrayList.size() == 0) {
                        ToastManager.showToast(context, R.string.no_save_data);
                        return;
                    }
                    if(!sdCardExist) {
                        ToastManager.showToast(context, R.string.no_sdcard);
                        return;
                    }
                    //如果SD卡存在，则将内存中数据复制到SD卡中
                    saveDataToSD();
                    break;
                case R.id.linearLayout_btooth:
                   if(isConnected()) {
                        Toast.makeText(MainActivity.this, "蓝牙已连接", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startDeviceListActivity();
                    break;

                default:
                    break;
            }
        }
    };
    
    private double calibrationValue;
    /**
     * 保存压力数据
     */
    public void saveDataToSD() {
        try {
            FileUtil.writeFile(FileUtil.RAMPATH , FileUtil.SDPATH, isAppend);
            ToastManager.showToast(context, R.string.save_success);
        }
        catch (IOException e) {
            ToastManager.showToast(context, R.string.save_fail);
            e.printStackTrace();
        }
    }
    
    public boolean cleanSaveData() {
        boolean isDelSuccess = false;
        try {
            FileUtil.delFile(FileUtil.RAMPATH);
            if(sdCardExist) {
                FileUtil.delFile(FileUtil.SDPATH);
            }
            isDelSuccess = true;
        }
        catch (Exception e) {
        }
        return isDelSuccess;
    }
    public void clearAll() {
        
        mPressRunnable = null;
        
        for(int i=0;i <= dataset.getSeriesCount();i++) {
            dataset.removeSeries(i);
        }
        dataset.clear();
        renderer.removeAllRenderers();
        chartLineLay.removeView(chartview);
        chartLineLay.invalidate();
        initDatas();
    }
    
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
                    System.out.println("activity : address " + address);
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
    
    private Integer status = 0;
   
    protected Handler btoothHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case BluetoothService.MESSAGE_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothService.STATE_CONNECTED:
                    btoothState.setImageResource(R.drawable.id_bluetooth_connect);
                    btoothName.setText(btName);
//                    setRenderEnable();
                    ToastManager.showToast(context, "已连接到   " + btName);
                    break;
                case BluetoothService.STATE_CONNECTING:
                    btoothName.setText(R.string.connecting);
                    btoothState.setImageResource(R.drawable.id_bluetooth_unconnect);
                    break;
                case BluetoothService.STATE_NONE:
                    btoothName.setText(R.string.unconnect);
                    btoothState.setImageResource(R.drawable.id_bluetooth_unconnect);
//                    setRenderEnable();
                    if(pressThread.isAlive()) {
                        isThreadPause = true;
                        voltageTv.setText("0");
                        mPressRunnable.setSuspendFlag();
                    }
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
                    if(pressFirstDrawflag) {
                        pressThread.start();
                        pressFirstDrawflag = false;
                    }else if(isThreadPause){
                        mPressRunnable.setResume();
                        isThreadPause = false;
                    }
                }
                catch (Exception e) {
                    CustomLog.v("txl", "读取异常  " + e.getMessage());
                }
                
                break;
            case BluetoothService.MESSAGE_TOAST:
                ToastManager.showToast(context, msg.getData().getString(BluetoothService.TOAST));
                break;
            case BluetoothService.MESSAGE_DEVICE:
                btName = msg.getData().getString(BluetoothService.DEVICE_NAME);
                btMac = msg.getData().getString(BluetoothService.DEVICE_ADDRESS);
                System.out.println(btName+" BluetoothService.MESSAGE_DEVICE "+btMac);
                cache.saveDeviceAddress(Cache.PressDevice, btMac);// 保存地址,以便下次自动连接
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
        sampleRateDouble = Integer.valueOf(mIntent.getStringExtra("sampleRate"));
        mArrayList = mIntent.getParcelableArrayListExtra("pressinfo");
        loopAmount = mIntent.getIntExtra("loopAmount", 1);
        mPressRunnable = new PressRunnable();
        for(PressInfo m:mArrayList) {
            System.out.println("  "+m.getPressValue()+" " +m.getPressValue());
        }
        xGap = baseTime/sampleRateDouble;
        timeInterval = time/sampleRateDouble;
        currentStandardLength = 0;
        currentVerticalX = 0;
        currentStep = 0;
        
        context = MainActivity.this;
        cache = new Cache(context);
        mBluetoothService = BluetoothService.getService(btoothHandler, true);// 异步方式
        pressThread=new Thread(mPressRunnable); 
        
        oldX = new ArrayList<Double>();
        oldY = new ArrayList<Double>();
        addX = 0;
        addY = 0;
        
        sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); //判断sd卡是否存在
        if (sdCardExist) {
          //创建存放数据的sdcard目录
            FileUtil.createDir(FileUtil.DIRPATH);
        }
        
        initChatDatas();
    }
    
    private double totalPressValue;
    private double voltageValue;
    private boolean isFirstProcess = true;
    protected void processReadData(byte[] readBuf) {
        
        resultArrayList = DataProcess.processReadData(readBuf);
        String dataLine = "";
        for(int i = 0;i<resultArrayList.size();i++){
            
            System.out.println("result = "+ resultArrayList.toString());
            
            if(resultArrayList.get(i).indexOf("0x") == -1) {
                int value = Integer.parseInt(resultArrayList.get(i), 16);
                double pressValue = DataProcess.countPressValue(value);
                totalPressValue += pressValue;
            }else {
                String voltageStr = resultArrayList.get(i).substring(2);
                addY = totalPressValue;
                if(isFirstProcess) {
                    isFirstProcess = false;
                    calibrationValue = addY;
//                    dataLine = "压力值" + "\t" + "电压值" + "\r\n";
//                    writeStrContent(dataLine);
                }
                addY = addY - calibrationValue;
                addY = DataProcess.getTransData(addY);
                totalPressValue = 0;
                int value = Integer.parseInt(voltageStr, 16);
                voltageValue = DataProcess.countElectricValue(value);
                voltageTv.setText(voltageValue+"");
                
                dataLine = Math.abs(addY) +"\t  "+voltageValue+"\r\n";
                System.out.println("dataLine = " + dataLine);
                writeStrContent(dataLine);
            }
            
        }
        
        /*BigDecimal b = new BigDecimal(((Math.random()) * bound)); //转换
        addY = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
        //double+double再转一次
        b = new BigDecimal(addY + value); //保留三位小数 四舍五入
        addY = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();*/
    }
    
    /**
     * 将字符串数据写入内存中
     * @param dataStr
     */
    public void writeStrContent(String dataStr) {
        try {
            FileUtil.writeContent(dataStr, FileUtil.RAMPATH, isAppend);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void setRenderEnable() {
        boolean isEnable;
        if(isConnected()) {
            isEnable = false;
            chartLineLay.setClickable(isEnable);
        }else {
            isEnable = true;
            chartLineLay.setClickable(isEnable);
        }
    }
    @SuppressLint("NewApi")
    public void initChatDatas(){
      //No.1 设定大渲染器的属性
        renderer = new XYRenderer("压力图", "时间(s)", "压力(kg)", X_MIN, X_MAX, -1, 1, Color.GRAY, Color.LTGRAY, XYRenderer.TEXT_SIZE, XYRenderer.TEXT_SIZE, XYRenderer.TEXT_SIZE,
                XYRenderer.TEXT_SIZE, 10, 10, true);
        dataset = new XYMultipleSeriesDataset();
        datarenderer = new XYSeriesRenderer();
//        datarenderer.setDisplayChartValues(true);
        xyseries = new XYSeries("实际压力");
        //2
        xyseries.add(X_MIN, 0);//先输入一个数据让它绘出renderer
        //3
        dataset.addSeries(0, xyseries);
        datarenderer.setColor(Color.GREEN);
        datarenderer.setPointStyle(PointStyle.POINT);
        datarenderer.setLineWidth(4.0f);
        //4
        renderer.addSeriesRenderer(datarenderer);
        new Thread(){
            public void run() {
                for(int i=0;i<loopAmount;i++) {
                    initStandardLine();
                }
            };
        }.start();
        renderer.setShowLegend(false);
        //刚开始渲染的时候禁止用户操作（包括缩放和移动），不然容易报错
        renderer.setPanEnabled(true);
        renderer.setExternalZoomEnabled(false);
        renderer.setClickEnabled(false);
        renderer.setZoomEnabled(false, false);
        //5
        
        

        //设置按钮 用来记录尺寸的标准(value)和阈值(bound),然后再主界面显示出红线

        //绘出两条红线 分别用新的XYSeriesRenderer要不然会报错
   
        
        /*xyseries_up = new XYSeries("最大压力");
        xyseries_down = new XYSeries("最小压力");
        xyseries_up.add(X_MIN, value + normalTimes * bound);
        xyseries_up.add(X_MAX, value + normalTimes * bound);
        xyseries_down.add(X_MIN, value - normalTimes * bound);
        xyseries_down.add(X_MAX, value - normalTimes * bound);
        dataset.addSeries(1, xyseries_up);
        dataset.addSeries(2, xyseries_down);

        datarenderer_up = new XYSeriesRenderer();
        datarenderer_down = new XYSeriesRenderer();
        datarenderer_up.setColor(Color.RED);
        datarenderer_down = datarenderer_up;
        renderer.addSeriesRenderer(datarenderer_up);
        renderer.addSeriesRenderer(datarenderer_down);*/
        renderer.setYAxisMin(value - (boundTimes * bound));// Y最小值
        renderer.setYAxisMax(value + (boundTimes * bound));// Y最小值
        //设置chart的视图范围  参数//1x->start 2max 3y->start 4max 
        renderer.setRange(new double[]
        {(double)X_MIN, (double) X_MAX, value - (boundTimes * bound), value + (boundTimes * bound)});
        
        chartview = ChartFactory.getLineChartView(context, dataset, renderer);
        chartLineLay.addView(chartview, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        
        chartview.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event)  {
                topLineLay.invalidate();
                System.out.println("touchhhhh");
                return false;
            }
        });
        chartview.repaint();
    }
    
    public void initStandardLine() {
        //初始化横线
        for(int i=0;i < mArrayList.size();i++){
            PressInfo mInfo = mArrayList.get(i);
            //因为一次能画出的最大值为30，所以如果需要画的值大于30，则分几次画
            if(mInfo.getPressTime() > max_Interval) {
                for(int j=0;j < mInfo.getPressTime()/max_Interval;j++) {
                    initBaseLine(currentStandardLength, mInfo.getPressValue(), currentStandardLength+max_Interval, mInfo.getPressValue());
                    currentStandardLength += max_Interval;
                }
                if(mInfo.getPressTime()%max_Interval != 0) {
                    initBaseLine(currentStandardLength, mInfo.getPressValue(), currentStandardLength+mInfo.getPressTime()%max_Interval, mInfo.getPressValue());
                    currentStandardLength += mInfo.getPressTime()%max_Interval;
                }
            }else {
                initBaseLine(currentStandardLength, mInfo.getPressValue(), currentStandardLength+mInfo.getPressTime(), mInfo.getPressValue());
                currentStandardLength += mInfo.getPressTime();
            }
            /*if(i == 0 && mInfo.getPressTime() > X_MAX) {
                mXySeries.add(currentStandardLength,mInfo.getPressValue());
                mXySeries.add(X_MAX, mInfo.getPressValue());
                
                XYSeriesRenderer mRenderer = new XYSeriesRenderer();
                mRenderer.setColor(Color.RED);
                dataset.addSeries(mXySeries);
                renderer.addSeriesRenderer(mRenderer);
                break;
            }
            
            if(currentStandardLength <= X_MAX) {
                mXySeries.add(currentStandardLength,mInfo.getPressValue());
                mXySeries.add(currentStandardLength+mInfo.getPressTime(), mInfo.getPressValue());
                
                XYSeriesRenderer mRenderer = new XYSeriesRenderer();
                mRenderer.setColor(Color.RED);
                dataset.addSeries(mXySeries);
                renderer.addSeriesRenderer(mRenderer);
            }else {
                mXySeries.add(currentStandardLength-mInfo.getPressTime(),mInfo.getPressValue());
                mXySeries.add(X_MAX, mInfo.getPressValue());
                
                XYSeriesRenderer mRenderer = new XYSeriesRenderer();
                mRenderer.setColor(Color.RED);
                dataset.addSeries(mXySeries);
                renderer.addSeriesRenderer(mRenderer);
                break;
            }*/
           
        }
        //初始化竖线
        for(int i = 0; i < mArrayList.size() - 1;i++) {
            currentVerticalX += mArrayList.get(i).getPressTime();
            initBaseLine(currentVerticalX, mArrayList.get(i).getPressValue(), currentVerticalX, mArrayList.get(i+1).getPressValue());
           
        }
    }
    
    public void initBaseLine(double startX, double startY, double endX, double endY ) {
        XYSeries mXySeries = new XYSeries("");
        mXySeries.add(startX,startY);
        mXySeries.add(endX, endY);
        
        XYSeriesRenderer mRenderer = new XYSeriesRenderer();
        mRenderer.setColor(Color.RED);
        mRenderer.setLineWidth(2.0f);
        dataset.addSeries(mXySeries);
        renderer.addSeriesRenderer(mRenderer);
    }
    
    public void initEvent() {
        topLineLay.invalidate();
//        chartview.setClickable(true);
//      connectPressDevice();
//      pressThread.start();
//      setVisibility();
    }
    
    /**
     * 设置几个button的显示与隐藏
     * 
     * @param status
     */
    private void setVisibility() {
        if (isConnected()) {
            btoothName.setText(btName);
            btoothState.setBackgroundResource(R.drawable.id_bluetooth_connect);
        } else {
            btoothName.setText("未连接");
            btoothState.setBackgroundResource(R.drawable.id_bluetooth_unconnect);
        }
    }
    /**
     * 连接到设备
     */
    private void connectPressDevice() {
        if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {// 空闲状态才连接
            String address = cache.getDeviceAddress(Cache.PressDevice);
            System.out.println("address = " + address);
            if(address != null) {
                btMac = address;
                BluetoothDevice device = mBluetoothService
                        .getRemoteDeviceByAddress(address);
                if(device != null) {
                btName = device.getName();
                System.out.println("btName = " +btName);
                mBluetoothService.connect(device);
                }
            }
        }
    }
    protected void onResume() {
        if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
           }
           super.onResume();
    };
    //handler处理UI更新
    Handler chartHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//          Bundle b=msg.getData();
//          double x=b.getDouble("part_X");
//          double y=b.getDouble("part_Y");
            
            chartview.invalidate();
            topLineLay.invalidate();
//           chartview.repaint();
            //part_id1.setText(x);
            //part_size1.setText(y);
             
            if(currentStep >= X_MAX) {//延长X_MAX造成右移效果

                X_MIN += 5;
                X_MAX += 5;//按2倍速度延长 可以设置成speed
                renderer.setXAxisMax(X_MAX);// 设置X最大值
                renderer.setXAxisMin(X_MIN);
                
//                datarenderer_up = new XYSeriesRenderer();
//                datarenderer_down = new XYSeriesRenderer();
//                datarenderer_up.setColor(Color.RED);
//                datarenderer_down=datarenderer_up;
            }
        }
        
    };
    public class PressRunnable implements Runnable {
        BigDecimal b;
        private boolean flag = true;
        private boolean suspendFlag = false; //控制线程的暂停和继续
        
        public void setSuspendFlag() {
            this.suspendFlag = true;
        }
        
        public synchronized void setResume() {
            this.suspendFlag = false;
            this.notify();
        }
        public void run() {
            try {
                if(!isConnected()) {
                    return;
                }
                while (flag) {
                    synchronized (this) {
                        while (suspendFlag) {
                            this.wait();
                        }
                        currentStep = currentStep + xGap;
                        System.out.println("currentStep"+currentStep);
                        /*if (i > 1100) {
                            i = 100;
                            xyseries.clear();
                        }*/
                        Thread.sleep((int)timeInterval);
                        addX = currentStep;
                        //设置好下一个需要增加的节点
                        
//                        b = new BigDecimal(((Math.random()) * bound)); //转换
//                        addY = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
//                        //double+double再转一次
//                        b = new BigDecimal(addY + value); //保留三位小数 四舍五入
//                        addY = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();

                        // 步骤不能变 1先清除数据 2保存原来的数据 3添加新数据 4添加原来的数据 5添加所有数据 
                        //1
                        dataset.removeSeries(xyseries);
                        //2首先保存原数据
                        for(int i=0;i<xyseries.getScaleNumber();i++) {
                            oldX.add((double)xyseries.getX(i));
                            oldY.add((double)xyseries.getY(i));
                        }
                     // 点集先清空，为了做成新的点集而准备
                        //3
                        xyseries.add(addX, addY);
                        //4
                        for(int i=0;i<oldX.size();i++) {
                            xyseries.add(oldX.get(i),oldY.get(i));
                        }
                        //5
                        dataset.addSeries(0,xyseries);
                        //传给hanlder
                       Message message = chartHandler.obtainMessage();
                       message.what = 1;//标志是哪个线程传数据  
                       chartHandler.sendMessage(message);//发送message信息  
                    }
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    
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
                    //设置好下一个需要增加的节点
                    b = new BigDecimal(((Math.random()) * bound * 4) - (2 * bound)); //转换
                    addY = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();
                    //double+double再转一次
                    b = new BigDecimal(addY + value); //保留三位小数 四舍五入
                    addY = b.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue();

                    dataset.removeSeries(xyseries);
                    xyseries.add(addX, addY);
                    dataset.addSeries(0, xyseries);
                    //传给hanlder
                    Message message = chatHandler.obtainMessage();
                    //                 Bundle bundle=new Bundle();  
                    //                 bundle.putDouble("part_X", addX);  
                    //                 bundle.putDouble("part_Y", addY); 
                    //                 message.setData(bundle);//bundle传值，耗时，效率低  
                    message.what = 1;//标志是哪个线程传数据  
                    chatHandler.sendMessage(message);//发送message信息  
                    //chartview.repaint();
                    //                 chartview.postInvalidate();
                     //步骤不能变 1先清除数据 2添加源数据 3添加数据
                     dataset.removeSeries(xyseries);
                     xyseries.add(addX, addY); 
                     dataset.addSeries(0,xyseries);
                     //chartview.repaint();
                     chartview.postInvalidate();
                     if(i*2>X_MAX)//延长X_MAX造成右移效果
                     {
                         X_MAX*=2;//按2倍速度延长 可以设置成speed
                         renderer.setXAxisMax(X_MAX);// 设置X最大值
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
        protected void onStop() {
            pressThread.interrupt();
//          dataset.clear();
//          renderer.removeAllRenderers();
//          chartLineLay.removeAllViews();
//          chartLineLay = null;
            super.onStop();
        };
        @Override
        protected void onDestroy() {
            pressThread.interrupt();
            BluetoothService.close();
//          dataset.clear();
//            renderer.removeAllRenderers();
            System.out.println("destory");
            
            super.onDestroy();
        }
}