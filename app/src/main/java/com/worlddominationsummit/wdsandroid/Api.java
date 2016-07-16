package com.worlddominationsummit.wdsandroid;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;
import java.util.Iterator;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.Response;
import android.content.Context;
import android.util.Log;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;


/**
 * Created by nicky on 5/17/15.
 */

public class Api {

    public static RequestQueue queue;

//    private static String url = "http://wds.nky";
    private static String url = "https://worlddominationsummit.com";

    public static void init(Context context){
        HurlStack hurlStack = new HurlStack() {
            @Override
            protected HttpURLConnection createConnection(URL url) throws IOException {
                HttpsURLConnection httpsURLConnection = (HttpsURLConnection) super.createConnection(url);
                try {
                    httpsURLConnection.setSSLSocketFactory(getSSLSocketFactory());
                    httpsURLConnection.setHostnameVerifier(getHostnameVerifier());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return httpsURLConnection;
            }
        };
        Api.queue = Volley.newRequestQueue(context);
    }

    public static void delete(String path, JSONObject raw_params, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Api.request(Request.Method.DELETE, path, raw_params, successListener, errorListener);
    }

    public static void post(String path, JSONObject raw_params, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Api.request(Request.Method.POST, path, raw_params, successListener, errorListener);
    }

    public static void get(String path, JSONObject raw_params, Response.Listener<JSONObject> successListener, Response.ErrorListener errorListener) {
        Api.request(Request.Method.GET, path, raw_params, successListener, errorListener);
    }

    public static void request(int method, String path, JSONObject params, final Response.Listener<JSONObject> successListener, final Response.ErrorListener errorListener) {
        String url = Api.url+"/api/"+path;
        if(Me.user_token != null && Me.user_token.length() > 0) {
            if (params == null) {
                params = new JSONObject();
            }
            try {
                params.put("user_token", Me.user_token);
            } catch (JSONException e) {
                Log.e("WDS", "Json Exception", e);
            }
        }
        if((method == Request.Method.GET || method == Request.Method.DELETE) && params != null) {
            url += "?"+JsonHelper.UrlEncode(params);
        }

//        Puts.i(params);
//        Puts.i(url);
        Response.Listener<JSONObject> onSuccess = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject rsp) {
                successListener.onResponse(rsp);
            }
        };
        Response.ErrorListener onError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                errorListener.onErrorResponse(volleyError);
            }
        };
        JsonObjectRequest request = new JsonObjectRequest(method, url, params, onSuccess, onError);
        Api.queue.add(request);
    }
    // Let's assume your server app is hosting inside a server machine
    // which has a server certificate in which "Issued to" is "localhost",for example.
    // Then, inside verify method you can verify "localhost".
    // If not, you can temporarily return true
    private static HostnameVerifier getHostnameVerifier() {
        return new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true; // verify always returns true, which could cause insecure network traffic due to trusting TLS/SSL server certificates for wrong hostnames
//                HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
//                return hv.verify("localhost", session);
            }
        };
    }

    private static TrustManager[] getWrappedTrustManagers(TrustManager[] trustManagers) {
        final X509TrustManager originalTrustManager = (X509TrustManager) trustManagers[0];
        return new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return originalTrustManager.getAcceptedIssuers();
                    }

                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0){
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkClientTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkClientTrusted", e.toString());
                        }
                    }

                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        try {
                            if (certs != null && certs.length > 0){
                                certs[0].checkValidity();
                            } else {
                                originalTrustManager.checkServerTrusted(certs, authType);
                            }
                        } catch (CertificateException e) {
                            Log.w("checkServerTrusted", e.toString());
                        }
                    }
                }
        };
    }

    private static SSLSocketFactory getSSLSocketFactory()
            throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, KeyManagementException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        InputStream caInput = MainActivity.self.getResources().openRawResource(R.raw.cert); // this cert file stored in \app\src\main\res\raw folder path
//
        Certificate ca = cf.generateCertificate(caInput);
        caInput.close();

        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null, null);
        keyStore.setCertificateEntry("ca", ca);

        String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
        tmf.init(keyStore);

        TrustManager[] wrappedTrustManagers = getWrappedTrustManagers(tmf.getTrustManagers());

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, wrappedTrustManagers, null);

        return sslContext.getSocketFactory();
    }
}
