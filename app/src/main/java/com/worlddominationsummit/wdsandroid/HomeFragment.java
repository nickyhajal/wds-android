package com.worlddominationsummit.wdsandroid;

import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nicky on 5/18/15.
 */
public class HomeFragment extends Fragment{
    public View view;
    public ListView dispatchContent;
    public DispatchAdapter adapter;
    public ArrayList<HashMap> items;
    public Dispatch mDispatch;
    public SwipeRefreshLayout mSwipeRefresh;
    public LinearLayout mOffline;
    public RelativeLayout mDispatchControls;
    public RelativeLayout mCommunityControls;
    public Button mCommunityBtn;
    public Event activeEvent;
    public int mTries = 0;
    public int mTypes = 1;

    public void init() {
        mDispatch = new Dispatch();
        mDispatch.context = this;
        JSONObject params = new JSONObject();
        try {
            params.put("channel_type", "global");
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        mDispatch.initParams(params);
        mDispatch.initFilters();
        this.items = new ArrayList<HashMap>();
    }
    public void setChannel(String channel_type, String channel_id) {
        mDispatch.setChannel(channel_type, channel_id);
        if (channel_type.equals("interest")) {
            mDispatchControls.setVisibility(View.GONE);
            mCommunityControls.setVisibility(View.VISIBLE);
            mCommunityBtn.setText(Dispatch.getCommunityFromInterest(channel_id));
        } else if (channel_type.equals("meetup")) {
            activeEvent = MainActivity.self.eventFragment.event;
            mDispatchControls.setVisibility(View.GONE);
            mCommunityControls.setVisibility(View.VISIBLE);
            mCommunityBtn.setText(Dispatch.getEventNameFromEventId(channel_id));
        }
        willDisplay();
    }
    public void leaveChannel() {
        activeEvent = null;
        if (mDispatch.mParams.optString("channel_type").equals("meetup")) {
            MainActivity.self.open_event();
        }
        mDispatch.leaveChannel();
        mDispatchControls.setVisibility(View.VISIBLE);
        mCommunityControls.setVisibility(View.GONE);
        willDisplay();
    }
    public void willDisplay() {
        updateDispatch();
    }
    public void resetDispatch() {
        mDispatch.mSince = "0";
    }
    public void updateDispatch() {
        final HomeFragment ref = this;
        if (mTries == 0) {
            setOffline(false);
        }
        mDispatch.fetch(new com.android.volley.Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject rsp) {
                try {
                    setOffline(false);
                    ref.update_items((ArrayList<HashMap>) JsonHelper.toList(rsp.getJSONArray("feed_contents")));
                    mTries = 0;
                } catch (JSONException e) {
                    Log.e("Dispatch JSON Error", e.toString());
                    updateFailed();
                }
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Dispatch Response Error", error.toString());
                updateFailed();
            }
        });
    }

    public void updateFailed() {
        setOffline(true);
        int seconds = 5000;
        if (mTries > 20) {
            seconds = 10000;
        } else if (mTries > 40) {
            seconds = 15000;
        } else if (mTries > 80) {
            seconds = 20000;
        } else if (mTries > 100) {
            seconds = 25000;
        }
        mTries += 1;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                updateDispatch();
            }
        }, seconds);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (this.view == null) {
            this.view = inflater.inflate(R.layout.dispatch, container, false);
            this.dispatchContent = (ListView) this.view.findViewById(R.id.dispatchContent);
            mDispatchControls = (RelativeLayout) this.view.findViewById(R.id.dispatch_controls);
            mCommunityControls = (RelativeLayout) this.view.findViewById(R.id.community_controls);
            mCommunityBtn = (Button) this.view.findViewById(R.id.community_btn);
            mOffline = (LinearLayout) this.view.findViewById(R.id.offlineMessage);
            Font.applyTo(mOffline);
            mSwipeRefresh = (SwipeRefreshLayout) this.view.findViewById(R.id.swipeRefresh);
            mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    update_dispatch();
                }
            });
            View.OnClickListener dispatchControl = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Button t = (Button) v;
                    String action = t.getTag().toString();
                    if (action.equals("filters")) {
                        MainActivity.self.open_filters();
                    } else if (action.equals("communities")) {
                        MainActivity.self.open_communities();
                    } else if (action.equals("post")) {
                        MainActivity.self.open_post();
                    } else if (action.equals("leave")) {
                        leaveChannel();
                    }
                }
            };
            int count = mDispatchControls.getChildCount();
            for (int i = 0; i <= count; i++) {
                View v = mDispatchControls.getChildAt(i);
                if (v instanceof Button) {
                    ((Button) v).setTypeface(Font.use("Vitesse_Medium"));
                    v.setOnClickListener(dispatchControl);
                }
            }
            count = mCommunityControls.getChildCount();
            for (int i = 0; i <= count; i++) {
                View v = mCommunityControls.getChildAt(i);
                if (v instanceof Button) {
                    ((Button) v).setTypeface(Font.use("Vitesse_Medium"));
                    v.setOnClickListener(dispatchControl);
                }
            }

        }
        return this.view;
    }

    public void load_more(String before, final Response.Listener<JSONObject> successListener, final Response.ErrorListener errorListener) {
        mDispatch.fetch(before, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject rsp) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                add_items((ArrayList<HashMap>) JsonHelper.toList(rsp.getJSONArray("feed_contents")));
                                successListener.onResponse(rsp);
                            } catch (JSONException e) {
                                Log.e("WDS", "Json Exception", e);
                            }
                        }
                    });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                errorListener.onErrorResponse(error);
            }
        });
    }
    public void load_new() {
        if (adapter != null) {
            mDispatch.mSince = adapter.getSince();
        }
        mDispatch.fetch(new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(final JSONObject rsp) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            prepend_items((ArrayList<HashMap>) JsonHelper.toList(rsp.getJSONArray("feed_contents")));
                            mSwipeRefresh.setRefreshing(false);
                        } catch (JSONException e) {
                            Log.e("WDS", "Json Exception", e);
                        }
                    }
                });
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                mSwipeRefresh.setRefreshing(false);
                MainActivity.offlineAlert();
            }
        });
    }
    public void update_dispatch() {
        load_new();
    }
    public void prepend_items(ArrayList<HashMap> items) {
        int len = items.size()-1;
        for (int i = len; i >= 0; i--) {
            HashMap item = items.get(i);
            adapter.insert(item, 0);
        }
        this.adapter.notifyDataSetChanged();
    }
    public void add_items(ArrayList<HashMap> items) {
        this.adapter.addAll(items);
        this.adapter.notifyDataSetChanged();
    }
    public void update_items(ArrayList<HashMap> items) {
        if (items == null) {
            items = new ArrayList<HashMap>();
        }
        this.items = items;
        this.update_items();
    }
    public ArrayList<HashMap> merge_special_items() {
        ArrayList<HashMap> finalItems = this.items;
        if (finalItems.size() > 0) {
            HashMap first = finalItems.get(0);
            String type = (String) first.get("type");
            if (type != null) {
                finalItems.remove(0);
            }
        }
        HashMap<String, Object> state = MainActivity.state;
        if (state != null) {
//            String special = (String) state.get("special");
            String special = (String) state.get("test19_special");
//            Puts.i(special);
            String version = (String) state.get("and_version");
            mTypes = 1;
            if (version != null && version.compareTo(MainActivity.version) > 0) {
                mTypes = 2;
                HashMap<String, Object> item = new HashMap<>();
                item.put("type", "update");
                finalItems.add(0, item);
            }
            if (special != null) {
                if (special.equals("announce")) {
                    HashMap<String, Object> announce = (HashMap<String, Object>) state.get("announce");
                    mTypes = 2;
                    HashMap<String, Object> item = new HashMap<>();
                    item.put("type", "announce");
                    item.put("msg", announce);
                    finalItems.add(0, item);
                }
                else if (special.equals("attendee-stories")) {
//                    Store.set("atnstory", "");
                    String atnstory = Store.get("atnstory");
                    if (atnstory.equals("")) {
                        mTypes = 2;
                        HashMap<String, Object> item = new HashMap<>();
                        item.put("type", "attendee-stories");
                        finalItems.add(0, item);
                    }
                } else if (special.equals("preorder")) {
//                    Store.set("preorder19", "");
                    String preorder = Store.get("preorder19");
//                    Puts.i(preorder);
                    if (preorder.equals("")) {
//                        Puts.i("TYPE PRE OPEN IT");
                        mTypes = 2;
                        HashMap<String, Object> item = new HashMap<>();
                        item.put("type", "preorder");
                        finalItems.add(0, item);
                    } else if (preorder.equals("closed")) {
                        mTypes = 2;
                        HashMap<String, Object> item = new HashMap<>();
                        item.put("type", "closed-preorder");
                        finalItems.add(0, item);
                    } else if (preorder.equals("purchased")) {
                        mTypes = 2;
                        HashMap<String, Object> item = new HashMap<>();
                        item.put("type", "postorder");
                        finalItems.add(0, item);
                    }
                }
            }
        }
        return finalItems;
    }
    public void update_items() {
        ArrayList<HashMap> finalItems = merge_special_items();
        if (this.getActivity() != null) {
            this.adapter = new DispatchAdapter(this.getActivity(), finalItems);
            this.adapter.mTypes = mTypes;
            this.adapter.mContext = this;
            if (this.dispatchContent != null) {
                this.dispatchContent.setAdapter(this.adapter);
            }
        } else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    update_items();
                }
            }, 1000);
        }
    }
    public void setOffline(Boolean isOffline) {
        if(isOffline) {
            if (view != null) {
                mOffline.setVisibility(View.VISIBLE);
                mSwipeRefresh.setVisibility(View.GONE);
            }
        }
        else {
            if (view != null) {
                mOffline.setVisibility(View.GONE);
                mSwipeRefresh.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onDetach() {
        activeEvent = null;
        super.onDetach();
    }
}
