package com.worlddominationsummit.wdsandroid;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by nicky on 5/28/15.
 */
public class PostFragment extends Fragment {
    public View mView;
    public ImageView mAvatar;
    public Button mPostBtn;
    public TextView mPostField;
    public ImageLoader mLoader;
    public String mUserId;
    public HashMap<String, String> mFeedItem;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(mPostField.requestFocus()) {
            openKeypad(mPostField);
        }
        if (mUserId != null) {
            mPostBtn.setText("Save Note");
            mPostField.setHint("Write your note here...");
        }
        else if (mFeedItem != null) {
            mPostBtn.setText("Post Comment");
            mPostField.setHint("Start typing here to post a comment...");
        }
        else {
            mPostBtn.setText("Post");
            mPostField.setHint("Start typing here to share a post...");
        }
    }

    @Override
    public void onDestroyView() {
        mFeedItem = null;
        mUserId = null;
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView == null) {
            mLoader = new ImageLoader(getActivity());
            mView = inflater.inflate(R.layout.post, container, false);
            mAvatar = (ImageView) mView.findViewById(R.id.avatar);
            mPostBtn = (Button) mView.findViewById(R.id.postBtn);
            mPostField = (TextView) mView.findViewById(R.id.postField);
            RelativeLayout controls = (RelativeLayout) mView.findViewById(R.id.controls);
            mLoader.DisplayImage(Me.atn.pic, mAvatar);
            Font.applyTo(mView);
            Font.applyTo(controls);
            mPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = mPostField.getText().toString();
                    if (mUserId != null) {
                        mPostBtn.setText("Saving...");
                        JSONObject params = new JSONObject();
                        try {
                            params.put("about_id", mUserId);
                            params.put("note", text);
                        } catch (JSONException e) {
                            Log.e("WDS", "Json Exception", e);
                        }
                        Api.post("user/note", params, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                closeKeyPad(mPostField);
                                mPostBtn.setText("Saved!");
                                mPostField.setText("");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                                        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                                    }
                                }, 100);
                                //MainActivity.self.open_user_notes(mUserId);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                MainActivity.offlineAlert();
                            }
                        });
                    } else if (mFeedItem != null) {
                        mPostBtn.setText("Posting...");
                        JSONObject params = new JSONObject();
                        try {
                            params.put("feed_id", mFeedItem.get("feed_id"));
                            params.put("comment", text);
                        } catch (JSONException e) {
                            Log.e("WDS", "Json Exception", e);
                        }
                        Api.post("feed/comment", params, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                MainActivity.self.dispatchContentFragment.scrollToBottom = true;
                                closeKeyPad(mPostField);
                                mPostBtn.setText("Posted!");
                                mPostField.setText("");
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                                        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                                    }
                                }, 100);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                MainActivity.offlineAlert();
                            }
                        });
                    }
                    else {
                        mPostBtn.setText("Posting...");
                        MainActivity.self.homeFragment.mDispatch.post(text, new Response.Listener() {
                            @Override
                            public void onResponse(Object o) {
                                mPostBtn.setText("Posted!");
                                mPostField.setText("");
                                MainActivity.self.homeFragment.load_new();
                                closeKeyPad(mPostField);
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
                                        getActivity().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
                                    }
                                }, 100);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                MainActivity.offlineAlert();
                            }
                        });
                    }
                }
            });
        }
        return mView;
    }

    public void closeKeyPad(final View v) {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }
    public void openKeypad(final View v)
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                InputMethodManager inputManager =   (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                inputManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
            }
        },100);
    }
}
