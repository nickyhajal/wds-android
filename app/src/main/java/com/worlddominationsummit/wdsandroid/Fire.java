package com.worlddominationsummit.wdsandroid;

import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import io.fabric.sdk.android.services.common.Crash;

/**
 * Created by nicky on 08/06/16.
 */
public class Fire {
    public static DatabaseReference mRef;
    private static FirebaseAuth mAuth;
    private static FirebaseAuth.AuthStateListener mAuthListener;


    public static void init(){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        Fire.mRef = database.getReference("");
        Fire.mAuth = FirebaseAuth.getInstance();
        Fire.mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Puts.i("FIRE SIGNED IN");
                    Me.fireAuthCallback();
                } else {
                    Puts.i("NOT SIGNED IN");
                    // Userz is signed out
//                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // ...
            }
        };
        Fire.mAuth.addAuthStateListener(Fire.mAuthListener);
    }
    public static void auth(String token) {
        Crashlytics.log(">>> AUTH FIREBASE");
        Crashlytics.log(token);
        Fire.mAuth.signInWithCustomToken(token).addOnCompleteListener(MainActivity.self, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                Crashlytics.log(">>>>> SIGN IN RESPONSE");
//                Puts.i(task.toString());
                // If sign in fails, display a message to the user. If sign in succeeds
                // the auth state listener will be notified and logic to handle the
                // signed in user can be handled in the listener.
                if (!task.isSuccessful()) {
                    Log.w("WDS", "signInWithCustomToken", task.getException());
//                    Toast.makeText(MainActivity.self, "Authentication failed.",
//                            Toast.LENGTH_SHORT).show();
                }
            }
        });
//        .addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                Puts.i("FAILLL >>");
//                Puts.i(e.getLocalizedMessage());
//            }
//        }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
//            @Override
//            public void onSuccess(AuthResult authResult) {
//                Puts.i("SUCCESS >>");
//                Puts.i(authResult.toString());
//            }
//        })
    }
    public static void set(String path, Object val) {
        Fire.mRef.child(path).setValue(val);
    }
    public static void get(String path, ValueEventListener cb) {
        Fire.mRef.child(path).addListenerForSingleValueEvent(cb);
    }
    public static void remove(String path) {
        Fire.mRef.child(path).removeValue();
    }
    public static String createAt(String path) {
        return Fire.mRef.child(path).push().getKey();
    }
    public static ValueEventListener watch(String path, ValueEventListener cb) {
        return Fire.mRef.child(path).addValueEventListener(cb);
    }
    public static ChildEventListener watch(String path, ChildEventListener cb) {
        return Fire.mRef.child(path).addChildEventListener(cb);
    }
    public static ChildEventListener query(String path, ArrayList<HashMap<String, String>> query, ChildEventListener cb) {
        Query qRef = getQuery(path, query);
//        Puts.i("WITH QUERY>>>");
//        Puts.i(qRef.toString());
        return qRef.addChildEventListener(cb);
    }
    public static ValueEventListener query(String path, ArrayList<HashMap<String, String>> query, ValueEventListener cb) {
        Query qRef = getQuery(path, query);
        return qRef.addValueEventListener(cb);
    }
    public static void querySingle(String path, ArrayList<HashMap<String, String>> query, ValueEventListener cb) {
        Query qRef = getQuery(path, query);
        qRef.addListenerForSingleValueEvent(cb);
    }
    public static Query getQuery(String path, ArrayList<HashMap<String, String>> query) {
        Query qRef = Fire.mRef.child(path);
        for (HashMap q : query) {
            if (q.get("type").equals("orderKey")) {
                qRef.orderByKey();
            }
            if (q.get("type").equals("orderChild")) {
                qRef.orderByChild((String) q.get("val"));
//                Puts.i("orderChild!!");
//                Puts.i(q.get("val").toString());
            }
            if (q.get("type").equals("limitLast")) {
                qRef.limitToLast(Integer.valueOf((String) q.get("val")));
            }
            if (q.get("type").equals("startChildAt")) {
//                Puts.i("startChildAt!!!");
//                Puts.i(q.get("val").toString());
//                qRef.startAt((String) q.get("val"));
                qRef.startAt(Double.valueOf(q.get("val").toString()));
            }
        }
        return qRef;
    }
    public static void unwatch(ValueEventListener listener) {
        if (Fire.mRef != null && listener != null)  {
            Fire.mRef.removeEventListener(listener);
        }
    }
    public static void unwatch(ChildEventListener listener) {
        if (Fire.mRef != null && listener != null) {
            Fire.mRef.removeEventListener(listener);
        }
    }
}


