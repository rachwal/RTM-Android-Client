/*
 * Created by Bartosz Rachwal.
 * Copyright (c) 2015 The National Institute of Advanced Industrial Science and Technology, Japan. All rights reserved.
 */

package jp.go.aist.rachwal.rtmclient.camera;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.io.IOException;
import java.util.List;

import jp.go.aist.rachwal.rtmclient.R;
import jp.go.aist.rachwal.rtmclient.web.WebClient;
import jp.go.aist.rachwal.rtmclient.web.WebClientCallback;
import roboguice.inject.InjectResource;

@Singleton
public class RTMCamera {
    private static final String CAMERA_THREAD_NAME = "RTC_CAMERA_THREAD";

    @Inject
    private WebClient webClient;

    @Inject
    private Resources resources;

    @Inject
    private Application application;

    @InjectResource(R.string.camera_key)
    private String cameraKey;

    @InjectResource(R.string.video_size_key)
    private String videoSizeKey;

    @InjectResource(R.string.streaming_key)
    private String streamingKey;

    private Camera camera;
    private HandlerThread handlerThread;
    private Handler handler;

    public Camera.Size imageSize;

    public int getNumberOfCameras() {
        return Camera.getNumberOfCameras();
    }

    public void open(final RTMCameraCallback callback) {

        if (handlerThread == null) {
            handlerThread = new HandlerThread(CAMERA_THREAD_NAME);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());
        }

        handler.post(new Runnable() {

            @Override
            public void run() {

                camera = setUpCamera();

                if (camera != null) {
                    Camera.Parameters parameters = camera.getParameters();
                    imageSize = parameters.getPreviewSize();
                    callback.opened();
                }
            }
        });
    }

    private Camera setUpCamera() {

        Camera device = null;
        try {
            if (getNumberOfCameras() > 1) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(application);

                int cameraNo = Integer.parseInt(preferences.getString(cameraKey, "0"));

                device = Camera.open(cameraNo);
            } else {
                device = Camera.open();
            }

            device.setPreviewCallback(sendImage);

            applyPreferences(device);

            rotateCamera(device);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return device;
    }

    private void applyPreferences(Camera device) {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(application);
        int size = Integer.parseInt(preferences.getString(videoSizeKey, "1"));

        Camera.Parameters parameters = device.getParameters();

        List<Camera.Size> supportedSizes = parameters.getSupportedPreviewSizes();
        int index = (2-size)*((supportedSizes.size()-1)/2);
        Camera.Size selectedSize = supportedSizes.get(index);
        parameters.setPreviewSize(selectedSize.width, selectedSize.height);

        device.setParameters(parameters);

        streaming = preferences.getBoolean(streamingKey, false);
    }

    private void rotateCamera(Camera device) {

        Configuration configuration = resources.getConfiguration();

        if(configuration.orientation == Surface.ROTATION_0) {
            device.setDisplayOrientation(0);
        } else if(configuration.orientation == Surface.ROTATION_90) {
            device.setDisplayOrientation(90);
        } else if(configuration.orientation == Surface.ROTATION_180) {
            device.setDisplayOrientation(180);
        } else if(configuration.orientation == Surface.ROTATION_270) {
            device.setDisplayOrientation(270);
        }
    }

    public void stopPreview() {

        if (camera == null) {
            return;
        }

        try {
            camera.setPreviewCallback(null);
            camera.stopPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setPreviewDisplay(SurfaceHolder surfaceHolder) {

        if (camera == null) {
            return;
        }

        try {
            camera.setPreviewDisplay(surfaceHolder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean streaming = false;

    public void startPreview() {

        if (camera == null) {
            return;
        }

        webClient.initialize();

        camera.startPreview();

        canSend = true;
    }

    public void release() {

        if (camera == null) {
            return;
        }

        camera.release();
        camera = null;
    }

    private volatile Boolean canSend = true;

    private Camera.PreviewCallback sendImage = new Camera.PreviewCallback() {

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

            if(!streaming) {
                return;
            }

            if (canSend == false) {
                return;
            }

            canSend = false;

            if (webClient.initialized) {
                webClient.postImage(data, imageSize, responseCallback);
            }
        }
    };

    private WebClientCallback responseCallback = new WebClientCallback() {

        @Override
        public void getResponse() {
            canSend = true;
        }
    };
}
