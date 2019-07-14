package jp.co.ricoh.hmp.test.model;

import android.arch.lifecycle.LifecycleObserver;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


import org.greenrobot.eventbus.EventBus;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Bluetooth接続機器管理(ESP32を想定)
 * - ひとまず1Managerで1Deviceの前提．余力あれば拡張
 * - デバイスとの接続/切断
 * - 書き込み
 * - 読み取り
 */
public class BtDeviceManager implements LifecycleObserver {

    /**
     * タグ
     */
    private static final String TAG = BtDeviceManager.class.getSimpleName();

    /**
     * インスタンス
     */
    private static BtDeviceManager sInstance = null;

    /**
     * コンテキストリファレンス
     */
    private final Context mContext;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private static final UUID SERIAL_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private enum CONNECTING_STATE{
        NONE,// we're doing nothing
        CONNECTING,// now initiating an outgoing connection
        CONNECTED// now connected to a remote device
    }
    private  CONNECTING_STATE mState;

    private Handler mUiThreadHandler = null;
    private RingBufferedBlockReader mBlockReader;

    /**
     * 初期化
     *
     * @param context コンテキスト
     */
    public static synchronized void initialize(Context context) {
        if (sInstance == null) {
            sInstance = new BtDeviceManager(context);
        }
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    private BtDeviceManager(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * インスタンス取得
     *
     * @return インスタンス
     */
    public static synchronized BtDeviceManager getInstance() {
        return sInstance;
    }

//    /**
//     * 復帰
//     *
//     * @param source ライフサイクルオーナー
//     */
//    @OnLifecycleEvent(ON_RESUME)
//    void onResume(LifecycleOwner source) {
//        if (source instanceof MainActivity) {
//            mActivity = (MainActivity) source;
//            /* アダプタの利用可否をチェック */
//            Logger.i(TAG, "onResume()- info HMPSDK : APP->SDK:  HmpAdapter is enabled by isEnabled().");
//            if (!mAdapter.isEnabled()) {
//                enable();
//            }
//        }
//    }

    /**
     * ペアリングされたBluetooth機器一覧取得
     *
     * @return ペアリングされたBluetooth機器一覧(Set)
     */
     public Set<BluetoothDevice> getBondedDevices(){
        BluetoothManager mBtManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);

        BluetoothAdapter mBTAdapter= mBtManager.getAdapter();
        return mBTAdapter.getBondedDevices();
    }

    /**
     * 接続確認
     *
     * @return 接続確認結果
     */
    public synchronized boolean isConnected() {
        return CONNECTING_STATE.CONNECTED==mState;
    }

    /**
     * 接続
     *
     * @param device BluetoothDevice
     */
    public synchronized void connect(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        if(mState == CONNECTING_STATE.CONNECTING){
            disConnect();
        }

        if (mState == CONNECTING_STATE.CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();

    }

    private Handler getUiThreadHandler(){
       if(mUiThreadHandler == null) {
           return new Handler(mContext.getMainLooper());
       }
       return mUiThreadHandler;
    }

    public synchronized  void write(String sendText){
        byte[] out = sendText.getBytes();
        if(mConnectedThread != null){
            mConnectedThread.write(out);
        }
    }
//    public synchronized String read(){
//        if(mState != CONNECTING_STATE.CONNECTED){
//            return null;
//        }
//
//        byte[] readData = new byte[2];
//        readData[0] =mBlockReader.read();
//        return new String(readData);
//    }

    public synchronized String readBlock(){
        if(mState != CONNECTING_STATE.CONNECTED){
            return null;
        }
        if(mBlockReader.getUnreadBlockNum() == 0){
            Log.d(TAG,"readBlock() called but there are no blocks");
            return null;
        }

        return new String(mBlockReader.readBlock());
    }

    private void connectionLost() {
        getUiThreadHandler().post(() -> Toast.makeText(mContext, "Device connection was lost", Toast.LENGTH_SHORT).show());

        mState = CONNECTING_STATE.NONE;
        // Start the service over to restart listening mode
        this.resetThreads();
    }

    private void disConnect(){
        getUiThreadHandler().post(() -> Toast.makeText(mContext, "Disconnect current connection", Toast.LENGTH_SHORT).show());
        mState = CONNECTING_STATE.NONE;
        this.resetThreads();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;

        ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                tmp = device.createRfcommSocketToServiceRecord( SERIAL_UUID);
            } catch (IOException e) {
                Log.e(TAG,   "create() failed", e);
            }
            mmSocket = tmp;
            mState = CONNECTING_STATE.CONNECTING;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:") ;
            setName("ConnectThread") ;

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure",e2);
                }
                Log.e(TAG, "unable to connect() socket during connection failure",connectException);
                getUiThreadHandler().post(() -> Toast.makeText(mContext, "Failed to connect", Toast.LENGTH_SHORT).show());
                this.start();
                return;
            }

            synchronized (BtDeviceManager.this) {
                mConnectThread = null;
            }
            // Do work to manage the connection (in a separate thread)
            connected(mmSocket);
        }

        /** Will cancel an in-progress connection, and close the socket */
        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) { }
        }
    }

    private synchronized void resetThreads() {
        Log.d(TAG, "resetThreads");

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }


    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mBlockReader = new RingBufferedBlockReader();
            mState = CONNECTING_STATE.CONNECTED;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    mBlockReader.write(buffer,bytes);
                    Event.post(Event.HAVE_READ_CHARACTER);
                    if(mBlockReader.getUnreadBlockNum() >0){
                        Event.post(Event.HAVE_READ_BLOCK);
                        Log.d(TAG,"read a block");
                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException ignored) { }
        }

        /* Call this from the main activity to shutdown the connection */
        void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) { }
        }
    }
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     */
    private synchronized void connected(BluetoothSocket socket) {

        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        getUiThreadHandler().post(() -> Toast.makeText(mContext, "Connected!!", Toast.LENGTH_SHORT).show());
    }

    public enum Event {
        CONNECTED,
        DISCONNECTED,
        HAVE_READ_BLOCK,
        HAVE_READ_CHARACTER;

        /**
         * イベント送信
         *
         * @param event イベント
         */
        static void post(Event event) {
            EventBus.getDefault().post(event);
        }
    }

    private class RingBufferedBlockReader {
        private static final int DEFAULT_BUF_SIZE=1024;
        private int mMax;
        private byte[] buf;
        private int readPos;//次に読み込む位置
        private int writePos;//次に書き込む位置
        private static final byte DEFAULT_EOL = ';';
        private byte eol = DEFAULT_EOL;
        private int unreadBlockNum=0;

        RingBufferedBlockReader(){
            this(DEFAULT_BUF_SIZE);
        }

        RingBufferedBlockReader(int bufSize){
           mMax=bufSize;
           buf=new byte[mMax];
        }

        private int getNextPos(int curPos){
            if(curPos == mMax){
                return 0;
            }
            return curPos+1;
        }

        void write(byte[] in, int length){
            for(int i=0;i<length;i++){
                if(in[i]=='\0'){
                    return;
                }
                buf[writePos]=in[i];
                writePos = getNextPos(writePos);
                if(in[i]==eol){
                    unreadBlockNum++;
                }
            }
        }

        byte read(){
            byte ret = buf[readPos];
            readPos= getNextPos(readPos);
            return ret;
        }

        byte[] readBlock(){
            byte[] ret = new byte[mMax];
            if(unreadBlockNum ==0){
                return null;
            }

            int i;
            for(i=0;;i++){
                ret[i] = this.read();
                if(ret[i]==eol){
                    unreadBlockNum--;
                    break;
                }
            }
            ret[i+1] = '\0';
            return ret;
        }

        int getUnreadBlockNum(){
            return unreadBlockNum;
        }
    }

}
