# IPC_Test
进程间通讯的概念就不在这里做介绍了。

## Android中单工程实现多线程的方法
四大组件（Activity、Service、Receiver、ContentProvider）在AndroidMenifest中指定android:process属性，便可实现多进程，process属性可以直接填写新的进程名（一个项目默认的进程名为项目包名）或者直接简写，如：android:process=":remote" 这样写的话，这个组件会创建在进程名为“xxx:xxx:remote”,其中“xxx:xxx”代表你的项目默认的包名。在这个项目中，我使用了Service的组件实现多进程。

## 在Android中实现进程间通讯有以下几种方法：
![image](https://github.com/SUNXT/IPC_Test/blob/master/所有进程间通讯的方法.jpg)

## 本项目主要从Messenger 和 AIDL 两个方法实现进程间通讯
### 1. Messenger方法实现进程间通讯，实现简单的message通讯
#### 服务端的写法
（1）新建一个MessengerService类继承于Service，在AndroidMenifest中声明该服务
```
<service
        android:name=".service.MessengerService"
        android:process=":remote">
</service>
```
（2）在MessengerService中自定义一个Handler接收客户端发过来的信息，通过判断message的what来判断是哪个客户端发过来的消息。
```
private static class MessengerHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MyConstants.MSG_FROM_CLIENT:
                    String client_msg = msg.getData().getString(MyConstants.CLIENT_MSG);
                    Log.d(Tag, "from client message: " + client_msg);
                    Messenger client = msg.replyTo;//这是获取到客户端的Messenger对象，对客户端做出回复
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
```
（3）在MessengerService中整个一个Messenger变量，并用以上自定义的MessengerHandler初始化，并在Service中的onBind方法中返回Messenger的Binder对象，代码如下：
```
private final Messenger mMessenger = new Messenger(new MessengerHandler());

@Nullable
@Override
public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
}
```

#### 客户端的写法
你可在MainActivity中实现客户端代码，在这工程中，我只对通讯的结果进行打印，你可以自己做相应的操作
（1）定义一个ServiceConnection变量和一个Messenger来实现向和MessengerService建立起连接
```
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
```
（2）处理服务端回复的信息
```
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
```
（3）在MainActivity的onCreate()中bindService即可建立起连接（记得在OnDestroy()中进行unbindService操作），然后你可用用一个按钮来模拟客户端进程向服务端进程发送信息并查看日志。
```
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
```
#### 这样就完成了使用Messenger来实现进程间通讯！
