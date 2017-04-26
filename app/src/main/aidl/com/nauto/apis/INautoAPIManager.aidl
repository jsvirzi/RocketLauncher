/*
 * Copyright (C) 2016 Qisda.corp
 *
 * Version: 0.9.5
 */
package com.nauto.apis;

import com.nauto.apis.NautoAPIManagerData;
import com.nauto.apis.INautoAPIManagerListener;

interface INautoAPIManager {
    /**
     * Get data
     * @return {@code NautoAPIManagerData}. 
     */
    NautoAPIManagerData getNautoAPIManagerData();
    
    /**
     * Register listener which used to notify the state changes
     * @param listener {@code INautoAPIManagerListener}.
     */
    void registerListener(INautoAPIManagerListener listener);

    /**
     * Unregister listener
     * @param listener {@code INautoAPIManagerListener}.
     */
    void unregisterListener(INautoAPIManagerListener listener);
    
    /**
     * Set led RGB level.
     * @param index  0 means left RGB led, 1 means right RGB led.
     * @param levelR param range: 0 ~ 25 , 0->Turn off,25->25mA;
     * @param levelG param range: 0 ~ 25 , 0->Turn off,25->25mA;
     * @param levelB param range: 0 ~ 25 , 0->Turn off,25->25mA;
     */
    void setLedRGBLevel(int index, int levelR, int levelG, int levelB);

    /**
     * Set IR led level.
     * @param level IR led value, param range:0 ~ 200, 0->Turn off;200->200mA for 1 Led,and total is 400mA for 2 IR leds
     */
    void setIRLedLevel(int level);
    
    /**
     * Set IR cutoff mode.
     * @param mode {@code 0} night mode, {@code 1} normal mode.
     */
    void setIRCutoffFilterMode(int mode);
    
    /**
     * Get current fan speed (rpm: round per minute).
     * @return The fan speed, such as 13653
     */
    int getFanRPM();
    
    /**
     * Set fan speed.
     * @param rpm rpm level, param range:0~255, 0->Turn off;255->13000RPM(Max 20% inaccuracy)
     */
    void setFanRPM(int rpm);

    /**
     * Get the ambient temperature.
     * @return The ambient temperature using Celsius.
     */
    float getAmbientTemperature();

    /**
     * Switch from otg mode to client mode, need reboot the device
     * @param mode {@code 1} otg mode, {@code 0} client mode.
     */
    void switchUsbMode(int mode);
    
    /**
     * Get SD card serial number
     * @return The SD card serial number.
     */
    String getSDSerialNumber();
    
    /**
     * The Health Status Register allows access to supplementary information about the SanDisk Industrial microSD card. 
     * Contents include items such as identifiers, health status, and version information. 
     * The SanDisk Industrial microSD card uses the SD General Command (GEN_CMD) to query the Health Status Register. 
     * To query the Health Status register, CMD56 with argument of [00 00 00 01] is used. The SD card will respond with 512 bytes. 
     * 
     * Table 1 identifies the meaning of the 512 bytes response.
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |   Byte#   |           Description           |     Number of bytes    |                                Value                               |
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |     1     |   SD Identifier                 |            2           | Hex; 0x4453                                                        |
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |     3     |   Manufacture date              |            6           | ASCII ; YYMMDD                                                     |
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |     9     |   Health Status in % used       |            1           | Hex; Calculated                                                    |
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |   10-11   |   Reserved                      |            2           | Reserved                                                           |
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |   12-13   |   Feature Revision              |            2           | Hex; Refer to Generation identifierThe generation identifier is    |
     * |                                                                      | used to track updates in the health status register implementation.|
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |    14     |                                 |            1           | Reserved                                                           |
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |    15     |   Generation Identifier         |            1           | Hex; Refer to Generation Identifier section                        |
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |   16-49   |                                 |            34          | Reserved                                                           |
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |   50-81   |   Programmable Product String   |            32          | ASCII; default set as "SanDisk" followed by 0x20 (ASCII spaces)    |
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |  82-405   |   Reserved                      |            324         | Reserved                                                           |
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |  406-411  |   Reserved                      |            6           | Reserved                                                           |
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     * |  412-512  |   Reserved                      |            99          | Reserved                                                           |
     * ---------------------------------------------------------------------------------------------------------------------------------------------
     *
     * @return The SD health status response
     */
    byte[] getSDHealthStatus();
    
    /**
     * Get running CPU cores number
     * @return The running CPU cores number.
     */
    int getRunningCpuCores();
    
    /**
     * Set running CPU cores number
     * @param coreNum Running CPU cores number.
     */
    void setRunningCpuCores(int coreNum);
    
    /**
     * Get frequencies of running CPU cores
     * @return The frequencies of running CPU cores.
     */
    int[] getRunningCpuCoresFrequencies();

    /**
     * Set frequencies of running CPU cores
     * @param frequencies Frequencies of running CPU cores.
     */
    void setRunningCpuCoresFrequencies(in int[] frequencies);
    
    /**
     * Get CPU temperature
     * @return The CPU temperature using Celsius.
     */
    float getCpuTemperature();
    
    /**
     * Get CPU core temperature
     * @return The CPU core temperature using Celsius.
     */
    float[] getCpuCoreTemperature();
    
    /**
     * Get IMU register value
     * @param address IMU register address.
     * @return The IMU register value.
     */
    byte[] getIMURegisterValue(int address);
    
    /**
     * 1st g-sensor is used for wake up system on motion event:
     * 1st g-sensor default sensitivity:  -16g ~ +16g
     * 1st g-sensor threshold_value = threshold * 0.004g (0 ~ 1.02g)
     *
     * @param threshold 0 ~ 255.
     */
    void set1stGSensorWOMThreshold(byte threshold);
    
    /**
     * It will get WOM threshold of 1st G-Sensor.
     *
     * @return The wom threshold value.
     */
    byte get1stGSensorWOMThreshold();
    
    /**
     * 2nd g-sensor is used for report sensor data to nauto2 
     * when detecting motion exceeds threshold setting.
     * 2nd g-sensor default sensitivity: -4g ~ +4g
     * 2nd g-sensor threshold_value = threshold * 0.031g ( 0 ~ 7.97g)
     *
     * @param threshold param range 0 ~ 255, -1 -> Disable 2nd g-sensor.
     */
    void set2ndGSensorMotionThreshold(byte threshold);
    
    /**
     * It will get motion threshold of 2nd G-Sensor.
     *
     * @return The motion threshold value.
     */
    byte get2ndGSensorMotionThreshold();
   
    /**
     * 1st g-sensor is used for wake up system on motion event:
     * 1st g-sensor default sensitivity:  -16g ~ +16g
     * 2nd g-sensor is used for report sensor data to nauto2
     * when detecting motion exceeds threshold setting
     * 2nd g-sensor default sensitivity:  -4g ~ +4g
     * need reboot the device after setting.
     *
     * @param index 1st g-sensor (0), 2nd g-sensor (1)
     * @param sensitivity 2g (0), 4g (1), 8g (2), 16g (3)
     */
    void setGSensorSensitivity (int index, int sensitivity);

    /**
     * Get G-sensor sensityvity
     *
     * @param index 1st g-sensor (0), 2nd g-sensor (1)
     * @return sensitivity value of g-sensor
     */
     int getGSensorSensitivity (int index);
 
    /**
     * Set WOA threshold.
     * @param threshold Threshold value,TBD.
     */
    void setWOAThreshold(int threshold);

    /**
     * Do factory reset: enter recovery mode and clear all user data.
     */
    void doFactoryReset();

    /**
     * Reboot the device.
     */
    void reboot();
    
    /**
     * Force the device to go to sleep
     */
    void goToSleep();
    
    /**
     * Turn on or off radio power.
     * @param on {@code true} to turn on, {@code false} to turn off.
     */
    void setRadioPower(boolean on);
}

