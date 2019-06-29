package jp.co.ricoh.hmp.test;

import jp.co.ricoh.hmp.sdk.HmpService;
import jp.co.ricoh.hmp.test.model.Logger;
import jp.co.ricoh.hmp.test.model.PrinterManager;
import jp.co.ricoh.hmp.test.preference.PreferenceManager;

/**
 * TestApplication
 */
public class TestApplication extends android.app.Application {

    /**
     * タグ
     */
    private static final String TAG = TestApplication.class.getSimpleName();
    
    @Override
    public void onCreate() {
        super.onCreate();

        Logger.initialize();
        Logger.i(TAG, "onCreate()- info HMPSDK : APP->SDK: HmpService initialize by initialize().");
        HmpService.initialize(this);
        PrinterManager.initialize(this);
        PreferenceManager.initialize(this);

    }
}
