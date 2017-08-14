package com.sunxuedian.ipc_test.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.sunxuedian.ipc_test.utils.MyConstants;
import com.sunxuedian.ipc_test.utils.ToastUtils;

/**
 * 使用Messenger进行进程间通讯的Service类，运行在独立的线程
 *
 * Created by sunxuedian on 2017/8/14.
 */

public class MessengerService extends Service {

    private static String Tag = "MessengerService";
    private static class MessengerHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MyConstants.MSG_FROM_CLIENT:
                    String client_msg = msg.getData().getString(MyConstants.CLIENT_MSG);
                    Log.d(Tag, "from client message: " + client_msg);
                    Messenger client = msg.replyTo;
                    Message message = Message.obtain(null, MyConstants.MSG_FROM_SERVICE);
                    Bundle data = new Bundle();
                    data.putString(MyConstants.SERVICE_REPLY, "收到信息了，这是回复！");
                    message.setData(data);
                    try {
                        client.send(message);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private final Messenger mMessenger = new Messenger(new MessengerHandler());

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }
}
