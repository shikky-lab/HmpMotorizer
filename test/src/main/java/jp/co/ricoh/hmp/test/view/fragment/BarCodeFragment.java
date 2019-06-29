package jp.co.ricoh.hmp.test.view.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import jp.co.ricoh.hmp.sdk.image.HmpImage;
import jp.co.ricoh.hmp.sdk.image.HmpImageFactory;
import jp.co.ricoh.hmp.sdk.image.generator.Barcode.Format;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.model.PrinterManager;
import jp.co.ricoh.hmp.test.view.widget.CopiesEdit;
import jp.co.ricoh.hmp.test.view.widget.FormatSpinner;
import jp.co.ricoh.hmp.test.view.widget.FrameSpinner;
import jp.co.ricoh.hmp.test.view.widget.HeightSpinner;
import jp.co.ricoh.hmp.test.view.widget.WeightSpinner;

import static jp.co.ricoh.hmp.sdk.image.generator.Barcode.Weight;


/**
 * バーコードに関する功能を実現
 *　１，バーコード生成。SDKのHmpImageFactory.generateBarcode関数で実現
 *　２，バーコード印刷。SDKのprint関数で実現
 *  ３，印刷取消。SDKのcancel関数で実現
 *　４，印刷開始通知。SDKからJOB_STARTED受信時
 *　５，印刷完了通知。SDKからJOB_ENDED受信時
 *　６，印刷取消通知。SDKからJOB_CANCELED受信時
 *
 * シナリオ１：正常印刷
 * ユーザー、本文を入力
 * App側で、SDKのgenerateBarcode関数を呼び出し、バーコードを生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのprint関数を呼び出し、バーコードデーターを本体へ送信
 * SDK側で、本体側からJOB_STARTEDを受信して、App側へJOB_STARTEDイベントを送信
 * App側で、JOB_STARTEDイベントを受信して、印刷開始メッセージを表示
 * ユーザー、印刷する
 * SDK側で、印刷完了後、本体側からJOB_ENDEDを受信して、App側へJOB_ENDEDイベントを送信
 * App側で、JOB_ENDEDイベントを受信して、印刷完了メッセージを表示
 *
 * シナリオ２：印刷キャンセル
 * ユーザー、本文を入力
 * App側で、SDKのgenerateBarcode関数を呼び出し、バーコードを生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのprint関数を呼び出し、バーコードデーターを本体へ送信
 * SDK側で、本体側からJOB_STARTEDを受信して、App側へJOB_STARTEDイベントを送信
 * App側で、JOB_STARTEDイベントを受信して、印刷開始メッセージを表示
 * ユーザー、キャンセルボタンを押す
 * App側で、SDKのcancel関数を呼び出し、キャンセルを本体へ送信
 * SDK側で、App側へJOB_CANCELEDイベントを送信
 * App側で、JOB_CANCELEDイベントを受信して、印刷キャンセルメッセージを表示
 */
public class BarCodeFragment extends BaseFragment {

    /**
     * タグ
     */
    private static final String TAG = BarCodeFragment.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();

    /**
     * データー
     */
    @BindView(R.id.body_edit)
    EditText mBodyEdit;

    /**
     * プレビュー
     */
    @BindView(R.id.body_view)
    ImageView mBodyView;

    /**
     * フォーマット選択
     */
    @BindView(R.id.format)
    FormatSpinner mFormatView;

    /**
     * 太さ選択
     */
    @BindView(R.id.weight)
    WeightSpinner mWeightView;

    /**
     * 高さ選択
     */
    @BindView(R.id.height)
    HeightSpinner mHeightView;

    /**
     * 囲み有無選択
     */
    @BindView(R.id.frame)
    FrameSpinner mFrameView;

    @BindView(R.id.head_title)
    TextView tvTitle;

    /**
     * 部数エディット
     */
    @BindView(R.id.copies_edit)
    CopiesEdit mCopiesEdit;

    /**
     * 部数
     */
    int mCopies = 1;

    ArrayList<HmpImage> mImages = new ArrayList<>();

    Bitmap mBitmap = null;

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    public static void startFragment(MainActivity.Transition transition) {
        MainActivity.TransactionEvent.post(transition, new BarCodeFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_barcode, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFormatView.set(Format.CODE_128);
        mWeightView.set(Weight.NORMAL);
        mHeightView.set(5.0);
        mFrameView.set(false);

    }


    @Override
    public void onStart() {
        super.onStart();
        tvTitle.setText(getResources().getString(R.string.barcode_title));
        mCopiesEdit.setCopies(mCopies);
        generateBarCode();
        mFormatView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               generateBarCode();
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {

           }
       });

        mWeightView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                generateBarCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mHeightView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                generateBarCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mFrameView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                generateBarCode();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        mPrinterManager.cancel();
    }

    @OnClick(R.id.print_button)
    public void OnClickPrintButton(View v)
    {
        if (mBodyEdit.getText().toString().equals("")) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_no_input_text),Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBitmap == null) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_data_not_created),Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mPrinterManager.isConnected()) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_connect_device),Toast.LENGTH_SHORT).show();
            PrinterListFragment.startFragment(MainActivity.Transition.NEXT,getResources().getString(R.string.barcode_categray));
            return;
        }

        if (checkPrintEnable()) {
            return;
        }
        generateBarCode();

        int copies = mCopiesEdit.getCopies();
        if (copies < 1 || copies > 999) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_value_invalid), Toast.LENGTH_LONG).show();
            return;
        }
        HmpSettings mSettings = new HmpSettings(mPreferenceManager.getPosition(), HmpSettings.Direction.RIGHT, HmpSettings.Pass.SINGLE, HmpSettings.Theta.ENABLE);

        mPrinterManager.print(mImages, mSettings, copies);
    }

    @OnClick(R.id.cancel_button)
    public void OnClickCancelButton(View v) {
        mPrinterManager.cancel();
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

    /**
     * マイナスボタン
     */
    @OnClick(R.id.minus_button)
    public void onClickMinusButton() {
        mCopiesEdit.decrement();
    }

    /**
     * プラスボタン
     */
    @OnClick(R.id.plus_button)
    public void onClickPlusButton() {
        mCopiesEdit.increment();
    }

    @OnTextChanged(value = R.id.body_edit, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterBodyEditChanged(Editable s) {

        String strBodyEdit = mBodyEdit.getText().toString();
        if (strBodyEdit.equals("")) {
            mBodyView.setImageBitmap(null);
            return;
        }

        generateBarCode();
    }

    /**
     * バーコードを生成
     *
     */
    void generateBarCode() {

        mImages.clear();
        mBitmap = null;

        /* " 10.0 mm", 236 */
        try {
            /* バーコード生成 */
            Logger.i(TAG, "generateBarCode()- info HMPSDK : APP->SDK: Generate barcode by generateBarcode().");
            HmpImage image = HmpImageFactory.generateBarcode(mBodyEdit.getText().toString(), mFormatView.get(), mWeightView.get(), mHeightView.get(), null, mFrameView.get());
            Logger.i(TAG, "generateBarCode()- info HMPSDK : APP->SDK: Get barcode image height by getHeight().");
            Logger.i(TAG, "generateBarCode()- info HMPSDK : APP->SDK: Get barcode image width by getWidth().");
            if ((image.getHeight() == 0) || (image.getWidth() == 0))
            {
                mBodyView.setImageBitmap(null);
                Toast.makeText(getContext(),getResources().getString(R.string.message_unvalid),Toast.LENGTH_SHORT).show();
                return;
            }

            mImages.add(image);
            Logger.i(TAG, "generateBarCode()- info HMPSDK : APP->SDK: Get barcode bitmap by getBitmap().");
            mBitmap = image.getBitmap();
            mBodyView.setImageBitmap(mBitmap);
        }
        catch (Exception e)
        {
            Logger.e(TAG, "generateBarCode()- error barcode is generated fail ");
            e.printStackTrace();
        }
    }

    /**
     * プリンタデバイス管理イベント受信
     *
     * @param event プリンタデバイス管理イベント
     */
    @Subscribe
    public void onReceiveEvent(PrinterManager.Event event) {
        switch (event) {
            case JOB_STARTED:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_print_start),Toast.LENGTH_SHORT).show();
                break;
            case JOB_ENDED:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_print_complete),Toast.LENGTH_SHORT).show();
                break;
            case JOB_CANCELED:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_print_cancel),Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }
}
