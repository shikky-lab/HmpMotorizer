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

/**
 * 高さスピナ
 */
public class HeightSpinner extends AppCompatSpinner {

    /**
     * タグ
     */
    private static final String TAG = HeightSpinner.class.getSimpleName();

    /**
     * アダプタ
     */
    final Adapter mAdapter;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public HeightSpinner(Context context) {
        this(context, null);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs   属性
     */
    public HeightSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * コンストラクタ
     *
     * @param context      コンテキスト
     * @param attrs        属性
     * @param defStyleAttr デフォルト属性
     */
    public HeightSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAdapter = new Adapter(context);
        setAdapter(mAdapter);
    }

    /**
     * 取得
     *
     * @return 高さ
     */
    public double get() {
        return ((Item) getSelectedItem()).getHeight();
    }

    /**
     * 設定
     *
     * @param height 高さ
     */
    public void set(double height) {
        setSelection(Item.position(height));
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
                convertView = mInflater.inflate(R.layout.item_height, parent, false);
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
                convertView = mInflater.inflate(R.layout.item_height, parent, false);
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
         * 5.0 mm
         */
        MM050(5.0, R.string.barcode_height_50),

        /**
         * 5.5 mm
         */
        MM055(5.5, R.string.barcode_height_55),

        /**
         * 6.0 mm
         */
        MM060(6.0, R.string.barcode_height_60),

        /**
         * 6.5 mm
         */
        MM065(6.5, R.string.barcode_height_65),

        /**
         * 7.0 mm
         */
        MM070(7.0, R.string.barcode_height_70),

        /**
         * 7.5 mm
         */
        MM075(7.5, R.string.barcode_height_75),

        /**
         * 8.0 mm
         */
        MM080(8.0, R.string.barcode_height_80),

        /**
         * 8.5 mm
         */
        MM085(8.5, R.string.barcode_height_85),

        /**
         * 9.0 mm
         */
        MM090(9.0, R.string.barcode_height_90),

        /**
         * 9.50 mm
         */
        MM095(9.5, R.string.barcode_height_95),

        /**
         * 10.0 mm
         */
        MM100(10.0, R.string.barcode_height_100),

        /**
         * 10.5 mm
         */
        MM105(10.5, R.string.barcode_height_105),

        /**
         * 11.0 mm
         */
        MM110(11.0, R.string.barcode_height_110),

        /**
         * 11.0 mm
         */
        MM135(13.5, R.string.barcode_height_135);

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
                return MM050;
            }
        }

        /**
         * 位置取得
         *
         * @param height 高さ
         * @return アイテム
         */
        static int position(double height) {
            for (Item item : values()) {
                if (item.mHeight == height) {
                    return item.ordinal();
                }
            }
            return MM050.ordinal();
        }

        /**
         * コンストラクタ
         *
         * @param height 高さ
         * @param resId  リソースID
         */
        Item(double height, int resId) {
            mHeight = height;
            mResId = resId;
        }

        /**
         * 高さ
         */
        final double mHeight;

        /**
         * リソースID
         */
        final int mResId;

        /**
         * 高さの取得
         *
         * @return 高さ
         */
        public double getHeight() {
            return mHeight;
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