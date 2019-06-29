package jp.co.ricoh.hmp.test.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import java.util.ArrayList;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.co.ricoh.hmp.test.R;

public class FunctionListView extends ListView {

    /**
     * タグ
     */
    private static final String TAG = FunctionListView.class.getSimpleName();

    /**
     * アイテムリスト
     */
    final ArrayList<FunctionListView.Item> mItems = new ArrayList<>();

    /**
     * アダプタ
     */
    final FunctionListView.Adapter mAdapter;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public FunctionListView(Context context) {
        this(context, null);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs   属性
     */
    public FunctionListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * コンストラクタ
     *
     * @param context      コンテキスト
     * @param attrs        属性
     * @param defStyleAttr デフォルト属性
     */
    public FunctionListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAdapter = new FunctionListView.Adapter(context);
        setAdapter(mAdapter);
    }

    /**
     * 更新
     */
    public synchronized void update(ArrayList<String> names) {
        mItems.clear();

        for(int i = 0; i < names.size();i++)
        {
            mItems.add(new FunctionListView.Item(names.get(i)));
        }
        mAdapter.notifyDataSetChanged();
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
            return mItems.size();
        }

        @Override
        public FunctionListView.Item getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            FunctionListView.ViewHolder holder;

            if (view != null) {
                holder = (FunctionListView.ViewHolder) view.getTag();

            } else {
                view = mInflater.inflate(R.layout.item_function_item, parent, false);
                holder = new FunctionListView.ViewHolder(view);
                view.setTag(holder);
            }

            holder.setItem(getItem(position));

            return view;
        }
    }

    /**
     * アイテム
     */
    static class Item {

        /**
         * プリンタデバイス
         */
        final String mName;

        /**
         * コンストラクタ
         *
         * @param name プリンタデバイス
         */
        Item(String name) {
            mName = name;
        }
    }

    /**
     * ビューホルダ
     */
    static class ViewHolder {

        /**
         * アイテム
         */
        FunctionListView.Item mItem = null;

        /**
         * プリント情報
         */
        @BindView(R.id.function)
        TextView mFunction;

        /**
         * コンストラクタ
         *
         * @param view ビュー
         */
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void setItem(FunctionListView.Item item) {
            mItem = item;
            mFunction.setText(mItem.mName);
        }
    }
}
