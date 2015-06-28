package siat.ncu.press.main;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

public class WelcomeActivity extends Activity{

    private TextView versionTv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_welcome);
        initView();
        initData();
        initEvent();
    }
    
    private void initView() {
        try {
        PackageManager pm = getPackageManager();
        PackageInfo pi;
        pi = pm.getPackageInfo(this.getPackageName(), 0);
        versionTv = (TextView) findViewById(R.id.versionNumber);
        versionTv.setText("Version " + pi.versionName);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void initData() {
        
    }

    private void initEvent() {
        
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Intent intent = new Intent(WelcomeActivity.this,PressMainActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        }, 2500);
    }
    
}
