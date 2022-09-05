package com.devlouix.tiltawake;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.devlouix.tiltawake.services.ForegroundService;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    Button startButton;
    CheckBox enableVibrator;
    TextView helpText;
    String v;
    AdView mAdView;

    public void setV(String v) {
        this.v = v;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        helpText = findViewById(R.id.help_text);


        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        ActivityManager activityManager = (ActivityManager)getSystemService(Context.ACTIVITY_SERVICE);
        if(!IsRunning()){
            if(activityManager!= null){
                List<ActivityManager.AppTask> runningApps = activityManager.getAppTasks();
                if(runningApps != null && runningApps.size() > 0){
                    runningApps.get(0).setExcludeFromRecents(true);
                }
            }
        }

        enableVibrator = findViewById(R.id.enable_vibrator);
        enableVibrator.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(compoundButton.isChecked()){
                    setV("VIBRATE");
                    Toast.makeText(MainActivity.this, "Vibrate on awake ON!", Toast.LENGTH_SHORT).show();
                }else if(!compoundButton.isChecked()){
                    setV("");
                    Toast.makeText(MainActivity.this, "Vibrate on awake OFF!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        startButton = findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                Intent startServiceIntent = new Intent(MainActivity.this, ForegroundService.class);
                startServiceIntent.setAction("START");

                Intent stopServiceIntent = new Intent(MainActivity.this, ForegroundService.class);
                stopServiceIntent.setAction("STOP");

                if(IsRunning()){
                    helpText.setText("Click the start button to Tilt Awake your Device");
                    enableVibrator.setEnabled(true);
                    startButton.setText("START");
                    startButton.setBackgroundResource(R.drawable.btn1);
                    startService(stopServiceIntent);
                    Toast.makeText(MainActivity.this, "Stopped", Toast.LENGTH_SHORT).show();
                }else if(!IsRunning()) {
                    startServiceIntent.putExtra("Vibrate",v);
                    helpText.setText("Tilt Device upwards on screen off to Awake");
                    enableVibrator.setEnabled(false);
                    startButton.setText("STOP");
                    startButton.setBackgroundResource(R.drawable.btn2);
                    startService(startServiceIntent);
                }
            }
        });
    }

    public boolean IsRunning(){
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo serviceInfo: activityManager.getRunningServices(Integer.MAX_VALUE)){
            if(ForegroundService.class.getName().equals(serviceInfo.service.getClassName())){
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}