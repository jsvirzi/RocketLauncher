package com.mam.lambo.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by jsvirzi on 12/2/16.
 */

public class Utils {
    static final String TAG = "Utils";

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd-HH-mm-ss");;

    public static String humanReadableTime(long timestamp) {
        return simpleDateFormat.format(new Date(timestamp));
    }

    public static String humanReadableTime() {
        long timestamp = System.currentTimeMillis();
        return humanReadableTime(timestamp);
    }

    public static void post(Handler handler, Runnable runnable, boolean sync) {
        if ((handler == null) || (runnable == null)) {
            return;
        }
        if (sync) {
            handler.post(runnable);
        } else {
            runnable.run();
        }
    }

    public static void wait(int delay) {
        try {
            Thread.sleep(delay);
        } catch (InterruptedException ex) {
            Log.e(TAG, "InterruptedException caught in sleep(). Really?", ex);
        }
    }

    /* terminate thread and return once thread has died */
    public static void goodbyeThread(HandlerThread thread) {
        String msg;
        Thread currentThread = Thread.currentThread();
        if (currentThread.getId() == thread.getId()) {
            msg = String.format("attempt to kill thread(name=%s,id=%s) from same thread", thread.getName(), thread.getId());
            Log.d(TAG, msg);
            return;
        }
        thread.quitSafely();
        try {
            thread.join();
        } catch (InterruptedException ex) {
            msg = String.format("exception closing thread %s", thread.getName());
            Log.e(TAG, msg, ex);
        }
    }

    public static int atoi(String string, int defaultValue) {
        int value = defaultValue;
        try {
            value = Integer.valueOf(string);
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
        return value;
    }

    public static List<InetAddress> getIpAddresses() {
        List<InetAddress> addresses = new ArrayList<>(1);
        try {
            Enumeration<NetworkInterface> enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (enumNetworkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = enumNetworkInterfaces.nextElement();
                Enumeration<InetAddress> enumInetAddress = networkInterface.getInetAddresses();
                while (enumInetAddress.hasMoreElements()) {
                    InetAddress inetAddress = enumInetAddress.nextElement();
                    if (inetAddress.isSiteLocalAddress()) {
                        addresses.add(inetAddress);
                        String msg = "Server running at : " + inetAddress.getHostAddress();
                        Log.d(TAG, msg);
                    }
                }
            }

        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return addresses;
    }

    public static InetAddress getIpAddress() {
        List<InetAddress> addresses = getIpAddresses();
        if (addresses.size() == 0) return null;
        return addresses.get(0);
    }

    public static BufferedWriter getBufferedWriter(String filename, int length) {
        return getBufferedWriter(filename, length, false);
    }

    public static BufferedWriter getBufferedWriter(String filename, int length, boolean append) {
        String msg;
        FileOutputStream outputStream = null;
        BufferedWriter writer = null;
        File file = new File(filename);
        return getBufferedWriter(file, length, append);
    }

    public static BufferedWriter getBufferedWriter(File file, int length, boolean append) {
        String msg;
        FileOutputStream outputStream = null;
        BufferedWriter writer = null;

        try {
            outputStream = new FileOutputStream(file, append);
        } catch (FileNotFoundException ex) {
            msg = String.format("unable to create file [%s]", file.getPath());
            Log.e(TAG, msg, ex);
        }

        if (outputStream == null) return writer;

        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
        writer = new BufferedWriter(outputStreamWriter, length);
        if (writer == null) {
            msg = String.format("unable to create file [%s]", file.getPath());
            Log.e(TAG, msg);
        }

        return writer;
    }

    public static boolean writeLine(BufferedWriter bufferedWriter, String line) {
        try {
            bufferedWriter.write(line);
        } catch (IOException ex) {
            String msg = String.format("IOException writing to file [%s]", bufferedWriter.toString());
            Log.e(TAG, msg, ex);
            return false;
        }
        return true;
    }

    public static void closeStream(BufferedWriter writer) {
        String msg;
        if (writer == null) {
            return;
        }
        try {
            writer.flush();
        } catch (IOException ex) {
            msg = String.format("IOException closing file [%s]", writer.toString());
            Log.e(TAG, msg, ex);
        } finally {
            closeSilently(writer);
            writer = null;
        }
    }

    public static boolean closeSilently(Closeable closeable) {
        boolean succeeded;
        try {
            if (closeable != null) {
                closeable.close();
            }
            succeeded = true;
        } catch (Exception exc) {
            Log.w(TAG, "Exception silenced during a close() call.", exc);
            succeeded = false;
        }
        return succeeded;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static void checkPermissions(Activity activity) {
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        final int MY_REQUEST_CODE = 0;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            boolean cameraOk = activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED;
            boolean sdOk = activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED;
            if (!cameraOk || !sdOk) {
                activity.requestPermissions(new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE,}, MY_REQUEST_CODE);
            }
        }
    }

    public static short[] readFileShorts(String filename, int skip) {
        File file = new File(filename);
        int size = (int) file.length() / 2 - skip;
        short[] shorts = new short[size];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            DataInputStream dataInputStream = new DataInputStream(bufferedInputStream);
            for (int i = 0; i < skip; i++) {
                dataInputStream.readShort();
            }
            for (int i = 0; i < size; i++) {
                shorts[i] = dataInputStream.readShort();
                byte b1 = (byte) (shorts[i] & 0xff);
                byte b2 = (byte) ((shorts[i] >> 8) & 0xff);
                short a = b1;
                a = (short) (a << 8);
                shorts[i] = (short) (a & 0xff00);
                a = b2;
                a = (short) (a & 0xff);
                shorts[i] = (short) (shorts[i] + a);
            }
            dataInputStream.close();
        } catch (FileNotFoundException ex) {
            String msg = String.format("error accessing file [%s]", filename);
            Log.e(TAG, msg, ex);
            return null;
        } catch (IOException ex) {
            String msg = String.format("error reading from file [%s]", filename);
            Log.e(TAG, msg, ex);
            return null;
        }
        return shorts;
    }

    public static short[] duplicateShortArray(short[] iArray, int multiplicity) {
        return duplicateShortArray(iArray, multiplicity, 0);
    }

    public static short[] duplicateShortArray(short[] iArray, int multiplicity, int gapLength) {
        int i, j, index = 0;
        short[] oArray = new short[(iArray.length + gapLength) * multiplicity];
        for(j=0;j<multiplicity;++j) {
            for (i = 0; i < iArray.length; ++i) {
                oArray[index] = iArray[i];
                ++index;
            }
            for (i = 0; i < gapLength; ++i) {
                oArray[index] = 0;
                ++index;
            }
        }
        return oArray;
    }

    public static byte[] readFileBytes(String filename) {
        File file = new File(filename);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(bytes);
            bufferedInputStream.close();
        } catch (FileNotFoundException ex) {
            String msg = String.format("error accessing file [%s]", filename);
            Log.e(TAG, msg, ex);
            return null;
        } catch (IOException ex) {
            String msg = String.format("error reading from file [%s]", filename);
            Log.e(TAG, msg, ex);
            return null;
        }
        return bytes;
    }

    public static byte[] duplicateByteArray(byte[] iArray, int multiplicity) {
        return duplicateByteArray(iArray, multiplicity, 0);
    }

    public static byte[] duplicateByteArray(byte[] iArray, int multiplicity, int gapLength) {
        int i, j, index = 0;
        byte[] oArray = new byte[(iArray.length + gapLength) * multiplicity];
        for(j=0;j<multiplicity;++j) {
            for (i = 0; i < iArray.length; ++i) {
                oArray[index] = iArray[i];
                ++index;
            }
            for (i = 0; i < gapLength; ++i) {
                oArray[index] = 0;
                ++index;
            }
        }
        return oArray;
    }
}
