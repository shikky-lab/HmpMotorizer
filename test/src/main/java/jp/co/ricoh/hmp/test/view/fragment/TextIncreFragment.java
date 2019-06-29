package jp.co.ricoh.hmp.test.view.fragment;

import android.content.Context;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import jp.co.ricoh.hmp.sdk.printer.HmpCommand;
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
import timber.log.Timber;

import static android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT;

/**
 *　横書きテキスト連番に関する功能を実現
 *　１，テキスト連番画像生成。SDKのHmpImageFactory.generateTextImages関数で実現
 *　２，テキスト連番画像データー送信。SDKのprint関数で実現
 *  ３，印刷取消。SDKのcancel関数で実現
 *　４，印刷開始通知。SDKからJOB_STARTED受信時
 *　５，ジョブ印刷完了通知。SDKからJOB_ENDED受信時
 *　６，印刷取消通知。SDKからJOB_CANCELED受信時
 *  ７，パスごと印刷完了通知。SDKからPRINTED_PASS受信時
 *  ８，コピー枚数こと印刷完了通知。SDKからPRINTED_PAGE受信時
 *
 * シナリオ１：正常印刷（ 連番：１～３、コピー枚数：1）
 * ユーザー、テキスト＆連番を入力
 * App側で、SDKのgenerateTextImages関数を呼び出し、連番１のテイスト連番の画像を生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのprint関数を呼び出し、連番１のテキスト連番の画像データーを本体へ送信
 * SDK側で、本体側からJOB_STARTEDを受信して、App側へJOB_STARTEDイベントを送信
 * App側で、JOB_STARTEDイベントを受信して、印刷開始メッセージを表示
 * ユーザー、印刷する
 * SDK側で、連番１のテキスト連番が印刷完了後、本体側からPRINTED_PASSを受信して、App側へPRINTED_PASSイベントを送信
 * App側で、PRINTED_PASSイベントを受信して、SDKのgenerateTextImages関数を再度呼び出し、次の番号のテイスト連番の画像を生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのprint関数を呼び出し、次の番号のテキスト連番の画像データーを本体へ送信
 * ユーザー、印刷する
 * SDK側で、連番３のテキスト連番が印刷完了後、本体側からPRINTED_PAGEを受信して、App側へPRINTED_PAGEイベントを送信。コピー枚数は1なので、App側へJOB_ENDEDイベントも送信
 * App側で、JOB_ENDEDイベントを受信して、印刷完了メッセージを表示
 *
 *
 *  * シナリオ：正常印刷（ 連番：１～３、コピー枚数：2）
 * ユーザー、テキスト＆連番を入力
 * App側で、SDKのgenerateTextImages関数を呼び出し、連番１のテイスト連番の画像を生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのprint関数を呼び出し、連番１のテキスト連番の画像データーを本体へ送信
 * SDK側で、本体側からJOB_STARTEDを受信して、App側へJOB_STARTEDイベントを送信
 * App側で、JOB_STARTEDイベントを受信して、印刷開始メッセージを表示
 * ユーザー、印刷する
 * SDK側で、連番１のテキスト連番が印刷完了後、本体側からPRINTED_PASSを受信して、App側へPRINTED_PASSイベントを送信
 * App側で、PRINTED_PASSイベントを受信して、SDKのgenerateTextImages関数を再度呼び出し、次の番号のテイスト連番の画像を生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのprint関数を呼び出し、次の番号のテキスト連番の画像データーを本体へ送信
 * ユーザー、印刷する
 * SDK側で、連番３のテキスト連番が印刷完了後、本体側からPRINTED_PAGEを受信して、App側へPRINTED_PAGEイベントを送信。コピー枚数は2なので、App側へJOB_ENDEDイベントを送信しない
 * App側で、PRINTED_PAGEイベントを受信して、連番１のテイスト連番の画像を再度生成
 * ユーザー、印刷する
 * SDK側で、連番３のテキスト連番が印刷完了後、本体側からPRINTED_PAGEを受信して、App側へPRINTED_PAGEイベントを送信。コピー枚数は2なので、App側へJOB_ENDEDイベントを送信
 *
 *
 * シナリオ３：印刷キャンセル
 * ユーザー、テキスト＆連番を入力
 * App側で、SDKのgenerateTextImages関数を呼び出し、連番１のテイスト連番の画像を生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのprint関数を呼び出し、連番１のテキスト連番の画像データーを本体へ送信
 * SDK側で、本体側からJOB_STARTEDを受信して、App側へJOB_STARTEDイベントを送信
 * App側で、JOB_STARTEDイベントを受信して、印刷開始メッセージを表示
 * ユーザー、キャンセルボタンを押す
 * SDKのcancel関数を呼び出し、キャンセルを本体へ送信
 * SDK側で、App側へJOB_CANCELEDイベントを送信
 * App側で、JOB_CANCELEDイベントを受信して、ジョブキャンセルメッセージを表示
 */
public class TextIncreFragment extends BaseFragment {

    /**
     * タグ
     */
    private static final String TAG = TextIncreFragment.class.getSimpleName();

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
    @BindView(R.id.body_edit)
    EditText mText;

    /**
     * プレビュー
     */

    @BindView(R.id.container)
    LinearLayout containerLayout;

    /**
     * フォマット
     */
    @BindView(R.id.format)
    EditText mFormat;

    /**
     * 最大値
     */
    @BindView(R.id.max_value)
    EditText mMax;

    /**
     * 最小値
     */
    @BindView(R.id.min_value)
    EditText mMin;

    /**
     * 部数エディット
     */
    @BindView(R.id.copies_edit)
    CopiesEdit mCopiesEdit;

    @BindView(R.id.gravitySpinner)
    Spinner gravitySpinner;

    Text.Gravity mGravity = Text.Gravity.TOP;
    /**
     * 部数
     */
    int mCopies = 1;

    /**
     * 印刷中のパス
     */
    int mPrintingNum = 1;

    /**
     * 印刷パスのカウント
     */
    int mPrintingCount = 1;

    int mPage = 1;

    int mCurrentCopy = 1;

    ArrayList<HmpImage> mImages = new ArrayList<>();

    ArrayList<HmpImage> mPreImages = new ArrayList<>();


    @BindView(R.id.head_title)
    TextView tvTitle;

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    public static void startFragment(Transition transition) {
        MainActivity.TransactionEvent.post(transition, new TextIncreFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text_incre, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSettings = new HmpSettings(mPreferenceManager.getPosition(), Direction.RIGHT, Pass.SINGLE, Theta.DISABLE);
        tvTitle.setText(getResources().getString(R.string.text_incre_title));
        gravitySpinner.setSelection(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        gravitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                switch (position) {
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
                generateImage();
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

    @OnTextChanged(value = R.id.body_edit, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterBodyEditChanged(Editable s) {

        String strText = s.toString();

        generateImage();
    }

    @OnTextChanged(value = R.id.min_value, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterMinValueChanged(Editable s) {

        if (s.toString().equals("")) {
            return;
        }

        int min  = Integer.parseInt(s.toString());
        int max = Integer.parseInt(mMax.getText().toString());
        if (min >= max) {
            if (containerLayout.getChildCount() > 0) {
                containerLayout.removeAllViews();
            }
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_min_larger_max), Toast.LENGTH_SHORT).show();
            return;
        }
        generateImage();
    }

    @OnTextChanged(value = R.id.max_value, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterMaxValueChanged(Editable s) {

        if (s.toString().equals("")) {
            return;
        }

        int max  = Integer.parseInt(s.toString());
        int min = Integer.parseInt(mMin.getText().toString());
        if (min >= max) {
            if (containerLayout.getChildCount() > 0) {
                containerLayout.removeAllViews();
            }
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_min_larger_max), Toast.LENGTH_SHORT).show();
            return;
        }

        generateImage();
    }

    @OnTextChanged(value = R.id.format, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterFormatChanged(Editable s) {

        if (s.toString().equals("")) {
            return;
        }

        int format  = Integer.parseInt(s.toString());
        if ((format > 1) || (format < 0)) {
            if (containerLayout.getChildCount() > 0) {
                containerLayout.removeAllViews();
            }
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_format_illegal), Toast.LENGTH_SHORT).show();
            return;
        }

        generateImage();
    }

    @OnClick(R.id.print_button)
    public void OnClickPrintButton(View v) {

        if (mPrinterManager.getError() == HmpCommand.DeviceStatus.PRINTING) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_printing), Toast.LENGTH_SHORT).show();
            return;
        } else {
            mCurrentCopy = 1;
            mPrintingNum = 1;
            generateImage();
        }

        if (!mPrinterManager.isConnected()) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_connect_device), Toast.LENGTH_SHORT).show();
            PrinterListFragment.startFragment(Transition.NEXT, getResources().getString(R.string.text_incr_categray));
            return;
        }

        if (checkPrintEnable()) {
            return;
        }

        mCopies = mCopiesEdit.getCopies();
        if (mCopies < 1 || mCopies > 999) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_value_invalid), Toast.LENGTH_LONG).show();
            Timber.w("print() - warning : data is not validate.");
            return;
        }

        if (mMin.getText().toString().equals("") || mMax.getText().toString().equals("") || (Integer.valueOf(mMax.getText().toString()) <= Integer.valueOf(mMin.getText().toString()))) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_value_invalid), Toast.LENGTH_LONG).show();
            return;
        }

        if (mFormat.getText().toString().equals("") || Integer.valueOf(mFormat.getText().toString()) > 1 || Integer.valueOf(mFormat.getText().toString()) < 0){
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_value_invalid), Toast.LENGTH_LONG).show();
            return;
        }

        int min = Integer.parseInt(mMin.getText().toString());
        int max = Integer.parseInt(mMax.getText().toString());
        mPrintingCount = max - min + 1;

        mPrinterManager.incrementPrint(mImages.get(0), mSettings, mCopies, mPrintingCount, mPrintingNum);
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

    @OnClick(R.id.cancel_button)
    public void OnClickCancelButton(View v) {
       mPrinterManager.cancel();
    }

    @OnClick(R.id.minus_button)
    public void onClickCopiesMinusButton() {
        mCopiesEdit.decrement();
    }

    @OnClick(R.id.plus_button)
    public void onClickCopiesPlusButton() {
        mCopiesEdit.increment();
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
            case PRINTED_PASS:
                if (mCurrentCopy <= mCopies) {
                    mPrintingNum++;
                    if (mPrintingNum == mPrintingCount) {
                        mCurrentCopy++;
                    }
                    if (mPrintingNum > mPrintingCount) {
                        mPrintingNum = 1;
                    }
                    generateImage();
                    mPrinterManager.incrementPrint(mImages.get(0), mSettings, mCopies, mPrintingCount, mPrintingNum);
                }
                break;
            case PRINTED_PAGE:
                mPage++;
                break;
            case JOB_ENDED:
                mPage = 1;
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_print_complete),Toast.LENGTH_SHORT).show();
                break;
            case JOB_CANCELED:
                Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_print_cancel),Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

    void generateImage() {
        if (containerLayout.getChildCount() > 0){
            containerLayout.removeAllViews();
        }

        if (mMin.getText().toString().equals("") || mMax.getText().toString().equals("")
                || (mFormat.getText().toString().equals("") || Integer.valueOf(mFormat.getText().toString()) < 0 || Integer.valueOf(mFormat.getText().toString()) > 1)) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_value_not_enter), Toast.LENGTH_SHORT).show();
            return;
        }

        int min = Integer.parseInt(mMin.getText().toString());
        int max = Integer.parseInt(mMax.getText().toString());

        if (max <= min) {
            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_min_larger_max), Toast.LENGTH_SHORT).show();
            return;
        }

        mImages.clear();
        mPreImages.clear();

        int fontSize = 12 * 600 / 72;   /* フォントサイズ：12 */
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(fontSize);
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        spannable.setSpan(sizeSpan, 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(sizeSpan, spannable.length(), spannable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.append(getIncreString());
        spannable.setSpan(sizeSpan, spannable.getSpanStart(sizeSpan), spannable.getSpanEnd(sizeSpan), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.append('\n');
        spannable.setSpan(sizeSpan, spannable.getSpanStart(sizeSpan), spannable.getSpanEnd(sizeSpan), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        int size = 138 * 6000 / 254;  /* サイズ：A4 */
        boolean omitted = false;
        /* 印刷の連番テキストの画像生成 */
        Logger.i(TAG, "generateImage()- info1 HMPSDK : APP->SDK: Generate print text image by generateTextImages().");
        mImages = HmpImageFactory.generateTextImages(spannable, size, omitted, null, mGravity);
        ArrayList<HmpImage> lists = new ArrayList<>();
        int textIncrementNumber = max - min;
        if (textIncrementNumber > 2){
            for (int i = 0; i < 2; i++){
                AbsoluteSizeSpan sizeSpan1 = new AbsoluteSizeSpan(fontSize);
                SpannableStringBuilder spannable1 = new SpannableStringBuilder();
                spannable1.setSpan(sizeSpan1, 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                spannable1.setSpan(sizeSpan1, spannable1.length(), spannable1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                spannable1.append(getCurrentIncreString(i+min));
                spannable1.setSpan(sizeSpan1, spannable1.getSpanStart(sizeSpan1), spannable1.getSpanEnd(sizeSpan1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable1.append('\n');
                spannable1.setSpan(sizeSpan1, spannable1.getSpanStart(sizeSpan1), spannable1.getSpanEnd(sizeSpan1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                /* UIで表示用の連番テキストの画像生成 */
                Logger.i(TAG, "generateImage()- info2 HMPSDK : APP->SDK: Generate display text image by generateTextImages().");
                lists = HmpImageFactory.generateTextImages(spannable1, size, omitted, null, mGravity);
                if (lists != null){
                    Log.e(TAG, "generateImage: images != null");
                    mPreImages.add(lists.get(0));
                }
            }

            AbsoluteSizeSpan sizeSpan1 = new AbsoluteSizeSpan(fontSize);
            SpannableStringBuilder spannable1 = new SpannableStringBuilder();
            spannable1.setSpan(sizeSpan1, 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannable1.setSpan(sizeSpan1, spannable1.length(), spannable1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
            spannable1.append(getCurrentIncreString(max));
            spannable1.setSpan(sizeSpan1, spannable1.getSpanStart(sizeSpan1), spannable1.getSpanEnd(sizeSpan1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable1.append('\n');
            spannable1.setSpan(sizeSpan1, spannable1.getSpanStart(sizeSpan1), spannable1.getSpanEnd(sizeSpan1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            /* UIで表示用の連番テキストの画像生成 */
            Logger.i(TAG, "generateImage()- info3 HMPSDK : APP->SDK: Generate display text image by generateTextImages().");
            lists = HmpImageFactory.generateTextImages(spannable1, size, omitted, null, mGravity);
            if (lists != null){
                Log.e(TAG, "generateImage: images != null");
                mPreImages.add(lists.get(0));
            }
        } else {
            for (int i = min; i <= max; i++){
                AbsoluteSizeSpan sizeSpan1 = new AbsoluteSizeSpan(fontSize);
                SpannableStringBuilder spannable1 = new SpannableStringBuilder();
                spannable1.setSpan(sizeSpan1, 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                spannable1.setSpan(sizeSpan1, spannable1.length(), spannable1.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
                spannable1.append(getCurrentIncreString(i));
                spannable1.setSpan(sizeSpan1, spannable1.getSpanStart(sizeSpan1), spannable1.getSpanEnd(sizeSpan1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                spannable1.append('\n');
                spannable1.setSpan(sizeSpan1, spannable1.getSpanStart(sizeSpan1), spannable1.getSpanEnd(sizeSpan1), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                /* UIで表示用の連番テキストの画像生成 */
                Logger.i(TAG, "generateImage()- info4 HMPSDK : APP->SDK: Generate display text image by generateTextImages().");
                lists = HmpImageFactory.generateTextImages(spannable1, size, omitted, null, mGravity);
                if (lists != null){
                    Log.e(TAG, "generateImage: images != null");
                    mPreImages.add(lists.get(0));
                }
            }
        }

        /* UIで連番テキストの画像を表示 */
        GradientDrawable background1 = new GradientDrawable(LEFT_RIGHT, new int[]{0x00FFFFFF, 0xFFFFFFFF});
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        for (HmpImage image : mPreImages) {
            /* 表示用のbitmapを取得 */
            Logger.i(TAG, "generateImage()- info4 HMPSDK : APP->SDK: Get bitmap by getBitmap().");
            Bitmap bitmap = image.getBitmap();
            Logger.i(TAG, "generateImage()- info4 HMPSDK : APP->SDK: Get bitmap height by getHeight().");
            Logger.i(TAG, "generateImage()- info4 HMPSDK : APP->SDK: Get bitmap width by getWidth().");
            int height1 = image.getHeight() * getResources().getDisplayMetrics().densityDpi / 600;
            int width1 = image.getWidth() * getResources().getDisplayMetrics().densityDpi / 600;
            if (width1 > containerLayout.getWidth()){
                width1 = containerLayout.getWidth();
                Bitmap line1 = Bitmap.createBitmap(width1 , height1 , Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(line1);
                canvas.drawColor(Color.TRANSPARENT);

                canvas.drawBitmap(
                        bitmap,
                        new Rect(0 , 0 , width1*bitmap.getHeight() / height1 , bitmap.getHeight()),
                        new Rect(0, 0 , width1 , height1),
                        null);
                background1.setBounds((width1 - height1 * 2) , 0 , width1 , height1);
                background1.draw(canvas);
                bitmaps.add(line1);
            } else {
                Bitmap line1 = Bitmap.createBitmap(width1 , height1 , Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(line1);
                canvas.drawColor(Color.TRANSPARENT);

                canvas.drawBitmap(
                        bitmap ,
                        new Rect(0 , 0 , bitmap.getWidth() , bitmap.getHeight()),
                        new Rect(0 , 0 , line1.getWidth() , line1.getHeight()),
                        null);
                bitmaps.add(line1);
            }
            bitmap.recycle();
        }

        WindowManager manager = (WindowManager) this.getContext().getSystemService(Context.WINDOW_SERVICE);

        DisplayMetrics metrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(metrics);

        /* the width of current device*/
        int deviceWidth = metrics.widthPixels;

        for (int i = 0; i < bitmaps.size(); i++){
            Bitmap bitmap = bitmaps.get(i);
            if (i < 2 ){
                ImageView imageView = new ImageView(getContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_START);
                imageView.setImageBitmap(bitmap);
                if (bitmap != null){
                    containerLayout.addView(imageView , new LinearLayout.LayoutParams(bitmap.getWidth() , bitmap.getHeight()));
                }
            } else {
                if (textIncrementNumber > 2){
                    TextView textView = new TextView(getContext());
                    textView.setSingleLine(false);
                    textView.setText(".\n.\n.");
                    if (bitmap != null){
                        containerLayout.addView(textView , new LinearLayout.LayoutParams(bitmap.getWidth() , bitmap.getHeight()));
                    }
                    textView.setWidth(deviceWidth);
                    textView.setGravity(Gravity.CENTER_HORIZONTAL);
                }

                ImageView imageView = new ImageView(getContext());
                imageView.setScaleType(ImageView.ScaleType.FIT_START);
                imageView.setImageBitmap(bitmap);
                if (bitmap != null){
                    containerLayout.addView(imageView , new LinearLayout.LayoutParams(bitmap.getWidth() , bitmap.getHeight()));
                }
            }
        }
        containerLayout.setBackgroundColor(Color.WHITE);

    }

    public String getIncreString() {
        String text = mText.getText().toString();
        int min = Integer.parseInt(mMin.getText().toString());
        int max = Integer.parseInt(mMax.getText().toString());
        String textAndNum = "";

        if (!mFormat.getText().toString().equals("")){
            if (Integer.parseInt(mFormat.getText().toString()) == 0) {
                int num = min + mPrintingNum - 1;
                textAndNum = text + num;
            } else {
                int num = min + mPrintingNum - 1;
                textAndNum = text + " " + num + "/" + max;
            }
        }
        return textAndNum;
    }
    public String getCurrentIncreString(int number){
        String text = mText.getText().toString();
        int max = Integer.parseInt(mMax.getText().toString());
        String currentText = "";

        if (!mFormat.getText().toString().equals("")){
            if (Integer.parseInt(mFormat.getText().toString()) == 0) {
                currentText = text + number;
            } else {
                currentText = text + number + "/" + max;
            }
        }

        return currentText;
    }


}
