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
import jp.co.ricoh.hmp.sdk.image.generator.Barcode.Weight;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;

/**
 * 太さスピナ
 */
public class WeightSpinner extends AppCompatSpinner {

    /**
     * タグ
     */
    private static final String TAG = WeightSpinner.class.getSimpleName();

    /**
     * アダプタ
     */
    final Adapter mAdapter;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public WeightSpinner(Context context) {
        this(context, null);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs   属性
     */
    public WeightSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * コンストラクタ
     *
     * @param context      コンテキスト
     * @param attrs        属性
     * @param defStyleAttr デフォルト属性
     */
    public WeightSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAdapter = new Adapter(context);
        setAdapter(mAdapter);
        setSelection(Weight.NORMAL.ordinal());
    }

    /**
     * 取得
     *
     * @return 太さ
     */
    public Weight get() {
        return ((Item) getSelectedItem()).getWeight();
    }

    /**
     * 設定
     *
     * @param weight 太さ
     */
    public void set(Weight weight) {
        setSelection(Item.position(weight));
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
                convertView = mInflater.inflate(R.layout.item_weight, parent, false);
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
                convertView = mInflater.inflate(R.layout.item_weight, parent, false);
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
         * 最大
         */
        MAXIMUM(Weight.MAXIMUM, R.string.barcode_weight_maximum),

        /**
         * 標準
         */
        NORMAL(Weight.NORMAL, R.string.barcode_weight_normal),

        /**
         * 最小
         */
        MINIMUM(Weight.MINIMUM, R.string.barcode_weight_minimum);

        /**
         * 太さ
         */
        final Weight mWeight;

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
                return NORMAL;
            }
        }

        /**
         * 位置取得
         *
         * @param weight 太さ
         * @return アイテム
         */
        static int position(Weight weight) {
            for (Item item : values()) {
                if (item.mWeight == weight) {
                    return item.ordinal();
                }
            }
            return NORMAL.ordinal();
        }

        /**
         * コンストラクタ
         *
         * @param weight 太さ
         * @param resId  リソースID
         */
        Item(Weight weight, int resId) {
            mWeight = weight;
            mResId = resId;
        }

        /**
         * 太さ
         *
         * @return 太さ
         */
        public Weight getWeight() {
            return mWeight;
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