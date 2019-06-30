package jp.co.ricoh.hmp.test.view.fragment;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Set;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.BtDeviceManager;
import jp.co.ricoh.hmp.test.view.widget.BluetoothDeviceSelectListView;

/**
 * Blueetooth接続．ペアリング接続しているものを一覧で出し，接続する．
 */
public class BluetoothConnectFragment extends BaseFragment{
    final BtDeviceManager mBtDeviceManager = BtDeviceManager.getInstance();

//    @BindView(R.id.head_title)
//    TextView tvTitle;

    @BindView(R.id.bt_device_list)
    BluetoothDeviceSelectListView mBTDevListView;

    public static void startFragment(MainActivity.Transition transition) {
        MainActivity.TransactionEvent.post(transition, new BluetoothConnectFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_device_list, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Set<BluetoothDevice> bondedDeviceSet = mBtDeviceManager.getBondedDevices();
        mBTDevListView.update(bondedDeviceSet);
    }


//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_bluetooth_set, container, false);
//    }
//
//    @OnClick(R.id.nameset)
//    public void OnClickNameSetButton(View v)
//    {
//        BlueetoothNameSettingFragment.startFragment(MainActivity.Transition.NEXT);
//    }

    @OnItemClick(R.id.bt_device_list)
    public void onItemClick(int position) {
        BluetoothDevice mBtDevice =  mBTDevListView.getSelectedDevice(position);
        mBtDeviceManager.connect(mBtDevice);
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

    /*scan試そうとした際の残骸．未使用*/
//    @NeedsPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
//    void startScan() {
//        BluetoothManager mBtManager = (BluetoothManager) this.getContext().getSystemService(Context.BLUETOOTH_SERVICE);
//        mBTAdapter = mBtManager.getAdapter();
//        if ((mBTAdapter != null) && (!mIsScanning)) {
//            BluetoothLeScanner mBleScanner = mBTAdapter.getBluetoothLeScanner();
//            mBleScanner.startScan(new ScanCallback() {
//                                      @Override
//                                      public void onScanResult(int callbackType, ScanResult result) {
//                                          super.onScanResult(callbackType, result);
//                                          mBTDevListView.update(result.getDevice());
//                                      }
//                                  }
//
//            );
//            mIsScanning = true;
//            Toast.makeText(this.getContext(), "scan start", Toast.LENGTH_SHORT).show();
//        }
//    }


}
