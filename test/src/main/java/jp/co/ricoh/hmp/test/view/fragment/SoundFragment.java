package jp.co.ricoh.hmp.test.view.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.PrinterManager;

/**
 * Sound有無設定
 *  １，Sound有無設定。setBuzzer関数で設定
 *　２，Sound有無取得。isBuzzer関数で取得
 */
public class SoundFragment extends BaseFragment {

    /**
     * タグ
     */
    private static final String TAG = SoundFragment.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();

    /**
     * タイマー
     */
    @BindView(R.id.on_button)
    Button mOnButton;

    /**
     * タイマー
     */
    @BindView(R.id.off_button)
    Button mOffButton;

    @BindView(R.id.bt_save)
    TextView tvSave;

    @BindView(R.id.head_title)
    TextView tvTitle;

    boolean mInitBuzzerChecked;

    boolean mBuzzerChecked;

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    public static void startFragment(MainActivity.Transition transition) {
        MainActivity.TransactionEvent.post(transition, new SoundFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sound, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        tvSave.setText(getResources().getString(R.string.save));
        tvTitle.setText(getResources().getString(R.string.sound_setting_title));
        if (!mPrinterManager.isConnected()) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_connect_device),Toast.LENGTH_SHORT).show();
            PrinterListFragment.startFragment(MainActivity.Transition.NEXT,getResources().getString(R.string.sound_categray));
            return;
        }

        mInitBuzzerChecked = mPrinterManager.isBuzzer();
        setSoundSelect(mInitBuzzerChecked);
    }

    /**
     * 外装右合わせボタン
     */
    @OnClick(R.id.on_button)
    public void onClickOnButton() {
        setSoundSelect(true);
    }

    /**
     * ヘッド中央合わせボタン
     *
     * @param v ビュー
     */
    @OnClick(R.id.off_button)
    public void onClicrOffButton(View v) {
        setSoundSelect(false);
    }

    /**
     * ヘッド中央合わせボタン
     *
     * @param v ビュー
     */
    @OnClick(R.id.bt_save)
    public void onClicrSaveButton(View v) {
        if (!mPrinterManager.isConnected()) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_connect_device),Toast.LENGTH_SHORT).show();
            PrinterListFragment.startFragment(MainActivity.Transition.NEXT,getResources().getString(R.string.sound_categray));
            return;
        }

        mPrinterManager.setBuzzer(mBuzzerChecked);
        DeviceSetFragment.startFragment(MainActivity.Transition.BACK);
    }

    /**
     * ヘッド中央合わせボタン
     *
     * @param v ビュー
     */
    @OnClick(R.id.iv_back)
    public void onClicrDoneButton(View v) {
        DeviceSetFragment.startFragment(MainActivity.Transition.BACK);
    }

    @Override
    public boolean onBackPressed() {
        DeviceSetFragment.startFragment(MainActivity.Transition.BACK);
        return true;
    }

    /**
     * ボタンセット
     *
     * @param isBuzzer
     */
    void setSoundSelect(boolean isBuzzer) {
        if (isBuzzer) {
            mOnButton.setBackgroundColor(Color.parseColor("#000000"));
            mOnButton.setTextColor(Color.parseColor("#FFFFFF"));
            mOffButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
            mOffButton.setTextColor(Color.parseColor("#000000"));
            mBuzzerChecked = true;
        } else {
            mOnButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
            mOnButton.setTextColor(Color.parseColor("#000000"));
            mOffButton.setBackgroundColor(Color.parseColor("#000000"));
            mOffButton.setTextColor(Color.parseColor("#FFFFFF"));
            mBuzzerChecked = false;
        }
    }
}
