package com.worlddominationsummit.wdsandroid;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nicky on 8/6/16.
 */
public class Chat {
    public boolean mCreating;
    public boolean mReady;
    public ArrayList<JSONObject> mMsgs;
    public ArrayList<JSONObject> mParticipants;
    public HashMap<String, String> mMeta;
    public String last_read;

    public void isExisting() {

    }

}
