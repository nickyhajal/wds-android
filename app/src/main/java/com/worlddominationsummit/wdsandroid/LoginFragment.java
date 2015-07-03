package com.worlddominationsummit.wdsandroid;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.*;
import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by nicky on 5/18/15.
 */
public class LoginFragment extends Fragment{
    public View view;
    public Button btn;
    public TextView email_inp;
    public TextView pw_inp;
    public TextView forgot;
    public ImageView logo;
    public ScrollView mScrollView;
    public Boolean focused = false;


    @Override
    public void onStop() {
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(email_inp.getWindowToken(), 0);
        imm.hideSoftInputFromWindow(pw_inp.getWindowToken(), 0);
        super.onStop();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.view == null) {
            final EditText email_inp;
            final EditText pw_inp;
            final ViewGroup fContainer = container;
            this.view = inflater.inflate(R.layout.login, container, false);
            this.logo = (ImageView) this.view.findViewById(R.id.logo);
            mScrollView = (ScrollView) this.view.findViewById(R.id.scrollView);
            this.btn = (Button) this.view.findViewById(R.id.login_button);
            this.btn.setTypeface(Font.use("Karla_Bold"));
            this.btn.setTransformationMethod(null);
            this.email_inp = email_inp = (EditText) this.view.findViewById(R.id.email_inp);
            this.email_inp.setTypeface(Font.use("Karla_Bold"));
            this.pw_inp = pw_inp = (EditText) this.view.findViewById(R.id.pw_inp);
            this.pw_inp.setTypeface(Font.use("Karla_Bold"));
            this.email_inp.setOnFocusChangeListener(inpFocus);
            this.forgot = (TextView) this.view.findViewById(R.id.forgot);
            this.forgot.setTypeface(Font.use("Karla"));
            this.pw_inp.setOnFocusChangeListener(inpFocus);
            forgot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://worlddominationsummit.com/forgot-password")));
                }
            });
            this.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    final Button btn = (Button) fContainer.findViewById(R.id.login_button);
                    String email = email_inp.getText().toString();
                    String pw = pw_inp.getText().toString();
                    btn.setText("Logging in...");
                    JSONObject params = new JSONObject();
                    String err = "";
                    try {
                        if (email.length() == 0) {
                            err = "Enter an Email";
                        }
                        else if (pw.length() == 0) {
                            err = "Enter a Password";
                        }
                        params.put("username", email);
                        params.put("password", pw);
                        params.put("request_user_token", "1");
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                    if (err.length() > 0) {
                        btn.setText(err);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                btn.setText("Login to WDS");
                            }
                        }, 2000);
                    }
                    else {
                        Api.post("user/login", params, new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject rsp) {
                                Log.i("WDS", rsp.toString());
                                if (rsp.has("user_token")) {
                                    try {
                                        Me.saveUserToken(rsp.getString("user_token"));
                                    } catch (JSONException e) {
                                        Log.e("WDS", "Json Exception", e);
                                    }
                                    getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                                    btn.setText("Success!");
                                } else if (rsp.has("loggedin")) {
                                    btn.setText("Wrong Email/Password");
                                } else {
                                    btn.setText("Try Again");
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                btn.setText("You're Offline");
                                MainActivity.offlineAlert();
                            }
                        });
                    }
                }
            });
        }
        return this.view;
    }
    private View.OnFocusChangeListener inpFocus =  new View.OnFocusChangeListener() {
        public void onFocusChange(View view, boolean gainFocus) {
            if (gainFocus) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mScrollView.fullScroll(View.FOCUS_DOWN);
                    }
                }, 400);
            }
        }
    };
}
