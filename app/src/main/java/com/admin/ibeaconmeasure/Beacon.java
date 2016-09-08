package com.admin.ibeaconmeasure;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Administrator on 2016/8/26 0026.
 */
public class Beacon {

    private byte[] mScanRecord = null;
    private int startByte = 2;
    private BluetoothDevice mDevice;
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static final String DEFAULT_UUID_FOR_BEACON = "4CDBC040-657A-4847-B266-7E31D9E2C3D9";


    public Beacon(byte[] scanRecord, BluetoothDevice device) {
        this.mScanRecord = scanRecord;
        this.mDevice = device;

    }

    public boolean isBeacon() {


        while (startByte <= 5) {

            if (((int) mScanRecord[startByte + 2] & 0xff) == 0x02 && // Identifies an iBeacon
                    ((int) mScanRecord[startByte + 3] & 0xff) == 0x15) { // Identifies correct
                // data length
                return true;
            }
            startByte++;
        }
        return false;
    }


    public String getUuid() {

        byte[] uuidBytes = new byte[16];
        System.arraycopy(mScanRecord, startByte + 4, uuidBytes, 0, 16);
        String hexString = bytesToHex(uuidBytes);
        String uuid = hexString.substring(0, 8) + "-"
                + hexString.substring(8, 12) + "-"
                + hexString.substring(12, 16) + "-"
                + hexString.substring(16, 20) + "-"
                + hexString.substring(20, 32);

        return uuid;
    }

    public int getMajor() {
        int major = (mScanRecord[startByte + 20] & 0xff) * 0x100
                + (mScanRecord[startByte + 21] & 0xff);
        return major;
    }


    public int getMinor() {
        int minor = (mScanRecord[startByte + 22] & 0xff) * 0x100
                + (mScanRecord[startByte + 23] & 0xff);
        return minor;
    }

    public String getBeaconName() {
        return mDevice.getName();
    }

    public String getMac() {
        return mDevice.getAddress();
    }

    public int getTxpower() {
        return (mScanRecord[startByte + 24]);
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    protected static double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0; // if we cannot determine accuracy, return -1.
        }
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

}
