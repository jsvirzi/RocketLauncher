package com.mam.lambo.rocketlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * Created by jsvirzi on 1/16/17.
 */

public class StartMyActivityAtBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        MediaPlayer mediaPlayer = new MediaPlayer();
        File dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
            String filePath = String.format("%s/no-trespassing.mp3", dirPath);
            mediaPlayer.reset();
            try {
                mediaPlayer.setDataSource(filePath);
                mediaPlayer.prepare();
                mediaPlayer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
            RocketLauncher rocketLauncher = RocketLauncher.getInstance();
            rocketLauncher.incrementOnBootIntentsReceived();
            Intent myStarterIntent = new Intent(context, MainActivity.class);
            myStarterIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(myStarterIntent);
        }
    }
}
