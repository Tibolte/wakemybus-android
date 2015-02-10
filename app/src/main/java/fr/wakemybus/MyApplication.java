package fr.wakemybus;

import android.app.Application;

/**
 * Created by thibaultguegan on 10/02/15.
 */
public class MyApplication extends Application {

    private static MyApplication instance;

    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }

    public static MyApplication getInstance() {
        return instance;
    }
}
