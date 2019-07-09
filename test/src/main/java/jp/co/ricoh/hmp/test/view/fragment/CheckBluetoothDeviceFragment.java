package jp.co.ricoh.hmp.test.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.MainActivity.Transition;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.BleDeviceManager;

/**
 *
 * bluetooth接続の確認用
 *
 */

public class CheckBluetoothDeviceFragment extends BaseFragment {

    private static final String TAG = CheckBluetoothDeviceFragment.class.getSimpleName();

    final BleDeviceManager mBleDeviceManager = BleDeviceManager.getInstance();

    @BindView(R.id.head_title)
    TextView tvTitle;

    @BindView(R.id.text_from_device)
    TextView mReadText;

    @BindView(R.id.to_write_text)
    EditText mWriteText;

    @BindView(R.id.send_button)
    Button mSendButton;

    @BindView(R.id.read_button)
    Button mReadButton;

    @BindView(R.id.notified_text)
    TextView mNotifiedText;

    public static void startFragment(Transition transition) {
        MainActivity.TransactionEvent.post(transition, new CheckBluetoothDeviceFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_check, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        tvTitle.setText(getResources().getString(R.string.bluetooth_check_title));
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @OnTextChanged(value = R.id.to_write_text, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTextChanged(Editable s) {
    }

    @OnClick(R.id.send_button)
    public void onClickSendButton() {
        String sendText = mWriteText.getText().toString();
        mBleDeviceManager.write(sendText);
    }

    @OnClick(R.id.send_button)
    public void onClickReadButton() {
        mBleDeviceManager.read();
    }

    @OnClick(R.id.iv_back)
    public void OnClickDoneButton(View v) {
        FunctionListFragment.startFragment(Transition.BACK);
    }

    @Override
    public boolean onBackPressed() {
        FunctionListFragment.startFragment(Transition.BACK);
        return true;
    }
}
