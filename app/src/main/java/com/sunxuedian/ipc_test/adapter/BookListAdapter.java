package com.sunxuedian.ipc_test.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.sunxuedian.ipc_test.R;
import com.sunxuedian.ipc_test.aidl.Book;

import java.util.List;

/**
 * Created by sunxuedian on 2017/8/14.
 */

public class BookListAdapter extends BaseAdapter {

    List<Book> mData;
    Context mContext;

    public BookListAdapter(Context context){
        mContext = context;
    }

    public void setData(List<Book> data){
        mData = data;
    }

    @Override
    public int getCount() {
        if (mData != null){
            return mData.size();
        }
        return 0;
    }

    @Override
    public Book getItem(int i) {
        if (mData != null){
            return mData.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder holder;
        if (view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.book_item, null);
            holder = new ViewHolder();
            holder.tv_name = (TextView) view.findViewById(R.id.tv_book_name);
            holder.tv_price = (TextView) view.findViewById(R.id.tv_book_price);
            view.setTag(holder);
        }else {
            holder = (ViewHolder) view.getTag();
        }
        Book book = getItem(i);
        holder.tv_name.setText(book.getName());
        holder.tv_price.setText(String.valueOf(book.getPrice()));
        return view;
    }

    class ViewHolder{
        private TextView tv_name;
        private TextView tv_price;
    }
}
