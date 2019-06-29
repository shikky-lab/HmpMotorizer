package jp.co.ricoh.hmp.test.view.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnClick;
import jp.co.ricoh.hmp.sdk.image.HmpImage;
import jp.co.ricoh.hmp.sdk.image.HmpImageFactory;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.model.PrinterManager;
import jp.co.ricoh.hmp.test.view.widget.CopiesEdit;
import timber.log.Timber;

/**
 * 画像に関する功能を実現
 *　１，画像の読み込むみ。SDKのHmpImageFactory.createImageFromFile関数で実現
 *　２，設定する幅と高さにより、印刷画像生成。SDKのHmpImageFactory.resize関数で実現
 *　２，画像印刷。SDKのprint関数で実現
 *  ３，印刷取消。SDKのcancel関数で実現
 *　４，印刷開始通知。印刷開始にSDKからJOB_STARTED受信
 *　５，印刷完了通知。印刷完了後にSDKからJOB_ENDED受信時
 *　６，印刷取消通知。印刷取消にSDKからJOB_CANCELED受信時
 *
 * シナリオ１：正常印刷
 * ユーザー、画像を選択
 * App側で、SDKのcreateImageFromFile関数を呼び出し、画像を生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのresize関数を呼び出し、設定する幅と高さにより、印刷画像生成。print関数を呼び出し、バーコードデーターを本体へ送信
 * SDK側で、本体側からJOB_STARTEDを受信して、App側へJOB_STARTEDイベントを送信
 * App側で、JOB_STARTEDイベントを受信して、印刷開始メッセージを表示
 * ユーザー、印刷する
 * SDK側で、印刷完了後、本体側からJOB_ENDEDを受信して、App側へJOB_ENDEDイベントを送信
 * App側で、JOB_ENDEDイベントを受信して、印刷完了メッセージを表示
 *
 * シナリオ２：印刷キャンセル
 * ユーザー、画像を選択
 * App側で、SDKのcreateImageFromFile関数を呼び出し、画像を生成
 * ユーザー、印刷ボタンを押す
 * App側で、SDKのresize関数を呼び出し、設定する幅と高さにより、印刷画像生成。print関数を呼び出し、バーコードデーターを本体へ送信
 * SDK側で、本体側からJOB_STARTEDを受信して、App側へJOB_STARTEDイベントを送信
 * App側で、JOB_STARTEDイベントを受信して、印刷開始メッセージを表示
 * ユーザー、キャンセルボタンを押す
 * App側で、SDKのcancel関数を呼び出し、キャンセルを本体へ送信
 * SDK側で、App側へJOB_CANCELEDイベントを送信
 * App側で、JOB_CANCELEDイベントを受信して、印刷キャンセルメッセージを表示
 *
 */
public class PhotoFragment extends BaseFragment {

    /**
     * タグ
     */
    private static final String TAG = PhotoFragment.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();

    /**
     * 画像
     */
    @BindView(R.id.image)
    ImageView mImage;

    @BindView(R.id.head_title)
    TextView tvTitle;

    @BindView(R.id.pic_select)
    Spinner sp_select;

    @BindView(R.id.line_switch)
    Switch mLineSwitch;

    ArrayList<HmpImage> mImages = new ArrayList<>();

    Bitmap mBitmap = null;

    HmpImage mHmpImage = null;

    String path = "";

    /**
     * イメージタイプ
     */
    HmpImage.ImageType imageType = HmpImage.ImageType.GRAPHIC;

    /**
     * 部数
     */
    int mCopies = 1;

    /**
     * 部数エディット
     */
    @BindView(R.id.copies_edit)
    CopiesEdit mCopiesEdit;

    /**
     * スタート
     *
     * @param transition 遷移種別
     */
    public static void startFragment(MainActivity.Transition transition) {
        MainActivity.TransactionEvent.post(transition, new PhotoFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_photo, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mCopiesEdit.setCopies(mCopies);
        tvTitle.setText(getResources().getString(R.string.photo_title));
    }

    @Override
    public void onStop() {
        super.onStop();
        mPrinterManager.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
       sp_select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
           @Override
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
               switch (position) {
                   case 0:
                       String pic = "sample.jpg";
                       path = getLocalPhotoPath(pic);
                       if (path.equals("")) {
                           Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.photo_no_file),Toast.LENGTH_SHORT).show();
                       }
                       /* 画像生成、SDK側で画像生成完了後に、アプリ側へLOADEDイベントを通知 */
                       Logger.i(TAG, "onItemSelected()- info HMPSDK : APP->SDK: Create Image by createImageFromFile().");
                       HmpImageFactory.createImageFromFile(path, mListener);
                       break;
                   case 1:
                       String pic1 = "sample.png";
                       path = getLocalPhotoPath(pic1);
                       if (path.equals("")) {
                           Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.photo_no_file),Toast.LENGTH_SHORT).show();
                       }
                       /* 画像生成、SDK側で画像生成完了後に、アプリ側へLOADEDイベントを通知 */
                       Logger.i(TAG, "onItemSelected()- info HMPSDK : APP->SDK: Create Image by createImageFromFile().");
                       HmpImageFactory.createImageFromFile(path, mListener);
                       break;
                   case 2:
                       String pic2 = "sample.bmp";
                       path = getLocalPhotoPath(pic2);
                       if (path.equals("")) {
                           Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.photo_no_file),Toast.LENGTH_SHORT).show();
                       }
                       /* 画像生成、SDK側で画像生成完了後に、アプリ側へLOADEDイベントを通知 */
                       Logger.i(TAG, "onItemSelected()- info HMPSDK : APP->SDK: Create Image by createImageFromFile().");
                       HmpImageFactory.createImageFromFile(path, mListener);
                       break;
               }
           }

           @Override
           public void onNothingSelected(AdapterView<?> parent) {

           }
       });

       mLineSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
               if (isCheck){
                   imageType = HmpImage.ImageType.GRAPHIC_LINE;
               } else {
                   imageType = HmpImage.ImageType.GRAPHIC;
               }
               Logger.i(TAG, "onCheckedChanged()- info HMPSDK : APP->SDK: Create Image by createImageFromFile().");
               mHmpImage = HmpImageFactory.createImageFromFile(path, mListener);
           }
       });
    }

    /**
     * リスナー
     */
    HmpImage.Listener mListener = new HmpImage.Listener() {
        @Override
        public void onReceive(@NonNull HmpImage image, @NonNull HmpImage.Event event) {
            Logger.i(TAG, "mPrinterListener - info HMPSDK : SDK->APP:  event=" + event);
            switch (event) {
                case LOADED:
                    /* App側でSDK側のcreateImageFromFile関数を呼び出す。
                    SDK側で画像を生成した後に、App側へLOADEDイベントを通知する。
                    App側でLOADEDイベントを受信してbitmap取得し、UIで表示する */
                    Logger.i(TAG, "onReceive()- info HMPSDK : APP->SDK: Set ImageType by setImageType().");
                    image.setImageType(imageType , null);
                    mHmpImage = image;
                    Logger.i(TAG, "onReceive()- info HMPSDK : APP->SDK: Get bitmap by getPrintBitmap().");
                    mBitmap = image.getPrintBitmap();
                    mImage.setImageBitmap(mBitmap);
                    break;

                case PROCESSED:
                    /* App側でSDK側のresize関数を呼び出す。
                    SDK側で画像を拡縮した後に、App側へPROCESSEDイベントを通知する。
                    App側でPROCESSEDイベントを受信して、拡縮後の画像を本体へ送信 */
                    mImages.add(image);

                    int copies = mCopiesEdit.getCopies();
                    if (copies < 1 || copies > 999) {
                        Toast.makeText(getActivity().getApplicationContext(), getResources().getString(R.string.message_value_invalid), Toast.LENGTH_LONG).show();
                        Timber.w("onReceive() - warning : data is not validate.");
                        return;
                    }
                    HmpSettings mSettings = new HmpSettings(mPreferenceManager.getPosition(), HmpSettings.Direction.RIGHT, HmpSettings.Pass.MULTI, HmpSettings.Theta.ENABLE);

                    mPrinterManager.print(mImages, mSettings, copies);
                    break;
            }
        }
    };

    @OnClick(R.id.print_button)
    public void OnClickPrintButton(View v)
    {
        mImages.clear();

        if (!mPrinterManager.isConnected()) {
            Toast.makeText(getActivity().getApplicationContext(),getResources().getString(R.string.message_connect_device),Toast.LENGTH_SHORT).show();
            PrinterListFragment.startFragment(MainActivity.Transition.NEXT,getResources().getString(R.string.photo_categray));
            return;
        }

        if (checkPrintEnable()) {
            return;
        }

        int width = 90 * 6000 / 254;    /* mm To px */
        int height = 60 * 6000 / 254;       /* mm To px */

        if (mHmpImage != null) {
            /* 設定する画像の幅と高さにより、画像を拡縮する */
            /* SDK側で画像を拡縮した後に、アプリ側へPROCESSEDを通知する */
            Logger.i(TAG, "OnClickPrintButton()- info HMPSDK : APP->SDK: Resize Image by resize().");
            mHmpImage.resize(width, height, mListener);
        }
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
    public void onClickCopiesMinusButton() {
        mCopiesEdit.decrement();
    }

    /**
     * プラスボタン
     */
    @OnClick(R.id.plus_button)
    public void onClickCopiesPlusButton() {
        mCopiesEdit.increment();
    }

    /**
     * 画像の保存パスを取得
     *
     * @return 画像を保存するパス
     */
    public String getLocalPhotoPath(String fileName) {
        String path = mMainActivity.getFilesDir().getAbsolutePath() + File.separator + fileName;

        try {
            InputStream inputStream = mMainActivity.getAssets().open(fileName);
            File file = new File(mMainActivity.getFilesDir().getAbsolutePath() + File.separator + fileName);

            if(!file.exists() || file.length()==0) {
                FileOutputStream fos =new FileOutputStream(file);
                int len=-1;
                byte[] buffer = new byte[1024];
                while ((len=inputStream.read(buffer))!=-1){
                    fos.write(buffer,0,len);
                }
                fos.flush();
                inputStream.close();
                fos.close();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return path;
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
