package jp.co.ricoh.hmp.test.model;

import android.util.Log;

import jp.co.ricoh.hmp.test.BuildConfig;

/**
 * プリンタデバイス管理
 */
public class Logger{
    public static int LOG_LEVEL = 0;
    public static int ERROR = 1;
    public static int WARN = 2;
    public static int INFO = 3;
    public static int DEBUG = 4;
    public static int VERBOS = 5;

    public static void initialize() {
        if (BuildConfig.LOG) {
            LOG_LEVEL = 6;
        } else {
            LOG_LEVEL = 4;
        }

        Log.d("dddd", LOG_LEVEL + "");
    }

    public static void e(String tag,String message){
        if (LOG_LEVEL > ERROR) {
            Log.e(tag, message);
        }
    }

    public static void w(String tag,String message){
        if (LOG_LEVEL > WARN) {
            Log.w(tag, message);
        }
    }

    public static void i(String tag,String message){
        if (LOG_LEVEL > INFO) {
            Log.i(tag, message);
        }
    }

    public static void d(String tag,String message){
        if (LOG_LEVEL > DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void v(String tag,String message){
        if (LOG_LEVEL > VERBOS) {
            Log.v(tag,message);
        }
    }
}
