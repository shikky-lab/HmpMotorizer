package jp.co.ricoh.hmp.test.model;

import android.arch.lifecycle.LifecycleObserver;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.UUID;

import jp.co.ricoh.hmp.test.util.BleUuid;

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

    /**
     * 選択したBluetooth機器
     */
    private BluetoothDevice mBtDevice = null;

    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private static final UUID MY_UUID_SECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_INSECURE =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device
    private int mState;

//    /**
//     * アクテビティ
//     */
//    MainActivity mActivity = null;

//    /**
//     * エラー状態
//     */
//    HmpCommand.DeviceStatus mError = HmpCommand.DeviceStatus.DISCONNECTED;

    //callback共用変数
    private int mStatus;
    private Handler mUiThreadHandler = null;

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
        /* プリンタアダプタリスナレジスタ */
        Logger.i(TAG, "PrinterManager()- info HMPSDK : APP->SDK:  Register Listener by setListener().");
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
     * 使用しているBluetooth機器取得
     *
     * @return Bluetooth機器
     */
    public synchronized BluetoothDevice getBtDevice() {
        return mBtDevice;
    }

//    /**
//     * 接続確認
//     *
//     * 保留
//     * 参考：https://codeday.me/jp/qa/20190406/567763.html
//     * @return 接続確認結果
//     */
//    public synchronized boolean isConnected() {
//        /* プリンタ接続を判断 */
//        Logger.i(TAG, "isConnected()- info HMPSDK : APP->SDK:  Printer is connected by getConnection().");
//        return mBtDevice != null && mBtDevice.getBondState();
//    }

    /**
     * 接続
     *
     * @param device BluetoothDevice
     */
    public synchronized void connect(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        mBtDevice = device;

        if (mState == STATE_CONNECTING) {
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

//        mConnectThread = new ConnectThread(device,false);
        mConnectThread = new ConnectThread(device,true);
        mConnectThread.start();

    }

    /**
     * 切断
     */
//    public synchronized void disconnect() {
//        mConnGatt.disconnect();
//    }

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

    public synchronized void read(){
        if(mStatus != BluetoothProfile.STATE_CONNECTED) {
            return;
        }
    }

    private void connectionLost() {
        Runnable myRunnable = () -> Toast.makeText(mContext, "Device connection was lost", Toast.LENGTH_SHORT).show();
        getUiThreadHandler().post(myRunnable);

        mState = STATE_NONE;

        // Start the service over to restart listening mode
        BtDeviceManager.this.start();
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device,boolean secure) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            MY_UUID_SECURE);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
                }
            } catch (IOException e) {

                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
            } catch (IOException connectException) {
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                }
                Log.e(TAG, "unable to close() " + mSocketType +
                        " socket during connection failure",connectException);
                Runnable myRunnable = () -> Toast.makeText(mContext, "Failed to connect", Toast.LENGTH_SHORT).show();
                getUiThreadHandler().post(myRunnable);
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
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    public synchronized void start() {
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
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
            mState = STATE_CONNECTED;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    // Read from the InputStream
                    bytes = mmInStream.read(buffer);
                    if(bytes != 0){
                        Runnable myRunnable = () -> Toast.makeText(mContext, "Read"+ new String(buffer), Toast.LENGTH_SHORT).show();
                        getUiThreadHandler().post(myRunnable);
                    }

                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(byte[] bytes) {
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }
    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     */
    public synchronized void connected(BluetoothSocket socket) {

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
    }
}
