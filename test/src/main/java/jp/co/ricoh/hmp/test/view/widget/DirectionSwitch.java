package jp.co.ricoh.hmp.test.view.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings.Direction;
import jp.co.ricoh.hmp.test.R;

/**
 * 印刷方向選択
 */
public class DirectionSwitch extends ConstraintLayout {

    /**
     * タグ
     */
    private static final String TAG = DirectionSwitch.class.getSimpleName();

    /**
     * 印刷方向
     */
    Direction mDirection = Direction.RIGHT;

    /**
     * 右印刷方向ボタン
     */
    @BindView(R.id.direction_right_button)
    ImageButton mDirectionRightButton;

    /**
     * ジグザク印刷方向ボタン
     */
    @BindView(R.id.direction_zigzag_button)
    ImageButton mDirectionZigzagButton;

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    public DirectionSwitch(Context context) {
        this(context, null);
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     * @param attrs   属性
     */
    public DirectionSwitch(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * コンストラクタ
     *
     * @param context  コンテキスト
     * @param attrs    属性
     * @param defStyle スタイル
     */
    public DirectionSwitch(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        View view = View.inflate(context, R.layout.widget_direction_switch, this);
        ButterKnife.bind(this, view);
    }

    /**
     * 印刷方向取得
     *
     * @return 印刷方向
     */
    public Direction get() {
        return mDirection;
    }

    /**
     * 印刷方向設定
     *
     * @param direction 印刷方向
     */
    public void set(Direction direction) {
        switch (direction) {
            case RIGHT:
                mDirectionRightButton.setBackgroundColor(Color.parseColor("#000000"));
                mDirectionZigzagButton.setBackgroundResource(R.drawable.bg_border);
                break;
            case ZIGZAG:
                mDirectionRightButton.setBackgroundResource(R.drawable.bg_border);
                mDirectionZigzagButton.setBackgroundColor(Color.parseColor("#000000"));
                break;
        }

        if (mDirection != direction) {
            mDirection = direction;
            Event.post(Event.CHANGE);
        }
    }

    @OnClick(R.id.direction_right_button)
    public void onClickDirectionRightButton() {
        set(Direction.RIGHT);
    }

    @OnClick(R.id.direction_zigzag_button)
    public void onClickDirectionZigzagButton() {
        set(Direction.ZIGZAG);
    }

    /**
     * イベント
     */
    public enum Event {

        /**
         * 変化
         */
        CHANGE;

        /**
         * 送信
         *
         * @param event イベント
         */
        static void post(Event event) {
            EventBus.getDefault().post(event);
        }
    }
}