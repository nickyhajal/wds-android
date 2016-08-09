package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nicky on 08/07/16.
 */
public class ChatsFragment extends Fragment {
    public View mView;
    public ChatRowAdapter mAdapter;
    public ArrayList<HashMap> mChats;
    public String mState = "loading";
    public HashMap<String, Long> mLastReads;
    public ArrayList<String> mWatchingKeys;
    public ArrayList<Object> mListeners;

    // Views
    public ListView mListview;
    public TextView mMessage;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mChats = new ArrayList<>();
        mLastReads = new HashMap<>();
        mListeners = new ArrayList<>();
        HashMap ord = new HashMap();
        ord.put("type", "orderKey");
        ArrayList query = new ArrayList<>();
        query.add(ord);
        Fire.querySingle("/chats_by_user/" + Me.atn.user_id, query, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.getChildrenCount() < 1) {
                    setState("null");
                }
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    setState("loaded");
                    for (DataSnapshot c : dataSnapshot.getChildren()) {
                        watchChat(c.getKey(), Long.parseLong(String.valueOf(c.getValue())));
                    }
                } else {
                    setState("null");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView == null) {
            mView = inflater.inflate(R.layout.chats, container, false);
            mListview = (ListView) mView.findViewById(R.id.chatsList);
            mMessage = (TextView) mView.findViewById(R.id.msg);
            mAdapter = new ChatRowAdapter(getActivity(), mChats);
            mListview.setAdapter(mAdapter);
            Font.applyTo(mView);
        }
        return mView;
    }

    public void watchChat(final String chatId, long last_read) {
        mLastReads.put(chatId, last_read);
        ValueEventListener readListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    mLastReads.put(chatId, Long.parseLong(String.valueOf(dataSnapshot.getValue())));
                    updateChatList();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ValueEventListener chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    updateChat(dataSnapshot.getKey(), (HashMap<String, Object>) dataSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        Fire.watch("/chats_by_user/"+Me.atn.user_id+"/"+chatId, readListener);
        Fire.watch("/chats/"+chatId, chatListener);
        mListeners.add(readListener);
        mListeners.add(chatListener);
    }

    public void setState(String state) {
        mState = state;
        switch (mState) {
            case "loading":
                mMessage.setText("Loading...");
                mMessage.setVisibility(View.VISIBLE);
                mListview.setVisibility(View.GONE);
                break;
            case "null":
                mMessage.setText("Your messages will appear here.");
                mMessage.setVisibility(View.VISIBLE);
                mListview.setVisibility(View.GONE);
                break;
            case "loaded":
                mMessage.setVisibility(View.GONE);
                mListview.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void processReads() {
        if (mChats.size() > 0) {
            int i;
            for (i = 0; i < mChats.size(); i++) {
                HashMap<String, Object> chat = mChats.get(i);
                long lastStamp = (long) chat.get("lastStamp");
                long lastRead = 0;
                if (mLastReads.containsKey(chat.get("id"))) {
                    lastRead = mLastReads.get(chat.get("id")) + 500;
                }
                boolean read = lastRead >= lastStamp;
                chat.put("read", read);
                mChats.set(i, chat);
            }
        }
    }
    public void updateChatList() {
        if (mChats.size() > 1) {
            Collections.sort(mChats, new Comparator<HashMap>() {
                @Override
                public int compare(HashMap h1, HashMap h2) {
                    Long v1 = (long) h1.get("lastStamp");
                    Long v2 = (long) h2.get("lastStamp");
                    return v2.compareTo(v1);
                }
            });
        }
        processReads();
        updateItems();
    }

    public void updateItems() {
        mAdapter = new ChatRowAdapter(this.getActivity(), mChats);
        if (mListview != null) {
            mListview.setAdapter(mAdapter);
        }
    }

    public void updateChat(String key, HashMap<String, Object> chat) {
        Puts.i("UPDATE CHAT");
        Puts.i(chat.toString());
        List<String> with = new ArrayList<String>();
        String withStr = "";
        String av_id = "";
        long lastStamp = 0;
        String lastMsgContent = "";
        if (chat != null && chat.get("participants") != null && chat.get("last_msg") != null) {
            HashMap<String, Object> lastMsg = (HashMap<String, Object>) chat.get("last_msg");

            String pre = "";
            for (HashMap<String, Object> p : (ArrayList<HashMap>) chat.get("participants")) {
                if (p.get("user_id") == lastMsg.get("user_id")) {
                    pre = p.get("first_name") + ": ";
                }
                if (!String.valueOf(p.get("user_id")).equals(Me.atn.user_id)) {
                    with.add(p.get("first_name") + " " + p.get("last_name"));
                    if (av_id.length() < 1) {
                        av_id = String.valueOf(p.get("user_id"));
                    }
                }
            }
            withStr = TextUtils.join(", ", with);
            if (withStr.length() > 65) {
                withStr = withStr.substring(0, 65) + "...";
            }
            if (String.valueOf(lastMsg.get("user_id")).equals(Me.atn.user_id)) {
                pre = "You: ";
            } else {
                av_id = String.valueOf(lastMsg.get("user_id"));
            }
            lastMsgContent = pre + lastMsg.get("msg");
            if (lastMsgContent.length() > 95) {
                lastMsgContent = lastMsgContent.substring(0, 95) + "...";
            }
            lastStamp = (long) lastMsg.get("created_at");
            Puts.i(lastMsgContent);
            HashMap<String, Object> c = new HashMap<String, Object>();
            c.put("id", key);
            c.put("pid", chat.get("pid"));
            c.put("lastMsg", lastMsgContent);
            Puts.i(">> update chat crate ob");
            Puts.i(lastStamp);
            c.put("lastStamp", lastStamp);
            c.put("with", withStr);
            c.put("av_id", av_id);
            Puts.i(c.toString());;
            if (mChats.size() > 0) {
                int found = -1;
                int i;
                for (i = 0; i < mChats.size(); i++) {
                    if (mChats.get(i).get("inx") == key) {
                        found = i;
                    }
                }
                if (found > -1) {
                    mChats.set(i, c);
                } else {
                    mChats.add(c);
                }
            } else {
                mChats.add(c);
            }
            updateChatList();
        }
    }
}
