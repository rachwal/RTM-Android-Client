/*
 * Created by Bartosz Rachwal.
 * Copyright (c) 2015 The National Institute of Advanced Industrial Science and Technology, Japan. All rights reserved.
 */

package jp.go.aist.rachwal.rtmclient.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;

import com.google.inject.Inject;

import jp.go.aist.rachwal.rtmclient.R;
import jp.go.aist.rachwal.rtmclient.camera.RTMCamera;
import roboguice.fragment.provided.RoboPreferenceFragment;
import roboguice.inject.InjectResource;

public class SettingsFragment extends RoboPreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    private RTMCamera camera;

    @InjectResource(R.string.streaming_key)
    private String streamingKey;

    @InjectResource(R.string.camera_key)
    private String cameraKey;

    @InjectResource(R.string.video_size_key)
    private String videoSizeKey;

    @InjectResource(R.string.video_quality_key)
    private String videoQualityKey;

    @InjectResource(R.string.host_key)
    private String hostKey;

    @InjectResource(R.string.port_key)
    private String portKey;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] keys;

        if (camera.getNumberOfCameras() ==  1) {
            addPreferencesFromResource(R.xml.settings_one_camera);
            keys = new String[]{ videoSizeKey, videoQualityKey, hostKey, portKey };
        } else {
            addPreferencesFromResource(R.xml.settings);
            keys = new String[]{ cameraKey, videoSizeKey, videoQualityKey, hostKey, portKey };
        }

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

        for (int i = 0; i < keys.length; i++) {
           updateSummary(sharedPreferences, keys[i]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        updateSummary(sharedPreferences, key);
    }

    private void updateSummary(SharedPreferences sharedPreferences, String key) {

        if (key.equals(streamingKey)) {
            return;
        }

        if (key.equals(hostKey) || key.equals(portKey)) {
            Preference preference = findPreference(key);
            preference.setSummary(sharedPreferences.getString(key, ""));
            return;
        }

        if (key.equals(cameraKey) || key.equals(videoSizeKey) || key.equals(videoQualityKey)) {
            ListPreference preference = (ListPreference)findPreference(key);
            preference.setSummary(preference.getEntry());
            return;
        }
    }
}
