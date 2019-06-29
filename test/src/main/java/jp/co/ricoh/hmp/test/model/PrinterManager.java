package jp.co.ricoh.hmp.test.model;

import android.app.Activity;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.OnLifecycleEvent;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import org.greenrobot.eventbus.EventBus;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import jp.co.ricoh.hmp.sdk.image.HmpImage;
import jp.co.ricoh.hmp.sdk.printer.HmpAdapter;
import jp.co.ricoh.hmp.sdk.printer.HmpCommand;
import jp.co.ricoh.hmp.sdk.printer.HmpPrinter;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings;
import jp.co.ricoh.hmp.test.MainActivity;

import static android.arch.lifecycle.Lifecycle.Event.ON_PAUSE;
import static android.arch.lifecycle.Lifecycle.Event.ON_RESUME;
import static jp.co.ricoh.hmp.sdk.printer.HmpPrinter.Connection.CONNECTED;

/**
 * プリンタデバイス管理
 * プリンタアダプタリスナのレジスタ。アプリ起動時、SDKのHmpAdapterのsetListenerでレジスタ
 * プリンタアダプタリスナでデバイスが見つけられることをlistenする
 *
 * プリンタデバイスリスナのレジスタ。プリント接続時、SDKのHmpPrinterのsetListenerでレジスタ
 * プリンタデバイスリスナでデバイスのステータスをlistenする
 */
public class PrinterManager implements LifecycleObserver {

    /**
     * タグ
     */
    private static final String TAG = PrinterManager.class.getSimpleName();

    /**
     * リクエストコード
     */
    static final int REQUEST_ENABLE_BT = 1;

    /**
     * インスタンス
     */
    static PrinterManager sInstance = null;

    /**
     * コンテキストリファレンス
     */
    final Context mContext;

    /**
     * プリンタアダプタ
     */
    final HmpAdapter mAdapter = HmpAdapter.getDefaultAdapter();

    /**
     * 接続タスク
     */
    ScheduledFuture mConnectFuture = null;

    /**
     * 選択したプリンタデバイス
     */
    HmpPrinter mPrinter = null;

    /**
     * アクテビティ
     */
    MainActivity mActivity = null;

    /**
     * エラー状態
     */
    HmpCommand.DeviceStatus mError = HmpCommand.DeviceStatus.DISCONNECTED;

    /**
     * Bluetooth使用可否
     */
    boolean isBluetoothEnable = false;

    /**
     * 初期化
     *
     * @param context コンテキスト
     */
    public static synchronized void initialize(Context context) {
        if (sInstance == null) {
            sInstance = new PrinterManager(context);
        }
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    PrinterManager(Context context) {
        mContext = context.getApplicationContext();
        /* プリンタアダプタリスナレジスタ */
        Logger.i(TAG, "PrinterManager()- info HMPSDK : APP->SDK:  Register Listener by setListener().");
        mAdapter.setListener(mAdapterListener);
    }

    /**
     * インスタンス取得
     *
     * @return インスタンス
     */
    public static synchronized PrinterManager getInstance() {
        return sInstance;
    }

    /**
     * 復帰
     *
     * @param source ライフサイクルオーナー
     */
    @OnLifecycleEvent(ON_RESUME)
    void onResume(LifecycleOwner source) {
        if (source instanceof MainActivity) {
            mActivity = (MainActivity) source;
            /* アダプタの利用可否をチェック */
            Logger.i(TAG, "onResume()- info HMPSDK : APP->SDK:  HmpAdapter is enabled by isEnabled().");
            if (!mAdapter.isEnabled()) {
                enable();
            }
        }
    }

    /**
     * 有効化
     */
    public void enable() {
        if (mActivity != null && !mAdapter.isEnabled()) {
            mActivity.startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        }
    }

    /**
     * 結果取得
     *
     * @param requestCode リクエスト
     * @param resultCode  リザルト
     * @param data        データ
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            isBluetoothEnable = true;
        }
    }

    /**
     * プリンタ一覧取得
     *
     * @return プリンタリスト
     */
    public List<HmpPrinter> getDiscoveredPrinters() {
        /* デバイス取得 */
        Logger.i(TAG, "getDiscoveredPrinters()- info HMPSDK : APP->SDK:  Get discovered printers by getDiscoveredPrinters().");
        List<HmpPrinter> printers = mAdapter.getDiscoveredPrinters();
        if (mPrinter != null) {
            printers.add(mPrinter);
        }
        return printers;
    }

    /**
     * プリンタデバイス取得
     *
     * @return プリンタデバイス
     */
    public synchronized HmpPrinter getPrinter() {
        return mPrinter;
    }

    /**
     * プリンタ接続確認
     *
     * @return プリンタ接続確認
     */
    public synchronized boolean isConnected() {
        /* プリンタ接続を判断 */
        Logger.i(TAG, "isConnected()- info HMPSDK : APP->SDK:  Printer is connected by getConnection().");
        return mPrinter != null && mPrinter.getConnection() == CONNECTED;
    }

    /**
     * 接続
     *
     * @param printer プリンタデバイス
     */
    public synchronized void open(HmpPrinter printer) {
        if (printer == null) {
            Event.post(Event.CONNECTION_FAILED);
            return;
        }

        if (mPrinter != null) {
            /* リスナをクリア */
            Logger.i(TAG, "open()- info HMPSDK : APP->SDK:  Clear listener by clearListener().");
            mPrinter.clearListener();
            /* リスナをクリア */
            Logger.i(TAG, "open()- info HMPSDK : APP->SDK:  Close device by close().");
            mPrinter.close();
        }
        mPrinter = printer;
        /* リスナをレジスト */
        Logger.i(TAG, "open()- info HMPSDK : APP->SDK:  Register Listener to listen to printer by setListener().");
        mPrinter.setListener(mPrinterListener);
        /* プリントを接続 */
        Logger.i(TAG, "open()- info HMPSDK : APP->SDK:  Connect Printer by open().");
        mPrinter.open();
    }

    /**
     * 切断
     */
    public synchronized void close() {
        if (mPrinter != null) {
            /* リスナをクリア */
            Logger.i(TAG, "close()- info HMPSDK : APP->SDK:  Clear Listener by clearListener().");
            mPrinter.clearListener();
            /* プリントをクローズ */
            Logger.i(TAG, "close()- info HMPSDK : APP->SDK:  Disconnect Printer by close().");
            mPrinter.close();
        }
    }

    /**
     * 印刷
     *
     * @param images   印刷イメージ
     * @param settings 設定
     * @param copies   部数
     * @return 実行可否
     */
    public synchronized boolean print(ArrayList<HmpImage> images, HmpSettings settings, int copies) {
        if (mPrinter == null || images == null || settings == null || copies <= 0 || copies > 999) {
            Logger.e(TAG, "print() - error : parameter is invalid");
            return false;
        }
        /* 印刷 */
        Logger.i(TAG, "print()- info HMPSDK : APP->SDK: Print data by print().");
        return mPrinter.print(images, settings, copies);
    }

    /**
     * インクリメント印刷
     *
     * @param image   印刷イメージ
     * @param settings 設定
     * @param copies   部数
     * @param printPassCount   印刷中ジョブのパスのカウント
     * @param printPassNum   印刷中ジョブのパスの番号
     * @return 実行可否
     */
    public synchronized boolean incrementPrint(HmpImage image, HmpSettings settings, int copies, int printPassCount, int printPassNum) {
        if (image == null || settings == null || copies <= 0 || copies > 999 || printPassCount < 1 || printPassCount > 1000 || printPassNum < 1 || printPassNum > 1000) {
            Logger.e(TAG,"incrementPrint() - error : parameter is invalid");
            return false;
        }
        /* テキスト連番印刷 */
        Logger.i(TAG, "incrementPrint()- info HMPSDK : APP->SDK: Print text by incrementPrint().");
        return mPrinter != null && mPrinter.incrementPrint(image, settings, copies, printPassCount, printPassNum);
    }


    /**
     * キャンセル
     */
    public synchronized void cancel() {
        if (mPrinter != null) {
            /* 印刷取消 */
            Logger.i(TAG, "cancel()- info HMPSDK : APP->SDK: Cancel print by cancel().");
            mPrinter.cancel();
        }
    }

    /**
     * 中断
     *
     * @param source ライフサイクルオーナー
     */
    @OnLifecycleEvent(ON_PAUSE)
    void onPause(LifecycleOwner source) {
        if (source instanceof MainActivity) {
            if (mActivity == source) mActivity = null;
        }
    }

    /**
     * プリンタアダプタリスナ
     */
    final HmpAdapter.Listener mAdapterListener = (adapter, event) -> {
        Logger.i(TAG, "mAdapterListener - info HMPSDK : SDK->APP:  event=" + event);
        switch (event) {
            case FOUND:
                Event.post(Event.DEVICE_FOUND);
                break;
            default:
                break;
        }
    };

    /**
     * プリンタデバイスリスナ
     */
    final HmpPrinter.Listener mPrinterListener = (printer, event) -> {
        Logger.i(TAG, "mPrinterListener - info HMPSDK : SDK->APP:  event=" + event);
        switch (event) {
            case CONNECTION_CONNECTED:
                Event.post(Event.CONNECTION_CONNECTED);
                break;
            case CONNECTION_FAILED:
                Event.post(Event.CONNECTION_FAILED);
                break;
            case CONNECTION_DISCONNECTED:
                Event.post(Event.CONNECTION_DISCONNECTED);
                break;
            case STATUS_CHANGED:
                Event.post(Event.STATUS_CHANGED);
                break;
            case INFORMATION_CHANGED:
                Event.post(Event.INFORMATION_CHANGED);
                break;
            case JOB_STARTED:
                Event.post(Event.JOB_STARTED);
                break;
            case PRINTED_PASS:
                Event.post(Event.PRINTED_PASS);
                break;
            case PRINTED_PAGE:
                Event.post(Event.PRINTED_PAGE);
                break;
            case JOB_ENDED:
                Event.post(Event.JOB_ENDED);
                break;
            case JOB_CANCELED:
                Event.post(Event.JOB_CANCELED);
                break;
            case JOB_ABORTED:
                Event.post(Event.JOB_ABORTED);
                break;
            case UPDATE_STATUS:
                onUpdateStatus();
                Event.post(Event.UPDATE_STATUS);
                break;
            default:
                break;
        }
    };

    /**
     * プリント状態通知
     */
    void onUpdateStatus() {
        if (mPrinter != null) {
            Logger.i(TAG, "onUpdateStatus - info HMPSDK : APP->SDK:  Get device status by getDeviceStatus().");
            mError = mPrinter.getDeviceStatus();
            switch (mError) {
                case FRONT_COVER:
                case CARTRIDGE_OFF:
                case BATTERY_END:
                case BLACK_END:
                case CARTRIDGE_FAILURE1:
                case CARTRIDGE_FAILURE2:
                case ERR100:
                case ERR101:
                case ERR200:
                case ERR201:
                case ERR202:
                case ERR203:
                    /* 印刷取消 */
                    Logger.i(TAG, "cancel()- info HMPSDK : APP->SDK: Cancel print by cancel().");
                    mPrinter.cancel();
                default:
                    break;
            }
        } else {
            mError = HmpCommand.DeviceStatus.DISCONNECTED;
        }
    }

    /**
     * イベント
     */
    public enum Event {

        /**
         * デバイス発見
         */
        DEVICE_FOUND,

        /**
         * 検索終了
         */
        DISCOVERY_FINISH,

        /**
         * 接続
         */
        CONNECTION_CONNECTED,

        /**
         * 切断
         */
        CONNECTION_DISCONNECTED,

        /**
         * 接続失敗
         */
        CONNECTION_FAILED,

        /**
         * ページ印刷
         */
        PRINTED_PAGE,

        /**
         * パス印刷
         */
        PRINTED_PASS,

        /**
         * ジョブ開始
         */
        JOB_STARTED,

        /**
         * ジョブ終了
         */
        JOB_ENDED,

        /**
         * ジョブキャンセル
         */
        JOB_CANCELED,

        /**
         * ジョブ中断
         */
        JOB_ABORTED,

        /**
         * プリンタ情報更新
         */
        INFORMATION_CHANGED,

        /**
         * プリンタ状態更新
         */
        STATUS_CHANGED,

        /**
         * 印字状態通知
         */
        UPDATE_STATUS;

        /**
         * イベント送信
         *
         * @param event イベント
         */
        static void post(Event event) {
            EventBus.getDefault().post(event);
        }
    }

    /**
     * スキャンモード開始
     */
    public synchronized void startScan() {
        if (mPrinter != null) {
            /* デバイスをクローズ */
            Logger.i(TAG,"startScan() - info : HMPSDK : APP->SDK: Close device by close().");
            mPrinter.close();
        }
        /* デバイスをスキャン */
        Logger.i(TAG,"startScan() - info : HMPSDK : APP->SDK: start discovery by startDiscovery().");
        mAdapter.startDiscovery();
    }

    /**
     * スキャンモード中断
     */
    public synchronized void stopScan() {
        /* スキャンを取消 */
        Logger.i(TAG,"stopScan()- info HMPSDK : APP->SDK: cancel discovery by cancelDiscovery().");
        mAdapter.cancelDiscovery();
    }

    /**
     * サウンド設定
     *
     * @param isBuzzer サウンド設定
     */
    public synchronized void setBuzzer(boolean isBuzzer) {
        /* Sound有無設定 */
        Logger.i(TAG, "setBuzzer()- info HMPSDK : APP->SDK: Set sound by setBuzzer().");
        mPrinter.setBuzzer(isBuzzer);
        Logger.i(TAG, "setBuzzer()- info HMPSDK : APP->SDK: Change sound by changeBuzzer().");
        if (!mPrinter.changeBuzzer(isBuzzer)) {
            Logger.e(TAG,"setBuzzer() - error : failed to change buzzer setting.");
        }
    }

    /**
     * サウンド設定取得
     *
     * @return サウンド設定
     */
    public boolean isBuzzer() {
        /* Sound有無取得 */
        Logger.i(TAG, "isBuzzer()- info HMPSDK : APP->SDK: Get sound by isBuzzer().");
        return mPrinter.isBuzzer();
    }

    /**
     * オートオフ時間設定
     *
     * @param autoOff オートオフ時間
     */
    public synchronized void setAutoOff(int autoOff) {
        /* オートオフ時間設定 */
        Logger.i(TAG, "setAutoOff()- info HMPSDK : APP->SDK: Set auto off time by setAutoOff().");
        mPrinter.setAutoOff(autoOff);
        Logger.i(TAG, "setAutoOff()- info HMPSDK : APP->SDK: Change auto off time by changeAutoOff().");
        if (!mPrinter.changeAutoOff(autoOff)) {
            Logger.e(TAG,"setAutoOff() - error : failed to change auto off setting.");
        }
    }

    /**
     * オートオフ時間取得
     *
     * @return オートオフ時間
     */
    public int getAutoOff() {
        /* オートオフ時間取得 */
        Logger.i(TAG, "getAutoOff()- info HMPSDK : APP->SDK: Get auto off time by getAutoOff().");
        return mPrinter.getAutoOff();
    }

    /**
     * エラー取得
     *
     * @return エラー
     */
    public HmpCommand.DeviceStatus getError() {
        return mError;
    }

}
