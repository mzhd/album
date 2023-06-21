package com.example.shakeshake;

import android.app.Instrumentation;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

public class ShakeService extends Service {
    float screenWidth;
    float screenHeight;

    public ShakeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        //
        Log.e("onBind","onBind");


        throw new UnsupportedOperationException("Not yet implemented");


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        screenWidth = intent.getFloatExtra("screenWidth", 0);
        screenHeight = intent.getFloatExtra("screenHeight", 0);
        x=screenWidth/2;
        y=screenHeight*1999/2000;

        new Thread() {
            @Override
            public void run() {
                super.run();
                while(true){

                    try {
                        Thread.sleep(1000);
                        autoClick();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
        return super.onStartCommand(intent, flags, startId);
    }

    float x;
    float y;

    public void autoClick() {
        Instrumentation instrumentation = new Instrumentation();
        instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN,
                x, y, 0));
        instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                SystemClock.uptimeMillis(), MotionEvent.ACTION_UP,
                x, y, 0));
    }
}
