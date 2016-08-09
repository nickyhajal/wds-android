package com.worlddominationsummit.wdsandroid;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by nicky on 08/07/16.
 */
public class ChatFragment extends Fragment {
    public View mView;
    public ChatAdapter mAdapter;
    public ArrayList<HashMap> mMsgs;
    public Chat mChat;
    public String mState;
    public Boolean mReadyToReceive;
    public Attendee mAtn;

    // View
    public TextView mMessage;
    public TextView mSubMessage;
    public ListView mListview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView == null) {
            mView = inflater.inflate(R.layout.chat, container, false);
            mListview = (ListView) mView.findViewById(R.id.chatMsgList);
            mMessage = (TextView) mView.findViewById(R.id.msg);
            mSubMessage = (TextView) mView.findViewById(R.id.submsg);
            Font.applyTo(mView);
            setState("loading");
        }
        return mView;
    }



    public void setAttendees(ArrayList<Attendee> atns) {
        setState("loading");

    }
    public void setAttendee(Attendee atn) {
        Puts.i("SET ATN");
        setState("loading");
        if (mChat == null) {
            mAtn = atn;
            mAtn.readyForMessages(new Runnable() {
                @Override
                public void run() {
                    if (mAtn.receivesMsgs) {
                        mReadyToReceive = true;
                    } else {
                        mReadyToReceive = false;
                    }
                    syncState();
                }
            });
            ArrayList<Attendee> atns = new ArrayList<>();
            atns.add(atn);
            atns.add(Me.atn);
            mChat = new Chat(atns);
            startChat();
        }

    }
    public void setPid(String pid) {
        setState("loading");
        if (mChat == null) {
            clearMsgs();
            mChat = new Chat(pid);
            startChat();
        }
    }

    public void startChat() {
        Puts.i("START CHAT");
        mChat.whenReady(new Runnable() {
            @Override
            public void run() {
                Puts.i("START WATCH");
                setState("null");
                mChat.watch();
                mChat.watchTyping();
            }
        });

    }

    public void updateMessages(ArrayList<HashMap> msgs) {
        Puts.i("UPD MESG");
        setState("loaded");
        mMsgs = msgs;
        updateItems();
    }

    public void updateItems() {
        mAdapter = new ChatAdapter(this.getActivity(), mMsgs);
        if (mListview != null) {
            mListview.setAdapter(mAdapter);
        }
    }

    public void updateTyping(String typing) {
        Puts.i("typing");
    }

    public void clearMsgs() {
        if (mMsgs != null) {
            mMsgs.clear();
        } else {
            mMsgs = new ArrayList<>();
        }
    }

    public void setState(String state) {
        Puts.i("SET STATE");
        mState = state;
        syncState();
    }
    public void syncState(){
        if (mMessage != null) {
            switch (mState) {
                case "loading":
                    Puts.i("LOADING");
                    mMessage.setText("Loading...");
                    mMessage.setVisibility(View.VISIBLE);
                    mSubMessage.setVisibility(View.GONE);
                    mListview.setVisibility(View.GONE);
                    break;
                case "null":
                    Puts.i("NULL");
                    mMessage.setText("Send a Message to " + mAtn.first_name + " below.");
                    if (mReadyToReceive) {
                        mSubMessage.setText(mAtn.first_name + " has the latest WDS App installed and will receive your messages immediately.");
                    } else {
                        mSubMessage.setText(mAtn.first_name + " hasn't installed the latest WDS App but will receive your messages once they do.");
                    }
                    mSubMessage.setVisibility(View.VISIBLE);
                    mListview.setVisibility(View.GONE);
                    break;
                case "loaded":
                    Puts.i("LOADED");
                    mMessage.setVisibility(View.GONE);
                    mListview.setVisibility(View.VISIBLE);
                    mSubMessage.setVisibility(View.GONE);
                    break;
            }
        }
    }
}
