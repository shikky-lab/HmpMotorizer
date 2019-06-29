package jp.co.ricoh.hmp.test.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.co.ricoh.hmp.sdk.printer.HmpPrinter;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.PrinterManager;

public class PrinterStatusListView extends ListView {

    /**
     * タグ
     */
    private static final String TAG = PrinterSelectListView.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();

    /**
     * アイテムリスト
     */
    final ArrayList<PrinterStatusListView.Item> mItems = new ArrayList<>();

    /**
     * アダプタ
     */
    final PrinterStatusListView.Adapter mAdapter;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public PrinterStatusListView(Context context) {
        this(context, null);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs   属性
     */
    public PrinterStatusListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * コンストラクタ
     *
     * @param context      コンテキスト
     * @param attrs        属性
     * @param defStyleAttr デフォルト属性
     */
    public PrinterStatusListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAdapter = new PrinterStatusListView.Adapter(context);
        setAdapter(mAdapter);
    }

    /**
     * 更新
     */
    public synchronized void update(ArrayList<String> titles, ArrayList<String> status) {

        mItems.clear();

        for(int i = 0; i < titles.size();i++)
        {
            mItems.add(new PrinterStatusListView.Item(titles.get(i),status.get(i)));
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
        public PrinterStatusListView.Item getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            PrinterStatusListView.ViewHolder holder;

            if (view != null) {
                holder = (PrinterStatusListView.ViewHolder) view.getTag();

            } else {
                view = mInflater.inflate(R.layout.item_status_item, parent, false);
                holder = new PrinterStatusListView.ViewHolder(view);
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
        final String mTitle;

        final String mStatus;

        /**
         * コンストラクタ
         *
         */
        Item(String title, String status) {
            mTitle = title;
            mStatus = status;
        }
    }

    /**
     * ビューホルダ
     */
    static class ViewHolder {


        /**
         * アイテム
         */
        PrinterStatusListView.Item mItem = null;

        /**
         * プリント情報
         */
        @BindView(R.id.title)
        TextView mTitle;

        /**
         * プリント情報
         */
        @BindView(R.id.status)
        TextView mStatus;

        /**
         * コンストラクタ
         *
         * @param view ビュー
         */
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void setItem(PrinterStatusListView.Item item) {
            mItem = item;
            mTitle.setText(item.mTitle);
            mStatus.setText(item.mStatus);
        }
    }
}
