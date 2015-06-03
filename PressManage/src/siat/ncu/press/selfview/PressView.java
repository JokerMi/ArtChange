package siat.ncu.press.selfview;

import siat.ncu.press.main.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.LinearLayout;

public class PressView extends LinearLayout {
    private EditText pressValueEdt;
    private EditText pressTimeEdt;

    public PressView(Context context) {
        super(context);
    }
    public PressView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(R.layout.item_press, this);
        pressValueEdt = (EditText) findViewById(R.id.edtTxt_PressValue);
        pressTimeEdt = (EditText) findViewById(R.id.edtTxt_PressTime);
    }
    
    @SuppressLint("NewApi")
    public PressView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    public String getPressValue() {
        return pressValueEdt.getText()+"";
    }
    public String getPressTime() {
        return pressTimeEdt.getText()+"";
    }
    
    public void setPressValue(String value) {
        pressValueEdt.setText(value);
    }
    public void setPressTime(String time) {
        pressTimeEdt.setText(time);
    }

}
