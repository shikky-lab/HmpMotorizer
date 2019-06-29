package jp.co.ricoh.hmp.test.view.widget;

import android.content.Context;
import android.graphics.Color;
import android.text.Editable;
import android.text.InputFilter;
import android.util.AttributeSet;

import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.view.fragment.BarCodeFragment;

/**
 * 部数エディットテキスト
 */
public class CopiesEdit extends EditText {

    /**
     * タグ
     */
    private static final String TAG = CopiesEdit.class.getSimpleName();

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public CopiesEdit(Context context) {
        this(context, null);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs   属性
     */
    public CopiesEdit(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * コンストラクタ
     *
     * @param context      コンテキスト
     * @param attrs        属性
     * @param defStyleAttr デフォルト属性
     */
    public CopiesEdit(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setFilters(new InputFilter[]{(source, start, end, dest, dstart, dend) -> {
            try {
                int input = Integer.parseInt(dest.toString() + source.toString());
                if (input >= 1 && input <= 999) return null;
            } catch (NumberFormatException nfe) {
                Logger.e(TAG, "filter() - error : failed convert.");
            }
            return "";
        }});
    }

    /**
     * 部数取得
     *
     * @param copies 部数
     */
    public void setCopies(int copies) {
        setText(String.valueOf(copies));
    }

    /**
     * 部数取得
     *
     * @return 部数
     */
    public int getCopies() {
        return valueOf(getText());
    }

    /**
     * 増量
     */
    public void increment() {
        int copies = getCopies();
        if (copies < 999) {
            copies++;
        }
        setText(String.valueOf(copies));
    }

    /**
     * 減量
     */
    public void decrement() {
        int copies = getCopies();
        if (copies > 1) {
            copies--;
        }
        setText(String.valueOf(copies));
    }

    @Override
    protected void onAfterTextChanged(Editable s) {
        int length = valueOf(s);
        if (1 <= length && length <= 999) {
            setTextColor(Color.parseColor("#808080"));
        } else {
            setTextColor(Color.parseColor("#FF0000"));
        }
    }
}