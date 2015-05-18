package siat.ncu.press.main;

import java.util.ArrayList;

import siat.ncu.press.bean.PressInfo;
import siat.ncu.press.selfview.PressView;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class InfoSetActivity extends Activity implements OnClickListener{
    
    private Spinner sampleRateSpn;
    private EditText sampleRateEdtTxt;
    private ArrayAdapter sampleRateAdapter;
    private PressView pv1;
    private PressView pv2;
    private PressView pv3;
    private Button compBtn;
    private Button addPressBtn;
    private Button delPressBtn;
    private LinearLayout pressLineLay;
    
    private static final int PV_NORMAL_AMOUNT = 2;
    private int pvCurrentAmount; 
    private String sampleRateStr;
    private static final int DEFAULT_PV1_VALUE = 30;
    private static final int DEFAULT_pv1_TIME = 60;
    private static final int DEFAULT_PV2_VALUE = 0;
    private static final int DEFAULT_pv2_TIME = 60;
     
    private ArrayList<PressInfo> mArrayList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_setinfo);
        initView();
        initData();
        initEvent();
    }
    private void initView() {
        sampleRateSpn = (Spinner)findViewById(R.id.spn_SampleRate);
        sampleRateSpn.setVisibility(View.VISIBLE);
        
        sampleRateEdtTxt = (EditText)findViewById(R.id.edtTxt_sampleRate);
        
        pressLineLay = (LinearLayout)findViewById(R.id.linelay_press);
        pv1 = (PressView) findViewById(R.id.pv1);
        pv2 = (PressView) findViewById(R.id.pv2);
        pv3 = (PressView) findViewById(R.id.pv3);
        pv1.setPressValue(DEFAULT_PV1_VALUE+"");
        pv1.setPressTime(DEFAULT_pv1_TIME + "");
        pv2.setPressValue(DEFAULT_PV2_VALUE + "");
        pv2.setPressTime(DEFAULT_pv2_TIME+"");
        
        compBtn = (Button)findViewById(R.id.btn_comp);
        addPressBtn = (Button)findViewById(R.id.btn_addpress);
        delPressBtn = (Button)findViewById(R.id.btn_delpress);
    }
    private void initData() {
        sampleRateAdapter = ArrayAdapter.createFromResource(InfoSetActivity.this, R.array.array_sample_rate,
                android.R.layout.simple_spinner_item);
        sampleRateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sampleRateSpn.setAdapter(sampleRateAdapter);
        
        pvCurrentAmount = PV_NORMAL_AMOUNT;
        mArrayList = new ArrayList<PressInfo>();
    }
    private void initEvent() {
        sampleRateSpn.setOnItemSelectedListener(new SpinnerSamplingRateListener());
        compBtn.setOnClickListener(this);
        addPressBtn.setOnClickListener(this);
        delPressBtn.setOnClickListener(this);
    }
    
    public class SpinnerSamplingRateListener implements OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            TextView tv = (TextView) view;
            tv.setTextSize(getResources().getDimension(R.dimen.spinner_size)); //设置大小  
            tv.setGravity(android.view.Gravity.CENTER); //设置居中  
            sampleRateStr =tv.getText()+"";
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            
        }
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_comp:
                mArrayList = getPressInfoList();
                if(mArrayList.size() == 0) {
                    Toast.makeText(InfoSetActivity.this, getResources().getString(R.string.add_press), Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent mIntent = new Intent();
                mIntent.putParcelableArrayListExtra("pressinfo", mArrayList);
                mIntent.putExtra("sampleRate", sampleRateStr);
                mIntent.setClass(InfoSetActivity.this, MainActivity.class);
                startActivity(mIntent);
                break;
            case R.id.btn_addpress:
                addPressView();
                pvCurrentAmount++;
                break;
            case R.id.btn_delpress:
                if(pvCurrentAmount == PV_NORMAL_AMOUNT){
                    Toast.makeText(InfoSetActivity.this, getResources().getString(R.string.default_amount),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                delPressView();
                pvCurrentAmount--;
                break;
            default:
                break;
        }
    }
    protected ArrayList<PressInfo> getPressInfoList() {
        ArrayList<PressInfo> mList = new ArrayList<PressInfo>();
        for(int i=0;i < pvCurrentAmount;i++) {
            PressView mPressView = (PressView)pressLineLay.getChildAt(i);
            if(mPressView != null && !"".equals(mPressView.getPressValue()) && !"".equals(mPressView.getPressValue())) {
                PressInfo mInfo = new PressInfo();
                mInfo.setPressValue(mPressView.getPressValue());
                mInfo.setPressTime(mPressView.getPressTime());
                mList.add(mInfo);
                System.out.println("value:"+mPressView.getPressValue()+"  time" + mPressView.getPressTime());
            }
        }
        return mList;
    }
    protected void addPressView() {
        PressView mPressView = new PressView(InfoSetActivity.this,null);
        LayoutParams mParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        pressLineLay.addView(mPressView, mParams);
    }
    
    protected void delPressView() {
        pressLineLay.removeViewAt(pvCurrentAmount-1);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
