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
import android.util.Log;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

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
     * リクエストコード
     */
//    static final int REQUEST_ENABLE_BT = 1;

    /**
     * インスタンス
     */
    static BtDeviceManager sInstance = null;

    /**
     * コンテキストリファレンス
     */
    final Context mContext;

    /**
     * デバイスアダプタ
     */
    private BluetoothAdapter mBTAdapter;

    /**
     * 接続タスク
     */
    ScheduledFuture mConnectFuture = null;

    /**
     * 選択したBluetooth機器
     */
    BluetoothDevice mBtDevice = null;
    BluetoothGattCharacteristic targetChar1 = null;
    BluetoothGattCharacteristic targetChar4 = null;

    /**
     * アクテビティ
     */
//    MainActivity mActivity = null;

    /**
     * エラー状態
     */
//    HmpCommand.DeviceStatus mError = HmpCommand.DeviceStatus.DISCONNECTED;

    /**
     * Bluetooth使用可否
     */
    boolean isBluetoothEnable = false;

    //callback共用変数
    private int mStatus;
    private BluetoothGatt mConnGatt;

    private final BluetoothGattCallback mGattcallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                mStatus = newState;
                mConnGatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mStatus = newState;
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
                    targetChar1 = service.getCharacteristic(UUID.fromString(BleUuid.CHAR_SAMPLE_R));
                    targetChar4 = service.getCharacteristic(UUID.fromString(BleUuid.CHAR_SAMPLE_RWN));
                    registerNotification(targetChar4);
//                        Logger.i(TAG, "read Char_sample_rwn:" + targetChar.getStringValue(0));
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (BleUuid.CHAR_MANUFACTURER_NAME_STRING
                        .equalsIgnoreCase(characteristic.getUuid().toString())) {
                    final String name = characteristic.getStringValue(0);

                } else if (BleUuid.CHAR_SERIAL_NUMBER_STRING
                        .equalsIgnoreCase(characteristic.getUuid().toString())) {
                    final String name = characteristic.getStringValue(0);

                } else if (BleUuid.CHAR_SAMPLE_RWN.equalsIgnoreCase(characteristic.getUuid().toString())) {
//                    final String readData = characteristic.getStringValue(0);
                    final byte[] readData = characteristic.getValue();
                    ByteBuffer bb = ByteBuffer.wrap(readData);
                    Logger.i(TAG, "read data:" + bb);
//                    Toast.makeText(mContext, "read data:" + readData, Toast.LENGTH_SHORT).show();
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

//            if (BleUuid.CHAR_SAMPLE_RWN.equalsIgnoreCase(characteristic.getUuid().toString())) {
//                final String readData4 = characteristic.getStringValue(0);
//                Logger.i(TAG, "read data:" + readData4);
//            }
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
    BtDeviceManager(Context context) {
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

    /**
     * 復帰
     *
     * @param source ライフサイクルオーナー
     */
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
        mBTAdapter = mBtManager.getAdapter();
        return mBTAdapter.getBondedDevices();
    }

    /**
     * 使用しているBluetooth機器取得
     *
     * @return Bluetooth機器
     */
//    public synchronized BluetoothDevice getBtDevice() {
//        return mBtDevice;
//    }

    /**
     * 接続確認
     *
     * 保留
     * 参考：https://codeday.me/jp/qa/20190406/567763.html
     * @return 接続確認結果
     */
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

        //接続
        if ((mConnGatt == null)
                && (mStatus == BluetoothProfile.STATE_DISCONNECTED)) {
            // try to connect
            Toast.makeText(mContext, "Connect Start" , Toast.LENGTH_SHORT).show();
            mConnGatt = mBtDevice.connectGatt(mContext, false, mGattcallback);
            mStatus = BluetoothProfile.STATE_CONNECTING;
        } else {
            if (mConnGatt == null){
                Log.e(TAG, "state error");
                return;
            }

            if(mStatus == BluetoothProfile.STATE_CONNECTED){
                //write some sample
                Toast.makeText(mContext, "Already connecting, write SAMPLE", Toast.LENGTH_SHORT).show();

                if(targetChar4!=null){

                    mConnGatt.readCharacteristic(targetChar4);
                    targetChar4.setValue("Hello BLE".getBytes());
                    mConnGatt.writeCharacteristic(targetChar4);
                }
            }else if(mStatus == BluetoothProfile.STATE_DISCONNECTED){
                // re-connect and re-discover Services
                Toast.makeText(mContext, "Re connect Start" , Toast.LENGTH_SHORT).show();
                mConnGatt.connect();
                mConnGatt.discoverServices();
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
//        mConnGatt.close();
    }

    private void registerNotification(BluetoothGattCharacteristic mChar){
        // ペリフェラルのnotificationを有効化する。下のUUIDはCharacteristic Configuration Descriptor UUIDというもの
        BluetoothGattDescriptor descriptor = mChar.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
        Logger.i(TAG, "current descriptor :"+descriptor.getValue());

        // Androidフレームワークに対してnotification通知登録を行う, falseだと解除する
        mConnGatt.setCharacteristicNotification(mChar, true);

        // characteristic のnotification 有効化する
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mConnGatt.writeDescriptor(descriptor);
    }
}
