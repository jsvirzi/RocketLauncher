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
import java.io.FileNotFoundException;
import java.util.List;

public class MainActivity extends Activity implements MediaPlayer.OnCompletionListener {

    private static final String TAG = "RocketLauncher";
    private CheckBox checkBoxAutoLaunchNautobahn;
    private CheckBox checkBoxAutoLaunchDistraction;
    private CheckBox checkBoxAutoLaunchImuLogger;
    private CheckBox checkBoxNightModeNautobahn;
    private CheckBox checkBoxHdModeNautobahn;
    private Button buttonExit;
    private Button buttonSave;
    private Button buttonStartNautobahn;
    private Button buttonStartDistraction;
    private Button buttonStartFan;
    private Button buttonStopFan;
    private Button buttonLed;
    private Button buttonIrLed;
    private File nautobahnFile;
    private Context context;
    private boolean automaticBoot;
    private TextView textViewStatus;
    private int fanSpeed = 255;
    private Button buttonFanSpeed;
    private static final int ActivityDogfood = 0;
    private static final int ActivityDistraction = 1;
    private static final int ActivityImuLogger = 2;

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
        int activity; /* 0 = dogfood, 1 = distraction, 2 = imu logger */
        static final String resolutionString = "resolution";
        static final String nightModeString = "night";
        static final String autoLaunchString = "autolaunch";
        static final String activityString = "activity";

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

    public void startNautobahnDistraction() {
        Intent intent = new Intent();
        intent.setAction("com.nauto.Distraction.action.launch");
        writeNautobahnConfigurationFile(nautobahnFile, nautobahnConfigurationParameters);
        intent.putExtra(nautobahnConfigurationParameters.resolutionString, nautobahnConfigurationParameters.resolution);
        intent.putExtra(nautobahnConfigurationParameters.nightModeString, nautobahnConfigurationParameters.nightMode);
        intent.putExtra(nautobahnConfigurationParameters.activityString, nautobahnConfigurationParameters.activity);
        intent.putExtra(nautobahnConfigurationParameters.autoLaunchString, nautobahnConfigurationParameters.autoLaunch);
        startActivity(intent);
    }

    public void startNautobahnDogfood() {
        Intent intent = new Intent();
        intent.setAction("com.nauto.DogFood.action.launch");
        writeNautobahnConfigurationFile(nautobahnFile, nautobahnConfigurationParameters);
        intent.putExtra(nautobahnConfigurationParameters.resolutionString, nautobahnConfigurationParameters.resolution);
        intent.putExtra(nautobahnConfigurationParameters.nightModeString, nautobahnConfigurationParameters.nightMode);
        intent.putExtra(nautobahnConfigurationParameters.activityString, nautobahnConfigurationParameters.activity);
        intent.putExtra(nautobahnConfigurationParameters.autoLaunchString, nautobahnConfigurationParameters.autoLaunch);
        startActivity(intent);
    }

    public void startNautobahnImuLogger() {
        Intent intent = new Intent();
        intent.setAction("com.nauto.ImuLogger.action.launch");
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
        line = String.format("%s,%d\n", nautobahnConfigurationParameters.activityString, nautobahnConfigurationParameters.activity);
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
                } else if (strings[0].equals(parameters.activityString)) {
                    parameters.activity = Utils.atoi(strings[1], 0);
                }
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        return parameters;
    }

    private boolean autoLaunchingNautobahnDogfood() {
        return (nautobahnConfigurationParameters.autoLaunch == 1) && (nautobahnConfigurationParameters.activity == ActivityDogfood);
    }

    private boolean autoLaunchingNautobahnDistraction() {
        return (nautobahnConfigurationParameters.autoLaunch == 1) && (nautobahnConfigurationParameters.activity == ActivityDistraction);
    }

    private boolean autoLaunchingNautobahnImuLogger() {
        return (nautobahnConfigurationParameters.autoLaunch == 1) && (nautobahnConfigurationParameters.activity == ActivityImuLogger);
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (automaticBoot) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            MediaPlayer mediaPlayer = MediaPlayer.create(context, notification);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.start();
            if (autoLaunchingNautobahnDogfood()) {
                startNautobahnDogfood();
                textViewStatus.setText("starting nautobahn dogfood");
            } else if (autoLaunchingNautobahnDistraction()) {
                startNautobahnDistraction();
                textViewStatus.setText("starting nautobahn distraction");
            } else if (autoLaunchingNautobahnImuLogger()) {
                startNautobahnImuLogger();
                textViewStatus.setText("starting nautobahn imu logger");
            }
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

        buttonIrLed = (Button) findViewById(R.id.irLed);
        buttonIrLed.setTag(1);
        buttonIrLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int action = (int) buttonIrLed.getTag();
                if (action == 1) {
                    rocketLauncher.setIRLed(true);
                    buttonIrLed.setTag(0);
                } else {
                    rocketLauncher.setIRLed(false);
                    buttonIrLed.setTag(1);
                }
            }
        });

        buttonStartNautobahn = (Button) findViewById(R.id.startNautobahn);
        buttonStartNautobahn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNautobahnDogfood();
            }
        });

        buttonStartDistraction = (Button) findViewById(R.id.startDistraction);
        buttonStartDistraction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNautobahnDistraction();
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

        checkBoxHdModeNautobahn = (CheckBox) findViewById(R.id.hdModeNautobahn);
        checkBoxHdModeNautobahn.setChecked(nautobahnConfigurationParameters.resolution == 1080);

        checkBoxAutoLaunchNautobahn = (CheckBox) findViewById(R.id.autoLaunchNautobahn);
        checkBoxAutoLaunchNautobahn.setChecked((nautobahnConfigurationParameters.activity == ActivityDogfood) && (nautobahnConfigurationParameters.autoLaunch == 1));

        checkBoxAutoLaunchDistraction = (CheckBox) findViewById(R.id.autoLaunchDistraction);
        checkBoxAutoLaunchDistraction.setChecked((nautobahnConfigurationParameters.activity == ActivityDistraction) && (nautobahnConfigurationParameters.autoLaunch == 1));

        checkBoxAutoLaunchImuLogger = (CheckBox) findViewById(R.id.autoLaunchImuLogger);
        checkBoxAutoLaunchImuLogger.setChecked((nautobahnConfigurationParameters.activity == ActivityImuLogger) && (nautobahnConfigurationParameters.autoLaunch == 1));

        buttonSave = (Button) findViewById(R.id.save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateParameters();
                writeNautobahnConfigurationFile(nautobahnFile, nautobahnConfigurationParameters);
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

    private void updateParameters() {
        nautobahnConfigurationParameters.resolution = checkBoxHdModeNautobahn.isChecked() ? 1080 : 720;
        nautobahnConfigurationParameters.nightMode = checkBoxNightModeNautobahn.isChecked() ? 1 : 0;
        nautobahnConfigurationParameters.autoLaunch = 0; /* default value */
        if (checkBoxAutoLaunchDistraction.isChecked() || checkBoxAutoLaunchNautobahn.isChecked() || checkBoxAutoLaunchImuLogger.isChecked()) {
            nautobahnConfigurationParameters.autoLaunch = 1; /* override */
            if (checkBoxAutoLaunchNautobahn.isChecked()) {
                nautobahnConfigurationParameters.activity = ActivityDogfood;
            } else if (checkBoxAutoLaunchDistraction.isChecked()) {
                nautobahnConfigurationParameters.activity = ActivityDistraction;
            } else if (checkBoxAutoLaunchImuLogger.isChecked()) {
                nautobahnConfigurationParameters.activity = ActivityImuLogger;
            }
        }
    }
}
