package com.worlddominationsummit.wdsandroid;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.Response;
import com.joooonho.SelectableRoundedImageView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nicky on 5/18/15.
 */
public class ProfileFragment extends Fragment {
    public View mView;
    public Attendee mAtn;
    public TextView mName;
    public TextView mHeading;
    public WebView mAbout;
    public ScrollView mScrollView;
    public Button mFb;
    public Button mInstagram;
    public Button mTwitter;
    public Button mSite;
    public Button mConnect;
    public Button mOpenMessage;
    public Button mNotes;
    public SelectableRoundedImageView mAv;
    public LinearLayout mLoading;
    public ImageLoader mImgLoader = new ImageLoader(getActivity());
    public LinearLayout mAvatarLayout;

    public void setAttendee(Attendee atn) {
        if (atn.qnaStr == null) {
            mAtn = new Attendee();
            if (mLoading != null) {
                mLoading.setVisibility(View.VISIBLE);
                mScrollView.setVisibility(View.GONE);
                mConnect.setVisibility(View.GONE);
            }
            JSONObject params = new JSONObject();
            try {
                params.put("user_id", atn.user_id);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
            if(params.has("user_id")) {
                Api.get("user", params, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        try {
                            JSONObject user = jsonObject.getJSONObject("user");
                            Attendee atn = Attendee.fromJson(user);
                            atn.initQna(new JSONArray(user.getString("answers")));
                            setAttendee(atn);
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
        else {
            mAtn = atn;
            mLoading.setVisibility(View.GONE);
            mScrollView.setVisibility(View.VISIBLE);
            mConnect.setVisibility(View.VISIBLE);
        }
        if (mView != null) {
            mName.setText(mAtn.full_name);
            if (mAtn.facebook != null && !mAtn.facebook.equals("null") && mAtn.facebook.length() > 0) {
                mFb.setText("fb.com/"+mAtn.facebook);
                mFb.setVisibility(View.VISIBLE);
            }
            else {
                mFb.setVisibility(View.GONE);
            }
            if (mAtn.instagram != null && !mAtn.instagram.equals("null") && mAtn.instagram.length() > 0) {
                mInstagram.setText("ig.com/"+mAtn.instagram);
                mInstagram.setVisibility(View.VISIBLE);
            }
            else {
                mInstagram.setVisibility(View.GONE);
            }
            if (mAtn.twitter != null && !mAtn.twitter.equals("null") && mAtn.twitter.length() > 0) {
                mTwitter.setText("@"+mAtn.twitter);
                mTwitter.setVisibility(View.VISIBLE);
            }
            else {
                mTwitter.setVisibility(View.GONE);
            }
            if (mAtn.site != null && !mAtn.site.equals("null") && mAtn.site.length() > 0) {
                mSite.setText(mAtn.site);
                mSite.setVisibility(View.VISIBLE);
            }
            else {
                mSite.setVisibility(View.GONE);
            }
            if (atn.qnaStr == null) {
                mAbout.loadData("", "text/html", null);
            }
            else {
                mAbout.loadData(mAtn.qnaStr, "text/html", null);
            }
            if (Me.isFriend(mAtn.user_id)) {
                mConnect.setText("You're Friends!");
            }
            else {
                mConnect.setText("Friend "+mAtn.first_name);
            }
            mNotes.setText("Your Notes on "+mAtn.first_name);
            mOpenMessage.setText("Send a Message to "+mAtn.first_name);
            mHeading.setText("A bit about "+mAtn.first_name);
            updateAvatar();
        }
        if (mScrollView != null) {
            mScrollView.post(new Runnable() {
                public void run() {
                    mScrollView.scrollTo(0, 0);
                }
            });
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (mAtn != null) {
            updateAvatar();
        }
    }

    public void updateAvatar() {
        if (mAv == null) {
            float density = MainActivity.density;
            ViewGroup vg = (ViewGroup) (MainActivity.self.getWindow().getDecorView().getRootView());
            mAvatarLayout = new LinearLayout(MainActivity.self);
            mAv = new SelectableRoundedImageView(MainActivity.self);
            mAv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            mAv.setCornerRadiiDP(12, 12, 12, 12);
            mAv.setBorderWidthDP(5);
            mAv.setBorderColor(MainActivity.self.getResources().getColor(R.color.light_tan));
            //mAv.mutateBackground(true);
            mImgLoader.DisplayImage(mAtn.pic, mAv);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
            ViewGroup.LayoutParams vparams = new ViewGroup.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
            mAvatarLayout.setOrientation(LinearLayout.VERTICAL);
            params.width = (int) (160 * density);
            params.height = (int) (160 * density);
            params.gravity = Gravity.CENTER_HORIZONTAL;
            params.topMargin = (int) (45 * density);
            mAvatarLayout.addView(mAv, params);
            vg.addView(mAvatarLayout, vparams);
        }
        else {
           mImgLoader.DisplayImage(mAtn.pic, mAv);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mView == null) {
            mView = inflater.inflate(R.layout.profile, container, false);
            LinearLayout content = (LinearLayout) mView.findViewById(R.id.content);
            final ScrollView scrollView = (ScrollView) mView.findViewById(R.id.scrollView);
            mName = (TextView) mView.findViewById(R.id.name);
            mFb = (Button) mView.findViewById(R.id.fb);
            mInstagram = (Button) mView.findViewById(R.id.instagram);
            mTwitter = (Button) mView.findViewById(R.id.twitter);
            mSite = (Button) mView.findViewById(R.id.site);
            mConnect = (Button) mView.findViewById(R.id.connect);
            mOpenMessage = (Button) mView.findViewById(R.id.openMessage);
            mNotes = (Button) mView.findViewById(R.id.notes);
            mHeading = (TextView) mView.findViewById(R.id.about_heading);
            mScrollView = (ScrollView) mView.findViewById(R.id.scrollView);
            mLoading = (LinearLayout) mView.findViewById(R.id.loading);
            mName.setTypeface(Font.use("Vitesse_Medium"));
            mAbout = (WebView) mView.findViewById(R.id.about);
            Font.applyTo(mLoading);
            mConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    JSONObject params = new JSONObject();
                    try {
                        params.put("to_id", mAtn.user_id);
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                    if (Me.isFriend(mAtn.user_id)) {
                        Api.delete("user/connection", params, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                mConnect.setText("Friend " + mAtn.first_name);
                                Me.removeFriend(mAtn.user_id);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                MainActivity.offlineAlert();
                                Log.e("VOLLEY ER", volleyError.toString());
                            }
                        });
                    } else {
                        Api.post("user/connection", params, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject jsonObject) {
                                mConnect.setText("You're Friends!");
                                Me.addFriend(mAtn.user_id);
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.e("VOLLEY ER", volleyError.toString());
                                MainActivity.offlineAlert();
                            }
                        });
                    }
                }
            });
            mOpenMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.open_chat(mAtn);
                }
            });
            mNotes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.self.open_user_notes(mAtn.user_id);
                }
            });
            mTwitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String twitter = mAtn.twitter;
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + twitter)));
                    }catch (Exception e) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/" + twitter)));
                    }
                }
            });
            mInstagram.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String instagram = "http://instagram.com/"+mAtn.instagram;
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(instagram)));
                }
            });
            mSite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String site = mAtn.site;
                    if(!site.contains("http://") && !site.contains("https://")) {
                        site = "http://"+site;
                    }
                    getActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(site)));
                }
            });
            mFb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                String fb = mAtn.facebook;
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://facebook.com/" + fb)));
                }
            });
            Font.applyTo(content);
            setupWebView();
            scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {

                @Override
                public void onScrollChanged() {
                    if (mAv != null) {
                        int scrollY = scrollView.getScrollY(); //for verticalScrollView
                        float density = MainActivity.density;
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
                        int dim = (160 - scrollY);
                        if (dim > 160) {
                            dim = 160;
                        }
                        if (dim < 60) {
                            dim = 60;
                        }
                        int ddim = dim * (int) density;
                        params.width = ddim;
                        params.height = ddim;
                        params.gravity = Gravity.CENTER_HORIZONTAL;
                        int mdim = 45 - (scrollY / 3);
                        if (mdim < 30) {
                            mdim = 30;
                        }
                        params.topMargin = (int) (mdim * density);
                        mAv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        mImgLoader.DisplayImage(mAtn.pic, mAv);
                        mAv.setLayoutParams(params);
                        mAv.setBorderWidthDP((5f / (160f / (float) dim)));
                    }
                }
            });
        }
        if (mAtn != null) {
            setAttendee(mAtn);
        }
        return mView;
    }

    @Override
    public void onDestroyView() {
        ViewGroup vg = (ViewGroup)(getActivity().getWindow().getDecorView().getRootView());
        vg.removeView(mAvatarLayout);
        mAv = null;
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void setupWebView() {
        mAbout.getSettings().setJavaScriptEnabled(true);
        mAbout.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                mAbout.loadUrl("javascript:MyApp.resize(0)");
                mAbout.loadUrl("javascript:MyApp.resize(document.body.getBoundingClientRect().height)");
                super.onPageFinished(view, url);
            }
        });
        mAbout.addJavascriptInterface(this, "MyApp");
    }

    @JavascriptInterface
    public void resize(float height) {
        final float h = height + 80;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAbout.setLayoutParams(new LinearLayout.LayoutParams(getResources().getDisplayMetrics().widthPixels, (int) (h * getResources().getDisplayMetrics().density)));
            }
        });
    }

}
