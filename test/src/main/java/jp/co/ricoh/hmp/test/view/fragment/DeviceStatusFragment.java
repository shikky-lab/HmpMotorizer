package jp.co.ricoh.hmp.test.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import jp.co.ricoh.hmp.sdk.printer.HmpCommand;
import jp.co.ricoh.hmp.sdk.printer.HmpPrinter;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.model.PrinterManager;
import jp.co.ricoh.hmp.test.view.widget.PrinterStatusListView;

/**
 * デバイス状態表示
 *  １，デバイス状態更新タイミング。デバイスから下記イベントを受信する
 *  CONNECTION_CONNECTED
 *  CONNECTION_DISCONNECTED
 *  STATUS_CHANGED
 *  INFORMATION_CHANGED
 *  UPDATE_STATUS
 *　２，デバイス状態取得。それぞれ状態の関数で取得
 */
public class DeviceStatusFragment extends BaseFragment {
    /**
     * タグ
     */
    private static final String TAG = DeviceStatusFragment.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();

    /**
     * ステータス
     */
    @BindView(R.id.status_list)
    PrinterStatusListView mStatusList;

    @BindView(R.id.head_title)
    TextView tvTitle;

    ArrayList<String> mTitles = new ArrayList<String>();
    ArrayList<String> mStatus = new ArrayList<>();

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    public static void startFragment(MainActivity.Transition transition) {
        MainActivity.TransactionEvent.post(transition, new DeviceStatusFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_status, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        tvTitle.setText(getResources().getString(R.string.device_status_title));

        mTitles.add(getResources().getString(R.string.device_status_bluetooth));
        mTitles.add(getResources().getString(R.string.device_status_ink));
        mTitles.add(getResources().getString(R.string.device_status_battery));
        mTitles.add(getResources().getString(R.string.device_status_error_message));
        mTitles.add(getResources().getString(R.string.device_status_firmware_version));
        mTitles.add(getResources().getString(R.string.device_status_serial_number));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDeviceStatus();
    }

    /**
     * プリンタデバイス管理イベント受信
     *
     * @param event プリンタデバイス管理イベント
     */
    @Subscribe
    public void onEvent(PrinterManager.Event event) {
        switch (event) {
            case CONNECTION_CONNECTED:
            case CONNECTION_DISCONNECTED:
            case STATUS_CHANGED:
            case INFORMATION_CHANGED:
            case UPDATE_STATUS:
                updateDeviceStatus();
                break;
            default:
                break;
        }
    }

    @OnClick(R.id.iv_back)
    public void OnClickDoneButton(View v)
    {
        FunctionListFragment.startFragment(MainActivity.Transition.BACK);
    }

    @Override
    public boolean onBackPressed() {
        FunctionListFragment.startFragment(MainActivity.Transition.BACK);
        return true;
    }

    /**
     * デバイス状態を更新
     *
     */
    void updateDeviceStatus() {
        HmpPrinter printer = mPrinterManager.getPrinter();
        Logger.i(TAG, "updateDeviceStatus()- info HMPSDK : APP->SDK: Get printer connection  by getConnection().");
        if (printer != null && printer.getConnection().equals(HmpPrinter.Connection.CONNECTED)) {
            mStatus.add(getResources().getString(R.string.device_status_connected));
            /* Ink Remain取得 */
            Logger.i(TAG, "updateDeviceStatus()- info HMPSDK : APP->SDK: Get Ink Remain by getInkRemain().");
            mStatus.add(printer.getInkRemain() + getResources().getString(R.string.device_status_label));

            /* Battery Level取得 */
            Logger.i(TAG, "updateDeviceStatus()- info HMPSDK : APP->SDK: Get Battery Level by getBatteryLevel().");
            mStatus.add(printer.getBatteryLevel() + getResources().getString(R.string.device_status_label));

            /* デバイス状態取得 */
            HmpCommand.DeviceStatus error = mPrinterManager.getError();
            switch (error) {
                case UPDATE_FAILED:
                    mStatus.add(getResources().getString(R.string.device_status_update_failed));
                    break;
                case FRONT_COVER:
                    mStatus.add(getResources().getString(R.string.device_status_front_cover));
                    break;
                case CARTRIDGE_OFF:
                    mStatus.add(getResources().getString(R.string.device_status_cartridge_off));
                    break;
                case BATTERY_END:
                    mStatus.add(getResources().getString(R.string.device_status_battery_end));
                    break;
                case CARTRIDGE_FAILURE1:
                    mStatus.add(getResources().getString(R.string.device_status_cartridge_failure1));
                    break;
                case CARTRIDGE_FAILURE2:
                    mStatus.add(getResources().getString(R.string.device_status_cartridge_failure2));
                    break;
                case ERR100:
                    mStatus.add(getResources().getString(R.string.device_status_err100));
                    break;
                case ERR101:
                    mStatus.add(getResources().getString(R.string.device_status_err101));
                    break;
                case ERR200:
                    mStatus.add(getResources().getString(R.string.device_status_err200));
                    break;
                case ERR201:
                    mStatus.add(getResources().getString(R.string.device_status_err201));
                    break;
                case ERR202:
                    mStatus.add(getResources().getString(R.string.device_status_err202));
                    break;
                case ERR203:
                    mStatus.add(getResources().getString(R.string.device_status_err203));
                    break;
                default:
                    mStatus.add(getResources().getString(R.string.device_status_none));
                    break;
            }
            /* Firmware Version取得 */
            Logger.i(TAG, "updateDeviceStatus()- info HMPSDK : APP->SDK: Get Firmware Version by getFirmwareVersion().");
            mStatus.add(printer.getFirmwareVersion());

            /* Serial Number取得 */
            Logger.i(TAG, "updateDeviceStatus()- info HMPSDK : APP->SDK: Get Serial Number by getSerialNumber().");
            mStatus.add(printer.getSerialNumber());
        }
        else
        {
            mStatus.add(getResources().getString(R.string.device_status_no_connected));
            mStatus.add(getResources().getString(R.string.device_status_no_obtained));
            mStatus.add(getResources().getString(R.string.device_status_no_obtained));
            mStatus.add(getResources().getString(R.string.device_status_no_obtained));
            mStatus.add(getResources().getString(R.string.device_status_no_obtained));
            mStatus.add(getResources().getString(R.string.device_status_no_obtained));
        }

        mStatusList.update(mTitles,mStatus);
    }
}
