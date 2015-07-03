package com.worlddominationsummit.wdsandroid;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import com.android.volley.*;
import com.android.volley.Response;
import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by nicky on 6/23/15.
 */
public class MyGcmRegistrationService extends IntentService {
    private static final String TAG = "MyRegistrationService";
    private static final String GCM_SENDER_ID = "388613775470";
    private static final String[] TOPICS = {"global"};

    public MyGcmRegistrationService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID.getToken(GCM_SENDER_ID,
                        GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                sendTokenToServer(token);
                subscribeTopics(token);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendTokenToServer(String token) {
        JSONObject params = new JSONObject ();
        try {
            params.put("token", token);
            params.put("type", "android");
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        Api.post("device", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Store.set("saved_device_token", "1");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("WDS Device Fail", volleyError.toString());

            }
        });
    }

    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}
