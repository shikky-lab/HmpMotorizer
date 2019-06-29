package jp.co.ricoh.hmp.test.view.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import jp.co.ricoh.hmp.sdk.image.HmpImage;
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
import jp.co.ricoh.hmp.test.view.richedit.RichEditText;
import jp.co.ricoh.hmp.test.view.richedit.RichEditTextCallback;
import jp.co.ricoh.hmp.test.view.widget.CopiesEdit;

/**
 *　縦書きテキストに関する功能を実現
 *　１，テキストのbitmapの生成。SDKのgenerateVertical関数で実現
 *　2，テキスト印刷用のHmpImageの生成。SDKのHmpImage関数で実現
 *　２，テキスト画像データー送信。SDKのprint関数で実現
 *  ３，印刷取消。SDKのcancel関数で実現
 *　４，印刷開始通知。SDKからJOB_STARTED受信時
 *　５，印刷完了通知。SDKからJOB_ENDED受信時
 *　６，印刷取消通知。SDKからJOB_CANCELED受信時
 *
 * シナリオ１：正常印刷
 * ユーザー、テキストを入力
 * ユーザー、プレビューボタンを押す
 * App側で、SDKのgenerateVertical関数を呼び出し、bitmapを生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのgenerateVertical関数で生成するbitmapをHmpImageキスト画像データーを本体へ送信
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
public class VTextFragment extends BaseFragment {

    /**
     * タグ
     */
    private static final String TAG = VTextFragment.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();

    /**
     * 印刷設定
     */
    HmpSettings mSettings = null;

    @BindView(R.id.richEditText)
    RichEditText mRichEditText;

    @BindView(R.id.linearLayoutImages)
    LinearLayout mLinearLayoutImages;
    /**
     * 部数エディット
     */
    @BindView(R.id.copies_edit)
    CopiesEdit mCopiesEdit;

    int mCopies = 1;

    ArrayList<HmpImage> mImages = new ArrayList<>();

    @BindView(R.id.head_title)
    TextView tvTitle;

    @BindView(R.id.bt_save)
    TextView tvSave;

    @BindView(R.id.gravitySpinner)
    Spinner gravitySpinner;

    Text.Gravity mGravity = Text.Gravity.CENTER;

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    public static void startFragment(Transition transition) {
        MainActivity.TransactionEvent.post(transition, new VTextFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vtext, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSettings = new HmpSettings(mPreferenceManager.getPosition(), Direction.RIGHT, Pass.SINGLE, Theta.DISABLE);
        tvTitle.setText(getResources().getString(R.string.vertical_text_title));
        mCopiesEdit.setCopies(mCopies);

        gravitySpinner.setSelection(1);
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
                mRichEditText.getContent("preview");
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mRichEditText.setEditTextCallback(new RichEditTextCallback() {
            @Override
            public void onGetContentCompleted(String handleType, String content) {
                if (content.isEmpty()){
                    mLinearLayoutImages.removeAllViews();
                    Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.qrcode_body_empty), Toast.LENGTH_LONG).show();
                    return;
                }
                /* テキスト画像生成 */
                Logger.i(TAG, "onGetContentCompleted()- info HMPSDK : APP->SDK: Generate text image by generateVertical().");
                ArrayList<Bitmap> bitmaps = Text.generateVertical(content, 225, mGravity, null);
                VTextFragment.this.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (content.equals("")) {
                            Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_no_input_text), Toast
                                    .LENGTH_SHORT).show();
                            return;
                        }

                        if (handleType.equals("preview")) { /* プレビューボタンを押す */
                            mLinearLayoutImages.removeAllViews();
                            for (Bitmap bitmap : bitmaps) {
                                ImageView imageView = new ImageView(VTextFragment.this.getActivity());
                                imageView.setImageBitmap(bitmap);
                                imageView.setBackgroundColor(Color.WHITE);
                                imageView.setScaleType(ImageView.ScaleType.MATRIX);
                                LinearLayout.LayoutParams layoutParams = new LinearLayout
                                        .LayoutParams(bitmap.getWidth(), LinearLayout.LayoutParams
                                        .MATCH_PARENT);
                                layoutParams.setMargins(10, 0, 0, 0);
                                imageView.setLayoutParams(layoutParams);
                                mLinearLayoutImages.addView(imageView, 0);
                            }
                        } else if (handleType.equals("print")) { /* 印刷ボタンを押す */
                            mImages.clear();
                            if (!mPrinterManager.isConnected()) {
                                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_connect_device),
                                        Toast.LENGTH_SHORT).show();
                                PrinterListFragment.startFragment(Transition.NEXT, getResources().getString(R.string.ver_text_categray));
                                return;
                            }
                            if (checkPrintEnable()) {
                                return;
                            }
                            int copies = mCopiesEdit.getCopies();
                            if (copies < 1) {
                                Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_value_invalid), Toast.LENGTH_LONG).show();
                                return;
                            }

                            for (Bitmap bitmap : bitmaps) {
                                if (bitmap != null) {
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(90);
                                    Bitmap lineBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                                    Mat mat = new Mat();
                                    Utils.bitmapToMat(lineBitmap, mat);

                                    /* 印刷用画像生成 */
                                    HmpImage hmpImage = new HmpImage();
                                    Logger.i(TAG, "onGetContentCompleted()- info HMPSDK : APP->SDK: Set ImageType by setImageType().");
                                    hmpImage.setImageType(HmpImage.ImageType.TEXT, null);
                                    Logger.i(TAG, "onGetContentCompleted()- info HMPSDK : APP->SDK: Set Image by setImage().");
                                    hmpImage.setImage(mat);

                                    mImages.add(hmpImage);
                                }
                            }
                            mSettings = new HmpSettings(mPreferenceManager.getPosition(), HmpSettings.Direction.RIGHT, HmpSettings.Pass.SINGLE, HmpSettings.Theta.DISABLE, HmpSettings.Coordinate.RIGHT);

                            mPrinterManager.print(mImages, mSettings, copies);
                        } else {

                        }
                    }
                });
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mPrinterManager.cancel();
    }

    @OnClick(R.id.copies_minus_button)
    public void onClickCopiesMinusButton() {
        mCopiesEdit.decrement();
    }

    @OnClick(R.id.copies_plus_button)
    public void onClickCopiesPlusButton() {
        mCopiesEdit.increment();
    }

    @OnClick(R.id.Preview_button)
    public void OnClickPreviewButton(View v) {
        mRichEditText.getContent("preview");
    }

    @OnClick(R.id.Print_button)
    public void OnClickPrintButton(View v) {
        mRichEditText.getContent("print");
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
