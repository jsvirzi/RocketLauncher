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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "RocketLauncher";
    private CheckBox checkBoxAutoLaunchNautobahn;
    private CheckBox checkBoxNightModeNautobahn;
    private Button buttonExit;
    private Button buttonStartNautobahn;
    private Button buttonStopNautobahn;
    private Button buttonStartFan;
    private Button buttonStopFan;
    private Button buttonLed;
    private File nautobahnFile;
    private Context context;
    private boolean automaticBoot;
    private TextView textViewStatus;
    private int fanSpeed = 255;
    private Button buttonFanSpeed;
    MediaPlayer mediaPlayer;

    /* these need to be in lock-step with *soundIndex* */
    private final String[] sounds = new String[] {
        "sound0.mp3",
        "sound1.mp3",
        "sound2.mp3",
        "sound3.mp3",
    };

    /* these need to be in lock-step with *sounds* */
    private int[] soundIndex = new int[] {
        R.raw.sound0,
        R.raw.sound1,
        R.raw.sound2,
        R.raw.sound3
    };

    public void startNautobahn() {
        Intent intent = new Intent();
        intent.setAction("com.nauto.DogFood.action.launch");
        byte[] line = new byte[32];
        try {
            FileInputStream fileInputStream = new FileInputStream(nautobahnFile);
            int nBytes = fileInputStream.read(line);
            fileInputStream.close();
        } catch (IOException ex) {
            String msg = String.format("error reading file [%s]", nautobahnFile.getName());
            Log.e(TAG, msg, ex);
        }
        intent.putExtra("mode", line);
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
        try {
            String msg = String.format("looking for file [%s]", nautobahnFile.getCanonicalFile().toString());
            textViewStatus.setText(msg);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return nautobahnFile.exists();
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.stop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (automaticBoot && autoLaunchingNautobahn()) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            mediaPlayer = MediaPlayer.create(context, notification);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.start();
            startNautobahn();
            textViewStatus.setText("starting nautobahn");
        } else {
            textViewStatus.setText("no autostart nautobahn");
        }
    }

    public void playSound(int index) {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, soundIndex[index]);
        mediaPlayer.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        String filename = "nautobahn.txt";
        File directory = getExternalFilesDir(null);
        nautobahnFile = new File(directory, filename);

        final RocketLauncher rocketLauncher = RocketLauncher.getInstance();

        textViewStatus = (TextView) findViewById(R.id.status);

        if (rocketLauncher.getOnBootIntentsReceived() == 0) {
            playSound(0);
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
                int maxLed = 25;
                int action = (int) buttonLed.getTag();
                if (action == 1) {
                    rocketLauncher.setLed(0, maxLed, 0, 0);
                    rocketLauncher.setLed(1, maxLed, 0, 0);
                    buttonLed.setTag(2);
                } else if (action == 2) {
                    rocketLauncher.setLed(0, 0, maxLed, 0);
                    rocketLauncher.setLed(1, 0, maxLed, 0);
                    buttonLed.setTag(3);
                } else if (action == 3) {
                    rocketLauncher.setLed(0, 0, 0, maxLed);
                    rocketLauncher.setLed(1, 0, 0, maxLed);
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

        checkBoxNightModeNautobahn = (CheckBox) findViewById(R.id.nightModeNautobahn);

        checkBoxAutoLaunchNautobahn = (CheckBox) findViewById(R.id.autoLaunchNautobahn);
        checkBoxAutoLaunchNautobahn.setChecked(autoLaunchingNautobahn());
        checkBoxAutoLaunchNautobahn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkBoxAutoLaunchNautobahn.isChecked()) {
                    try {
                        FileOutputStream fileOutputStream = new FileOutputStream(nautobahnFile);
                        boolean nightMode = checkBoxNightModeNautobahn.isChecked();
                        String line = String.format("mode=%s", nightMode ? "night" : "normal");
                        fileOutputStream.write(line.getBytes());
                        fileOutputStream.flush();
                        fileOutputStream.close();
                    } catch (IOException ex) {
                        String msg = String.format("FileNotFoundException accessing file [%s]", nautobahnFile.getName());
                        Log.e(TAG, msg, ex);
                    }
                } else {
                    nautobahnFile.delete();
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
