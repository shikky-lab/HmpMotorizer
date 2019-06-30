package jp.co.ricoh.hmp.test.view.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import jp.co.ricoh.hmp.sdk.image.HmpImage;
import jp.co.ricoh.hmp.sdk.image.HmpImageFactory;
import jp.co.ricoh.hmp.sdk.image.generator.Text;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings.Direction;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings.Pass;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings.Theta;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.MainActivity.Transition;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.model.PrinterManager;
import jp.co.ricoh.hmp.test.view.widget.CopiesEdit;
import jp.co.ricoh.hmp.test.view.widget.DirectionSwitch;

import static android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT;

/**
 *
 * bluetooth接続の確認用
 *
 */

public class CheckBluetoothDeviceFragment extends BaseFragment {

    private static final String TAG = CheckBluetoothDeviceFragment.class.getSimpleName();

    @BindView(R.id.head_title)
    TextView tvTitle;

    @BindView(R.id.text_from_device)
    TextView mReadText;

    @BindView(R.id.to_write_text)
    EditText mWriteText;

    @BindView(R.id.send_button)
    Button mSendButton;

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
