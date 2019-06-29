package jp.co.ricoh.hmp.test.view.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import org.greenrobot.eventbus.EventBus;

import jp.co.ricoh.hmp.test.model.Logger;

/**
 * エディットテキスト
 */
public class EditText extends AppCompatEditText {

    /**
     * タグ
     */
    private static final String TAG = EditText.class.getSimpleName();

    /**
     * 入力管理
     */
    private final InputMethodManager mInputMethodManager;

    /**
     * 初期値
     */
    private String mText = "";

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public EditText(Context context) {
        this(context, null);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs   属性
     */
    public EditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * コンストラクタ
     *
     * @param context      コンテキスト
     * @param attrs        属性
     * @param defStyleAttr デフォルト属性
     */
    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        setOnFocusChangeListener(this::onFocusChange);
        addTextChangedListener(mTextWatcher);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            requestFocus();
            return false;
        }
        return super.onKeyPreIme(keyCode, event);
    }

    /**
     * 本文設定
     *
     * @param text テキスト
     */
    public void set(String text) {
        if (!TextUtils.isEmpty(text)) {
            mText = text;
            super.setText(text);
        }
    }

    /**
     * 本文取得
     *
     * @return テキスト
     */
    public String get() {
        return getText().toString();
    }

    /**
     * 変更有無
     *
     * @return 変更有無
     */
    public boolean isChanged() {
        return !mText.equals(getText().toString());
    }

    /**
     * 存在有無
     *
     * @return 存在有無
     */
    public boolean isEmpty() {
        return TextUtils.isEmpty(getText().toString());
    }

    /**
     * 入力監視
     */
    private final TextWatcher mTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            onAfterTextChanged(s);
        }
    };

    /**
     * 入力確定
     *
     * @param s 確定文字列
     */
    protected void onAfterTextChanged(Editable s) {
        if (isFocused()) {
            EventBus.getDefault().post(Event.CHANGE);
        }
    }

    /**
     * フォーカスチェンジ
     *
     * @param view     ビュー
     * @param hasFocus フォーカス
     */
    protected void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            showSoftInput();
        } else {
            hideSoftInputFromWindow();
        }
    }

    /**
     * ソフトウェアキーボード表示
     */
    protected void showSoftInput() {
        if (mInputMethodManager != null) {
            boolean result = mInputMethodManager.showSoftInput(this, InputMethodManager.SHOW_FORCED);
            Logger.d(TAG, "showSoftInput() - debug : result= " + result);
        }
    }

    /**
     * ソフトウェアキーボード消去
     */
    protected void hideSoftInputFromWindow() {
        if (mInputMethodManager != null) {
            boolean result = mInputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            Logger.d(TAG, "hideSoftInputFromWindow() - debug : result= " + result);
        }
    }

    /**
     * 数値変換
     *
     * @param editable 編集テキスト
     * @return 数値
     */
    protected static int valueOf(Editable editable) {
        try {
            return Integer.valueOf(editable.toString());
        } catch (NumberFormatException e) {
            Logger.w(TAG, "valueOf() - warning : failed convert.");
            return 0;
        }
    }

    /**
     * イベント
     */
    public enum Event {

        /**
         * 変更
         */
        CHANGE
    }
}