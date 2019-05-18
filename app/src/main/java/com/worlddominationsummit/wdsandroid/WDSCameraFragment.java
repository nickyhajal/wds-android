package com.worlddominationsummit.wdsandroid;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Surface;
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
import com.github.florent37.camerafragment.CameraFragment;
import com.github.florent37.camerafragment.configuration.Configuration;
import com.github.florent37.camerafragment.listeners.CameraFragmentResultListener;
import com.github.florent37.camerafragment.listeners.CameraFragmentStateListener;
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
import static com.github.florent37.camerafragment.internal.ui.BaseAnncaFragment.MIN_VERSION_ICECREAM;

/**
 * Created by nicky on 5/28/15.
 */
public class WDSCameraFragment extends Fragment implements OrientationManager.OrientationListener {
    public View mView;
    public ImageView mAvatar;
    public ImageView mPreview;
    public RelativeLayout mShutter;
    public TextView mPostField;
    public TextView mStatus;
    public ImageButton mCamera;
    public ImageLoader mLoader;
    public String mUserId;
    public Context mContext;
    public HashMap<String, String> mFeedItem;
    public String mCurrentPhotoPath;
    public String mCurrentPhotoFile;
    public int mFlashState = 0;
    public int mCameraState = 0;
    public ImageButton mFlashChooser;
    public ImageButton mCameraChooser;
    public String mCurrentPhotoDir;
    public CameraFragment cameraFragment;
    public OrientationManager orientationManager;
    public RelativeLayout mRetry;
    public int systemVis;
    public RelativeLayout mAccept;
    public byte[] image;
    public int mAngle = 0;
    public int mImgAngle = 0;
    private final static int REQUEST_IMAGE_CAPTURE = 6969;

    @Override
    public void onDestroyView() {
        mFeedItem = null;
        mUserId = null;
        View decorView = ((Activity) getContext()).getWindow().getDecorView();
        if (Build.VERSION.SDK_INT > MIN_VERSION_ICECREAM) {
            decorView.setSystemUiVisibility(systemVis);
            MainActivity.self.getActionBar().show();
        }
        super.onDestroyView();
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(mView == null) {
            View decorView = ((Activity) getContext()).getWindow().getDecorView();
            if (Build.VERSION.SDK_INT > MIN_VERSION_ICECREAM) {
                int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
                systemVis = decorView.getSystemUiVisibility();
            }
            mLoader = ImageLoader.getInstance();
            mView = inflater.inflate(R.layout.wds_camera, container, false);
            mRetry = (RelativeLayout) mView.findViewById(R.id.cancel);
            mAccept = (RelativeLayout) mView.findViewById(R.id.accept);
            mShutter = (RelativeLayout) mView.findViewById(R.id.shutter);
            mStatus = (TextView) mView.findViewById(R.id.status);
            mStatus.setTypeface(Font.use("Karla_Bold"));
            mCameraChooser = (ImageButton) mView.findViewById(R.id.cameraChooser);
            mFlashChooser = (ImageButton) mView.findViewById(R.id.flashChooser);
            mCameraChooser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cameraFragment.switchCameraTypeFrontBack();
                }
            });
            mFlashChooser.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    cameraFragment.toggleFlashMode();
                }
            });
            final WDSCameraFragment self = this;
            int perm = getContext().checkCallingOrSelfPermission(android.Manifest.permission.CAMERA);
            if (perm == PackageManager.PERMISSION_GRANTED) {
                cameraFragment = CameraFragment.newInstance(new Configuration.Builder().build());
                cameraFragment.setStateListener(new CameraFragmentStateListener() {
                    @Override
                    public void onCurrentCameraBack() {
                        mCameraChooser.setBackground(getContext().getResources().getDrawable(R.drawable.ic_camera_rear_white_24dp));
                    }

                    @Override
                    public void onCurrentCameraFront() {
                        mCameraChooser.setBackground(getContext().getResources().getDrawable(R.drawable.ic_camera_front_white_24dp));
                    }

                    @Override
                    public void onFlashAuto() {
                        mFlashChooser.setBackground(getContext().getResources().getDrawable(R.drawable.ic_flash_auto_white_24dp));
                    }

                    @Override
                    public void onFlashOn() {
                        mFlashChooser.setBackground(getContext().getResources().getDrawable(R.drawable.ic_flash_on_white_24dp));
                    }

                    @Override
                    public void onFlashOff() {
                        mFlashChooser.setBackground(getContext().getResources().getDrawable(R.drawable.ic_flash_off_white_24dp));
                    }

                    @Override
                    public void onCameraSetupForPhoto() {

                    }

                    @Override
                    public void onCameraSetupForVideo() {

                    }

                    @Override
                    public void onRecordStateVideoReadyForRecord() {

                    }

                    @Override
                    public void onRecordStateVideoInProgress() {

                    }

                    @Override
                    public void onRecordStatePhoto() {

                    }

                    @Override
                    public void shouldRotateControls(int degrees) {

                    }

                    @Override
                    public void onStartVideoRecord(File outputFile) {

                    }

                    @Override
                    public void onStopVideoRecord() {

                    }
                });
                mShutter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        processStatus("shuttered");
                        try {
                            self.createImageFile();
                            cameraFragment.takePhotoOrCaptureVideo(new CameraFragmentResultListener() {
                                @Override
                                public void onVideoRecorded(String filePath) {
                                }

                                @Override
                                public void onPhotoTaken(byte[] bytes, String filePath) {
                                    if (mAngle != 90) {
                                        image = rotateImage(mAngle+90, bytes);
                                    } else {
                                        image = bytes;
                                    }
                                    processStatus("photo-ready");
                                }
                            }, mCurrentPhotoDir, mCurrentPhotoFile);
                        }
                        catch (IOException e) {

                        }

                    }
                });
                mAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (image != null) {
                            processStatus("saving");
                            sendPhoto(image);
                        }
                    }
                });
                mRetry.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        image = null;
                        processStatus("waiting");
                        cameraFragment.switchCameraTypeFrontBack();
                        cameraFragment.switchCameraTypeFrontBack();
                    }
                });
            }
        }
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.replace(R.id.cameraFrame, cameraFragment, "cameraFragment");
        transaction.commit();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraFragment.toggleFlashMode();
            }
        }, 150);
        return mView;
    }

    public void processStatus (String status) {
        if (status.equals("shuttered")) {
            mShutter.setVisibility(View.GONE);
            mStatus.setText("Processing...");
        }
        else if (status.equals("photo-ready")) {
            mShutter.setVisibility(View.GONE);
            mRetry.setVisibility(View.VISIBLE);
            mAccept.setVisibility(View.VISIBLE);
            mStatus.setText("");
        } else if (status.equals("saving")) {
            mStatus.setText("Saving...");
            mShutter.setVisibility(View.GONE);
            mRetry.setVisibility(View.GONE);
            mAccept.setVisibility(View.GONE);
        } else if (status.equals("waiting")) {
            mStatus.setText("");
            mShutter.setVisibility(View.VISIBLE);
            mRetry.setVisibility(View.GONE);
            mAccept.setVisibility(View.GONE);
        }

    }

    public byte[] rotateImage(int angle, byte[] bytes) {
        Bitmap bitmapSrc = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Bitmap.createBitmap(bitmapSrc, 0, 0,
                bitmapSrc.getWidth(), bitmapSrc.getHeight(), matrix, true).compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        orientationManager = new OrientationManager(getActivity(), SensorManager.SENSOR_DELAY_NORMAL, this);
        orientationManager.enable();
    }

    @Override
    public void onOrientationChange(OrientationManager.ScreenOrientation screenOrientation) {
        switch(screenOrientation){
            case PORTRAIT:
                mAngle = 0;
                break;
            case REVERSED_PORTRAIT:
                mAngle = 180;
                break;
            case REVERSED_LANDSCAPE:
                mAngle = -90;
                break;
            case LANDSCAPE:
                mAngle = 90;
                break;
        }
        syncViewAngles();
    }

    public void syncViewAngles() {
        mFlashChooser.setRotation(mAngle);
        mCameraChooser.setRotation(mAngle);
        mAccept.setRotation(mAngle);
        mRetry.setRotation(mAngle);
        mShutter.setRotation(mAngle);
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoDir = storageDir.getAbsolutePath();
        mCurrentPhotoFile = imageFileName;

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    public void sendPhoto(final byte[] image) {
        String photourl = "https://photos.wds.fm/photo";
        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, photourl, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);
//                Puts.i("on response");
                try {
//                    Puts.i("success");
                    processStatus("waiting");
                    JSONObject result = new JSONObject(resultResponse);
//                    getChildFragmentManager().popBackStack();
                    MainActivity.self.postFragment.setMedia(result.optString("id", ""));
                    MainActivity.self.getSupportFragmentManager().popBackStack();
                    String status = result.getString("status");
                    String message = result.getString("message");

//                    if (status.equals(Constant.REQUEST_SUCCESS)) {
//                        // tell everybody you have succed upload image and post strings
//                        Log.i("Messsage", message);
//                    } else {
//                        Log.i("Unexpected", message);
//                    }
                } catch (JSONException e) {
//                    Puts.i("error");
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
//                Puts.i("eRROR");
//                Puts.i(error.toString());
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
//                    Puts.i(errorMessage);
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("photo", new DataPart("file.jpg", image, "image/jpeg"));
                return params;
            }
        };

        VolleySingleton.getInstance(getContext()).addToRequestQueue(multipartRequest);

    }

}
