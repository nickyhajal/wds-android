package com.worlddominationsummit.wds;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(this.view == null) {
            final EditText email_inp;
            final EditText pw_inp;
            final ViewGroup fContainer = container;
            this.view = inflater.inflate(R.layout.login, container, false);
            this.btn = (Button) this.view.findViewById(R.id.login_button);
            this.btn.setTypeface(Font.use("Karla_Bold"));
            this.btn.setTransformationMethod(null);
            this.email_inp = email_inp = (EditText) this.view.findViewById(R.id.email_inp);
            this.email_inp.setTypeface(Font.use("Karla_Bold"));
            this.pw_inp = pw_inp = (EditText) this.view.findViewById(R.id.pw_inp);
            this.pw_inp.setTypeface(Font.use("Karla_Bold"));
            this.btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    final Button btn = (Button) fContainer.findViewById(R.id.login_button);
                    String email = email_inp.getText().toString();
                    String pw = pw_inp.getText().toString();
                    btn.setText("Logging in...");
                    JSONObject params = new JSONObject();
                    try {
                        params.put("username", email);
                        params.put("password", pw);
                        params.put("request_user_token", "1");
                    } catch (JSONException e) {
                        Log.e("WDS", "Json Exception", e);
                    }
                    Log.i("WDS", params.toString());
                    Api.post("user/login", params, new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject rsp) {
                            Log.i("WDS", rsp.toString());
                            if(rsp.has("user_token")) {
                                try {
                                    Me.saveUserToken(rsp.getString("user_token"));
                                } catch (JSONException e) {
                                    Log.e("WDS", "Json Exception", e);
                                }
                                btn.setText("Success!");
                            }
                            else if (rsp.has("loggedin")) {
                                btn.setText("Wrong Email/Password");
                            }
                            else {
                                btn.setText("Try Again");
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            // TODO: Better offline/server problem message
                            btn.setText("You're Offline");
                        }
                    });
                }
            });
        }
        return this.view;
    }
}
