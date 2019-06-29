package jp.co.ricoh.hmp.test.view.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import icepick.Icepick;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.model.PrinterManager;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.sdk.printer.HmpCommand;
import jp.co.ricoh.hmp.test.preference.PreferenceManager;

public class BaseFragment extends Fragment {
    /**
     * 設定値管理
     */
    final PreferenceManager mPreferenceManager = PreferenceManager.getInstance();

    /**
     * タグ
     */
    private static final String TAG = BaseFragment.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();

    /**
     * アンバインダ
     */
    Unbinder mUnbinder = null;

    /**
     * メイン画面
     */
    MainActivity mMainActivity = null;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.d(TAG,"onCreate() - debug : " + getClass().getSimpleName() + " " + hashCode() + "is create.");
        if (savedInstanceState != null) {
            Icepick.restoreInstanceState(this, savedInstanceState);
        }

        Activity activity = getActivity();
        if (activity instanceof MainActivity) {
            mMainActivity = (MainActivity) activity;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Logger.d(TAG,"onViewCreated() - debug : " + getClass().getSimpleName() + " " + hashCode() + "is view created.");
        mUnbinder = ButterKnife.bind(this, view);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }

    @Override
    public void onStart() {
        super.onStart();
        Logger.d(TAG,"onStart() - debug : " + getClass().getSimpleName() + " " + hashCode() + "is start.");
        try {
            EventBus.getDefault().register(this);
        } catch (EventBusException e) {
            Logger.w(TAG,"onStart() - warning : its super classes have no public methods with the @Subscribe annotation. [EventBusException]");
        }
    }

    /**
     * バックキー押下
     *
     * @return 処理を実施した場合は true
     */
    public boolean onBackPressed() {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        Logger.d(TAG,"onResume() - debug : " + getClass().getSimpleName() + " " + hashCode() + "is resume.");
    }

    @Override
    public void onPause() {
        super.onPause();
        Logger.d(TAG,"onPause() - debug : " + getClass().getSimpleName() + " " + hashCode() + "is pause.");
    }

    @Override
    public void onStop() {
        super.onStop();
        Logger.d(TAG,"onStop() - debug : " + getClass().getSimpleName() + " " + hashCode() + "is stop.");
        try {
            EventBus.getDefault().unregister(this);
        } catch (EventBusException e) {
            Logger.w(TAG,"onStop() - warning : failed unregister. [EventBusException]");
        }
        hideSoftInputFromWindow();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Logger.d(TAG,"onSaveInstanceState() - debug : " + getClass().getSimpleName() + " " + hashCode() + "is save instance.");
        Icepick.saveInstanceState(this, outState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Logger.d(TAG,"onDestroyView() - debug : " + getClass().getSimpleName() + " " + hashCode() + "is destroy view.");
        if (mUnbinder != null) {
            mUnbinder.unbind();
            mUnbinder = null;
        }
    }

    /**
     * ソフトウェアキーボード消去
     */
    public void hideSoftInputFromWindow() {
        Context context = getContext();
        if (context == null) {
            return;
        }

        InputMethodManager manager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (manager == null) {
            return;
        }

        View view = getView();
        if (view == null) {
            return;
        }

        manager.hideSoftInputFromWindow(view.getRootView().getWindowToken(), 0);
    }

    /**
     * エラー状態をチェックする
     */
    public boolean checkPrintEnable() {
        HmpCommand.DeviceStatus status = mPrinterManager.getError();
        switch (status) {
            case UPDATE_FAILED:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_update_fail),Toast.LENGTH_SHORT).show();
                break;
            case FRONT_COVER:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_front_cover_opened),Toast.LENGTH_SHORT).show();
                break;
            case CARTRIDGE_OFF:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_catridge_off),Toast.LENGTH_SHORT).show();
                break;
            case BATTERY_END:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_battery_end),Toast.LENGTH_SHORT).show();
                break;
            case BLACK_END:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_black_end),Toast.LENGTH_SHORT).show();
                break;
            case CARTRIDGE_FAILURE1:
            case CARTRIDGE_FAILURE2:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_cartridge_broken),Toast.LENGTH_SHORT).show();
                break;
            case CLEANING:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_printer_cleaning),Toast.LENGTH_SHORT).show();
                break;
            case ENGINE_TEST:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_calibrating_device),Toast.LENGTH_SHORT).show();
                break;
            case NEAREND_BLACK:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_black_near_end),Toast.LENGTH_SHORT).show();
                return false;
            case REQUEST_CAP:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_cap_need),Toast.LENGTH_SHORT).show();
                return false;
            case OVER_SPEED:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_move_slow),Toast.LENGTH_SHORT).show();
                return false;
            case FLOAT_HEAD:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_flat_surface),Toast.LENGTH_SHORT).show();
                return false;
            case ERR100:
            case ERR101:
            case ERR200:
            case ERR201:
            case ERR202:
            case ERR203:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_sc) + status,Toast.LENGTH_SHORT).show();
                break;
            default:
                return false;
        }
        return true;
    }

}
