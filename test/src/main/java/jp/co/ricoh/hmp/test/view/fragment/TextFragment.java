package jp.co.ricoh.hmp.test.view.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import jp.co.ricoh.hmp.sdk.image.HmpImage;
import jp.co.ricoh.hmp.sdk.image.HmpImageFactory;
import jp.co.ricoh.hmp.sdk.image.generator.Text;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings.Direction;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings.Pass;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings.Theta;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.MainActivity.Transition;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.model.PrinterManager;
import jp.co.ricoh.hmp.test.view.widget.CopiesEdit;
import jp.co.ricoh.hmp.test.view.widget.DirectionSwitch;

import static android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT;

/**
 *　横書きテキストに関する功能を実現
 *　１，テキスト画像生成。SDKのHmpImageFactory.generateTextImages関数で実現
 *　２，テキスト画像データー送信。SDKのprint関数で実現
 *  ３，印刷取消。SDKのcancel関数で実現
 *　４，印刷開始通知。SDKからJOB_STARTED受信時
 *　５，印刷完了通知。SDKからJOB_ENDED受信時
 *　６，印刷取消通知。SDKからJOB_CANCELED受信時
 *
 * シナリオ１：正常印刷
 * ユーザー、テキストを入力
 * App側で、SDKのgenerateTextImages関数を呼び出し、テイスト画像を生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのprint関数を呼び出し、テキスト画像データーを本体へ送信
 * SDK側で、本体側からJOB_STARTEDを受信して、App側へJOB_STARTEDイベントを送信
 * App側で、JOB_STARTEDイベントを受信して、印刷開始メッセージを表示
 * ユーザー、印刷する
 * SDK側で、印刷完了後、本体側からJOB_ENDEDを受信して、App側へJOB_ENDEDイベントを送信
 * App側で、JOB_ENDEDイベントを受信して、印刷完了メッセージを表示
 *
 * シナリオ２：印刷キャンセル
 * ユーザー、テキストを入力
 * App側で、SDKのgenerateTextImages関数を呼び出し、テイスト画像を生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのprint関数を呼び出し、テキスト画像データーを本体へ送信
 * SDK側で、本体側からJOB_STARTEDを受信して、App側へJOB_STARTEDイベントを送信
 * App側で、JOB_STARTEDイベントを受信して、印刷開始メッセージを表示
 * ユーザー、キャンセルボタンを押す
 * App側で、SDKのcancel関数を呼び出し、キャンセルを本体へ送信
 * SDK側で、App側へJOB_CANCELEDイベントを送信
 *
 */

public class TextFragment extends BaseFragment {

    /**
     * タグ
     */
    private static final String TAG = TextFragment.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();

    /**
     * 印刷設定
     */
    HmpSettings mSettings = null;

    /**
     * テキス
     */
    @BindView(R.id.text)
    EditText mText;

    /**
     * プレビュー
     */
    @BindView(R.id.image)
    ImageView mImage;

    Text.Gravity mGravity = Text.Gravity.TOP;

    /**
     * 部数エディット
     */
    @BindView(R.id.copies_edit)
    CopiesEdit mCopiesEdit;

    int mCopies = 1;

    ArrayList<HmpImage> mImages = new ArrayList<>();

    @BindView(R.id.head_title)
    TextView tvTitle;

    @BindView(R.id.gravitySpinner)
    Spinner gravitySpinner;
    /**
     * 印刷方向スイッチ
     */
    @BindView(R.id.direction_switch)
    DirectionSwitch mDirectionSwitch;

    String strText = "";

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    public static void startFragment(MainActivity.Transition transition) {
        MainActivity.TransactionEvent.post(transition, new TextFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSettings = new HmpSettings(mPreferenceManager.getPosition(), Direction.RIGHT, Pass.SINGLE, Theta.DISABLE);
        tvTitle.setText(getResources().getString(R.string.text_title));
        Logger.i(TAG, "onStart()- info HMPSDK : APP->SDK: Get Direction by getDirection().");
        mDirectionSwitch.set(mSettings.getDirection());
        mCopiesEdit.setCopies(mCopies);
        gravitySpinner.setSelection(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        gravitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position){
                    case 0:
                        mGravity = Text.Gravity.TOP;
                        break;
                    case 1:
                        mGravity = Text.Gravity.CENTER;
                        break;
                    case 2:
                        mGravity = Text.Gravity.BOTTOM;
                        break;
                }
                updateBitmap();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mPrinterManager.cancel();
    }

    @OnTextChanged(value = R.id.text, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTextChanged(Editable s) {

        strText = s.toString();
        if (strText.equals("")) {
            mImage.setImageBitmap(null);
            return;
        }
        updateBitmap();

    }

    public void updateBitmap(){
        mImages.clear();
        Bitmap map = null;
        Bitmap line = null;

        int fontSize = 12 * 600 / 72;   /* フォントサイズ：12 */
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(fontSize);
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        spannable.setSpan(sizeSpan, 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(sizeSpan, spannable.length(), spannable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.append(strText);
        spannable.setSpan(sizeSpan, spannable.getSpanStart(sizeSpan), spannable.getSpanEnd(sizeSpan), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.append('\n');
        spannable.setSpan(sizeSpan, spannable.getSpanStart(sizeSpan), spannable.getSpanEnd(sizeSpan), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        int size = 138 * 6000 / 254;  /* サイズ：A4 */
        boolean omitted = false;

        /* テキスト画像生成 */
        Logger.i(TAG, "generateTextImages()- info HMPSDK : APP->SDK: Generate text image by generateTextImages().");
        mImages = HmpImageFactory.generateTextImages(spannable, size, omitted, null, mGravity);

        /* UIで表示する画像を生成 */
        Logger.i(TAG, "generateTextImages()- info HMPSDK : APP->SDK: Generate text bitmap by getBitmap().");
        map = mImages.get(0).getBitmap();
        if (map == null) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.qrcode_image_not_created), Toast.LENGTH_SHORT).show();
        }

        int height = map.getHeight() * getResources().getDisplayMetrics().densityDpi / 600;
        int width = map.getWidth() * getResources().getDisplayMetrics().densityDpi / 600;
        GradientDrawable background = new GradientDrawable(LEFT_RIGHT, new int[]{0x00FFFFFF, 0xFFFFFFFF});
        if (width > mImage.getWidth()) {
            width = mImage.getWidth();
            line = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(line);
            canvas.drawColor(Color.TRANSPARENT);
            canvas.drawBitmap(
                    map,
                    new Rect(0, 0, width * map.getHeight() / height, map.getHeight()),
                    new Rect(0, 0, width, height),
                    null);
            background.setBounds((width - height * 2), 0, width, height);
            background.draw(canvas);
        } else {
            line = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(line);
            canvas.drawColor(Color.TRANSPARENT);
            canvas.drawBitmap(
                    map,
                    new Rect(0, 0, map.getWidth(), map.getHeight()),
                    new Rect(0, 0, line.getWidth(), line.getHeight()),
                    null);
        }

        mImage.setImageBitmap(line);
    }

    @OnClick(R.id.copies_minus_button)
    public void onClickCopiesMinusButton() {
        mCopiesEdit.decrement();
    }

    @OnClick(R.id.copies_plus_button)
    public void onClickCopiesPlusButton() {
        mCopiesEdit.increment();
    }

    @OnClick(R.id.Print_button)
    public void OnClickPrintButton(View v) {
        if (mText.getText().toString().equals("")) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_no_input_text), Toast.LENGTH_SHORT).show();
            return;
        }

        if (!mPrinterManager.isConnected()) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_connect_device), Toast.LENGTH_SHORT).show();
            PrinterListFragment.startFragment(Transition.NEXT, getResources().getString(R.string.text_categray));
            return;
        }

        if (checkPrintEnable()) {
            return;
        }

        Logger.i(TAG, "onStart()- info HMPSDK : APP->SDK: Set Direction by setDirection().");
        mSettings.setDirection(mDirectionSwitch.get());
        int copies = mCopiesEdit.getCopies();
        if (copies < 1 || copies > 999) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_value_invalid), Toast.LENGTH_LONG).show();
            Logger.w(TAG,"print() - warning : data is not validate.");
            return;
        }

        mPrinterManager.print(mImages, mSettings, copies);
    }

    @OnClick(R.id.iv_back)
    public void OnClickDoneButton(View v) {
        FunctionListFragment.startFragment(Transition.BACK);
    }

    @Override
    public boolean onBackPressed() {
        FunctionListFragment.startFragment(Transition.BACK);
        return true;
    }

    @OnClick(R.id.Cancel_button)
    public void OnClickCancelButton(View v) {
       mPrinterManager.cancel();
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
