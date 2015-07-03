package com.worlddominationsummit.wdsandroid;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by nicky on 5/18/15.
 */
public class DispatchContentFragment extends Fragment {
    public View mView;
    public HashMap mItem;
    public ArrayList<HashMap> mItems;
    public DispatchContentAdapter mAdapter;
    public ListView mDispatchContentList;
    public Button mStartComment;
    public int mNumComments = 0;
    public Boolean scrollToBottom = false;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        update_items();
    }

    public void setItem(HashMap item) {
        mItem = item;
        mNumComments = 0;
        mItem.put("content_type", "item");
        mItems = new ArrayList<HashMap>();
        mItems.add(mItem);
        JSONObject params = new JSONObject();
        try {
            params.put("feed_id", mItem.get("feed_id"));
            params.put("include_author", "1");
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        Api.get("feed/comments", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject rsp) {
                try {
                    loadComments((ArrayList<HashMap>) JsonHelper.toList(rsp.getJSONArray("comments")));
                    if (scrollToBottom) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mDispatchContentList.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // This method works but animates the scrolling
                                        // which looks weird on first load
                                        // scroll_view.fullScroll(View.FOCUS_DOWN);

                                        // This method works even better because there are no animations.
                                        scrollToBottom = false;
                                        mDispatchContentList.setSelection(mDispatchContentList.getCount() - 1);
                                    }
                                });
                            }
                        }, 75);
                    }
                } catch (JSONException e) {
                    Log.e("WDS", "Json Exception", e);
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                MainActivity.offlineAlert();
            }
        });
    }

    public void loadComments(ArrayList<HashMap> comments) {
        HashMap<String, String> content = mItems.get(0);
        mItems = new ArrayList<HashMap>();
        mItems.add(content);
        mItems.addAll(comments);
        update_items();
    }

    public void update_items() {
        mAdapter = new DispatchContentAdapter(this.getActivity(), mItems);
        mAdapter.mContext = this;
        if (mDispatchContentList != null) {
            mDispatchContentList.setAdapter(mAdapter);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mItem != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.dispatch_content, container, false);
            mDispatchContentList = (ListView) mView.findViewById(R.id.dispatchContentList);
            mStartComment = (Button) mView.findViewById(R.id.startComment);
            mStartComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.postFragment.mFeedItem = mItem;
                    MainActivity.self.open_post();
                }
            });
            Font.applyTo(mView);
        }
        if (mItem != null) {
            setItem(mItem);
        }
        return mView;
    }

}
