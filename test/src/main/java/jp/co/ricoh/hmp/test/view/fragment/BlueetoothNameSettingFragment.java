package jp.co.ricoh.hmp.test.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import butterknife.BindView;
import butterknife.OnClick;
import jp.co.ricoh.hmp.sdk.printer.HmpPrinter;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.MainActivity.Transition;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.model.PrinterManager;

/**
 * Blueetooth Name設定
 *  １，Blueetooth Name設定。changeName関数で設定
 *　２，Blueetooth Name取得。getName関数で取得
 */
public class BlueetoothNameSettingFragment extends BaseFragment {
    /**
     * タグ
     */
    private static final String TAG = BlueetoothNameSettingFragment.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();

    /**
     * 古い名称
     */
    @BindView(R.id.old_name)
    TextView mOldName;

    /**
     * 名称
     */
    @BindView(R.id.name)
    TextView mName;

    /**
     * 新しい名称
     */
    @BindView(R.id.new_name)
    EditText mNewName;

    String mDeviceName = "";

    String mInitNumber = "";

    String mNumberName = "";

    @BindView(R.id.head_title)
    TextView tvTitle;

    @BindView(R.id.bt_save)
    TextView tvSave;

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    public static void startFragment(MainActivity.Transition transition) {
        MainActivity.TransactionEvent.post(transition, new BlueetoothNameSettingFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_name_setting, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        /* プリント取得 */
        HmpPrinter printer = mPrinterManager.getPrinter();
        if (printer == null) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_connect_device),Toast.LENGTH_SHORT).show();
            PrinterListFragment.startFragment(MainActivity.Transition.NEXT,getResources().getString(R.string.bluetooth_name_categray));
            return;
        }

        /* Bluetooth name取得 */
        Logger.i(TAG, "onStart()- info HMPSDK : APP->SDK: Get name by getName().");
        mDeviceName = printer.getName();
        mInitNumber = mDeviceName.replaceAll("[^0-9]", "");
        mInitNumber = String.format(Locale.US, "%03d", Integer.valueOf(mInitNumber));
        mNumberName = mDeviceName.replaceAll("[0-9]", "");
        mOldName.setText(mDeviceName);
        mName.setText(mNumberName);
        mNewName.setText(mInitNumber);
        tvTitle.setText(getResources().getString(R.string.bluetooth_name_title));
        tvSave.setText(getResources().getString(R.string.save));
    }

    @OnClick(R.id.bt_save)
    public void OnClickSaveButton(View v)
    {
        String number = mNewName.getText().toString();
        if (number.equals("")) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_value_empty),Toast.LENGTH_SHORT).show();
            return;
        }

        if (number.length() != 3){
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_value_length),Toast.LENGTH_SHORT).show();
            return;
        }

        int value = Integer.valueOf(number);
        if (value < 0 || value > 999) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_value_invalid),Toast.LENGTH_SHORT).show();
            return;
        }

        /* プリント接続中であるか */
        if (!mPrinterManager.isConnected()) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_connect_device),Toast.LENGTH_SHORT).show();
            PrinterListFragment.startFragment(MainActivity.Transition.NEXT,getResources().getString(R.string.bluetooth_name_categray));
            return;
        }

        int val = Integer.valueOf(number);
        number = String.format(Locale.US, "%03d", val);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(mNumberName);
        stringBuilder.append(number);

        /* Bluetooth name設定 */
        Logger.i(TAG, "OnClickSaveButton()- info HMPSDK : APP->SDK: Change name by changeName().");
        mPrinterManager.getPrinter().changeName(stringBuilder.toString());

        BluetoothSetFragment.startFragment(Transition.BACK);
    }

    @OnClick(R.id.iv_back)
    public void OnClickCloseButton(View v)
    {
        BluetoothSetFragment.startFragment(Transition.BACK);
    }

    @Override
    public boolean onBackPressed() {
        BluetoothSetFragment.startFragment(Transition.BACK);
        return true;
    }
}
