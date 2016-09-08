package com.admin.ibeaconmeasure;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedList;


public class MainActivity extends Activity implements View.OnClickListener {


    public static final String DEFAULT_UUID_FOR_BEACON = "4CDBC040-657A-4847-B266-7E31D9E2C3D9";

    private LinkedList<IBeacon> mValueQueue = new LinkedList<>();

    private ArrayList<Integer> mData = new ArrayList<>();

    private LinkedList<Integer> mRssiQueue = new LinkedList<>();

    private BluetoothAdapter mBluetoothAdapter;

    private Button start, stop;

    private TextView mShow;

    private CircleView mCircle = null;

    private int oldRssi = 0;

    private int count = 0;

    private boolean isShow = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        start = (Button) findViewById(R.id.start);
        start.setOnClickListener(this);

        stop = (Button) findViewById(R.id.stop);
        stop.setOnClickListener(this);

        mCircle = (CircleView) findViewById(R.id.circle);

        mShow = (TextView) findViewById(R.id.show);

        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context
                .BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBluetooth, 1);
        }


    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback
            () {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi,
                             final byte[] scanRecord) {


            int startByte = 2;
            boolean patternFound = false;

            while (startByte <= 5) {

                if (((int) scanRecord[startByte + 2] & 0xff) == 0x02 && // Identifies an iBeacon
                        ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { // Identifies correct
                    // data length
                    patternFound = true;
                    break;
                }
                startByte++;
            }

            if (patternFound) {
                byte[] uuidBytes = new byte[16];
                System.arraycopy(scanRecord, startByte + 4, uuidBytes, 0, 16);
                String hexString = bytesToHex(uuidBytes);
                String uuid = hexString.substring(0, 8) + "-"
                        + hexString.substring(8, 12) + "-"
                        + hexString.substring(12, 16) + "-"
                        + hexString.substring(16, 20) + "-"
                        + hexString.substring(20, 32);

                if (uuid.equals(DEFAULT_UUID_FOR_BEACON)) {

                    int major = (scanRecord[startByte + 20] & 0xff) * 0x100
                            + (scanRecord[startByte + 21] & 0xff);

                    int minor = (scanRecord[startByte + 22] & 0xff) * 0x100
                            + (scanRecord[startByte + 23] & 0xff);

                    String ibeaconName = device.getName();
                    String mac = device.getAddress();
                    int txPower = (scanRecord[startByte + 24]);

                    if (minor == 2) {
                        System.out.println("-->>callback =" + mRssiQueue.size());

                        if (count++ < 3) {
                            return;
                        }
                        int sum1 = 0;
                        if (mData.size() == 30) {
                            for (int i : mData) {
                                sum1 = sum1 + i;
                            }
                            isShow = true;
                        } else if (mData.size() < 30) {
                            mData.add(rssi);
                        }


                        if (oldRssi == 0) {
                            oldRssi = rssi;
                        } else if (oldRssi + 20 < rssi || oldRssi - 20 >=
                                rssi) {
                            return;
                        } else {
                            oldRssi = rssi;
                        }

                        if (mRssiQueue.size() >= 10) {
                            mRssiQueue.remove(0);
                        }
                        mRssiQueue.add(rssi);
                        int sumRssi = 0;
                        for (int i : mRssiQueue) {
                            sumRssi = sumRssi + i;

                        }

                        if (mValueQueue.size() >= 10) {
                            mValueQueue.remove(0);
                        }

                        IBeacon bc = new IBeacon();
                        bc.setUuid(uuid);
                        bc.setName(ibeaconName);
                        bc.setMac(mac);
                        bc.setMajor(major);
                        bc.setMinor(minor);
                        bc.setTxPower(txPower);
                        bc.setRssi(rssi);
                        bc.setDistance(calculateAccuracy(txPower, rssi));
                        mValueQueue.add(bc);
                        double sum = 0;
                        for (IBeacon ibc : mValueQueue) {
                            sum = sum + ibc.getDistance();
                        }

                        System.out.println("-->>avr dis = " + sum / mValueQueue.size());
                        System.out.println("-->>array =" + mRssiQueue.toString());
                        System.out.println("-->>avr rssi=" + sumRssi / mRssiQueue.size());
                        System.out.println("-->>dis = " + calculateAccuracy(txPower, sumRssi /
                                mRssiQueue.size()));
                        mShow.setText("");
                        if (isShow) {
                            mShow.append("\n sample:" + sum1 / mData.size());
                            System.out.println("-->>result=" + sum1 / mData.size());
                        }
                        mShow.append("\n array:" + mRssiQueue.toString());
                        mShow.append("\n rssi:" + rssi);
                        mShow.append("\n avr rssi:" + sumRssi / mRssiQueue.size());
                        mShow.append("\n avr dis:" + sum / mValueQueue.size());
                        mShow.append("\n dis:" + calculateAccuracy(txPower, sumRssi / mRssiQueue
                                .size()) + "\n\n\n\n\n\n\n");
                        mCircle.setRadius(calculateAccuracy(txPower, sumRssi / mRssiQueue.size()));
                        mCircle.invalidate();

                    }


                }


            }
        }
    };
    static final char[] hexArray = "0123456789ABCDEF".toCharArray();

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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:


                mBluetoothAdapter.enable();
                mBluetoothAdapter.startLeScan(mLeScanCallback);
                Toast.makeText(this, "start scan", Toast.LENGTH_SHORT).show();
                break;

            case R.id.stop:
                mBluetoothAdapter.disable();
                Toast.makeText(this, "stop scan", Toast.LENGTH_SHORT).show();
                mBluetoothAdapter.stopLeScan(null);
                break;

        }
    }


    @Override
    protected void onResume() {
        super.onResume();


    }
}
