package com.example.shakeshake;

import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.ConnectionService;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements ServiceConnection{
float screenWidth;
float screenHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_main);

//        Button  btn_auto_click = findViewById(R.id.btn_auto_click);
//        btn_auto_click.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                new Thread(){
//                    @Override
//                    public void run() {
//                        super.run();
//                        autoClick();
//
//                    }
//                }.start();
//
//
//            }
//        });


        Display defaultDisplay = getWindow().getWindowManager().getDefaultDisplay();
        DisplayMetrics outMetrics=new DisplayMetrics();
        defaultDisplay.getMetrics(outMetrics);
        screenWidth=outMetrics.widthPixels;
        screenHeight=outMetrics.heightPixels;
//
//        x=screenWidth/2;
//        y=screenHeight*1999/2000;
//
//        Button btn_click=findViewById(R.id.btn_click);
//        btn_click.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                click();
//            }
//        });


        Intent shakeService=new Intent(MainActivity.this,ShakeService.class);
        shakeService.putExtra("screenWidth",screenWidth);
        shakeService.putExtra("screenHeight",screenHeight);
        startService(shakeService);

    }

//float x;
//    float y;
//    public void autoClick(){
//            Instrumentation instrumentation=new Instrumentation();
//            instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
//                    SystemClock.uptimeMillis(),MotionEvent.ACTION_DOWN,
//                    x,y,0));
//            instrumentation.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
//                    SystemClock.uptimeMillis(),MotionEvent.ACTION_UP,
//                x,y,0));
//    }
//
//public void click(){
//    Toast.makeText(this,"自动点击",Toast.LENGTH_SHORT).show();
//}

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

    }

    @Override
    public void onServiceDisconnected(ComponentName name) {

    }
}
