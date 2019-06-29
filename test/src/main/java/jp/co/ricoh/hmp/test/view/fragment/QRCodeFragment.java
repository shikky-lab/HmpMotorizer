package jp.co.ricoh.hmp.test.view.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import jp.co.ricoh.hmp.sdk.image.HmpImage;
import jp.co.ricoh.hmp.sdk.image.HmpImageFactory;
import jp.co.ricoh.hmp.sdk.image.generator.Qrcode;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.model.PrinterManager;
import jp.co.ricoh.hmp.test.view.widget.CopiesEdit;
import timber.log.Timber;

import static jp.co.ricoh.hmp.sdk.image.generator.Qrcode.Level.H;
import static jp.co.ricoh.hmp.sdk.image.generator.Qrcode.Level.L;
import static jp.co.ricoh.hmp.sdk.image.generator.Qrcode.Level.M;
import static jp.co.ricoh.hmp.sdk.image.generator.Qrcode.Level.Q;
import static jp.co.ricoh.hmp.sdk.image.generator.Qrcode.Ratio.MAXIMUM;
import static jp.co.ricoh.hmp.sdk.image.generator.Qrcode.Ratio.MINIMUM;
import static jp.co.ricoh.hmp.sdk.image.generator.Qrcode.Ratio.NORMAL;

/**
 * QRコードに関する功能を実現
 *　１，QRコード生成。SDKのHmpImageFactory.generateQrcode関数で実現
 *　２，QRコード印刷。SDKのprint関数で実現
 *  ３，印刷取消。SDKのcancel関数で実現
 *　４，印刷開始通知。本体受信完了後に、本体⇒SDKからJOB_STARTEDを通知
 *　５，印刷完了通知。本体印刷完了後に、本体⇒SDKからJOB_ENDEDを通知
 *　６，印刷取消通知。印刷取消後、SDKからJOB_CANCELEDを通知
 *
 *  * シナリオ１：正常印刷
 * ユーザー、本文を入力
 * App側で、SDKのgenerateQrcode関数を呼び出し、QRコードを生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのprint関数を呼び出し、QRコードデーターを本体へ送信
 * SDK側で、本体側からJOB_STARTEDを受信して、App側へJOB_STARTEDイベントを送信
 * App側で、JOB_STARTEDイベントを受信して、印刷開始メッセージを表示
 * ユーザー、印刷する
 * SDK側で、印刷完了後、本体側からJOB_ENDEDを受信して、App側へJOB_ENDEDイベントを送信
 * App側で、JOB_ENDEDイベントを受信して、印刷完了メッセージを表示
 *
 * シナリオ２：印刷キャンセル
 * ユーザー、本文を入力
 * App側で、SDKのgenerateBarcode関数を呼び出し、QRコードを生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのprint関数を呼び出し、QRコードデーターを本体へ送信
 * SDK側で、本体側からJOB_STARTEDを受信して、App側へJOB_STARTEDイベントを送信
 * App側で、JOB_STARTEDイベントを受信して、印刷開始メッセージを表示
 * ユーザー、キャンセルボタンを押す
 * App側で、SDKのcancel関数を呼び出し、キャンセルを本体へ送信
 * SDK側で、App側へJOB_CANCELEDイベントを送信
 * App側で、JOB_CANCELEDイベントを受信して、印刷キャンセルメッセージを表示
 */
public class QRCodeFragment extends BaseFragment {

    /**
     * タグ
     */
    private static final String TAG = QRCodeFragment.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();

    /**
     * 誤り訂正レベル
     */
    Qrcode.Level mLevel = M;

    /**
     * セルサイズ
     */
    Qrcode.Ratio mRatio = NORMAL;

    /**
     * エラー
     */
    @BindView(R.id.error_correction)
    EditText mErrorCorrection;

    /**
     * セール
     */
    @BindView(R.id.cell_size)
    EditText mCellSize;

    /**
     * テキスト
     */
    @BindView(R.id.body_edit)
    EditText mBodyEdit;

    /**
     * プレビュー
     */
    @BindView(R.id.body_view)
    ImageView mBodyView;

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
        MainActivity.TransactionEvent.post(transition, new QRCodeFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_qrcode, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        tvTitle.setText(getResources().getString(R.string.qrcode_title));
        mCopiesEdit.setCopies(mCopies);
        setLevel(mErrorCorrection.getText().toString());
        setRatio(mCellSize.getText().toString());
        generateQRCode();
    }

    @Override
    public void onStop() {
        super.onStop();
        mPrinterManager.cancel();
    }

    @OnClick(R.id.print_button)
    public void OnClickPrintButton(View v) {

        if (mBodyEdit.getText().toString().equals("")) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_no_input_text),Toast.LENGTH_SHORT).show();
        }

        if (!mPrinterManager.isConnected()) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_connect_device),Toast.LENGTH_SHORT).show();
            PrinterListFragment.startFragment(MainActivity.Transition.NEXT,getResources().getString(R.string.qrcode_categray));
            return;
        }

        /* 印刷可否を判断 */
        if (checkPrintEnable()) {
            return;
        }

        if (mErrorCorrection.getText().toString().equals("") || Integer.valueOf(mErrorCorrection.getText().toString()) > 3 || Integer.valueOf(mErrorCorrection.getText().toString()) < 0) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_value_invalid),Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCellSize.getText().toString().equals("") || Integer.valueOf(mCellSize.getText().toString()) > 2 || Integer.valueOf(mCellSize.getText().toString()) < 0) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_value_invalid),Toast.LENGTH_SHORT).show();
            return;
        }

        int copies = mCopiesEdit.getCopies();
        if (copies < 1 || copies > 999) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_value_invalid), Toast.LENGTH_LONG).show();
            Timber.w("print() - warning : data is not validate.");
            return;
        }

        HmpSettings mSettings = new HmpSettings(mPreferenceManager.getPosition(), HmpSettings.Direction.RIGHT, HmpSettings.Pass.SINGLE, HmpSettings.Theta.ENABLE);

        mPrinterManager.print(mImages, mSettings, copies);
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

    @OnTextChanged(value = R.id.error_correction, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterErrorCorrectionChanged(Editable s) {
        String strErrorCorrection = mErrorCorrection.getText().toString();
        if (strErrorCorrection.equals("")) {
            mBodyView.setImageBitmap(null);
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.qrcode_error_correction_empty),Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkNumber(strErrorCorrection)) {
            mBodyView.setImageBitmap(null);
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.qrcode_value_numeric),Toast.LENGTH_SHORT).show();
            return;
        }

        setLevel(strErrorCorrection);
        generateQRCode();
    }

    @OnTextChanged(value = R.id.cell_size, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterCellSizeChanged(Editable s) {
        String strCellSize = mCellSize.getText().toString();
        if(strCellSize.equals(""))
        {
            mBodyView.setImageBitmap(null);
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.qrcode_cell_size_empty),Toast.LENGTH_SHORT).show();
            return;
        }

        if (!checkNumber(strCellSize)) {
            mBodyView.setImageBitmap(null);
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.qrcode_value_numeric),Toast.LENGTH_SHORT).show();
            return;
        }

        setRatio(strCellSize);
        generateQRCode();
    }

    @OnTextChanged(value = R.id.body_edit, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterBodyEditChanged(Editable s) {
        String strBody = mBodyEdit.getText().toString();
        if (strBody.equals("")) {
            mBodyView.setImageBitmap(null);
            return;
        }
        generateQRCode();
    }

    /**
     * QRコードを生成
     *
     */
    void generateQRCode() {

        mImages.clear();
        mBitmap = null;

        if (mErrorCorrection.getText().toString().equals("") || Integer.valueOf(mErrorCorrection.getText().toString()) > 3 || Integer.valueOf(mErrorCorrection.getText().toString()) < 0) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_value_invalid),Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCellSize.getText().toString().equals("") || Integer.valueOf(mCellSize.getText().toString()) > 2 || Integer.valueOf(mCellSize.getText().toString()) < 0) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_value_invalid),Toast.LENGTH_SHORT).show();
            return;
        }

        if (mBodyEdit.getText().toString().equals("")) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.qrcode_body_empty),Toast.LENGTH_SHORT).show();
            return;
        }

        /* qrコード生成 */
        Logger.i(TAG, "generateQRCode()- info HMPSDK : APP->SDK: Generate qrcode by generateQrcode().");
        HmpImage image = HmpImageFactory.generateQrcode(mBodyEdit.getText().toString(), mLevel, mRatio, null);
        if ((image.getHeight() == 0) || (image.getWidth() == 0))
        {
            Toast.makeText(getContext(),getResources().getString(R.string.message_unvalid),Toast.LENGTH_SHORT).show();
            return;
        }

        mImages.add(image);
        Logger.i(TAG, "generateQRCode()- info HMPSDK : APP->SDK: Get qrcode bitmap by getBitmap().");
        mBitmap = image.getBitmap();
        mBodyView.setImageBitmap(mBitmap);
    }

    private boolean checkNumber(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if ( !isNum.matches() ) {
            return false;
        }
        return true;
    }

    private void setLevel(String value) {
        int level = Integer.parseInt(value);
        switch (level) {
            case 0:
                mLevel = L;
                break;
            case 1:
                mLevel = M;
                break;
            case 2:
                mLevel = Q;
                break;
            case 3:
                mLevel = H;
                break;
            default:
                mLevel = M;
                break;
        }
    }

    private void setRatio(String value) {
        int ratio = Integer.parseInt(value);
        switch (ratio) {
            case 0:
                mRatio = MINIMUM;
                break;
            case 1:
                mRatio = NORMAL;
                break;
            case 2:
                mRatio = MAXIMUM;
                break;
            default:
                mRatio = NORMAL;
                break;
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
