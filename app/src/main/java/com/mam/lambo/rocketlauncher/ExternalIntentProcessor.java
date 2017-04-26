package com.mam.lambo.rocketlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

/**
 * Created by jsvirzi on 1/17/17.
 */

public class ExternalIntentProcessor extends BroadcastReceiver {

    private static final String TAG = "ExternalIntentProcessor";

    private HandlerThread thread;
    private Handler handler;
    private int[] phases = new int[2]; /* one for each LED */
    private final int numberOfPhases = 5;

    public ExternalIntentProcessor() {
        for(int i=0;i<phases.length;++i) {
            phases[i] = 0;
        }
        thread = new HandlerThread(TAG);
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == "com.nautobahn.hereiam") {
            Log.d(TAG, "received intent: com.nautobahn.hereiam");
            RocketLauncher rocketLauncher = RocketLauncher.getInstance();
            if (rocketLauncher != null) {
                rocketLauncher.rotateLed();
            }
        } else if (intent.getAction() == "com.nautobahn.fanspeed") {
            Log.d(TAG, "received intent: com.nautobahn.fanspeed");
            final int fanSpeed = intent.getIntExtra("speed", -1);
            final RocketLauncher rocketLauncher = RocketLauncher.getInstance();
            if (rocketLauncher != null) {
                Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        rocketLauncher.setFan(fanSpeed);
                    }
                };
                handler.post(runnable);
            }
        } else if (intent.getAction() == "com.nautobahn.distress") {
            Log.d(TAG, "received intent: com.nautobahn.distress");
            final int level = intent.getIntExtra("level", -1);
            final RocketLauncher rocketLauncher = RocketLauncher.getInstance();
            if (rocketLauncher != null) {
                final int led = level;
                if ((led == 0) || (led == 1)) {
                    int maxLed = 25;
                    rocketLauncher.setLed(led, 0, 0, maxLed);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            rocketLauncher.setLed(led, 0, 0, 0);
                        }
                    };
                    if (handler != null) {
                        handler.postDelayed(runnable, 1000);
                    } else {
                        Log.e(TAG, "error starting thread");
                    }
                }
            }
        } else if (intent.getAction() == "com.nautobahn.status") {
            Log.d(TAG, "received intent: com.nautobahn.status");
            final int led = intent.getIntExtra("led", -1);
            final RocketLauncher rocketLauncher = RocketLauncher.getInstance();
            if (rocketLauncher != null) {
                if ((led == 0) || (led == 1)) {
                    int phase = ((0 <= phases[led]) && (phases[led] < numberOfPhases)) ? phases[led] : 0;
                    phases[led] = (phase + 1) % numberOfPhases;
                    int maxLed = 25;
                    rocketLauncher.setLed(led, 0, (phase + 1) * maxLed / numberOfPhases, 0);
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            rocketLauncher.setLed(led, 0, 0, 0);
                        }
                    };
                    if (handler != null) {
                        handler.postDelayed(runnable, 1000);
                    } else {
                        Log.e(TAG, "error starting thread");
                    }
                }
            }
        } else if (intent.getAction() == "com.nautobahn.itstoodark") {
            final RocketLauncher rocketLauncher = RocketLauncher.getInstance();
            rocketLauncher.setIRLed(true);
            int maxLed = 25;
            rocketLauncher.setLed(0, maxLed, 0, 0);
        } else if (intent.getAction() == "com.nautobahn.itslight") {
            final RocketLauncher rocketLauncher = RocketLauncher.getInstance();
            rocketLauncher.setIRLed(false);
            int maxLed = 25;
            rocketLauncher.setLed(0, 0, 0, maxLed);
        }
    }
}
