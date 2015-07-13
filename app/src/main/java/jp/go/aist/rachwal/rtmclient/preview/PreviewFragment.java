/*
 * Created by Bartosz Rachwal.
 * Copyright (c) 2015 The National Institute of Advanced Industrial Science and Technology, Japan. All rights reserved.
 */

package jp.go.aist.rachwal.rtmclient.preview;

import android.app.Application;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;

import jp.go.aist.rachwal.rtmclient.R;
import jp.go.aist.rachwal.rtmclient.camera.RTMCamera;
import jp.go.aist.rachwal.rtmclient.camera.RTMCameraCallback;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectResource;
import roboguice.inject.InjectView;

public class PreviewFragment extends RoboFragment {

    @Inject
    private RTMCamera camera;

    @Inject
    private Application application;

    @InjectResource(R.string.video_size_key)
    private String videoSizeKey;

    @InjectView(R.id.camera_view)
    private SurfaceView cameraPreview;

    private static SurfaceHolder holder;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_camera_preview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        holder = cameraPreview.getHolder();
    }

    @Override
    public void onStart() {
        super.onStart();

        startPreview();
    }

    @Override
    public void onStop() {
        super.onStop();

        stopPreview();
    }

    private void stopPreview() {

        holder.removeCallback(resizePreview);

        camera.stopPreview();
        camera.release();
    }

    private void startPreview() {
        initializationFinished = false;
        holder.addCallback(resizePreview);

        camera.open(cameraCallback);
    }

    private RTMCameraCallback cameraCallback = new RTMCameraCallback() {

        @Override
        public void opened() {

            camera.startPreview();
            camera.setPreviewDisplay(holder);
            initializationFinished = true;
        }
    };

    private volatile boolean initializationFinished;
    private SurfaceHolder.Callback resizePreview = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            final Handler handler = new Handler();
            while (!initializationFinished) {

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 100);
            }
            resizePreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }


        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private void resizePreview() {

        ViewGroup.LayoutParams params = cameraPreview.getLayoutParams();

        Display display = getActivity().getWindowManager().getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);

        if (camera.perpendicular) {
            params.width = metrics.widthPixels;
            params.height = (int) ((double) metrics.widthPixels * camera.aspectRatio);
        } else {
            params.height = metrics.widthPixels;
            params.width = (int) ((double) metrics.widthPixels * camera.aspectRatio);
        }
    }

}