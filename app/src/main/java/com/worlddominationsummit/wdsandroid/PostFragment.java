package com.worlddominationsummit.wdsandroid;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialcamera.MaterialCamera;
import com.android.volley.*;
import com.android.volley.Response;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

/**
 * Created by nicky on 5/28/15.
 */
public class PostFragment extends Fragment {
    public View mView;
    public ImageView mAvatar;
    public ImageView mPreview;
    public Button mPostBtn;
    public Button mRemoveMedia;
    public TextView mPostField;
    public ImageButton mCamera;
    public ImageLoader mLoader;
    public String mUserId;
    public Context mContext;
    public HashMap<String, String> mFeedItem;
    public String mMediaId;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mUserId != null) {
            mPostBtn.setText("Save Note");
            mPostField.setHint("Write your note here...");
            mCamera.setVisibility(View.GONE);
        }
        else if (mFeedItem != null) {
            mPostBtn.setText("Post Comment");
            mPostField.setHint("Start typing here to post a comment...");
            mCamera.setVisibility(View.GONE);
        }
        else {
            mPostBtn.setText("Post");
            mPostField.setHint("Start typing here to share a post...");
            mCamera.setVisibility(View.VISIBLE);
        }
        mContext = getContext();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        MainActivity.self.updateTitle();
    }

    @Override
    public void onDestroyView() {
        mFeedItem = null;
        mUserId = null;
        super.onDestroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView == null) {
            mLoader = ImageLoader.getInstance();
            mView = inflater.inflate(R.layout.post, container, false);
            mAvatar = (ImageView) mView.findViewById(R.id.avatar);
            mPreview = (ImageView) mView.findViewById(R.id.preview);
            mPostBtn = (Button) mView.findViewById(R.id.postBtn);
            mRemoveMedia = (Button) mView.findViewById(R.id.removeMedia);
            mPostField = (TextView) mView.findViewById(R.id.postField);
            mCamera = (ImageButton) mView.findViewById(R.id.camera);
            RelativeLayout controls = (RelativeLayout) mView.findViewById(R.id.controls);
            mLoader.displayImage(Me.atn.pic, mAvatar);
            Font.applyTo(mView);
            Font.applyTo(controls);
            final Fragment self = this;
            mRemoveMedia.setVisibility(View.GONE);
            mRemoveMedia.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clearMedia();
                }
            });
            mCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeKeyPad(mPostField);
                    MainActivity.self.openCamera();
//                    dispatchTakePictureIntent();
                }
            });
            mPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String text = mPostField.getText().toString();
                    if (text.length() > 0) {
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
                        } else {
                            mPostBtn.setText("Posting...");
                            MainActivity.self.homeFragment.mDispatch.post(text, new Response.Listener() {
                                @Override
                                public void onResponse(Object o) {
                                    mPostBtn.setText("Posted!");
                                    mPostField.setText("");
                                    MainActivity.self.homeFragment.load_new();
                                    closeKeyPad(mPostField);
                                    clearMedia();
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
                }
            });
        }
        return mView;
    }

    public void setMedia(String id) {
        mMediaId = id;
        mLoader.displayImage("https://photos.wds.fm/media/"+mMediaId+"?format=small", mPreview);
        mRemoveMedia.setVisibility(View.VISIBLE);
        mPreview.setVisibility(View.VISIBLE);
        mCamera.setVisibility(View.GONE);
        MainActivity.self.homeFragment.mDispatch.attachedMedia = id;
    }

    public void clearMedia() {
        mMediaId = null;
        mRemoveMedia.setVisibility(View.GONE);
        mPreview.setVisibility(View.GONE);
        mCamera.setVisibility(View.VISIBLE);
        MainActivity.self.homeFragment.mDispatch.attachedMedia = null;
    }

    public void closeKeyPad(final View v) {
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }
    public void openKeypad(final View v)
    {
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if (getActivity() != null) {
                    InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
                    inputManager.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        },100);
    }
}
