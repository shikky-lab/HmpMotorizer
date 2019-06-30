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
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;
import java.nio.ByteBuffer;
import java.util.Set;
import jp.co.ricoh.hmp.test.util.BleUuid;
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

    /**
     * 選択したBluetooth機器
     */
    private BluetoothDevice mBtDevice = null;
    private BluetoothGattCharacteristic mTargetCharacteristic = null;

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
    private BluetoothGatt mConnGatt;
    private Handler mUiThreadHandler = null;

    private final BluetoothGattCallback mGattcallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mStatus = newState;
                mConnGatt.discoverServices();

                Runnable myRunnable = () -> Toast.makeText(mContext, "Connected" , Toast.LENGTH_SHORT).show();
                getUiThreadHandler().post(myRunnable);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mStatus = newState;
                Runnable myRunnable = () -> Toast.makeText(mContext, "Disconnected" , Toast.LENGTH_SHORT).show();
                getUiThreadHandler().post(myRunnable);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            for (BluetoothGattService service : gatt.getServices()) {
                if ((service == null) || (service.getUuid() == null)) {
                    continue;
                }
                if (BleUuid.SERVICE_DEVICE_INFORMATION.equalsIgnoreCase(service
                        .getUuid().toString())) {
                    Logger.i(TAG, "read CHAR_MANUFACTURER_NAME_STRING:" + service.getCharacteristic(UUID.fromString(BleUuid.CHAR_MANUFACTURER_NAME_STRING)));
                    Logger.i(TAG, "read CHAR_SERIAL_NUMBER_STRING:" + service.getCharacteristic(UUID.fromString(BleUuid.CHAR_SERIAL_NUMBER_STRING)));
                }
                if (BleUuid.SERVICE_SAMPLE.equalsIgnoreCase(service
                        .getUuid().toString())) {
                    mTargetCharacteristic = service.getCharacteristic(UUID.fromString(BleUuid.CHAR_SAMPLE_RWN));
                    registerNotification(mTargetCharacteristic);
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (BleUuid.CHAR_SAMPLE_RWN.equalsIgnoreCase(characteristic.getUuid().toString())) {
                    final byte[] readData = characteristic.getValue();
                    ByteBuffer bb = ByteBuffer.wrap(readData);
                    Logger.i(TAG, "read data:" + bb);
                } else if (BleUuid.CHAR_SAMPLE_R.equalsIgnoreCase(characteristic.getUuid().toString())) {
                    final byte[] readData2 = characteristic.getValue();
                    ByteBuffer bb = ByteBuffer.wrap(readData2);
                    final String strChar = String.valueOf(bb.getShort());
                    Logger.i(TAG, "read data2:" + strChar);
                }
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);

            if (BleUuid.CHAR_SAMPLE_RWN.equalsIgnoreCase(characteristic.getUuid().toString())) {
                final String readData4 = characteristic.getStringValue(0);
                Logger.i(TAG, "read data:" + readData4);
            }
        }
    };

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
        if(mBtDevice != null && !device.equals(mBtDevice)){
            mBtDevice = null;
            mConnGatt.disconnect();
            Toast.makeText(mContext, "Other device selected.disconnecting" + mStatus, Toast.LENGTH_SHORT).show();
            return;
        }
        mBtDevice = device;

        //接続
        if (mConnGatt == null){
            // try to connect
            Toast.makeText(mContext, "connecting" , Toast.LENGTH_SHORT).show();
            mConnGatt = mBtDevice.connectGatt(mContext, false, mGattcallback);
            mStatus = BluetoothProfile.STATE_CONNECTING;
        } else {
            if(mStatus == BluetoothProfile.STATE_CONNECTED){
                if(mTargetCharacteristic !=null){
                    Toast.makeText(mContext, "Already connecting, write \"Hello BLE\"", Toast.LENGTH_SHORT).show();
                    mConnGatt.readCharacteristic(mTargetCharacteristic);
                    mTargetCharacteristic.setValue("Hello BLE".getBytes());
                    mConnGatt.writeCharacteristic(mTargetCharacteristic);
                }
            }else if(mStatus == BluetoothProfile.STATE_DISCONNECTED){
                // re-connect and re-discover Services
                Toast.makeText(mContext, "Re connect Start" , Toast.LENGTH_SHORT).show();
                mConnGatt.connect();
//                mConnGatt.discoverServices();
            }else{
                Toast.makeText(mContext, "Unexpected states: " + mStatus, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 切断
     */
    public synchronized void disconnect() {
        mConnGatt.disconnect();
    }

    private void registerNotification(BluetoothGattCharacteristic mChar){
        // ペリフェラルのnotificationを有効化する。下のUUIDはCharacteristic Configuration Descriptor UUIDというもの
        BluetoothGattDescriptor descriptor = mChar.getDescriptor(UUID.fromString(BleUuid.DESCRIPTOR_NOTIFICATION_ENABLED_STATE));
        Logger.i(TAG, "current descriptor :" + new String(descriptor.getValue()));

        // Androidフレームワークに対してnotification通知登録を行う, falseだと解除する
        mConnGatt.setCharacteristicNotification(mChar, true);

        // characteristic のnotification 有効化する
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mConnGatt.writeDescriptor(descriptor);
    }

    private Handler getUiThreadHandler(){
       if(mUiThreadHandler == null) {
           return new Handler(mContext.getMainLooper());
       }
       return mUiThreadHandler;
    }

    public synchronized  void write(String sendText){
        if(mStatus != BluetoothProfile.STATE_CONNECTED) {
            return;
        }

        if(sendText == null){
            sendText = "";
        }
        mTargetCharacteristic.setValue(sendText.getBytes());
        mConnGatt.writeCharacteristic(mTargetCharacteristic);
    }

    public synchronized void read(){
        if(mStatus != BluetoothProfile.STATE_CONNECTED) {
            return;
        }
        mConnGatt.readCharacteristic(mTargetCharacteristic);
    }
}
