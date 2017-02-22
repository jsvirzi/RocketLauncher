package com.mam.lambo.rocketlauncher;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.nauto.apis.INautoAPIManager;

/**
 * Created by jsvirzi on 1/18/17.
 */

public class RocketLauncher extends Application {
    private static RocketLauncher instance = null;
    private static final String TAG = "RocketLauncher";
    private int onBootIntentsReceived = 0;
    private INautoAPIManager apiService;
    private boolean irFilter = false;

    public int getOnBootIntentsReceived() {
        return onBootIntentsReceived;
    }

    public void incrementOnBootIntentsReceived() {
        ++onBootIntentsReceived;
    }

    public RocketLauncher() {
        instance = this;
    }

    public static RocketLauncher getInstance() {
        if (instance == null) {
            instance = new RocketLauncher();
        }
        return instance;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate(). launched");

        Intent intent = new Intent("com.nauto.apis.NautoAPIManager.BIND_SERVICE")
            .setClassName("com.nauto.apis", "com.nauto.apis.NautoAPIManager");
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            apiService = INautoAPIManager.Stub.asInterface(service);
            try {
                apiService.setFanRPM(0);
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

    public void setIRLed(boolean mode) {
        try {
            if (mode && (irFilter == false)) {
                apiService.setIRCutoffFilterMode(1);
                apiService.setIRLedLevel(200);
                irFilter = true;
            } else if ((mode == false) && irFilter) {
                apiService.setIRCutoffFilterMode(0);
                apiService.setIRLedLevel(0);
                irFilter = false;
            }
        } catch (RemoteException ex) {
            ex.printStackTrace();
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

    public void setLed(int led, int r, int g, int b) {
        try {
            apiService.setLedRGBLevel(led, r, g, b);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    public void setFan(int rpm) {
        try {
            apiService.setFanRPM(rpm);
        } catch (RemoteException ex) {
            Log.e(TAG, "Caught RemoteException when registering API service", ex);
        }
    }
}
