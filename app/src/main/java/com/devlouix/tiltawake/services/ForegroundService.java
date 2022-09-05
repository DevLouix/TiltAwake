package com.devlouix.tiltawake.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.devlouix.tiltawake.MainActivity;
import com.devlouix.tiltawake.R;

import static android.content.ContentValues.TAG;

public class ForegroundService extends Service implements SensorEventListener, View.OnTouchListener {
    double X, Y, Z;
    Vibrator vibrator;
    DisplayManager displayManager;
    PowerManager.WakeLock wakeLock;
    NotificationManager manager;

    String ACTION_STOP_SERVICE = "STOP";
    String V;

    public String getV() {
        return V;
    }

    public void setV(String v) {
        V = v;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this,sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        displayManager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        wakeLock = ((PowerManager) getSystemService(Context.POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP,"TiltAwake:WakelockTag"
        );
        setV(intent.getStringExtra("Vibrate"));

        if(intent != null){
            if(intent.getAction().equals("START")){
                Toast.makeText(this, "Running", Toast.LENGTH_SHORT).show();
            }else if(intent.getAction().equals(ACTION_STOP_SERVICE)){
                stopForegroundService();
            }
        }
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent =
                PendingIntent.getActivity(this,0,notificationIntent,0);

    //        Intent stopServiceIntent = new Intent(this, MainActivity.class);
    //        stopServiceIntent.setAction(ACTION_STOP_SERVICE);

//        PendingIntent stopService = PendingIntent.getService(this,0,stopServiceIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this,"NotificationId")
                .setContentTitle("Enabled")
                .setContentText("Tilt Awake is Running")
                .setSmallIcon(R.drawable.ic_baseline_phonelink_setup_24)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1,notification);

        return START_STICKY;
    }

    private int stopForegroundService() {
        stopForeground(true);
        stopSelf();
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType()==Sensor.TYPE_ACCELEROMETER){
            X = Math.abs(sensorEvent.values[0]);
//            Y = sensorEvent.values[1];
//            Z = sensorEvent.values[2];

            for(Display display : displayManager.getDisplays()){
                if(display.getState() == Display.STATE_OFF){
                    V = getV();
                    if(X >= 9) {
                        if (V != null && V.equals("VIBRATE")) {
                            vibrator.vibrate(100);
                        }
                        wakeLock.acquire();
                        wakeLock.release();
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        return false;
    }
}
