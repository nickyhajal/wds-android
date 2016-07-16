package com.worlddominationsummit.wdsandroid;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by nicky on 5/19/15.
 */
public class Puts {
    public static void i(JSONObject msg){
        Puts.i(msg.toString());
    }
    public static void i(HashMap msg){
        Puts.i(msg.toString());
    }
    public static void i(JSONArray msg){
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
        if (msg != null) {
            Log.i("WDS", msg);
        }
    }
    public static void e(String msg, Throwable th){
        Log.e("WDS", msg, th);
    }
}
