package mx.digitalcoaster.bbva_ingenico.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;

//import com.crashlytics.android.Crashlytics;

import java.util.Timer;
import java.util.TimerTask;

//import info.androidhive.materialdesign.R;
//import io.fabric.sdk.android.Fabric;
import mx.digitalcoaster.bbva_ingenico.R;

public class SplashScreenActivity extends AppCompatActivity {

    private static final long SPLASH_SCREEN_DELAY = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        // Set portrait orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Hide title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash_screen);

        TimerTask task = new TimerTask () {
            @Override
            public void run() {

                // Start the next activity
                Intent mainIntent = new Intent ().setClass( SplashScreenActivity.this, LoginActivity.class);
                startActivity(mainIntent);

                // Close the activity so the user won't able to go back this
                // activity pressing Back button
                finish();
            }
        };

        // Simulate a long loading process on application startup.
        Timer timer = new Timer ();
        timer.schedule(task, SPLASH_SCREEN_DELAY);
    }
}
