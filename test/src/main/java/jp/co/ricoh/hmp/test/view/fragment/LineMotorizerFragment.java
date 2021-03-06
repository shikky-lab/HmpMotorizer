package jp.co.ricoh.hmp.test.view.fragment;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import jp.co.ricoh.hmp.sdk.image.HmpImage;
import jp.co.ricoh.hmp.sdk.image.HmpImageFactory;
import jp.co.ricoh.hmp.sdk.image.generator.Text;
import jp.co.ricoh.hmp.sdk.printer.HmpCommand;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.BtDeviceManager;
import jp.co.ricoh.hmp.test.model.HmpConstants;
import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.model.PrinterManager;
import jp.co.ricoh.hmp.test.view.widget.CopiesEdit;
import timber.log.Timber;

import android.text.style.AbsoluteSizeSpan;

import static android.app.Activity.RESULT_OK;
import static android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT;

/**
 */
public class LineMotorizerFragment extends BaseFragment {

    /**
     * タグ
     */
    private static final String TAG = LineMotorizerFragment.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();
    final BtDeviceManager mBtDeviceManager = BtDeviceManager.getInstance();

    private static final int RESULT_PICK_IMAGEFILE = 1000;
    private static final int MAX_WIDTH=700;//公証594だが，700くらいまではいけた．1000だとダメだった．なお，サイレントで失敗するので厄介．
    private static final int MAX_TEXT_WIDTH=594;//こっちは700でダメだった．
//    private static final int MAX_WIDTH=10000;
    private static final int MAX_HEIGHT=13;
    private static final String digitRegex = "\\d+";

    /**
     * 画像
     */
    @BindView(R.id.image)
    ImageView mImage;

    @BindView(R.id.head_title)
    TextView tvTitle;

    @BindView(R.id.reference_button)
    Button refButton;

    @BindView(R.id.width)
    EditText widthEditText;

    @BindView(R.id.quantity)
    EditText heightEditText;

    @BindView(R.id.inputEditText)
    EditText inputEditText;

    @BindView(R.id.fontSizeEditText)
    EditText fontSizeEditText;

    ArrayList<HmpImage> mImages = new ArrayList<>();

    Bitmap mBitmap = null;

    HmpImage mHmpImage = null;

    /**
     * 画像の種類を指定？GRAPHIC_LINE指定すると線画になる．サイズ制限とは無関係っぽい．
     */
    HmpImage.ImageType imageType = HmpImage.ImageType.GRAPHIC;

    /**
     * 部数
     */
    int mCopies = 1;

    boolean fromText=false;

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
        MainActivity.TransactionEvent.post(transition, new LineMotorizerFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_line_motorizer, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        mCopiesEdit.setCopies(mCopies);
        tvTitle.setText(getResources().getString(R.string.image_motorizer_title));
        //画像を読み込むまではdeactivateしておく
        widthEditText.setEnabled(false);
        heightEditText.setEnabled(false);
    }

    @Override
    public void onStop() {
        super.onStop();
        mPrinterManager.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
//        HmpImageFactory.createImageFromFile(path, mListener);
    }

    @OnFocusChange(value = R.id.width)
    void afterWidthTextFocusChanged(@SuppressWarnings("unused")View v,boolean hasFocus) {
        if(hasFocus){
            return;
        }
        String inputText = widthEditText.getText().toString();

        if(!inputText.matches(digitRegex)){
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),"Please input digit",Toast.LENGTH_SHORT).show();
            return;
        }
        updateBasedWidth(mBitmap,Integer.parseInt(inputText));
    }

//    @OnTextChanged(value = R.id.width,callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
//    void afterWidthTextChanged(Editable s){
//        if(widthEditText.hasFocus()){
//            return;
//        }
//        String inputText = s.toString();
//        if(!inputText.matches(digitRegex)){
//            return;
//        }
//        updateBasedWidth(mBitmap,Integer.parseInt(inputText));
//    }

    @OnFocusChange(value = R.id.quantity)
    void afterHeightTextChanged(@SuppressWarnings("unused") View v, boolean hasFocus) {
        if(hasFocus){
            return;
        }
        String inputText = heightEditText.getText().toString();

        if(!inputText.matches(digitRegex)){
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),"Please input digit",Toast.LENGTH_SHORT).show();
            return;
        }
        updateBasedHeight(mBitmap,Integer.parseInt(inputText));
    }

//    @OnTextChanged(value = R.id.height,callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
//    void afterHeightTextChanged(Editable s){
//        if(quantityEditText.hasFocus()){
//            return;
//        }
//        String inputText = s.toString();
//        if(!inputText.matches(digitRegex)){
//            return;
//        }
//        updateBasedHeight(mBitmap,Integer.parseInt(inputText));
//    }
    private double getBmpRatio(Bitmap bmp){
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();

        return (double)bmpHeight/(double)bmpWidth;
    }

    private void updateBasedWidth(Bitmap bmp,int width){
        double bmpRatio = getBmpRatio(bmp);
        int calculated_width = width>MAX_WIDTH?MAX_WIDTH:width;
        int calculated_height =(int)((double)calculated_width*bmpRatio+0.5);//収束させるため四捨五入
        if(calculated_height>MAX_HEIGHT){
            calculated_height = MAX_HEIGHT;
            calculated_width = (int)((double)MAX_HEIGHT/bmpRatio+0.5);
        }
        updateEditTextIfNeeds(calculated_width,calculated_height);
    }

    private void updateBasedHeight(Bitmap bmp,int height){
        double bmpRatio = getBmpRatio(bmp);
        int calculated_height = height>MAX_HEIGHT?MAX_HEIGHT:height;
        int calculated_width =(int)((double)calculated_height/bmpRatio+0.5);
        if(calculated_width>MAX_WIDTH){
            calculated_width = MAX_WIDTH;
            calculated_height = (int)((double)MAX_WIDTH*bmpRatio+0.5);
        }
        updateEditTextIfNeeds(calculated_width,calculated_height);
    }

    private void updateEditTextIfNeeds(int calculated_width,int calculated_height){
        if(!widthEditText.getText().toString().equals(String.valueOf(calculated_width))){
            widthEditText.setText(String.valueOf(calculated_width));
        }
        if(!heightEditText.getText().toString().equals(String.valueOf(calculated_height))){
            heightEditText.setText(String.valueOf(calculated_height));
        }
    }

    @OnClick(R.id.generate_button)
    void generateImageFromText(){
        String inputText = inputEditText.getText().toString();
        if(inputText.equals("")){
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "please input texts", Toast.LENGTH_SHORT).show();
            return;
        }
        updateBitmap(inputText);
        fromText=true;
    }

    private void updateBitmap(String strText){
        mImages.clear();
        Bitmap map = null;
        Bitmap line = null;

        int fontSize= Integer.parseInt(fontSizeEditText.getText().toString());
        if(fontSize<6 || 36<fontSize){
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "set font size between 6 to 36", Toast.LENGTH_SHORT).show();
            return;
        }
//        int fontSize = 12 * HmpConstants.DPI / HmpConstants.FONT_PT_DENOMINATOR;   /* フォントサイズ：12．*/
        fontSize = fontSize * HmpConstants.DPI / HmpConstants.FONT_PT_DENOMINATOR;   /* フォントサイズ：12．*/
        AbsoluteSizeSpan sizeSpan = new AbsoluteSizeSpan(fontSize);
        SpannableStringBuilder spannable = new SpannableStringBuilder();
        spannable.setSpan(sizeSpan, 0, 0, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.setSpan(sizeSpan, spannable.length(), spannable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        spannable.append(strText);
        spannable.setSpan(sizeSpan, spannable.getSpanStart(sizeSpan), spannable.getSpanEnd(sizeSpan), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.append('\n');
        spannable.setSpan(sizeSpan, spannable.getSpanStart(sizeSpan), spannable.getSpanEnd(sizeSpan), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
//        spannable.setSpan(sizeSpan, 0, spannable.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);

//        int size = strText.length()*fontSize;//フォントによって同じサイズでも大きさ変わる気がするので，この指定法はダメな気がする．．．

        int size=MAX_TEXT_WIDTH* HmpConstants.DPI*10/254;
        boolean omitted = false;//これをtrueにしておくと，文字後の空白を除去してくれるらしい．つまりサイズに合わせてクロッピングしてくれる．

        /* テキスト画像生成 */
        Logger.i(TAG, "generateTextImages()- info HMPSDK : APP->SDK: Generate text image by generateTextImages().");
        mImages = HmpImageFactory.generateTextImages(spannable, size, omitted, null, Text.Gravity.TOP);
        if(mImages.size()>1){
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "Failed to generate.Reduce characters", Toast.LENGTH_SHORT).show();
            return;
        }

        /* UIで表示する画像を生成 */
        Logger.i(TAG, "generateTextImages()- info HMPSDK : APP->SDK: Generate text bitmap by getBitmap().");
        map = mImages.get(0).getBitmap();
        if (map == null) {
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), getResources().getString(R.string.qrcode_image_not_created), Toast.LENGTH_SHORT).show();
        }

        int height = Objects.requireNonNull(map).getHeight() * getResources().getDisplayMetrics().densityDpi / HmpConstants.DPI;
        int width = map.getWidth() * getResources().getDisplayMetrics().densityDpi / HmpConstants.DPI;
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
                    widthEditText.setEnabled(true);
                    heightEditText.setEnabled(true);
                    updateBasedWidth(mBitmap,mBitmap.getWidth());
                    break;

                case PROCESSED:
                    /* App側でSDK側のresize関数を呼び出す。
                    SDK側で画像を拡縮した後に、App側へPROCESSEDイベントを通知する。
                    App側でPROCESSEDイベントを受信して、拡縮後の画像を本体へ送信 */
                    mImages.add(image);

                    int copies = mCopiesEdit.getCopies();
                    if (copies < 1 || copies > 999) {
                        Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), getResources().getString(R.string.message_value_invalid), Toast.LENGTH_LONG).show();
                        Timber.w("onReceive() - warning : data is not validate.");
                        return;
                    }
                    HmpSettings mSettings = new HmpSettings(mPreferenceManager.getPosition(), HmpSettings.Direction.RIGHT, HmpSettings.Pass.SINGLE, HmpSettings.Theta.DISABLE);
                    mPrinterManager.print(mImages, mSettings, copies);
                    Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),"Sending...",Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHaveRead(BtDeviceManager.Event event){
        switch (event){
            case HAVE_READ_BLOCK:
                String readData = mBtDeviceManager.readBlock();
                Toast.makeText(this.getContext(), "called onHaveRead", Toast.LENGTH_SHORT).show();
                break;
            case HAVE_READ_CHARACTER:
                break;
            case CONNECTED:
            case DISCONNECTED:
                break;
            default:
                break;
        }
    }

    @OnClick(R.id.print_button)
    public void OnClickPrintButton(View v)
    {
        //noinspection PointlessBooleanExpression
        if (fromText == true) {
            int copies = mCopiesEdit.getCopies();
            if (copies < 1 || copies > 999) {
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), getResources().getString(R.string.message_value_invalid), Toast.LENGTH_LONG).show();
                Timber.w("onReceive() - warning : data is not validate.");
                return;
            }
            HmpSettings mSettings = new HmpSettings(mPreferenceManager.getPosition(), HmpSettings.Direction.RIGHT, HmpSettings.Pass.SINGLE, HmpSettings.Theta.DISABLE);
            mPrinterManager.print(mImages, mSettings, copies);
            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),"Sending...",Toast.LENGTH_LONG).show();

        }else {
            mImages.clear();

            //        if (!mPrinterManager.isConnected()) {
            //            Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),getResources().getString(R.string.message_connect_device),Toast.LENGTH_SHORT).show();
            ////            PrinterListFragment.startFragment(MainActivity.Transition.NEXT,getResources().getString(R.string.photo_categray));
            //            return;
            //        }

            if (checkPrintEnable()) {
                return;
            }

            int width = 0;
            int height = 0;
            try {
                width = Integer.parseInt(widthEditText.getText().toString());
                height = Integer.parseInt(heightEditText.getText().toString());
            } catch (NumberFormatException e) {
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(), "please input digit", Toast.LENGTH_SHORT).show();
            }
            width = width * HmpConstants.DPI * 10 / 254;    /* mm To px */
            height = height * HmpConstants.DPI * 10 / 254;       /* mm To px */

            if (mHmpImage != null) {
                /* 設定する画像の幅と高さにより、画像を拡縮する */
                /* SDK側で画像を拡縮した後に、アプリ側へPROCESSEDを通知する */
                Logger.i(TAG, "OnClickPrintButton()- info HMPSDK : APP->SDK: Resize Image by resize().");
                mHmpImage.resize(width, height, mListener);
            }
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

    @OnClick(R.id.reference_button)
    public void onClickRefButton(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT) ;
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent,RESULT_PICK_IMAGEFILE);
        fromText=false;
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent resultData){
        //noinspection SwitchStatementWithTooFewBranches
        switch(requestCode){
            case RESULT_PICK_IMAGEFILE:
                if(resultCode != RESULT_OK){
                    Log.d(TAG,"Failed to load Image");
                    return;
                }
                Uri uri = resultData.getData();
                String path = getPathFromUri(this.getContext(), Objects.requireNonNull(uri));
                HmpImageFactory.createImageFromFile(Objects.requireNonNull(path), mListener);
                break;
        }

    }
    private String getPathFromUri(final Context context, final Uri uri) {
        Log.e(TAG,"uri:" + uri.getAuthority());
        if (DocumentsContract.isDocumentUri(context, uri)) {
            if ("com.android.externalstorage.documents".equals(
                    uri.getAuthority())) {// ExternalStorageProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }else {
                    return "/stroage/" + type +  "/" + split[1];
                }
            }else if ("com.android.providers.downloads.documents".equals(
                    uri.getAuthority())) {// DownloadsProvider
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            }else if ("com.android.providers.media.documents".equals(
                    uri.getAuthority())) {// MediaProvider
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
//                final String type = split[0];
                Uri contentUri;
                contentUri = MediaStore.Files.getContentUri("external");
                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }else if ("content".equalsIgnoreCase(uri.getScheme())) {//MediaStore
            return getDataColumn(context, uri, null, null);
        }else if ("file".equalsIgnoreCase(uri.getScheme())) {// File
            return uri.getPath();
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {
        Cursor cursor = null;
        final String[] projection = {
                MediaStore.Files.FileColumns.DATA
        };
        //noinspection TryFinallyCanBeTryWithResources
        try {
            cursor = context.getContentResolver().query(
                    uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                final int cindex = cursor.getColumnIndexOrThrow(projection[0]);
                return cursor.getString(cindex);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
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
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),getResources().getString(R.string.message_print_start),Toast.LENGTH_SHORT).show();
                break;
            case JOB_ENDED:
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),getResources().getString(R.string.message_print_complete),Toast.LENGTH_SHORT).show();
                break;
            case JOB_CANCELED:
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),getResources().getString(R.string.message_print_cancel),Toast.LENGTH_SHORT).show();
                break;
            case UPDATE_STATUS://JOB_STARTEDの直後にUPDATE_STATUSが飛んできて，その際のerrorは"PRINTING".あとこれボタン押すたびに飛んでくるっぽい？
            case STATUS_CHANGED:
                HmpCommand.DeviceStatus mError = mPrinterManager.getError();
                Logger.i(TAG,"status changed:"+ mError.toString());
                break;
            case PRINTED_PASS://PRINTED_PAGEとの使い分けは謎だが，1パス終了時にどっちも飛んでくるので，ここではこっちを使用．
                Toast.makeText(Objects.requireNonNull(getActivity()).getApplicationContext(),"Printed one line",Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
    }

}
