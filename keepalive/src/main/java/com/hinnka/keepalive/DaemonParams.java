package com.hinnka.keepalive;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

public class DaemonParams implements Parcelable {

    public String[] fileList;
    public String packageName;
    public String processName;
    public String tmpDirPath;
//    public Intent c;
//    public Intent d;
//    public Intent e;

    public DaemonParams() {
    }

    public DaemonParams(Parcel parcel) {
        this.fileList = parcel.createStringArray();
        this.packageName = parcel.readString();
        this.processName = parcel.readString();
        this.tmpDirPath = parcel.readString();
//        if (parcel.readInt() != 0) {
//            this.c = (Intent) Intent.CREATOR.createFromParcel(parcel);
//        }
//        if (parcel.readInt() != 0) {
//            this.d = (Intent) Intent.CREATOR.createFromParcel(parcel);
//        }
//        if (parcel.readInt() != 0) {
//            this.e = (Intent) Intent.CREATOR.createFromParcel(parcel);
//        }
    }

    public static final Creator<DaemonParams> CREATOR = new Creator<DaemonParams>() {
        @Override
        public DaemonParams createFromParcel(Parcel in) {
            return new DaemonParams(in);
        }

        @Override
        public DaemonParams[] newArray(int size) {
            return new DaemonParams[size];
        }
    };

    public static DaemonParams parse(String str) {
        Parcel obtain = Parcel.obtain();
        byte[] decode = Base64.decode(str, 2);
        obtain.unmarshall(decode, 0, decode.length);
        obtain.setDataPosition(0);
        DaemonParams createFromParcel = CREATOR.createFromParcel(obtain);
        obtain.recycle();
        return createFromParcel;
    }

    public int describeContents() {
        return 0;
    }

    public String toString() {
        Parcel obtain = Parcel.obtain();
        writeToParcel(obtain, 0);
        String encodeToString = Base64.encodeToString(obtain.marshall(), 2);
        obtain.recycle();
        return encodeToString;
    }

    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeStringArray(this.fileList);
        parcel.writeString(this.packageName);
        parcel.writeString(this.processName);
        parcel.writeString(this.tmpDirPath);
//        if (this.c == null) {
//            parcel.writeInt(0);
//        } else {
//            parcel.writeInt(1);
//            this.c.writeToParcel(parcel, 0);
//        }
//        if (this.d == null) {
//            parcel.writeInt(0);
//        } else {
//            parcel.writeInt(1);
//            this.d.writeToParcel(parcel, 0);
//        }
//        if (this.e == null) {
//            parcel.writeInt(0);
//            return;
//        }
//        parcel.writeInt(1);
//        this.e.writeToParcel(parcel, 0);
    }
}
