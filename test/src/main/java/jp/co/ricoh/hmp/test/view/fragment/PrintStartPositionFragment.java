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
import jp.co.ricoh.hmp.sdk.printer.HmpSettings;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings.Position;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.R;

/**
 * 印刷開始位置設定
 */
public class PrintStartPositionFragment extends BaseFragment{

    /**
     * タグ
     */
    private static final String TAG = PrintStartPositionFragment.class.getSimpleName();

    @BindView(R.id.head_title)
    TextView tvTitle;

    @BindView(R.id.bt_save)
    TextView mSave;

    @BindView(R.id.positionGroup)
    RadioGroup mPositionGroup;

    @BindView(R.id.right)
    RadioButton rightButton;

    @BindView(R.id.center)
    RadioButton centerButton;

    Position initPosition = HmpSettings.Position.RIGHT;

    Position mPosition = HmpSettings.Position.RIGHT;


    public static void startFragment(MainActivity.Transition transition) {
        MainActivity.TransactionEvent.post(transition, new PrintStartPositionFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_print_start_position, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        tvTitle.setText(getResources().getString(R.string.device_set_title));
        mSave.setText(getResources().getString(R.string.save));
        if (mPreferenceManager != null){
            initPosition = mPreferenceManager.getPosition();
        }
        switch (initPosition){
            case RIGHT:
                rightButton.setChecked(true);
                break;
            case CENTER:
                centerButton.setChecked(true);
                break;
        }

        mPositionGroup.setOnCheckedChangeListener((radioGroup, checkId) -> {
            RadioButton checkedButton = mPositionGroup.findViewById(checkId);
            if (checkId == rightButton.getId()){
                mPosition = Position.RIGHT;
            } else if (checkId == centerButton.getId()){
                mPosition = Position.CENTER;
            }
            Toast.makeText(getContext() , checkedButton.getText().toString() , Toast.LENGTH_LONG).show();
        });

        mSave.setOnClickListener(view -> {
            save();
        });
    }


    @OnClick(R.id.iv_back)
    public void OnClickDoneButton(View v) {
        DeviceSetFragment.startFragment(MainActivity.Transition.BACK);
    }


    @Override
    public boolean onBackPressed() {
        DeviceSetFragment.startFragment(MainActivity.Transition.BACK);
        return true;
    }

    public void save(){
        mPreferenceManager.setPosition(mPosition);
        DeviceSetFragment.startFragment(MainActivity.Transition.BACK);
    }
}
