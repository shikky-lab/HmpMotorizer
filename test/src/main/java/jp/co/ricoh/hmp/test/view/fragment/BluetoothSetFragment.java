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
 * Blueetooth 設定　APIの使用なし
 */
public class BluetoothSetFragment extends BaseFragment {

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    @BindView(R.id.head_title)
    TextView tvTitle;

    public static void startFragment(MainActivity.Transition transition) {
        MainActivity.TransactionEvent.post(transition, new BluetoothSetFragment());
    }

    @Override
    public void onStart() {
        tvTitle.setText(getResources().getString(R.string.bluetooth_set_title));
        super.onStart();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_set, container, false);
    }

    @OnClick(R.id.nameset)
    public void OnClickNameSetButton(View v)
    {
        BlueetoothNameSettingFragment.startFragment(MainActivity.Transition.NEXT);
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
