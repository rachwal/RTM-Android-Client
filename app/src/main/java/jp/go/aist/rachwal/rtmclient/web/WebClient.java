/*
 * Created by Bartosz Rachwal.
 * Copyright (c) 2015 The National Institute of Advanced Industrial Science and Technology, Japan. All rights reserved.
 */

package jp.go.aist.rachwal.rtmclient.web;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.util.Base64OutputStream;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import java.io.ByteArrayOutputStream;

import jp.go.aist.rachwal.rtmclient.R;
import roboguice.inject.InjectResource;

@Singleton
public class WebClient {

    @Inject
    private Application application;

    @InjectResource(R.string.host_key)
    private String hostKey;

    @InjectResource(R.string.port_key)
    private String portKey;

    @InjectResource(R.string.video_quality_key)
    private String videoQualityKey;

    private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private Base64OutputStream base64Stream = new Base64OutputStream(outputStream, 0);

    private DefaultHttpClient client = new DefaultHttpClient();
    private HttpPost postImageRequest;
    private boolean isTimeoutSet = false;

    public boolean initialized = false;

    public void initialize() {
        if (!isTimeoutSet) {
            setTimeout(10, 10);
        }
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(application);
            String host = preferences.getString(hostKey, "");
            String port = preferences.getString(portKey, "9000");
            String address = String.format("http://%s:%s/api/images", host, port);
            postImageRequest = new HttpPost(address);

            int quality = Integer.parseInt(preferences.getString(videoQualityKey, "1"));
            setVideoQuality(quality);

            initialized = true;
        } catch (Exception e) {
            initialized = false;
            e.printStackTrace();
        }
    }

    private void setTimeout(int connection, int socket) {

        HttpParams httpParameters = client.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, connection * 1000);
        HttpConnectionParams.setSoTimeout(httpParameters, socket * 1000);
        isTimeoutSet = true;
    }

    private int videoQuality = 50;

    private void setVideoQuality(int quality) {
        if (quality == 0) {
            videoQuality = 25;
        } else if (quality == 2) {
            videoQuality = 75;
        } else {
            videoQuality = 50;
        }
    }

    public void postImage(byte[] data, Camera.Size size, WebClientCallback callback) {
        try {

            YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
            image.compressToJpeg(new Rect(0, 0, size.width, size.height), videoQuality, base64Stream);
            base64Stream.close();

            String encodedImage = String.format("{ \"data\" : \"%s\" }", outputStream.toString());
            outputStream.reset();

            StringEntity entity = new StringEntity(encodedImage);
            entity.setContentType("application/json");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));

            postImageRequest.setEntity(entity);
            client.execute(postImageRequest);
            callback.getResponse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
