package com.worlddominationsummit.wdsandroid;

import android.os.Handler;
import android.renderscript.Sampler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.android.volley.*;
import com.android.volley.Response;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RunnableFuture;

/**
 * Created by nicky on 8/6/16.
 */
public class Chat {
    public boolean mCreating = false;
    public boolean mReady = false;
    public ArrayList<HashMap> mParticipants;
    private List<Runnable> mReadies = new ArrayList<>();
    public HashMap<String, Object> mMeta;
    public String last_read;
    public ChildEventListener mListener;
    public ValueEventListener mTypingListener;
    public long mLastRead = 0;
    public String mChatId;
    public String mPid;
    public ArrayList<HashMap> mMsgs = new ArrayList<>();

    public Chat(ArrayList<Attendee> participants) {
        mParticipants = new ArrayList<>();
        for (Attendee p : participants) {
            addParticipant(p);
        }
        createIfNotExists();
    }
    public Chat(String p_id) {
        mPid = p_id;
        createIfNotExists();
    }
    public void init() {

    }
    public void whenReady(Runnable r) {
        if(mReady) {
            r.run();
        } else {
            mReadies.add(r);
        }
    }
    public void createIfNotExists() {
        final ValueEventListener metaListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Puts.i("GOT META");
                if (dataSnapshot.exists()) {
                    Puts.i("META EXISTS");
                    mMeta = (HashMap) dataSnapshot.getValue();
                    for ( HashMap p : ((ArrayList<HashMap>) mMeta.get("participants"))) {
                        mParticipants.add(p);
                    }
                    mReady = true;
                    Puts.i("CHAT READY");
                    if (mReadies.size() > 0) {
                        Puts.i("RUN READIES");
                        for (Runnable r : mReadies) {
                            Puts.i("RUN A READY");
                            r.run();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        ValueEventListener existingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Puts.i("EXISTS!");
                    mChatId = (String) dataSnapshot.getValue();

                } else {
                    Puts.i("NO EXIST, CREATE");
                    create();
                }
                Puts.i("GET META");
                Puts.i(mChatId);
                initLastRead();
                Fire.get("/chats/"+mChatId, metaListener);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        Puts.i("CHECK EXISTS");
        Fire.get("/chat_participants/"+mPid, existingListener);
    }
    public void create() {
        if (!mCreating) {
            mCreating = true;
            mChatId = Fire.createAt("/chats");
            Fire.set("/chat_participants/"+mPid, mChatId);
            set("last_msg", new HashMap<String, Object>());
            set("participants", mParticipants);
            set("pid", mPid);
            for (HashMap p : mParticipants) {
                Fire.set("/chats_by_user/"+p.get("user_id")+"/"+mChatId, "1");
            }
        }
    }
    public void set(String key, Object val) {
        Puts.i("SET "+key+" AT: ");
        Puts.i(mChatId);
        Puts.i("WITH: "+val.toString());
        Fire.set("/chats/"+mChatId+"/"+key, val);
    }
    public void initLastRead() {
        Puts.i("LAST_READ");
        Fire.get("/chats_by_user" + Me.atn.user_id + "/" + mChatId, new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Puts.i("LAST_READ RSP");
                if (dataSnapshot.exists()) {
                    mLastRead = (int) dataSnapshot.getValue();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    public void addParticipant(Attendee atn) {
        HashMap<String, Object> msgAtn = new HashMap<>();
        msgAtn.put("first_name", atn.first_name);
        msgAtn.put("last_name", atn.last_name);
        msgAtn.put("user_id", atn.user_id);
        mParticipants.add(msgAtn);
        ArrayList ids = new ArrayList<String>();
        Collections.sort(mParticipants, (new Comparator<HashMap>() {
            @Override
            public int compare(HashMap a1, HashMap a2) {
                return ((String) a1.get("user_id")).compareTo((String) a2.get("user_id"));
            }
        }));
        for(HashMap p : mParticipants) {
            ids.add(p.get("user_id"));
        }
        mPid = TextUtils.join("_", ids);
    }
    public long now() {
        return Calendar.getInstance().getTimeInMillis();
    }
    public void send(String msg) {
        String from = Me.atn.user_id;
        String msg_id = Fire.createAt("/chat_messages/"+mChatId);
        long now = now();
        HashMap msgObj = new HashMap<>();
        msgObj.put("msg", msg);
        msgObj.put("user_id", from);
        msgObj.put("created_at", now);
        Fire.set("/chat_messages/"+mChatId+"/"+msg_id, msgObj);
        Fire.set("/chats_by_user/"+Me.atn.user_id+"/"+mChatId, now);
        set("last_msg", msgObj);
        String summary = msg;
        if (summary.length() > 100 ) {
            summary = summary.substring(0, 100)+"...";
        }
        List<String> to = new ArrayList<>();
        for(HashMap p : mParticipants) {
            to.add((String) p.get("user_id"));
        }
        JSONObject post = new JSONObject();
        try {
            post.put("chat_id", mPid);
            post.put("user_id", to);
            post.put("summary", summary);
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        Api.post("message", post, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }
    public void watch() {
        if (mChatId != null && mChatId.length() > 0 && mListener == null) {
            long now = Calendar.getInstance().getTimeInMillis();
            ArrayList<HashMap> base = new ArrayList<>();
            HashMap ord = new HashMap();
            ord.put("type", "orderChild");
            ord.put("val", "created_at");
            HashMap small = new HashMap();
            small.put("type", "limitLast");
            small.put("val", "15");
            HashMap all = new HashMap();
            all.put("type", "limitLast");
            all.put("val", "500");
            HashMap start = new HashMap();
            all.put("type", "startAt");
            all.put("val", now);
            ArrayList smallQ = base;
            smallQ.add(small);
            final ArrayList allQ = base;
            allQ.add(small);
            ArrayList startQ = base;
            startQ.add(small);
            Fire.querySingle("/chat_messages" + mChatId, smallQ, new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            addMsg((HashMap) child.getValue());
                        }
                    }
                    drawMessages();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Fire.querySingle("/chat_messages" + mChatId, allQ, new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                                mMsgs.clear();
                                for (DataSnapshot child : dataSnapshot.getChildren()) {
                                    addMsg((HashMap) child.getValue());
                                }
                            }
                            drawMessages();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }, 3000);
            mListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.exists()) {
                        addMsg((HashMap) dataSnapshot.getValue());
                    }
                    drawMessages();
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };
        }
    }
    public void addMsg(HashMap<String, Object> msg) {
        int last = 0;
        String author = "";
        for(HashMap p : mParticipants) {
            if(p.get("user_id").equals(msg.get("user_id"))) {
                if(p.get("first_name") != null) {
                    author += p.get("first_name");
                }
                if(p.get("last_name") != null) {
                    author += " "+((String) p.get("last_name")).substring(0,1);
                }
            }
        }
        msg.put("author", author);
        HashMap<String, Object> last_msg;
        if (mMsgs.size() < 1) {
            mMsgs.add(0, msg);
        } else {
            last_msg = mMsgs.get(last);
            int msgCreated = Integer.valueOf((String) msg.get("created_at"));
            int lastMsgCreated = Integer.valueOf((String) last_msg.get("created_at"));
            int diff = (msgCreated - lastMsgCreated) / 1000;
            if (last_msg.get("user_id").equals(msg.get("user_id")) && diff < 120) {
                String text = (String) last_msg.get("msg");
                text += "\n\n"+msg.get("msg");
                last_msg.put("created_at", msg.get("created_at"));
                last_msg.put("msg", text);
                mMsgs.set(last, last_msg);
            } else {
                mMsgs.add(0, msg);
            }
        }
        if (mLastRead < (int) msg.get("created_at")) {
            mLastRead = now();
            Fire.set("/chats_by_user/"+Me.atn.user_id+"/"+mChatId, mLastRead);
        }
    }
    public void setTyping(boolean isTyping) {
        if (isTyping) {
            long now = (now() / 1000);
            Fire.set("/chats/"+mChatId+"/typing/"+Me.atn.user_id, now);
        } else {
            Fire.remove("/chats/"+mChatId+"/typing/"+Me.atn.user_id);
        }
    }
    public void watchTyping() {
        mTypingListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String writing = "";
                int count = 0;
                if (dataSnapshot.exists() && dataSnapshot.hasChildren()) {
                    for (DataSnapshot child : dataSnapshot.getChildren()) {
                        long now = (now() / 1000);
                        if ((now - Long.valueOf((String) child.getValue()) > 0)) {
                            HashMap writer = getParticipant(child.getKey());
                            if (count == 1) {
                                writing += " & ";
                            }
                            writing += writer.get("first_name");
                            count += 1;
                        }
                    }
                    if (count == 1) {
                        writing += " is typing...";
                    }
                    else if (count == 2) {
                        writing += " are typing...";
                    }
                    else if (count > 2) {
                        writing = String.valueOf(count)+ " people are typing...";
                    }
                }
                MainActivity.self.chatFragment.updateTyping(writing);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        Fire.watch("/chats/"+mChatId+"/typing", mTypingListener);
    }
    public void unwatch() {
        Fire.unwatch(mListener);
        Fire.unwatch(mTypingListener);
        mListener = null;
        mTypingListener = null;
    }
    public HashMap getParticipant(String user_id) {
        for (HashMap a : mParticipants) {
            if (a.get("user_id").equals(user_id)) {
                return a;
            }
        }
        return null;
    }
    public void drawMessages() {
        MainActivity.self.chatFragment.updateMessages(mMsgs);
    }

}
