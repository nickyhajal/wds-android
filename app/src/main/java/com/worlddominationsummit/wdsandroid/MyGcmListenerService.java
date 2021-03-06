package com.worlddominationsummit.wdsandroid;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

//import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

/**
 * Created by nicky on 6/23/15.
 */
//public class MyGcmListenerService extends GcmListenerService {
//
//    @Override
//    public void onMessageReceived(String from, Bundle data) {
//        String title = data.getString("title");
//        String message = data.getString("message");
//        String user_id = data.getString("user_id");
//        JSONObject content = new JSONObject();
//        try {
//            content = new JSONObject(data.getString("content"));
//            content.put("link", data.getString("link"));
//        } catch (JSONException e) {
//            Log.e("WDS", "Json Exception", e);
//        }
//
//        // Random num between 10000 and 100000 for the ID
//        Random r = new Random();
//        int id = r.nextInt(100000 - 10000) + 10000;
//
//        /**
//         * Production applications would usually process the message here.
//         * Eg: - Syncing with server.
//         *     - Store message in local database.
//         *     - Update UI.
//         */
//
//        /**
//         * In some cases it may be useful to show a notification indicating to the user
//         * that a message was received.
//         */
//        sendNotification(id, title, message, user_id, content.toString());
//    }
//
//    public void sendNotification(int id, String title, String text, String user_id, String content) {
//
//        Intent resultIntent = new Intent(this, MainActivity.class);
//        resultIntent.putExtra("notification", content);
//// Because clicking the notification opens a new ("special") activity, there's
//// no need to create an artificial back stack.
//        PendingIntent resultPendingIntent =
//                PendingIntent.getActivity(
//                        this,
//                        0,
//                        resultIntent,
//                        PendingIntent.FLAG_UPDATE_CURRENT
//                );
//        if (user_id == null) {
//            user_id = "1";
//        }
//        Bitmap bm = Avatar.bitmap(user_id, 192);
//        float density = MainActivity.density;
//        if (bm != null) {
//            bm = Bitmap.createScaledBitmap(bm, (int) (64 * density), (int) (64 * density), false);
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
//            .setContentIntent(resultPendingIntent)
//            .setSmallIcon(R.drawable.notification_icon)
//            .setLargeIcon(bm)
//            .setContentTitle(title)
//            .setContentText(text);
//            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            mNotifyMgr.notify(id, mBuilder.build());
//        } else {
//            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
//            .setContentIntent(resultPendingIntent)
//            .setSmallIcon(R.drawable.notification_icon)
//            .setContentTitle(title)
//            .setContentText(text);
//            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//            mNotifyMgr.notify(id, mBuilder.build());
//
//        }
//    }
//}
