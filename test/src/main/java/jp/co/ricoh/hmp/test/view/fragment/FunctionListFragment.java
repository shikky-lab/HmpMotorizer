package jp.co.ricoh.hmp.test.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnItemClick;
import jp.co.ricoh.hmp.test.MainActivity.TransactionEvent;
import jp.co.ricoh.hmp.test.MainActivity.Transition;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.view.widget.FunctionListView;

/**
 * 功能リスト APIの使用なし
 */
public class FunctionListFragment extends BaseFragment {

    /**
     * テキス
     */
    @BindView(R.id.function_list)
    FunctionListView mFunctionList;

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    public static void startFragment(Transition transition) {
        TransactionEvent.post(transition, new FunctionListFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_function_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        ArrayList<String> names = new ArrayList<>();
        names.add(getResources().getString(R.string.function_list_connect));
        names.add(getResources().getString(R.string.function_list_text));
        names.add(getResources().getString(R.string.function_list_vtext));
        names.add(getResources().getString(R.string.function_list_text_increment));
        names.add(getResources().getString(R.string.function_list_qrcode));
        names.add(getResources().getString(R.string.function_list_barcode));
        names.add(getResources().getString(R.string.function_list_photo));
        names.add(getResources().getString(R.string.function_list_device_setting));
        names.add(getResources().getString(R.string.function_list_device_status));
        names.add(getResources().getString(R.string.function_list_bluetooth_setting));
        names.add(getResources().getString(R.string.function_list_bluetooth_device_list));
        names.add(getResources().getString(R.string.function_list_check_bluetooth_device));
        names.add(getResources().getString(R.string.function_list_image_motorizer));
        names.add(getResources().getString(R.string.function_list_line_motorizer));
        names.add(getResources().getString(R.string.function_list_pokemon_motorizer));
        mFunctionList.update(names);
    }

    /**
     * アイテム押下
     *
     * @param position 位置
     */
    @OnItemClick(R.id.function_list)
    public void onItemClickFunction(int position) {
        switch (position) {
            case 0:
                PrinterListFragment.startFragment(Transition.NEXT, getResources().getString(R.string.function_list_categray));
                break;
            case 1:
                TextFragment.startFragment(Transition.NEXT);
                break;
            case 2:
                VTextFragment.startFragment(Transition.NEXT);
                break;
            case 3:
                TextIncreFragment.startFragment(Transition.NEXT);
                break;
            case 4:
                QRCodeFragment.startFragment(Transition.NEXT);
                break;
            case 5:
                BarCodeFragment.startFragment(Transition.NEXT);
                break;
            case 6:
                PhotoFragment.startFragment(Transition.NEXT);
                break;
            case 7:
                DeviceSetFragment.startFragment(Transition.NEXT);
                break;
            case 8:
                DeviceStatusFragment.startFragment(Transition.NEXT);
                break;
            case 9:
                BluetoothSetFragment.startFragment(Transition.NEXT);
                break;
            case 10:
                BluetoothConnectFragment.startFragment(Transition.NEXT);
                break;
            case 11:
                CheckBluetoothDeviceFragment.startFragment(Transition.NEXT);
                break;
            case 12:
                ImageMotorizerFragment.startFragment(Transition.NEXT);
                break;
            case 13:
                LineMotorizerFragment.startFragment(Transition.NEXT);
                break;
            case 14:
                PokemonMotorizerFragment.startFragment(Transition.NEXT);
                break;
        }
    }


}
