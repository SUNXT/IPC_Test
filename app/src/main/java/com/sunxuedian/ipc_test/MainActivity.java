package com.sunxuedian.ipc_test;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.sunxuedian.ipc_test.service.MessengerService;
import com.sunxuedian.ipc_test.utils.MyConstants;
import com.sunxuedian.ipc_test.utils.ToastUtils;

public class MainActivity extends AppCompatActivity {

    private static final String Tag = "MainActivity";

    /**
     * 与服务端进行连接
     */
    private Messenger mService;
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = new Messenger(iBinder);//获取到服务端的Service
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    /**
     * 接收服务端来的信息
     */
    private Messenger mGetReplyMessenger = new Messenger(new MessengerHandler());
    private static class MessengerHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MyConstants.MSG_FROM_SERVICE:
                    Log.d(Tag, "这是来自MessengerService的回复：" + msg.getData().getString(MyConstants.SERVICE_REPLY));
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //启动服务
        Intent intent = new Intent(MainActivity.this, MessengerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //向服务端发送消息
        findViewById(R.id.btn_ipc_messenger).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Message msg = Message.obtain(null, MyConstants.MSG_FROM_CLIENT);
                Bundle data = new Bundle();
                data.putString(MyConstants.CLIENT_MSG, "这是点击后发送的消息，也是从MainActivity中发送出来的！");
                msg.setData(data);
                msg.replyTo = mGetReplyMessenger;//用于接收MessengerService回复的消息
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        });

        findViewById(R.id.btn_ipc_aidl).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AIDLTestActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }
}
