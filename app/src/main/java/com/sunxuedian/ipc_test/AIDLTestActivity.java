package com.sunxuedian.ipc_test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.sunxuedian.ipc_test.adapter.BookListAdapter;
import com.sunxuedian.ipc_test.aidl.Book;
import com.sunxuedian.ipc_test.aidl.IBookManager;
import com.sunxuedian.ipc_test.service.BookManagerService;

public class AIDLTestActivity extends AppCompatActivity {

    private IBookManager mBookManager;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mBookManager = IBookManager.Stub.asInterface(iBinder);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    private ListView mLvBooks;
    private BookListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aidltest);
        //绑定服务
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        //调用addBook接口
        findViewById(R.id.btn_add_book).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText mEtName = (EditText) findViewById(R.id.et_book_name);
                EditText mEtPrice = (EditText) findViewById(R.id.et_bool_price);
                Book book = new Book(mEtName.getText().toString(), Integer.parseInt(mEtPrice.getText().toString()));
                try {
                    mBookManager.addBook(book);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        mLvBooks = (ListView) findViewById(R.id.lv_book_list);
        mAdapter = new BookListAdapter(this);
        mLvBooks.setAdapter(mAdapter);

        //调用getBookList()接口
        findViewById(R.id.btn_get_book_list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mAdapter.setData(mBookManager.getBookList());
                    mAdapter.notifyDataSetChanged();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }
}
