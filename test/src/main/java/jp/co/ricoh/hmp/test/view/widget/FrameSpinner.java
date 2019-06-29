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
 * 線枠スピナ
 */
public class FrameSpinner extends AppCompatSpinner {

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
    public FrameSpinner(Context context) {
        this(context, null);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs   属性
     */
    public FrameSpinner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * コンストラクタ
     *
     * @param context      コンテキスト
     * @param attrs        属性
     * @param defStyleAttr デフォルト属性
     */
    public FrameSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAdapter = new Adapter(context);
        setAdapter(mAdapter);
        setSelection(Weight.NORMAL.ordinal());
    }

    /**
     * 取得
     *
     * @return 囲み有無
     */
    public boolean get() {
        return ((Item) getSelectedItem()).getFrame();
    }

    /**
     * 設定
     *
     * @param frame 囲み有無
     */
    public void set(boolean frame) {
        setSelection(Item.position(frame));
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
                convertView = mInflater.inflate(R.layout.item_frame, parent, false);
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
                convertView = mInflater.inflate(R.layout.item_frame, parent, false);
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
         *  囲みなし
         */
        NOFRAME(false, R.string.barcode_no_frame),

        /**
         * 囲みあり
         */
        FRAME(true, R.string.barcode_frame);

        /**
         * 囲み有無
         */
        final boolean mFrame;

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
                return NOFRAME;
            }
        }

        /**
         * 位置取得
         *
         * @param frame 囲み有無
         * @return アイテム
         */
        static int position(boolean frame) {
            for (Item item : values()) {
                if (item.mFrame == frame) {
                    return item.ordinal();
                }
            }
            return NOFRAME.ordinal();
        }

        /**
         * コンストラクタ
         *
         * @param frame 囲み有無
         * @param resId  リソースID
         */
        Item(boolean frame, int resId) {
            mFrame = frame;
            mResId = resId;
        }

        /**
         * 囲み有無
         *
         * @return 囲み有無
         */
        public boolean getFrame() {
            return mFrame;
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