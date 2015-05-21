package com.worlddominationsummit.wds;

import android.util.Log;

/**
 * Created by nicky on 5/19/15.
 */
public class Puts {
    public static void i(String msg){
        Log.i("WDS", msg);
    }
    public static void e(String msg, Throwable th){
        Log.e("WDS", msg, th);
    }
}
