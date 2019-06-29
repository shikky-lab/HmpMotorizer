package jp.co.ricoh.hmp.test.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import butterknife.BindView;
import butterknife.OnClick;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;

/**
 * オートオフ時間設定
 *  １，オートオフ時間設定。setAutoOff関数で設定
 *　２，オートオフ時間取得。getAutoOff関数で取得
 */
public class AutoOffTimeFragment extends BaseFragment {

    /**
     * タグ
     */
    private static final String TAG = AutoOffTimeFragment.class.getSimpleName();

    @BindView(R.id.bt_save)
    TextView tvSave;

    @BindView(R.id.head_title)
    TextView tvTitle;

    @BindView(R.id.offTimeGroup)
    RadioGroup offGroup;

    @BindView(R.id.off_three)
    RadioButton threeOff;

    @BindView(R.id.off_five)
    RadioButton fiveOff;

    @BindView(R.id.off_ten)
    RadioButton tenOff;

    @BindView(R.id.off_none)
    RadioButton noneOff;

    int times = 0;

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    public static void startFragment(MainActivity.Transition transition) {
        MainActivity.TransactionEvent.post(transition, new AutoOffTimeFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_auto_off_time, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        tvTitle.setText(getResources().getString(R.string.auto_off_title));
        tvSave.setText(getResources().getString(R.string.save));
        if (!mPrinterManager.isConnected()) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_connect_device), Toast.LENGTH_SHORT).show();
            PrinterListFragment.startFragment(MainActivity.Transition.NEXT, getResources().getString(R.string.auto_off_categray));
            return;
        }

        int time = (int) (mPrinterManager.getAutoOff() * 0.5);
        switch (time){
            case 3:
                threeOff.setChecked(true);
                break;
            case 5:
                fiveOff.setChecked(true);
                break;
            case 10:
                tenOff.setChecked(true);
                break;
            case 0:
                noneOff.setChecked(true);
                break;
            default:
                Logger.w(TAG, "onStart()- warning time is wrong");
                break;
        }
        offGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkId) {
                if (checkId == threeOff.getId()){
                    times = 3;
                } else if (checkId == fiveOff.getId()){
                    times = 5;
                } else if (checkId == tenOff.getId()){
                    times = 10;
                } else if (checkId == noneOff.getId()){
                    times = 0;
                } else {
                    Logger.w(TAG, "OnCheckedChange()- warning time is wrong");
                }
            }
        });
    }

    @OnClick(R.id.iv_back)
    public void OnClickDoneButton(View v)
    {
        DeviceSetFragment.startFragment(MainActivity.Transition.BACK);
    }

    @Override
    public boolean onBackPressed() {
        DeviceSetFragment.startFragment(MainActivity.Transition.BACK);
        return true;
    }

    @OnClick(R.id.bt_save)
    public void OnClickSaveButton(View v)
    {
        int time = times * 2;
        /* 接続中であるかを判断 */
        if (!mPrinterManager.isConnected()) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_connect_device),Toast.LENGTH_SHORT).show();
            PrinterListFragment.startFragment(MainActivity.Transition.NEXT, getResources().getString(R.string.auto_off_categray));
            return;
        }
        mPrinterManager.setAutoOff(time);
        DeviceSetFragment.startFragment(MainActivity.Transition.BACK);
    }
}
