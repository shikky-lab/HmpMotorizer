package jp.co.ricoh.hmp.test.preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import dalvik.system.DexClassLoader;
import jp.co.ricoh.hmp.sdk.printer.HmpSettings.Position;
import jp.co.ricoh.hmp.test.model.Logger;

import static android.content.Context.MODE_PRIVATE;

public class PreferenceManager {

    /**
     * タグ
     */
    private static final String TAG = PreferenceManager.class.getSimpleName();

    /**
     * インスタンス
     */
    private static PreferenceManager sInstance = null;

    /**
     * コンテキストリファレンス
     */
    private final Context mContext;

    /**
     * プリファレンス
     */
    private final SharedPreferences mSharedPreferences;

    /**
     * パッケージ管理
     */
    private final PackageManager mPackageManager;

    /**
     * アセット情報
     */
    private final AssetManager mAssetManager;

    /**
     * プリファレンス名
     */
    private static final String SHARED_PREFERENCES_NAME = "preference";

    /**
     * 初期化
     *
     * @param context コンテキスト
     */
    public static synchronized void initialize(Context context) {
        if (sInstance == null) {
            sInstance = new PreferenceManager(context);
        }
    }

    /**
     * 終了
     */
    public static synchronized void terminate() {
        if (sInstance != null) {
            sInstance.onTerminate();
            sInstance = null;
        }
    }

    /**
     * インスタンス取得
     *
     * @return インスタンス
     */
    public static synchronized PreferenceManager getInstance() {
        return sInstance;
    }

    /**
     * コンストラクタ
     *
     * @param context コンテキスト
     */
    private PreferenceManager(Context context) {
        mContext = context.getApplicationContext();
        mSharedPreferences = context.getSharedPreferences(SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        mPackageManager = context.getPackageManager();
        mAssetManager = context.getAssets();
    }

    /**
     * デストラクタ
     */
    private void onTerminate() {
    }

    /**
     * パッケージ情報を取得する
     *
     * @return パッケージ情報
     */
    public PackageInfo getPackageInfo() {
        try {
            return mPackageManager.getPackageInfo(mContext.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            Logger.e(TAG, "getPackageInfo() - error : NameNotFoundException");
            return null;
        }
    }

    /**
     * 初回起動情報取得
     *
     * @return 初回起動情報
     */
    public boolean isFirstLaunch() {
        boolean result = mSharedPreferences.getBoolean(Key.FIRST_LAUNCH, true);
        if (result) {
            if (!mSharedPreferences.edit().putBoolean(Key.FIRST_LAUNCH, false).commit()) {
                Logger.e(TAG,"isFirstLaunch() - error : failed commit shared preferences.");
            }
        }
        return result;
    }

    public void setAgreementDenied(boolean denied) {
        mSharedPreferences.edit().putBoolean(Key.AGREEMENT_DENIED, denied).commit();
    }

    public boolean isDenied() {
        boolean result = mSharedPreferences.getBoolean(Key.AGREEMENT_DENIED, true);
        return result;
    }

    /**
     * ガイダンス表示設定
     *
     * @param guidance ガイダンス表示するか
     */
    public void setShowGuidance(boolean guidance) {
        mSharedPreferences.edit().putBoolean(Key.GUIDANCE_SHOW, guidance).commit();
    }

    /**
     * ガイダンス表示取得
     *
     * @return ガイダンス表示するか（true：表示 false：非表示）
     */
    public boolean getShowGuidance() {
        boolean result = mSharedPreferences.getBoolean(Key.GUIDANCE_SHOW, true);
        return result;
    }

    /**
     * テキスト読み出し
     *
     * @param fileName ファイル名
     * @return テキスト
     */
    private String getText(String fileName) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(mAssetManager.open(fileName)))) {
            StringBuilder builder = new StringBuilder();
            for (String line = reader.readLine(); line != null; line = reader.readLine()) {
                builder.append(line + "\n");
            }
            return builder.toString();
        } catch (IOException e) {
            Logger.e(TAG, "getText(String) - error : IOException");
            return null;
        }
    }

    /**
     * 印刷開始位置取得
     *
     * @return 印刷開始位置
     */
    public Position getPosition() {
        if (mSharedPreferences.getInt(Key.PRINT_START_POSITION, 0) == 0) {
            return Position.RIGHT;
        } else {
            return Position.CENTER;
        }
    }

    /**
     * 印刷開始位置セット
     *
     * @param position 印刷開始位置
     */
    public void setPosition(Position position) {
        if (!mSharedPreferences.edit().putInt(Key.PRINT_START_POSITION, position.ordinal()).commit()) {
            Logger.e(TAG,"setPosition() - error : failed commit shared preferences.");
        }
    }

    /**
     * 最後に切断したデバイスID
     *
     * @return 最後に切断したデバイスID
     */
    public String getLatestDeviceId() {
        return mSharedPreferences.getString(Key.LATEST_DEVICE, "");
    }

    /**
     * 最後に切断したデバイスID
     *
     * @param id 最後に切断したデバイスID
     */
    public void setLatestDeviceId(String id) {
        if (!mSharedPreferences.edit().putString(Key.LATEST_DEVICE, id).commit()) {
            Logger.e(TAG, "setLatestDeviceId() - error : failed commit shared preferences.");
        }
    }

    /**
     * クラスローダーの取得
     *
     * @param fileName ファイル名
     * @return クラスローダー
     */
    public ClassLoader getClassLoader(String fileName) {
        try (BufferedInputStream input = new BufferedInputStream(mAssetManager.open(fileName));
             BufferedOutputStream output = new BufferedOutputStream(mContext.openFileOutput(fileName, Context.MODE_PRIVATE))) {
            for (int buffer = input.read(); buffer != -1; buffer = input.read()) {
                output.write(buffer);
            }

            File file = new File(mContext.getFilesDir().getAbsolutePath() + File.separator + fileName);
            if (!file.exists()) {
                Logger.e(TAG, "getClassLoader(String) - error : file not found.");
                return null;
            }

            return new DexClassLoader(file.getAbsolutePath(), mContext.getFilesDir().getAbsolutePath(), null, mContext.getClassLoader());
        } catch (IOException e) {
            Logger.e(TAG, "getClassLoader(String) - error : IOException");
            return null;
        }
    }

    /**
     * キー
     */
    static class Key {

        /**
         * 初回起動
         */
        static final String FIRST_LAUNCH = "first_launch";

        /**
         * 印刷開始位置
         */
        static final String PRINT_START_POSITION = "print_start_position";

        /**
         * 最後のデバイス
         */
        static final String LATEST_DEVICE = "latest_device";

        static final String AGREEMENT_DENIED = "agreement_denied";

        /**
         *ガイダンス表示
         */
        static final String GUIDANCE_SHOW = "guidance_show";
    }
}
