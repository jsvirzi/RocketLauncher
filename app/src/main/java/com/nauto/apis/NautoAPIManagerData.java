package com.nauto.apis;

import android.os.Parcel;
import android.os.Parcelable;

public class NautoAPIManagerData implements Parcelable {     
    /**
     * data value with int type
     */
    public int mValueInt;

    /**
     * data value with char type
     */
    public float mValueFloat;

    /**
     * data value with String type
     */
    public String mValueString;
    
    /**
     * data value with byte array type
     */
    public byte[] mValueByteArray;

    public NautoAPIManagerData() {}
    
    private NautoAPIManagerData(Parcel source) {
        mValueInt = source.readInt();
        mValueFloat = source.readInt();
        mValueString = source.readString();
        mValueByteArray = new byte[20];
        source.readByteArray(mValueByteArray);
    }

    public String toString() {
        return null;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mValueInt);
        dest.writeFloat(mValueFloat);
        dest.writeString(mValueString);
        dest.writeByteArray(mValueByteArray);
    }

    public void readFromParcel(Parcel in) {
        mValueInt = in.readInt();
        mValueFloat = in.readFloat();
        mValueString = in.readString();
        in.readByteArray(mValueByteArray);
    }

    public static final Creator<NautoAPIManagerData> CREATOR =
        new Creator<NautoAPIManagerData>() {
        public NautoAPIManagerData createFromParcel(Parcel source) {
            return new NautoAPIManagerData(source);
        }
        public NautoAPIManagerData[] newArray(int size) {
            return new NautoAPIManagerData[size];
        }
    };
}
