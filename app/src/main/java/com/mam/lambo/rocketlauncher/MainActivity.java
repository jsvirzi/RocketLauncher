package com.mam.lambo.rocketlauncher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.nauto.apis.INautoAPIManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity {

    private static final String TAG = "RocketLauncher";
    private CheckBox checkBoxAutoLaunchNautobahn;
    private Button buttonExit;
    private Button buttonStartNautobahn;
    private Button buttonStopNautobahn;
    private Button buttonStartFan;
    private Button buttonStopFan;
    private Button buttonLed;
    private String nautobahnFilename = "nautobahn.txt";
    private Context context;
    private boolean automaticBoot;
    private TextView textViewStatus;
    private int fanSpeed = 255;
    private Button buttonFanSpeed;

    public void startNautobahn() {
        Intent intent = new Intent();
        intent.setAction("com.nauto.DogFood.action.launch");
        startActivity(intent);
    }

    public void stopNautobahn() {
        Intent intent = new Intent();
        intent.setAction("com.nauto.dogfood.stop");
        String msg = String.format("sending intent to Nautobahn");
        Log.d(TAG, msg);
        context.sendBroadcast(intent);
    }

    private boolean autoLaunchingNautobahn() {
        File directory = getExternalFilesDir(null);
        File file = new File(directory, nautobahnFilename);
        try {
            String msg = String.format("looking for file [%s]", file.getCanonicalFile().toString());
            textViewStatus.setText(msg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return file.exists();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (automaticBoot && autoLaunchingNautobahn()) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            MediaPlayer mp = MediaPlayer.create(context, notification);
            mp.start();
            startNautobahn();
            textViewStatus.setText("starting nautobahn");
        } else {
            textViewStatus.setText("no autostart nautobahn");
        }
    }

    String[] sounds = new String[] {
        "may-i-have-your-attention.mp3",
        "no-trespassing.mp3",
        "beep.mp3",
        "tiny-bell.mp3",
    };

    public void playSound(int index) {
        File dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        MediaPlayer mediaPlayer = new MediaPlayer();
        String filePath = String.format("%s/%s", dirPath, sounds[index]);
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        final RocketLauncher rocketLauncher = RocketLauncher.getInstance();

        textViewStatus = (TextView) findViewById(R.id.status);

//        mainActivity = this;

        if (rocketLauncher.getOnBootIntentsReceived() == 0) {
            playSound(0);
//            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
//            MediaPlayer mp = MediaPlayer.create(context, notification);
//            mp.start();
            automaticBoot = false;
        } else {
            playSound(1);
            automaticBoot = true;
        }

        buttonFanSpeed = (Button) findViewById(R.id.fanSpeed);
        buttonFanSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rocketLauncher.setFan(fanSpeed);
                String msg = String.format("current fan speed = %d", fanSpeed);
                textViewStatus.setText(msg);
                fanSpeed -= 32;
            }
        });

        buttonLed = (Button) findViewById(R.id.led);
        buttonLed.setTag(1);
        buttonLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int action = (int) buttonLed.getTag();
                if (action == 1) {
                    rocketLauncher.setLed(0, 255, 0, 0);
                    rocketLauncher.setLed(1, 255, 0, 0);
                    buttonLed.setTag(2);
                } else if (action == 2) {
                    rocketLauncher.setLed(0, 0, 255, 0);
                    rocketLauncher.setLed(1, 0, 255, 0);
                    buttonLed.setTag(3);
                } else if (action == 3) {
                    rocketLauncher.setLed(0, 0, 0, 255);
                    rocketLauncher.setLed(1, 0, 0, 255);
                    buttonLed.setTag(1);
                }
            }
        });

        buttonStartNautobahn = (Button) findViewById(R.id.startNautobahn);
        buttonStartNautobahn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNautobahn();
            }
        });

        buttonStopNautobahn = (Button) findViewById(R.id.stopNautobahn);
        buttonStopNautobahn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopNautobahn();
            }
        });

        buttonStartFan = (Button) findViewById(R.id.startFan);
        buttonStartFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startFan();
            }
        });

        buttonStopFan = (Button) findViewById(R.id.stopFan);
        buttonStopFan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopFan();
            }
        });

        checkBoxAutoLaunchNautobahn = (CheckBox) findViewById(R.id.autoLaunchNautobahn);
        checkBoxAutoLaunchNautobahn.setChecked(autoLaunchingNautobahn());
        checkBoxAutoLaunchNautobahn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File directory = getExternalFilesDir(null);
                File file = new File(directory, nautobahnFilename);
                if (checkBoxAutoLaunchNautobahn.isChecked()) {
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(file);
                    } catch (FileNotFoundException ex) {
                        String msg = String.format("FileNotFoundException opening file [%s]", file.getName());
                        Log.e(TAG, msg, ex);
                    }
                } else {
                    file.delete();
                }
            }
        });

        buttonExit = (Button) findViewById(R.id.exit);
        buttonExit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    private void startFan() {
        RocketLauncher rocketLauncher = RocketLauncher.getInstance();
        rocketLauncher.setFan(200);
    }

    private void stopFan() {
        RocketLauncher rocketLauncher = RocketLauncher.getInstance();
        rocketLauncher.setFan(0);
    }
}
