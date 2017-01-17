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
     * @param index which RGB led.
     * @param levelR red led value, 0 ~ 255.
     * @param levelG grenn led value, 0 ~ 255.
     * @param levelB blue RGB value, 0 ~ 255.
     */
    void setLedRGBLevel(int index, int levelR, int levelG, int levelB);

    /**
     * Set IR led level.
     * @param level IR led value, 0 ~ 200, 0 means off.
     */
    void setIRLedLevel(int level);
    
    /**
     * Set IR cutoff mode.
     * @param mode {@code 0} normal mode, {@code 1} night mode.
     */
    void setIRCutoffFilterMode(int mode);
    
    /**
     * Get current fan speed (rpm: round per minute).
     * @return The fan speed.
     */
    int getFanRPM();
    
    /**
     * Set fan speed.
     * @param rpm round per minute. 
     */
    void setFanRPM(int rpm);

    /**
     * Get the ambient temperature.
     * @return The ambient temperature using Celsius.
     */
    float getAmbientTemperature();

    /**
     * Start the coulomb counter.
     */
    void startCoulombCounter();

    /**
     * Reset the coulomb counter.
     */
    void resetCoulombCounter();

    /**
     * Get coulomb count form coulomb counter
     * @return The coulomb count.
     */
    int getCoulombCount();

    /**
     * Set WOM threshold.
     * The register holds the threshold value of Wake on Motion interrupt for accelerometer.
     * @param threshold Threshold value, 0 ~ 255.
     */
    void setWOMThreshold(int threshold);

    /**
     * Set WOA threshold.
     * @param threshold Threshold value, 0 ~ 255.
     */
    void setWOAThreshold(int threshold);

    /**
     * Get vehicle battery voltage from DC2DC convertor
     * @return The vehicle battery voltage.
     */
    float getVehicleBatteryVoltage();
    
    /**
     * Get backup battery voltage
     * @return The backup battery voltage.
     */
    float getBackupBatteryVoltage();
    
    /**
     * Switch from otg mode to clint mode, need reboot the device
     * @param mode {@code 0} otg mode, {@code 1} client mode.
     */
    void swtichUsbMode(int mode);
    
    /**
     * Get SD card serial number
     * @return The SD card serial number.
     */
    int getSDSerialNumber();
    
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
     * Get IMU register value
     * @return The IMU register value.
     */
    int getIMURegisterValue(int address);
    
    /**
     * Get IRQ register status
     * @return The IRQ register status.
     */
    int getIRQRegisterStatus(int address);
    
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
     * @param power {@code true} to turn on, {@code false} to turn off.
     */
    void setRadioPower(boolean power);
}

