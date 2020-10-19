package com.ninewatt.beacon_collector.beacon;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

import androidx.annotation.NonNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BeaconData {
    private String mUuid;
    private String mName;
    private String mMac;
    private String mTime;
    private int mMajor;
    private int mMinor;
    private int mTxPower;
    private int mRssi;
    private double mAccuracy;

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getUuid() {
        return mUuid;
    }

    public void setUuid(String uuid) {
        mUuid = uuid;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getMac() {
        return mMac;
    }

    public void setMac(String mac) {
        mMac = mac;
    }

    public int getMajor() {
        return mMajor;
    }

    public void setMajor(int major) {
        mMajor = major;
    }

    public int getMinor() {
        return mMinor;
    }

    public void setMinor(int minor) {
        mMinor = minor;
    }

    public double getAccuracy() {
        return mAccuracy;
    }

    public void setAccuracy(double accuracy) {
        mAccuracy = accuracy;
    }

    public int getTxPower() {
        return mTxPower;
    }

    public void setTxPower(int power) {
        mTxPower = power;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int rssi) {
        mRssi = rssi;
    }

    public void convertBeaconData(ScanResult _result) {
        // get scanned device
        BluetoothDevice device = _result.getDevice();
        if (device == null)
            return;

        mName = device.getName();
        mMac = device.getAddress();
//            if (device.getName().equalsIgnoreCase(mSavedDevice[0]) && device.getAddress().equalsIgnoreCase(mSavedDevice[1])) {
        String major = "", minor = "", uuid = "", power = "", rssi = "";
        double accuracy = 0;
        if (_result.getScanRecord() != null) {
            byte[] record = _result.getScanRecord().getBytes();
            String[] temp = getMinorMajorUuidAccuracy(record, _result.getRssi(), device);
            if (temp != null) {
                try {
                    uuid = temp[0];
                    major = temp[1];
                    minor = temp[2];
                    power = temp[3];
                    rssi = temp[4];
                    accuracy = Double.parseDouble(temp[5]);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.sss", Locale.getDefault());
                    Date timeInDate = new Date(System.currentTimeMillis());
                    String time = sdf.format(timeInDate);

                    mUuid = uuid;
                    mMajor = Integer.parseInt(major);
                    mMinor = Integer.parseInt(minor);
                    mTxPower = Integer.parseInt(power);
                    mRssi = Integer.parseInt(rssi);
                    mAccuracy = accuracy;
                    mTime = time;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // BLE 데이터에서 UUID, Major, Minor, TxPower, Rssi, Accuracy 값을 파싱.
    private String[] getMinorMajorUuidAccuracy(byte[] scanData, int rssi, BluetoothDevice device) {
        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int) scanData[startByte + 2] & 0xff) == 0x02 && ((int) scanData[startByte + 3] & 0xff) == 0x15) {
                // yes!  This is an iBeacon
                patternFound = true;
                break;
            } else if (((int) scanData[startByte] & 0xff) == 0x2d && ((int) scanData[startByte + 1] & 0xff) == 0x24 &&
                    ((int) scanData[startByte + 2] & 0xff) == 0xbf && ((int) scanData[startByte + 3] & 0xff) == 0x16) {
                // Logs.d(TAG, "This is a proprietary Estimote beacon advertisement that does not meet the iBeacon standard.  Identifiers cannot be read.");
                //Beacon iBeacon = new Beacon();
                //iBeacon.major = 0;
                //iBeacon.minor = 0;
                //iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
                //iBeacon.txPower = -55;
                //return iBeacon;
                return null;
            } else if (((int) scanData[startByte] & 0xff) == 0xad && ((int) scanData[startByte + 1] & 0xff) == 0x77 &&
                    ((int) scanData[startByte + 2] & 0xff) == 0x00 && ((int) scanData[startByte + 3] & 0xff) == 0xc6) {
                //Logs.d(TAG, "This is a proprietary Gimbal beacon advertisement that does not meet the iBeacon standard.  Identifiers cannot be read.");
                //Beacon iBeacon = new Beacon();
                //iBeacon.major = 0;
                //iBeacon.minor = 0;
                //iBeacon.proximityUuid = "00000000-0000-0000-0000-000000000000";
                //iBeacon.txPower = -55;
                //return iBeacon;
                return null;
            }
            startByte++;
        }

        if (!patternFound) {
            // This is not an iBeacon
            // Logs.d(TAG, "This is not an iBeacon advertisment (no 0215 seen in bytes 4-7).  The bytes I see are: "+bytesToHex(scanData));
            return null;
        }

        String major = String.valueOf((scanData[startByte + 20] & 0xff) * 0x100 + (scanData[startByte + 21] & 0xff));
        String minor = String.valueOf((scanData[startByte + 22] & 0xff) * 0x100 + (scanData[startByte + 23] & 0xff));

        int txPower = (int) scanData[startByte + 24]; // this one is signed
        double accuracy = calculateAccuracy(txPower, rssi);
//        beacon.getProximity();

        // AirLocate:
        // 02 01 1a 1a ff 4c 00 02 15  # Apple's fixed iBeacon advertising prefix
        // e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 # iBeacon profile uuid
        // 00 00 # major
        // 00 00 # minor
        // c5 # The 2's complement of the calibrated Tx Power

        // Estimote:
        // 02 01 1a 11 07 2d 24 bf 16
        // 394b31ba3f486415ab376e5c0f09457374696d6f7465426561636f6e00000000000000000000000000000000000000000000000000

        byte[] proximityUuidBytes = new byte[16];
        System.arraycopy(scanData, startByte + 4, proximityUuidBytes, 0, 16);
//        beacon.proximityUuidBytes = proximityUuidBytes;

        String hexString = bytesToHex(proximityUuidBytes);
        StringBuilder sb = new StringBuilder();
        sb.append(hexString.substring(0, 8));
        sb.append("-");
        sb.append(hexString.substring(8, 12));
        sb.append("-");
        sb.append(hexString.substring(12, 16));
        sb.append("-");
        sb.append(hexString.substring(16, 20));
        sb.append("-");
        sb.append(hexString.substring(20, 32));
        String proximityUuid = sb.toString();

//        if (device != null) {
//            beacon.bluetoothAddress = device.getAddress();
//        }

        return new String[]{proximityUuid, major, minor, String.valueOf(txPower), String.valueOf(rssi), String.valueOf(accuracy)};
    }

    // Accuracy 계산
    private double calculateAccuracy(int txPower, int rssi) {
//        Log.d(TAG, "calculateAccuracy - txPower : " + txPower + ", rssi : " + rssi);
        if (rssi == 0) {
            return -1.0; // if we cannot determine dAccuracy, return -1.
        }

        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            return (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes)
            hex.append(String.format("%02X", b));

        return hex.toString();
    }

    @NonNull
    @Override
    public String toString() {
        return "[BeaconData] NAME : " + mName + ", MAC : " + mMac + ", UUID : " + mUuid +
                "\nRSSI : " + mRssi + ", ACCURACY : " + mAccuracy +
                "\nMAJOR : " + mMajor + ", MINOR : " + mMinor + ", TXPOWER : " + mTxPower;
    }
}