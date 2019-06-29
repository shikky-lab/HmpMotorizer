package jp.co.ricoh.hmp.test.view.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;

import static jp.co.ricoh.hmp.sdk.image.generator.Barcode.Format;

/**
 * フォーマットスピナ
 */
public class FormatSpinner extends AppCompatSpinner {

    /**
     * タグ
     */
    private static final String TAG = FormatSpinner.class.getSimpleName();

    /**
     * アダプタ
     */
    final Adapter mAdapter;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public FormatSpinner(Context context) {
        this(context, null);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs   属性
     */
    public FormatSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * コンストラクタ
     *
     * @param context      コンテキスト
     * @param attrs        属性
     * @param defStyleAttr デフォルト属性
     */
    public FormatSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAdapter = new Adapter(context);
        setAdapter(mAdapter);
        setSelection(Item.position(Format.EAN_13));
    }

    /**
     * 取得
     *
     * @return フォーマット
     */
    public Format get() {
        return ((Item) getSelectedItem()).getFormat();
    }

    /**
     * 設定
     *
     * @param format フォーマット
     */
    public void set(Format format) {
        setSelection(Item.position(format));
    }

    /**
     * アダプタ
     */
    class Adapter extends BaseAdapter {

        /**
         * インフレータ
         */
        final LayoutInflater mInflater;

        /**
         * コンストラクタ
         *
         * @param context コンテキスト
         */
        Adapter(Context context) {
            super();
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return Item.values().length;
        }

        @Override
        public Item getItem(int position) {
            return Item.positionOf(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = mInflater.inflate(R.layout.item_format, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            holder.setItem(getItem(position));
            return convertView;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = mInflater.inflate(R.layout.item_format, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            }
            holder.setItem(getItem(position));
            return convertView;
        }
    }

    /**
     * アイテム
     */
    enum Item {
        /**
         * CODABAR 1D format.
         */
        CODABAR(Format.CODABAR, R.string.barcode_format_codabar),

        /**
         * Code 39 1D format.
         */
        CODE_39(Format.CODE_39, R.string.barcode_format_code_39),

        /**
         * Code 128 1D format.
         */
        CODE_128(Format.CODE_128, R.string.barcode_format_code_128),

        /**
         * EAN-8 1D format.
         */
        EAN_8(Format.EAN_8, R.string.barcode_format_ean_8),

        /**
         * EAN-13 1D format.
         */
        EAN_13(Format.EAN_13, R.string.barcode_format_ean_13),

        /**
         * ITF (Interleaved Two of Five) 1D format.
         */
        ITF(Format.ITF, R.string.barcode_format_itf);

        /**
         * フォーマット
         */
        final Format mFormat;

        /**
         * リソースID
         */
        final int mResId;

        /**
         * アイテム取得
         *
         * @param position 位置
         * @return アイテム
         */
        static Item positionOf(int position) {
            try {
                return values()[position];
            } catch (IndexOutOfBoundsException e) {
                Logger.w(TAG, "positionOf() - warning: index out of bounds.");
                return EAN_13;
            }
        }

        /**
         * 位置取得
         *
         * @param format フォーマット
         * @return アイテム
         */
        static int position(Format format) {
            for (Item item : values()) {
                if (item.mFormat == format) {
                    return item.ordinal();
                }
            }
            return 0;
        }

        /**
         * コンストラクタ
         *
         * @param format フォーマット
         * @param resId  リソースID
         */
        Item(Format format, int resId) {
            mFormat = format;
            mResId = resId;
        }

        /**
         * フォーマット
         *
         * @return フォーマット
         */
        public Format getFormat() {
            return mFormat;
        }

        /**
         * リソースID
         *
         * @return リソースID
         */
        public int getResId() {
            return mResId;
        }
    }

    /**
     * ビューホルダ
     */
    static class ViewHolder {

        /**
         * タイトルテキスト
         */
        @BindView(R.id.text)
        TextView mTextView;

        /**
         * コンストラクタ
         *
         * @param view ビュー
         */
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        /**
         * アイテム設定
         *
         * @param item アイテム
         */
        void setItem(Item item) {
            mTextView.setText(item.getResId());
        }
    }
}