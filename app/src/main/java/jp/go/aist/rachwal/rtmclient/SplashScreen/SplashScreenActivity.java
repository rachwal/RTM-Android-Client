package jp.go.aist.rachwal.rtmclient.SplashScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import jp.go.aist.rachwal.rtmclient.R;
import jp.go.aist.rachwal.rtmclient.preview.PreviewActivity;
import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;

@ContentView(R.layout.activity_splash_screen)
public class SplashScreenActivity extends RoboActivity {

    private static boolean activated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!activated) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startPreviewActivity();
                    activated = true;
                }
            }, 2000);
        } else {
            startPreviewActivity();
        }
    }

    private void startPreviewActivity() {
        startActivity(new Intent(this, PreviewActivity.class));
    }
}
