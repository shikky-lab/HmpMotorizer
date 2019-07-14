package jp.co.ricoh.hmp.test.view.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.co.ricoh.hmp.test.MainActivity;
import jp.co.ricoh.hmp.test.MainActivity.Transition;
import jp.co.ricoh.hmp.test.R;
import jp.co.ricoh.hmp.test.model.BtDeviceManager;

/**
 *
 * bluetooth接続の確認用
 *
 */

public class CheckBluetoothDeviceFragment extends BaseFragment {

    @SuppressWarnings("unused")
    private static final String TAG = CheckBluetoothDeviceFragment.class.getSimpleName();

    final BtDeviceManager mBtDeviceManager = BtDeviceManager.getInstance();
    private List<String> mItems = new ArrayList<>();
    CheckBluetoothDeviceFragment.Adapter mAdapter;


    @BindView(R.id.head_title)
    TextView tvTitle;

    @BindView(R.id.text_from_device)
    TextView mReadText;

    @BindView(R.id.to_write_text)
    EditText mWriteText;

    @BindView(R.id.send_button)
    Button mSendButton;

    @BindView(R.id.read_button)
    Button mReadButton;

    @BindView(R.id.notified_text)
    TextView mNotifiedText;

    @BindView(R.id.read_write_list_view)
    ListView mListView;

    public static void startFragment(Transition transition) {
        MainActivity.TransactionEvent.post(transition, new CheckBluetoothDeviceFragment());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_check, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        tvTitle.setText(getResources().getString(R.string.bluetooth_check_title));
        mAdapter = new CheckBluetoothDeviceFragment.Adapter(Objects.requireNonNull(this.getActivity()));
        mListView.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

//    @OnTextChanged(value = R.id.to_write_text, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
//    void afterTextChanged(Editable s) {
//    }

    @OnClick(R.id.send_button)
    public void onClickSendButton() {
        String sendText = mWriteText.getText().toString();
        mBtDeviceManager.write(sendText);
    }

    @OnClick(R.id.send_button)
    public void onClickReadButton() {
    }

    @OnClick(R.id.iv_back)
    public void OnClickDoneButton() {
        FunctionListFragment.startFragment(Transition.BACK);
    }

    @Override
    public boolean onBackPressed() {
        FunctionListFragment.startFragment(Transition.BACK);
        return true;
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onHaveRead(BtDeviceManager.Event event){
        switch (event){
            case HAVE_READ_BLOCK:
                String readData = mBtDeviceManager.readBlock();
                mItems.add(0,readData);
                mAdapter.notifyDataSetChanged();
                break;
            case HAVE_READ_CHARACTER:
                break;
            case CONNECTED:
            case DISCONNECTED:
                break;
            default:
                break;
        }
    }

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
        public String getItem(int position) {
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
                view = mInflater.inflate(R.layout.item_bluetooth_read_write, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }

            holder.setItem(getItem(position));

            return view;
        }
    }
    static class ViewHolder {

        @BindView(R.id.read_write_item)
        TextView mText;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }

        void setItem(String str){
            mText.setText(str);
        }

    }
}
