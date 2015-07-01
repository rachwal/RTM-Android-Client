/*
 * Created by Bartosz Rachwal.
 * Copyright (c) 2015 The National Institute of Advanced Industrial Science and Technology, Japan. All rights reserved.
 */

package jp.go.aist.rachwal.rtmclient.preview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;

import jp.go.aist.rachwal.rtmclient.R;
import jp.go.aist.rachwal.rtmclient.camera.RTMCamera;
import jp.go.aist.rachwal.rtmclient.camera.RTMCameraCallback;
import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

public class PreviewFragment extends RoboFragment {

    @Inject
    private RTMCamera camera;

    @InjectView(R.id.camera_view)
    private SurfaceView cameraPreview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_camera_preview, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        startStreaming();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopStreaming();
    }

    private void stopStreaming() {

        if (cameraPreview.getHolder().getSurface() == null) {
            return;
        }

        camera.stopPreview();
        camera.release();
    }

    private void startStreaming() {

        camera.open(cameraCallback);
    }

    private RTMCameraCallback cameraCallback = new RTMCameraCallback() {

        @Override
        public void opened() {
            camera.setPreviewDisplay(cameraPreview.getHolder());
            camera.startPreview();
        }
    };
}
