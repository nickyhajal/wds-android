package com.worlddominationsummit.wdsandroid;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by nicky on 8/12/16.
 */
public class AtnStoryFragment extends Fragment {
    public View mView;
    public TextView mText;
    public TextView mMustText;
    public TextView mCount;
    public EditText mPhone;
    public EditText mStory;
    public Button mSubmit;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView == null) {
            mView = inflater.inflate(R.layout.atnstory, container, false);
            ViewGroup vg = (ViewGroup) mView;
            mText = (TextView) mView.findViewById(R.id.text);
            mText.setTypeface(Font.use("Karla"));
            mCount = (TextView) mView.findViewById(R.id.storycount);
            mCount.setTypeface(Font.use("Karla_BoldItalic"));
            mMustText = (TextView) mView.findViewById(R.id.musttext);
            mPhone = (EditText) mView.findViewById(R.id.phone);
            mStory = (EditText) mView.findViewById(R.id.story);
            mSubmit = (Button) mView.findViewById(R.id.submit);
            mMustText.setTypeface(Font.use("Karla_Bold"));
            mPhone.setTypeface(Font.use("Karla"));
            mStory.setTypeface(Font.use("Karla"));
            mSubmit.setTypeface(Font.use("Vitesse_Bold"));
            mSubmit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    submitStory();
                }
            });
            mStory.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    mCount.setText(String.valueOf(250 - mStory.length()));
                }
            });
            Font.applyTo(mView);
        }
        return mView;
    }

    public void error(String text) {
        mSubmit.setText(text);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSubmit.setText("Submit");
            }
        }, 3000);
    }
    public void submitStory() {
        if (mPhone.getText().toString().length() < 1) {
            error("Add Your Phone #");
            return;
        }
        if (mStory.getText().toString().length() < 1) {
            error("Add Your Story");
            return;
        }
        mSubmit.setText("Sending...");
        JSONObject params = new JSONObject();
        try {
            params.put("story", mStory.getText().toString());
            params.put("phone", mPhone.getText().toString());
        } catch (JSONException e) {
            Log.e("WDS", "Json Exception", e);
        }
        Store.set("atnstory", "submitted");
        Api.post("user/story", params, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                mSubmit.setText("Success!");
                View view = mView;
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager)MainActivity.self.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.self.homeFragment.update_items();
                        MainActivity.self.open_dispatch();
                    }
                }, 750);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }
}
