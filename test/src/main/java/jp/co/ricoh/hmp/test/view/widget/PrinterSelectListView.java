package jp.co.ricoh.hmp.test.view.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import org.greenrobot.eventbus.ThreadMode;
import org.greenrobot.eventbus.Subscribe;
import java.util.ArrayList;
import java.util.HashMap;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.co.ricoh.hmp.sdk.printer.HmpPrinter;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.model.PrinterManager;

/**
 * アイテムリスト
 */
public class PrinterSelectListView extends ListView {

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
    final ArrayList<Item> mItems = new ArrayList<>();

    /**
     * アダプタ
     */
    final Adapter mAdapter;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public PrinterSelectListView(Context context) {
        this(context, null);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs   属性
     */
    public PrinterSelectListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * コンストラクタ
     *
     * @param context      コンテキスト
     * @param attrs        属性
     * @param defStyleAttr デフォルト属性
     */
    public PrinterSelectListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAdapter = new Adapter(context);
        setAdapter(mAdapter);
    }

    /**
     * プリンタデバイス管理イベント受信
     *
     * @param event プリンタデバイス管理イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveEvent(PrinterManager.Event event) {
        switch (event) {
            case DEVICE_FOUND:
                update();
                break;

            default:
                break;
        }
    }

    /**
     * 更新
     */
    public synchronized void update() {
        mItems.clear();

        HashMap<String, HmpPrinter> printerMap = new HashMap<>();

        HmpPrinter connect = mPrinterManager.getPrinter();
        if (connect != null) {
            Logger.i(TAG, "update()- info HMPSDK : APP->SDK: Get name by getName().");
            printerMap.put(connect.getName(), connect);
        }

        for (HmpPrinter printer : mPrinterManager.getDiscoveredPrinters()) {
            Logger.i(TAG, "update()- info HMPSDK : APP->SDK: Get name by getName().");
            printerMap.put(printer.getName(), printer);
        }

        if (!printerMap.isEmpty()) {
            for (HmpPrinter printer : printerMap.values()) {
                mItems.add(new Item(printer));
            }
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
        public Item getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;

            if (view != null) {
                holder = (ViewHolder) view.getTag();

            } else {
                view = mInflater.inflate(R.layout.item_printer_item, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            holder.setItem(getItem(position));
            return view;
        }
    }

    /**
     * 選択アイテムの取得
     *
     * @return アイテムリスト
     */
    public synchronized HmpPrinter getSelectedPrinter(int position) {
        Item item = mItems.get(position);
        return item.mPrinter;
    }

    /**
     * アイテム
     */
    static class Item {

        /**
         * プリンタデバイス
         */
        final HmpPrinter mPrinter;

        /**
         * コンストラクタ
         *
         * @param printer プリンタデバイス
         */
        Item(HmpPrinter printer) {
            mPrinter = printer;
        }

        /**
         * 名称の取得
         *
         * @return 名称
         */
        public synchronized String getName() {
            Logger.i(TAG, "getName()- info HMPSDK : APP->SDK: Get name by getName().");
            return mPrinter != null ? mPrinter.getName() : null;
        }

        /**
         * IDの取得
         *
         * @return ID
         */
        public synchronized String getId() {
            Logger.i(TAG, "getAddress()- info HMPSDK : APP->SDK: Get id by getAddress().");
            return mPrinter != null ? mPrinter.getId() : null;
        }

        /**
         * プリンタデバイスの取得
         *
         * @return プリンタデバイス
         */
        HmpPrinter getPrinter() {
            return mPrinter;
        }
    }

    /**
     * ビューホルダ
     */
    static class ViewHolder {


        /**
         * アイテム
         */
        Item mItem = null;

        /**
         * プリント情報
         */
        @BindView(R.id.printer_info)
        TextView mPrinterInfo;

        /**
         * コンストラクタ
         *
         * @param view ビュー
         */
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void setItem(Item item) {
            mItem = item;
            mPrinterInfo.setText(item.getName() +" - "+ item.getId());
        }
    }
}