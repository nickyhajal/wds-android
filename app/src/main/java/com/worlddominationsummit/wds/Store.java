package com.worlddominationsummit.wds;
/**
 * Created by nicky on 5/18/15.
 */

import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Store {
   private static SharedPreferences data;
   private static SharedPreferences.Editor editor;

   public static void init(MainActivity context) {
      Store.data = context.getSharedPreferences("WDSSTORE", 0);
   }

   public static void open() {
      Store.editor = Store.data.edit();
   }

   public static void close() {
      Store.editor.commit();
   }

   public static String get(String key) {
       return Store.data.getString(key, "");
   }
    public static String get(String key, String def) {
        return Store.data.getString(key, def);
    }
   public static List getArray(String key) {
      try {
         JSONArray json = new JSONArray(Store.data.getString(key, ""));
         return JsonHelper.toList(json);
      } catch (JSONException e) {
         Log.e("WDS", "Json Exception", e);
         return new ArrayList();
      }
   }
   public static HashMap getMap(String key) {
      try {
         HashMap map = (HashMap) JsonHelper.toMap(Store.getJsonObject(key));
         return map;
      } catch (JSONException e) {
         Log.e("WDS", "Json Exception", e);
         return new HashMap();
      }
   }
   public static JSONObject getJsonObject(String key) {
      try {
         return new JSONObject(Store.get(key, "{}"));
      } catch (JSONException e) {
         Log.e("WDS", "Json Exception", e);
         return new JSONObject();
      }
   }
    public static JSONArray getJsonArray(String key) {
        try {
            return new JSONArray(Store.get(key, "{}"));
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
            return new JSONArray();
        }
    }
   public static String get(String key, Boolean cache) {
      if(cache) {
         // TODO: Check Cache
      }
      return Store.data.getString(key, "");
   }

   public static void set (String key, String val) {
      Store.open();
      Store.editor.putString(key, val);
      Store.close();
   }

   public static void set (String key, HashMap val) {
      Store.open();
      Store.editor.putString(key, new JSONObject(val).toString());
      Store.close();
   }

   public static void set (String key, JSONObject val) {
      Store.open();
      Store.editor.putString(key, val.toString());
      Store.close();
   }

   public static void set (String key, JSONArray val) {
      Store.open();
      Store.editor.putString(key, val.toString());
      Store.close();
   }
}
