package jp.co.ricoh.hmp.test;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.EventBusException;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.model.PrinterManager;
import jp.co.ricoh.hmp.test.view.fragment.BaseFragment;
import jp.co.ricoh.hmp.test.view.fragment.FunctionListFragment;

/**
 * サンプル画面
 */

public class MainActivity extends AppCompatActivity{

    /**
     * タグ
     */
    private static final String TAG = MainActivity.class.getSimpleName();

    /**
     * プリンタデバイス管理
     */
    final PrinterManager mPrinterManager = PrinterManager.getInstance();

    /**
     * コンテナ
     */
    @BindView(R.id.container)
    FrameLayout mContainer;

    /**
     * フラグメント
     */
    BaseFragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLifecycle().addObserver(mPrinterManager);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            EventBus.getDefault().register(this);
        } catch (EventBusException e) {
            Logger.w(TAG,"onStart() - warning : failed register. [EventBusException]");
        }
        if (mFragment == null) {
            FunctionListFragment.startFragment(Transition.INIT);
        }
    }

    /**
     * 画面遷移イベント受信
     *
     * @param event 画面遷移イベント
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveTransactionEvent(TransactionEvent event) {
        mFragment = event.getFragment();

        Transition transition = event.getTransition();

        mContainer.removeAllViews();

        getSupportFragmentManager()
                .beginTransaction()
                .setCustomAnimations(transition.getEnter(), transition.getExit(), transition.getPopEnter(), transition.getPopExit())
                .replace(R.id.container, mFragment)
                .commitAllowingStateLoss();

        hideSoftInputFromWindow();
    }

    @Override
    public void onBackPressed() {
        if (mFragment == null || !mFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    /**
     * ソフトウェアキーボード消去
     */
    public void hideSoftInputFromWindow() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View view = getCurrentFocus();
        if (inputMethodManager != null && view != null) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPrinterManager.close();
    }

    /**
     * 遷移種別
     */
    public enum Transition {

        /**
         * 進む
         */
        NEXT(R.anim.slide_in_right, R.anim.slide_out_left, R.anim.slide_in_left, R.anim.slide_out_right),

        /**
         * 戻る
         */
        BACK(R.anim.slide_in_left, R.anim.slide_out_right, R.anim.slide_in_right, R.anim.slide_out_left),

        /**
         * 初期化
         */
        INIT(0, 0, R.anim.slide_in_left, R.anim.slide_out_right);

        /**
         * エンターアニメーション
         */
        final int mEnter;

        /**
         * イグジットアニメーション
         */
        final int mExit;

        /**
         * ポップエンターアニメーション
         */
        final int mPopEnter;

        /**
         * ポップイグジットアニメーション
         */
        final int mPopExit;

        /**
         * コンストラクタ
         *
         * @param enter    エンターアニメーション
         * @param exit     イグジットアニメーション
         * @param popEnter ポップエンターアニメーション
         * @param popExit  ポップイグジットアニメーション
         */
        Transition(int enter, int exit, int popEnter, int popExit) {
            mEnter = enter;
            mExit = exit;
            mPopEnter = popEnter;
            mPopExit = popExit;
        }

        /**
         * エンターアニメーション
         *
         * @return エンターアニメーション
         */
        int getEnter() {
            return mEnter;
        }

        /**
         * イグジットアニメーション
         *
         * @return イグジットアニメーション
         */
        int getExit() {
            return mExit;
        }

        /**
         * ポップエンターアニメーション
         *
         * @return ポップエンターアニメーション
         */
        int getPopEnter() {
            return mPopEnter;
        }

        /**
         * ポップイグジットアニメーション
         *
         * @return ポップイグジットアニメーション
         */
        int getPopExit() {
            return mPopExit;
        }
    }

    /**
     * 画面遷移イベント
     */
    public static class TransactionEvent {

        /**
         * 遷移種別
         */
        final Transition mTransition;

        /**
         * フラグメント
         */
        final BaseFragment mFragment;

        /**
         * イベント送信
         *
         * @param transition 遷移種別
         * @param fragment   フラグメント
         */
        public static void post(Transition transition, BaseFragment fragment) {
            Logger.d(TAG,"post() - debug : transaction " + transition + " " + fragment.getClass().getSimpleName());
            EventBus.getDefault().post(new TransactionEvent(transition, fragment));
        }

        /**
         * コンストラクタ
         *
         * @param transition 遷移種別
         * @param fragment   フラグメント
         */
        TransactionEvent(Transition transition, BaseFragment fragment) {
            mTransition = transition;
            mFragment = fragment;
        }

        /**
         * 遷移種別
         *
         * @return 遷移種別
         */
        Transition getTransition() {
            return mTransition;
        }

        /**
         * フラグメント
         *
         * @return フラグメント
         */
        BaseFragment getFragment() {
            return mFragment;
        }
    }


}
