/*
 * Copyright (C) 2016 Qisda.corp
 *
 * Version: 0.9.4
 */
package com.nauto.apis;

interface INautoAPIManagerListener {
    /**
     * Just for test
     */
    void onTestListener();
    /**
     * When 2nd g-sensor detect motion range > threshold, 
     * will report sensor data such as 1a2b3c4d5e6f hexadecimal number,
     * which represents xyz axises data containing 6 bytes,
     * and each axis containing 2 bytes
     * @param data sensor raw data of xyz axises
     */
    void onSensorData(String data);
}

