package siat.ncu.press.bean;

import java.io.Serializable;

import siat.ncu.press.util.MyContents;

import android.os.Parcel;
import android.os.Parcelable;

public class PressInfo implements Parcelable{
    private String pressValue;
    private String pressTime;
    
    public PressInfo() {
    }
    
    public String getPressValue() {
        return pressValue;
    }

    public void setPressValue(String pressValue) {
        this.pressValue = pressValue;
    }

    public String getPressTime() {
        return pressTime;
    }

    public void setPressTime(String pressTime) {
        this.pressTime = pressTime;
    }

    public static Parcelable.Creator<PressInfo> getCreator() {
        return CREATOR;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(pressValue);
        dest.writeString(pressTime);
    }
    
    public static final Parcelable.Creator<PressInfo> CREATOR = new Parcelable.Creator<PressInfo>() {

        @Override
        public PressInfo createFromParcel(Parcel source) {
            return new PressInfo(source);
        }

        @Override
        public PressInfo[] newArray(int size) {
            return new PressInfo[size];
        }
        
    };
    private PressInfo(Parcel in) {
        pressValue = in.readString();
        pressTime = in.readString();
    }
}
