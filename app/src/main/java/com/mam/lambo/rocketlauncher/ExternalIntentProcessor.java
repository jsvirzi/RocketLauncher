package com.mam.lambo.rocketlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by jsvirzi on 1/17/17.
 */

public class ExternalIntentProcessor extends BroadcastReceiver {

    private static final String TAG = "ExternalIntentProcessor";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction() == "com.nautobahn.hereiam") {
            Log.d(TAG, "received intent: com.nautobahn.hereiam");
            MainActivity mainActivity = MainActivity.mainActivity;
            if (mainActivity != null) {
                mainActivity.rotateLed();
            }
        }
    }
}
