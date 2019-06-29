package jp.co.ricoh.hmp.test.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.OnClick;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.R;

/**
 * デバイス設定　APIの使用なし
 */
public class DeviceSetFragment extends BaseFragment {

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    @BindView(R.id.head_title)
    TextView tvTitle;

    public static void startFragment(MainActivity.Transition transition) {
        MainActivity.TransactionEvent.post(transition, new DeviceSetFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_device_set, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        tvTitle.setText(getResources().getString(R.string.device_set_title));
    }

    @OnClick(R.id.sound_button)
    public void OnClickSoundButton(View v)
    {
        SoundFragment.startFragment(MainActivity.Transition.BACK);
    }

    @OnClick(R.id.autoofftime_button)
    public void OnClickAutoOffTimeButton(View v)
    {
        AutoOffTimeFragment.startFragment(MainActivity.Transition.BACK);
    }

    @OnClick(R.id.printstartposition_button)
    public void OnClickPrintStartPosition(View view){
        PrintStartPositionFragment.startFragment(MainActivity.Transition.BACK);
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
}
