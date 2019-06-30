package jp.co.ricoh.hmp.test.view.widget;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.Logger;

/**
 * アイテムリスト
 */
public class BluetoothDeviceSelectListView extends ListView {

    /**
     * タグ
     */
    private static final String TAG = BluetoothDeviceSelectListView.class.getSimpleName();


    /**
     * アイテムリスト
     */
    final List<Item> mItems = new ArrayList<>();

    /**
     * アダプタ
     */
    final Adapter mAdapter;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public BluetoothDeviceSelectListView(Context context) {
        this(context, null);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs   属性
     */
    public BluetoothDeviceSelectListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        mItems.clear();
    }

    /**
     * コンストラクタ
     *
     * @param context      コンテキスト
     * @param attrs        属性
     * @param defStyleAttr デフォルト属性
     */
    public BluetoothDeviceSelectListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mAdapter = new Adapter(context);
        setAdapter(mAdapter);
    }

    /**
     * プリンタデバイス管理イベント受信
     *
     * @param event プリンタデバイス管理イベント
     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    public void onReceiveEvent(PrinterManager.Event event) {
//        switch (event) {
//            case DEVICE_FOUND:
//                update();
//                break;
//
//            default:
//                break;
//        }
//    }

    /**
     * 更新
     */
    public synchronized void update(BluetoothDevice mBtDevice) {
        if(mItems.stream().anyMatch(e -> e.getAddress().equals(mBtDevice.getAddress()))){
            return;
        }
        mItems.add(new Item(mBtDevice));
        mAdapter.notifyDataSetChanged();

    }

    public synchronized void update(Set<BluetoothDevice> mBtDevices) {
        if(mBtDevices == null){
            return;
        }
        mItems.clear();
        mBtDevices.forEach(e->mItems.add(new Item(e)));
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
                view = mInflater.inflate(R.layout.item_bluetooth_device_item, parent, false);
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
    public synchronized BluetoothDevice getSelectedDevice(int position) {
        Item item = mItems.get(position);
        return item.getBtDevice();
    }

    /**
     * アイテム
     */
    static class Item {

        /**
         * プリンタデバイス
         */
        final BluetoothDevice mBtDevice;

        /**
         * コンストラクタ
         *
         */
        Item(BluetoothDevice btDevice) {
            mBtDevice = btDevice;
        }

        /**
         * 名称の取得
         *
         * @return 名称
         */
        public synchronized String getName() {
            Logger.i(TAG, "getName()- info HMPSDK : APP->SDK: Get name by getName().");
            return mBtDevice != null ? mBtDevice.getName() : "NoName";
        }

        /**
         * IDの取得
         *
         * @return ID
         */
        public synchronized String getAddress() {
            Logger.i(TAG, "getAddress()- info HMPSDK : APP->SDK: Get id by getAddress().");
            return mBtDevice != null ? mBtDevice.getAddress() : "";
        }

        /**
         * プリンタデバイスの取得
         *
         * @return プリンタデバイス
         */
        BluetoothDevice getBtDevice() {
            return mBtDevice;
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
        @BindView(R.id.bluetooth_device_info)
        TextView mDeviceInfo;

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
            mDeviceInfo.setText(item.getName() +" - "+ item.getAddress ());
        }
    }
}