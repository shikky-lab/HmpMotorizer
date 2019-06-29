package jp.co.ricoh.hmp.test.view.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import jp.co.ricoh.hmp.sdk.printer.HmpPrinter;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.PrinterManager;
import jp.co.ricoh.hmp.test.view.widget.PrinterSelectListView;

/**
 * デバイス接続に関する功能を実現
 *　１，デバイススキャン開始。SDKのstartDiscovery(startScan)関数を呼び出し、デバイスをスキャン
 *　２，デバイススキャン取消。SDKのcancelDiscovery(stopScan)関数を呼び出し、デバイスを取消
 *　３，見つけるデバイスをリスト。SDKからDEVICE_FOUNDを受信して、リストを更新
 *　４，デバイス接続。SDKのopen関数を呼び出し、デバイスを接続
 *  ５，接続成功の処理。SDKからCONNECTION_CONNECTEDを受信して、接続成功の処理を行う
 *  ６，接続失敗の処理。SDKからCONNECTION_FAILEDを受信して、接続失敗の処理を行う
 *
 *
 * シナリオ１：プリントスキャン
 * ユーザー、プリント選択画面を開く
 * App側で、SDKのstartDiscovery(startScan)関数を呼び出し、デバイスをスキャン
 * SDK側で、見つけるデバイスがある場合、App側へDEVICE_FOUNDを送信
 * App側で、DEVICE_FOUNDイベントを受信して、見つけるデバイスを表示
 *
 * シナリオ２：プリント接続（接続成功）
 * ユーザー、プリントを選択
 * App側で、SDKのopen関数を呼び出し、デバイスを接続
 * SDK側で、接続成功の場合、App側へCONNECTION_CONNECTEDイベントを送信
 * App側で、CONNECTION_CONNECTEDイベントを受信して、接続成功メッセージを表示
 *
 * シナリオ３：プリント接続（接続失敗）
 * ユーザー、プリントを選択
 * App側で、SDKのopen関数を呼び出し、デバイスを接続
 * SDK側で、接続成功の場合、App側へCONNECTION_FAILEDイベントを送信
 * App側で、CONNECTION_FAILEDイベントを受信して、接続失敗メッセージを表示
 *
 * シナリオ３：プリントスキャン取消
 * ユーザー、プリント選択画面を閉じる
 * App側で、SDKのcancelDiscovery(stopScan)関数を呼び出し、デバイスのスキャンをキャンセル
 *
 */
public class PrinterListFragment extends BaseFragment {

    /**
     * タグ
     */
    private static final String TAG = PrinterListFragment.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();

    /**
     * テキス
     */
    @BindView(R.id.device_list)
    PrinterSelectListView mPrinterList;

    @BindView(R.id.head_title)
    TextView tvTitle;

    HmpPrinter mPrinter = null;

    String mCategray = "";

    /**
     * キー
     */
    static class Key {

        /**
         * カテゴリ
         */
        static final String CATEGORY = "CATEGORY";
    }

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    public static void startFragment(MainActivity.Transition transition, String categray) {
        Bundle args = new Bundle();
        args.putString(Key.CATEGORY, categray);
        PrinterListFragment fragment = new PrinterListFragment();
        fragment.setArguments(args);
        MainActivity.TransactionEvent.post(transition, fragment);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_printer_list, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        mCategray = args.getString(Key.CATEGORY);
    }

    @Override
    public void onStart() {
        super.onStart();
        tvTitle.setText(getResources().getString(R.string.printer_list_title));

        mPrinterManager.startScan();
    }

    @Override
    public void onResume() {
        super.onResume();
        mPrinterList.update();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPrinterManager.stopScan();
    }

    /**
     * アイテム押下
     *
     * @param position 位置
     */
    @OnItemClick(R.id.device_list)
    public void onItemClickPrinter(int position) {
        mPrinter = mPrinterList.getSelectedPrinter(position);
        mPrinterManager.open(mPrinter);
    }

    @OnClick(R.id.iv_back)
    public void onBackDone() {
        FunctionListFragment.startFragment(MainActivity.Transition.BACK);
    }

    @Override
    public boolean onBackPressed() {
        FunctionListFragment.startFragment(MainActivity.Transition.BACK);
        return true;
    }

    /**
     * プリンタデバイス管理イベント受信
     *
     * @param event プリンタデバイス管理イベント
     */
    @Subscribe
    public void onReceiveEvent(PrinterManager.Event event) {
        switch (event) {
            case DEVICE_FOUND:
                mPrinterList.update();
                break;
            case CONNECTION_CONNECTED:
                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.printer_list_connect_success), Toast.LENGTH_SHORT).show();
                switch (mCategray) {
                    case "FunctionList":
                        FunctionListFragment.startFragment(MainActivity.Transition.BACK);
                        break;
                    case "Text":
                        TextFragment.startFragment(MainActivity.Transition.BACK);
                        break;
                    case "VText":
                        VTextFragment.startFragment(MainActivity.Transition.BACK);
                        break;
                    case "TextIncre":
                        TextIncreFragment.startFragment(MainActivity.Transition.BACK);
                        break;
                    case "QRCode":
                        QRCodeFragment.startFragment(MainActivity.Transition.BACK);
                        break;
                    case "BarCode":
                        BarCodeFragment.startFragment(MainActivity.Transition.BACK);
                        break;
                    case "Photo":
                        PhotoFragment.startFragment(MainActivity.Transition.BACK);
                        break;
                    case "AutoOffTime":
                        AutoOffTimeFragment.startFragment(MainActivity.Transition.BACK);
                        break;
                    case "Sound":
                        SoundFragment.startFragment(MainActivity.Transition.BACK);
                        break;
                    case "BlueetoothNameSetting":
                        BlueetoothNameSettingFragment.startFragment(MainActivity.Transition.BACK);
                        break;
                    default:
                        FunctionListFragment.startFragment(MainActivity.Transition.BACK);
                        break;
                }
                break;
            case CONNECTION_FAILED:
                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.printer_list_connect_fail), Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
