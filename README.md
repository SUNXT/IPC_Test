# IPC_Test
进程间通讯的概念就不在这里做介绍了。

## Android中单工程实现多线程的方法
四大组件（Activity、Service、Receiver、ContentProvider）在AndroidMenifest中指定android:process属性，便可实现多进程，process属性可以直接填写新的进程名（一个项目默认的进程名为项目包名）或者直接简写，如：android:process=":remote" 这样写的话，这个组件会创建在进程名为“xxx:xxx:remote”,其中“xxx:xxx”代表你的项目默认的包名。在这个项目中，我使用了Service的组件实现多进程。

## 在Android中实现进程间通讯有以下几种方法：
![image](https://github.com/SUNXT/IPC_Test/blob/master/所有进程间通讯的方法.jpg)

## 本项目主要从Messenger 和 AIDL 两个方法实现进程间通讯
### 1. Messenger方法实现进程间通讯，实现简单的message通讯，使用Messenger实现进程间通讯，一般用的是用Message来进行信息交流，如果要传自定义对象数据的话，对象类必须得实现Parcelable接口
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

### 2. 使用AIDL实现进程间通讯，其实上面的Messenger的底层也是AIDL，但是Messenger当遇到多个请求的话，只能串行处理，效率不高，而直接使用AIDL就可以并行处理。这个工程中主要通过在其他进程（非项目默认进程中）对自定义类Book的管理，通过客户端实现对服务端的书的管理，添加和查看书等操作，来演示进程间通讯。

#### AIDL文件的编写（开发的IDE为Android Studio）
（1）我们在src/main/java/包名 下新建一个包aidl（注意：这里一定要是aidl，不然后面构建项目会报找不到Book类，这里主要存放您要通过aidl通讯实现数据交换的自定义bean类），新建类Book.java，然后将该类实现Parcelable接口。
（2）直接在src/main 新建一个aidl文件，系统将自动在main目录下生成一个aidl目录，在该目录下有一个包名为xxx.xxx.aidl（xxx.xxx代表你的包名），里面为你新建的aidl文件。我们先新建一个Book.aidl，该aidl文件的作用为声明我们自定义的Book.java类，然后我们新建一个IBookManager.aidl文件,里面编写接口方法getBookList()和addBook(Book book);注意的是，必须import Book类完整的路径包名。
![image]()
##### Book.aidl 用parcelable声明Book类
```
// Book.aidl
package com.sunxuedian.ipc_test.aidl;

// Declare any non-default types here with import statements
parcelable Book;

```
##### IBookManager.aidl
```
// IBookManager.aidl
package com.sunxuedian.ipc_test.aidl;

// Declare any non-default types here with import statements
import com.sunxuedian.ipc_test.aidl.Book;

interface IBookManager {

    List<Book> getBookList();
    void addBook(in Book book);
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);

}
```
（3）构建你的项目，（Make Project）如果没问题的话，应该能构建成功，并在app/build/generated/source目录下找到aidl文件夹，并在aidl/debug中找到对应的包和你定义的IBookManager.java
![image]()

#### 可以开始编写客户端和服务端的代码了（兴奋）

#### 先写服务端，和Messenger写法有点相似
新建一个BookManagerService类继承于Service，在AndroidMenifest文件中声明为:remote_2进程，编写代码，新建一个Binder对象实现IBookManager.Stub接口
```
public class BookManagerService extends Service {

    private CopyOnWriteArrayList<Book> mBookList = new CopyOnWriteArrayList<>();

    private Binder mBinder = new IBookManager.Stub(){

        @Override
        public List<Book> getBookList() throws RemoteException {
            return mBookList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            mBookList.add(book);
        }

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }
    };


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
}
```
#### 客户端的编写
（1）声明一个IBookManager的对象mBookManager，在用ServiceConnection建立起连接的时候将IBinder对象通过IBookManager.Stub.asInterface(iBinder)赋值给mBookManager，代码如下
```
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
```
（2）bindService后，服务建立起连接，然后你就可以通过mBookManager来进行进程通讯了，可调用远端进程的服务接口方法 addBook(Book book) 和 getBookList()了
```

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


```
### ---END---
