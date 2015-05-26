package com.worlddominationsummit.wds;

import android.util.Log;

import org.json.JSONObject;

/**
 * Created by nicky on 5/19/15.
 */
public class Puts {
    public static void i(JSONObject msg){
        Puts.i(msg.toString());
    }
    public static void i(float msg){
        Puts.i(String.valueOf(msg));
    }
    public static void i(int msg){
        Puts.i(String.valueOf(msg));
    }
    public static void i(boolean msg){
        Puts.i(String.valueOf(msg));
    }
    public static void i(String msg){
        Log.i("WDS", msg);
    }
    public static void e(String msg, Throwable th){
        Log.e("WDS", msg, th);
    }
}
