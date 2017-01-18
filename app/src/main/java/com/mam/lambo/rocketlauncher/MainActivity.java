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
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.nauto.apis.INautoAPIManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

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
    private INautoAPIManager apiService;
    private Context context;
    public static MainActivity mainActivity;

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
//        intent.putExtra("state", stateString);
//        intent.putExtra("level", level);
        context.sendBroadcast(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        File directory = getExternalFilesDir(null);
        File file = new File(directory, nautobahnFilename);
        if (file.exists()) {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            MediaPlayer mp = MediaPlayer.create(context, notification);
            mp.start();
            startNautobahn();
        }
    }

    private int ledPhase = 0;
    public void rotateLed() {
        switch (ledPhase) {
            case 0:
                try {
                    apiService.setLedRGBLevel(0, 255, 0, 0);
                    apiService.setLedRGBLevel(1, 0, 255, 0);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                ++ledPhase;
                ledPhase = (ledPhase + 1) % 3;
                break;
            case 1:
                try {
                    apiService.setLedRGBLevel(0, 0, 255, 0);
                    apiService.setLedRGBLevel(1, 0, 0, 255);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                ++ledPhase;
                ledPhase = (ledPhase + 1) % 3;
                break;
            case 2:
                try {
                    apiService.setLedRGBLevel(0, 0, 0, 255);
                    apiService.setLedRGBLevel(1, 255, 0, 0);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
                ++ledPhase;
                ledPhase = (ledPhase + 1) % 3;
                break;
            default:
                ledPhase = 0;
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();

        mainActivity = this;

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        MediaPlayer mp = MediaPlayer.create(context, notification);
        mp.start();

        Intent intent = new Intent("com.nauto.apis.NautoAPIManager.BIND_SERVICE")
            .setClassName("com.nauto.apis", "com.nauto.apis.NautoAPIManager");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        buttonLed = (Button) findViewById(R.id.led);
        buttonLed.setTag(1);
        buttonLed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int action = (int) buttonLed.getTag();
                if (action == 1) {
                    try {
                        apiService.setLedRGBLevel(0, 255, 0, 0);
                        apiService.setLedRGBLevel(1, 255, 0, 0);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                    buttonLed.setTag(2);
                } else if (action == 2) {
                    try {
                        apiService.setLedRGBLevel(0, 0, 255, 0);
                        apiService.setLedRGBLevel(1, 0, 255, 0);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
                    buttonLed.setTag(3);
                } else if (action == 3) {
                    try {
                        apiService.setLedRGBLevel(0, 0, 0, 255);
                        apiService.setLedRGBLevel(1, 0, 0, 255);
                    } catch (RemoteException ex) {
                        ex.printStackTrace();
                    }
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
        try {
            apiService.setFanRPM(200);
        } catch (RemoteException exc) {
            Log.e(TAG, "Caught exception when registering API service", exc);
        }
    }

    private void stopFan() {
        try {
            apiService.setFanRPM(0);
        } catch (RemoteException exc) {
            Log.e(TAG, "Caught exception when registering API service", exc);
        }
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            apiService = INautoAPIManager.Stub.asInterface(service);
            try {
                apiService.setFanRPM(200);
                apiService.setLedRGBLevel(0, 0, 255, 0);
                apiService.setLedRGBLevel(1, 0, 0, 255);
            } catch (RemoteException exc) {
                Log.e(TAG, "Caught exception when registering API service", exc);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            apiService = null;
        }
    };
}
