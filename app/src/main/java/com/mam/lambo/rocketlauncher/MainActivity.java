package com.mam.lambo.rocketlauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.mam.lambo.utils.CsvFile;
import com.mam.lambo.utils.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class MainActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "RocketLauncher";
    private CheckBox checkBoxAutoLaunchNautobahn;
    private CheckBox checkBoxNightModeNautobahn;
    private CheckBox checkBoxHdModeNautobahn;
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

    class NautobahnConfigurationParameters {
        int resolution; /* 720 = 1280x720, 1080 = 1920x1080 */
        int nightMode; /* 0 = normal mode, 1 = night mode */
        int autoLaunch; /* 0 = no autolaunch, 1 = autolaunch */
        static final String resolutionString = "resolution";
        static final String nightModeString = "night";
        static final String autoLaunchString = "autolaunch";

        NautobahnConfigurationParameters() {
            resolution = 0;
            nightMode = 0;
        }
    };

    NautobahnConfigurationParameters nautobahnConfigurationParameters;

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
        writeNautobahnConfigurationFile(nautobahnFile, nautobahnConfigurationParameters);
        intent.putExtra(nautobahnConfigurationParameters.resolutionString, nautobahnConfigurationParameters.resolution);
        intent.putExtra(nautobahnConfigurationParameters.nightModeString, nautobahnConfigurationParameters.nightMode);
        startActivity(intent);
    }

    public void stopNautobahn() {
        Intent intent = new Intent();
        intent.setAction("com.nauto.dogfood.stop");
        String msg = String.format("sending intent to Nautobahn");
        Log.d(TAG, msg);
        context.sendBroadcast(intent);
    }

    private void writeNautobahnConfigurationFile(File file, NautobahnConfigurationParameters nautobahnConfigurationParameters) {
        BufferedWriter writer = Utils.getBufferedWriter(file, 1024, false);
        String line = String.format("%s,%d\n", nautobahnConfigurationParameters.resolutionString, nautobahnConfigurationParameters.resolution);
        Utils.writeLine(writer, line);
        line = String.format("%s,%d\n", nautobahnConfigurationParameters.nightModeString, nautobahnConfigurationParameters.nightMode);
        Utils.writeLine(writer, line);
        line = String.format("%s,%d\n", nautobahnConfigurationParameters.autoLaunchString, nautobahnConfigurationParameters.autoLaunch);
        Utils.writeLine(writer, line);
        Utils.closeStream(writer);
    }

    NautobahnConfigurationParameters readNautobahnConfigurationFile(File file) {
        NautobahnConfigurationParameters parameters = new NautobahnConfigurationParameters();
        CsvFile csvFile = null;
        try {
            csvFile = new CsvFile(file.getPath());
            List<String[]> results = csvFile.read();
            for (String[] strings : results) {
                if(strings.length <= 1) {
                    continue;
                }
                if (strings[0].equals(parameters.nightModeString)) {
                    parameters.nightMode = Utils.atoi(strings[1], 0);
                } else if (strings[0].equals(parameters.resolutionString)) {
                    parameters.resolution = Utils.atoi(strings[1], 0);
                } else if (strings[0].equals(parameters.autoLaunchString)) {
                    parameters.autoLaunch = Utils.atoi(strings[1], 0);
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return parameters;
    }

    private boolean autoLaunchingNautobahn() {
        return (nautobahnConfigurationParameters.autoLaunch == 1);
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.stop();
        mediaPlayer.release();
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

        /* read out any existing configuration parameters */
        nautobahnConfigurationParameters = readNautobahnConfigurationFile(nautobahnFile);

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
        checkBoxNightModeNautobahn.setChecked(nautobahnConfigurationParameters.nightMode != 0);
        checkBoxNightModeNautobahn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nautobahnConfigurationParameters.nightMode = checkBoxNightModeNautobahn.isChecked() ? 1 : 0;
            }
        });

        checkBoxHdModeNautobahn = (CheckBox) findViewById(R.id.hdModeNautobahn);
        checkBoxHdModeNautobahn.setChecked(nautobahnConfigurationParameters.resolution == 1080);
        checkBoxHdModeNautobahn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nautobahnConfigurationParameters.resolution = checkBoxHdModeNautobahn.isChecked() ? 1080 : 720;
            }
        });

        checkBoxAutoLaunchNautobahn = (CheckBox) findViewById(R.id.autoLaunchNautobahn);
        checkBoxAutoLaunchNautobahn.setChecked(nautobahnConfigurationParameters.autoLaunch == 1);
        checkBoxAutoLaunchNautobahn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nautobahnConfigurationParameters.autoLaunch = checkBoxAutoLaunchNautobahn.isChecked() ? 1 : 0;
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
