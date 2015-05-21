package com.worlddominationsummit.wds;
/**
 * Created by nicky on 5/17/15.
 */

import org.json.JSONObject;
import java.util.concurrent.Callable;


public class Response {
    public Boolean is_err;
    public int err_code;
    public JSONObject json;
    public Callable<Response> callback;

    public Response(Callable<Response> callback) {
        this.callback = callback;
    }

    public void onResponse(JSONObject rsp){
        this.is_err = false;
        this.json = rsp;
    }

}
