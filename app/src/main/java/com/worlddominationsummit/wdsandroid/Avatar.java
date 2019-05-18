package com.worlddominationsummit.wdsandroid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by nicky on 6/24/15.
 */
public class Avatar {
    public static String url(int user_id, int size) {
        return Avatar.url(String.valueOf(user_id), String.valueOf(size));
    }
    public static String url(int user_id, String size) {
        return Avatar.url(String.valueOf(user_id), size);
    }
    public static String url(String user_id, int size) {
        return Avatar.url(user_id, String.valueOf(size));
    }
    public static String url(String user_id, String size) {
        return "https://avatar.wds.fm/"+user_id+"?width="+size;
    }
    public static Bitmap bitmap(int user_id, int size) {
        return Avatar.bitmap(String.valueOf(user_id), String.valueOf(size));
    }
    public static Bitmap bitmap(int user_id, String size) {
        return Avatar.bitmap(String.valueOf(user_id), size);
    }
    public static Bitmap bitmap(String user_id, int size) {
        return Avatar.bitmap(user_id, String.valueOf(size));
    }
    public static Bitmap bitmap(String user_id, String size) {
        try {
            URL url = new URL(Avatar.url(user_id, size));
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
