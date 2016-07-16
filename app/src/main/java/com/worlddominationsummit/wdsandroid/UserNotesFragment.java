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
public class UserNotesFragment extends Fragment {
    public View mView;
    public String mUserId;
    public ArrayList<HashMap> mItems;
    public UserNotesAdapter mAdapter;
    public ListView mUserNotesList;
    public Button mStartNote;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void setUser(String user_id) {
        mUserId = user_id;
        if (mView != null) {
            JSONObject params = new JSONObject();
            try {
                params.put("about_id", mUserId);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            Api.get("user/notes", params, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject rsp) {
                    try {
                        loadNotes((ArrayList<HashMap>) JsonHelper.toList(rsp.getJSONArray("notes")));
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
    }

    public void loadNotes (ArrayList<HashMap> notes) {
        mItems = new ArrayList<HashMap>();
        mItems.addAll(notes);
        update_items();
    }

    public void update_items() {
        if (mItems != null) {
            mAdapter = new UserNotesAdapter(this.getActivity(), mItems);
            mAdapter.mContext = this;
            if (mUserNotesList != null) {
                mUserNotesList.setAdapter(mAdapter);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.user_notes, container, false);
            mUserNotesList = (ListView) mView.findViewById(R.id.userNotesList);
            mStartNote = (Button) mView.findViewById(R.id.startNote);
            mStartNote.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.postFragment.mUserId = mUserId;
                    MainActivity.self.open_post();
                }
            });
            Font.applyTo(mView);
        }
        if (mUserId != null) {
            setUser(mUserId);
        }
        return mView;
    }

}
