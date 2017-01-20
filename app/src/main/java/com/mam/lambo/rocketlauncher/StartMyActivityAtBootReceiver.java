package com.mam.lambo.rocketlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;

import java.io.File;
import java.io.IOException;

/**
 * Created by jsvirzi on 1/16/17.
 */

public class StartMyActivityAtBootReceiver extends BroadcastReceiver {
    private static final String TAG = "StartMyActivityAtBootReceiver";

    HandlerThread thread;
    Handler handler;

    public StartMyActivityAtBootReceiver() {
        thread = new HandlerThread(TAG);
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    @Override
    public void onReceive(final Context context, Intent intent) {

        final RocketLauncher rocketLauncher = RocketLauncher.getInstance();
        rocketLauncher.incrementOnBootIntentsReceived();

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            Runnable startNautobahnRunnable = new Runnable() {
                @Override
                public void run() {
                    MediaPlayer mediaPlayer = new MediaPlayer();
                    File dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    String filePath = String.format("%s/no-trespassing.mp3", dirPath);
                    mediaPlayer.reset();
                    try {
                        mediaPlayer.setDataSource(filePath);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    for(int i=0;i<15;++i) {
                        rocketLauncher.rotateLed();
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }
                    Intent myStarterIntent = new Intent(context, MainActivity.class);
                    myStarterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(myStarterIntent);
                }
            };
            handler.postDelayed(startNautobahnRunnable, 5000);
        }
    }

}
