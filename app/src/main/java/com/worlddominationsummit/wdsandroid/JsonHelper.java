package com.worlddominationsummit.wdsandroid;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class JsonHelper {
    public static Object toJSON(Object object) throws JSONException {
        if (object instanceof Map) {
            JSONObject json = new JSONObject();
            Map map = (Map) object;
            for (Object key : map.keySet()) {
                json.put(key.toString(), toJSON(map.get(key)));
            }
            return json;
        } else if (object instanceof Iterable) {
            JSONArray json = new JSONArray();
            for (Object value : ((Iterable)object)) {
                json.put(value);
            }
            return json;
        } else {
            return object;
        }
    }

    public static boolean isEmptyObject(JSONObject object) {
        return object.names() == null;
    }

    public static Map<String, Object> getMap(JSONObject object, String key) throws JSONException {
        return toMap(object.getJSONObject(key));
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap();
        Iterator keys = object.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, fromJson(object.get(key)));
        }
        return map;
    }

    public static JSONArray deleteVal(JSONArray json, String val) {
        JSONArray out = new JSONArray();
        int len = json.length();
        for(int i = 0; i < len; i++) {
            try {
                if(!String.valueOf(json.get(i)).equals(val)) {
                    out.put(json.get(i));
                }
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
        }
        return out;
    }

    public static List toList(JSONArray array) throws JSONException {
        List list = new ArrayList();
        for (int i = 0; i < array.length(); i++) {
            list.add(fromJson(array.get(i)));
        }
        return list;
    }

    private static Object fromJson(Object json) throws JSONException {
        if (json == JSONObject.NULL) {
            return null;
        } else if (json instanceof JSONObject) {
            return toMap((JSONObject) json);
        } else if (json instanceof JSONArray) {
            return toList((JSONArray) json);
        } else {
            return json;
        }
    }
    public static String UrlEncode(JSONObject json) {
        String output = "";
        List<String> keys = new ArrayList<String>();
        try {
            keys = JsonHelper.toList(json.names());
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        for (String currKey : keys) {
            try {
                output += UrlEncodePart(json.get(currKey), currKey);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
        }

        return output.substring(0, output.length()-1);
    }

    private static String UrlEncodePart(Object json, String prefix) {
        String output = "";
        if (json instanceof JSONObject) {
            JSONObject obj = (JSONObject)json;
            List<String> keys = new ArrayList<String>();
            try {
                keys = JsonHelper.toList(obj.names());
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            for (String currKey : keys) {
                String subPrefix = prefix + "[" + currKey + "]";
                try {
                    output += UrlEncodePart(obj.get(currKey), subPrefix);
                } catch (JSONException e) {
                    Log.e("WDS", "Json Exception", e);
                }
            }
        } else if (json instanceof JSONArray) {
            JSONArray jsonArr = (JSONArray) json;
            int arrLen = jsonArr.length();

            for (int i = 0; i < arrLen; i++) {
                String subPrefix = prefix + "[" + i + "]";
                Object child = new Object();
                try {
                    child = jsonArr.get(i);
                } catch (JSONException e) {
                    Log.e("WDS", "Json Exception", e);
                }
                output += UrlEncodePart(child, subPrefix);
            }
        } else {
            output = prefix + "=" + json.toString() + "&";
        }

        return output;
    }
}