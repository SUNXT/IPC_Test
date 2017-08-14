package com.sunxuedian.ipc_test.aidl;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 注意，这里的包名必须和main/aidl/..包名一样
 * 也就是说 这里的包名应该为 com.sunxuedian.ipc_test.aidl 不然Build 项目的时候会找不到Book这个类
 * Created by sunxuedian on 2017/8/14.
 */

public class Book implements Parcelable{

    protected Book(Parcel in) {
        name = in.readString();
        price = in.readInt();
    }

    public Book(){};

    public Book(String name, int price){
        this.name = name;
        this.price = price;
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    private String name;
    private int price;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeInt(price);
    }

    @Override
    public String toString() {
        return "Book: " +name + ", and Price: " + price + "$";
    }
}
