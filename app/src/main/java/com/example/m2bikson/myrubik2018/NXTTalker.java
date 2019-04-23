package com.example.m2bikson.myrubik2018;

// this file was first finished about 2012-2014. There were some codes how phones communicate with NXT could be found at github.
// for example, https://github.com/MarcProe/nxt-remote-control/blob/master/src/org/jfedor/nxtremotecontrol/NXTTalker.java
// I added some to send controlling messages

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

public class NXTTalker {

    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;
    private int mState;
    private Handler mHandler;
    private BluetoothAdapter mAdapter;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    boolean waitForResponse = false;

    int Clockwise90adjustCount = 0;
    int CounterClockwise90adjustCount = 0;
    boolean lastLayeradjust = false;//adjust degree, because counter rotation  is overdone.

    CommandsToNXT myCommandsToNXT = new CommandsToNXT();
    byte[] s = new byte[25];
    String ToastCovertedFromByte = "";
    byte[] emptyBuffer = new byte[1024];

    public NXTTalker(Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mHandler = handler;
        setState(STATE_NONE);
    }

    private synchronized void setState(int state) {
        mState = state;
        if (mHandler != null) {
            mHandler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE,
                    state, -1).sendToTarget();
        }
    }

    public synchronized void connect(BluetoothDevice device) {
        //Log.i("NXT", "NXTTalker.connect()");

        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        //toast("Connected to " + device.getName());

        setState(STATE_CONNECTED);
    }

    public void playTone() {
        write(myCommandsToNXT.PlayToneData);
    }

    public void motorCPull() { // motor port C
        // write(myCommandsToNXT.TestRampupSpeedMotorONwithRegulated);
        write(myCommandsToNXT.motorCPullMessage);
        // write(myCommandsToNXT.TestRRampDownSpeedCoast);
        // byte[] data = { 0x02, 0x00,0x01,(byte) 0x9B};//command: to get device
        // information
        trySleepThread();
    }

    public void motorCPush() {
        // write(myCommandsToNXT.TestRampupSpeedMotorONwithBreakRegulated);
        write(myCommandsToNXT.motorCPushMessage);
        trySleepThread();
    }

    public void motorCPullPlus() { // motor port C
        // write(myCommandsToNXT.TestRampupSpeedMotorONwithRegulated);
        write(myCommandsToNXT.motorCPullMessagePlus);
        trySleepThread();
    }

    public void motorCPushPlus() {
        // write(myCommandsToNXT.TestRampupSpeedMotorONwithBreakRegulated);
        write(myCommandsToNXT.motorCPushMessagePlus);
        trySleepThread();
    }

    public void motorCRelease() {
        // write(myCommandsToNXT.TestRRampDownSpeedMotorONwithBreakRegulated);
        write(myCommandsToNXT.motorCReleaseMessage);
        trySleepThread();
    }

    //look from top to bottom, motorBClockwise90 will turn clockwise
    public void motorBClockwise90() {
        // write(myCommandsToNXT.TestRRampDownSpeedMotorONwithRegulated);
        write(myCommandsToNXT.motorBClockwise90Message);
        trySleepThread();
        //put a correction for each 8 turn
        Clockwise90adjustCount++;
        /**/
        if (lastLayeradjust == true) {
            if ((CounterClockwise90adjustCount - Clockwise90adjustCount) == 6) {
                CounterClockwise90adjustCount = 0;
                myCommandsToNXT.motorBCounterClockwise90adjustMessage[10] = 0x1A;
            }
        }
        motorBCounterClockwise90adjust();//put here is better to repeat following motorBClockwise90();
        myCommandsToNXT.motorBCounterClockwise90adjustMessage[10] = 0x38;
    }

    //look from top to bottom, motorBCounterClockwise90 will turn counter clockwise
    public void motorBCounterClockwise90() { // motor port B
        // write(myCommandsToNXT.TestRRampDownNoRegMotorONwithBreak);
        write(myCommandsToNXT.motorBCounterClockwise90Message);
        trySleepThread();
        CounterClockwise90adjustCount++;
        if (lastLayeradjust == true) {
            if ((Clockwise90adjustCount - CounterClockwise90adjustCount) == 6) {
                Clockwise90adjustCount = 0;
                myCommandsToNXT.motorBClockwise90adjustMessage[10] = 0x1A;
            }
        }
        motorBClockwise90adjust();//put here is better to repeat following motorBCounterClockwise90();
        myCommandsToNXT.motorBClockwise90adjustMessage[10] = 0x38;
    }

    public void motorBClockwise90adjust() { // motor port B
        // write(myCommandsToNXT.TestRRampDownNoRegMotorONwithBreak);
        write(myCommandsToNXT.motorBClockwise90adjustMessage);
        trySleepThread();
    }

    public void motorBCounterClockwise90adjust() {  // motor port B
        // write(myCommandsToNXT.TestRRampDownNoRegMotorONwithBreak);
        write(myCommandsToNXT.motorBCounterClockwise90adjustMessage);
        trySleepThread();
    }

    public void motorBClockwise180() {
        // write(myCommandsToNXT.TestRRampDownSpeedMotorONwithBreak);
        write(myCommandsToNXT.motorBClockwise180Message);
        trySleepThread();
    }

    public void motorBCounterClockwise180() {
        // write(myCommandsToNXT.TestRRampDownNoRegMotorONwithBreakRegulated);
        write(myCommandsToNXT.motorBCounterClockwise180Message);
        trySleepThread();
    }

    public void GetOutputState() {
        waitForResponse = true;
        write(myCommandsToNXT.GetOutputStateData);
        for (int j = 0; j < 27; j++) {
            // myCommandsToNXT.GetOutputStateFeed[j]= emptyBuffer[j];
        }
    }

    public void ResetMotorPosition() {
        write(myCommandsToNXT.ResetMotorPositionData);
    }

    private void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (mState != STATE_CONNECTED) {
                return;
            }
            r = mConnectedThread;
        }
        r.write(out);
    }

    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
        }

        public void run() {
            setName("ConnectThread");
            mAdapter.cancelDiscovery();

            try {
                mmSocket = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                mmSocket.connect();
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    // This is a workaround that reportedly helps on some older devices like HTC Desire, where using
                    // the standard createRfcommSocketToServiceRecord() method always causes connect() to fail.
                    Method method = mmDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                    mmSocket = (BluetoothSocket) method.invoke(mmDevice, Integer.valueOf(1));
                    mmSocket.connect();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    connectionFailed();
                    try {
                        mmSocket.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    return;
                }
            }

            synchronized (NXTTalker.this) {
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                if (mmSocket != null) {
                    mmSocket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.read(buffer);
                    //toast(Integer.toString(bytes) + " bytes read from device");

                } catch (IOException e) {
                    e.printStackTrace();
                    connectionLost();
                    break;
                }
            }
        }

        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
                // XXX?
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void upsideTurnDown(char[] top, char[] bottom, char[] front, char[] back, char[] right, char[] left) {

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();
    }

    public void turnCubeClockwise(char[] top, char[] bottom, char[] front, char[] back, char[] right, char[] left) {

        motorCPush();

        motorBClockwise90();

        motorCPull();

    }

    public void turnCubeCounterclockwise(char[] top, char[] bottom, char[] front, char[] back, char[] right, char[] left) {

        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
    }

    public void rollingCubeclockwise(char[] top, char[] bottom, char[] front, char[] back, char[] right, char[] left) {

        motorCPush();

        motorCPull();
    }

    public void rollingCubeCounterclockwise(char[] top, char[] bottom, char[] front, char[] back, char[] right, char[] left) {

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();
    }

    private void trySleepThread() {
        // TODO Auto-generated method stub
        try {
            Thread.sleep(1000);//changed from 1000
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void getRubikRoll(String InputSolutionStr) {
        {
            String[] solutionOperation = InputSolutionStr.split(" "); //turn into an array
            int correctionNumber = 0;
            for (int kk = 0; kk < solutionOperation.length; kk++) {        //check the solution: follow the steps to twist the rubik cube, if the cube becomes back to a solved cube
                correctionNumber = correctionNumber + 1;
                if (correctionNumber == 9) {
                    motorBClockwiseCorrection();
                    correctionNumber = 0;
                }
                if (solutionOperation[kk].equals("U1")) {
                    if (kk < (solutionOperation.length - 1)) {
                        if (solutionOperation[kk + 1] == "F1") {
                            U1F1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F2") {
                            U1F2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F3") {
                            U1F3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B1") {
                            U1B1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B2") {
                            U1B2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B3") {
                            U1B3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R1") {
                            U1R1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R2") {
                            U1R2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R3") {
                            U1R3();
                            kk = kk + 1;
                        } else U1();
                    } else U1();
                } else if (solutionOperation[kk].equals("U2")) {
                    if (kk < (solutionOperation.length - 1)) {
                        if (solutionOperation[kk + 1] == "F1") {
                            U2F1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F2") {
                            U2F2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F3") {
                            U2F3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B1") {
                            U2B1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B2") {
                            U2B2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B3") {
                            U2B3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R1") {
                            U2R1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R2") {
                            U2R2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R3") {
                            U2R3();
                            kk = kk + 1;
                        } else U2();
                    } else U2();
                } else if (solutionOperation[kk].equals("U3")) {
                    if (kk < (solutionOperation.length - 1)) {
                        if (solutionOperation[kk + 1] == "F1") {
                            U3F1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F2") {
                            U3F2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F3") {
                            U3F3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B1") {
                            U3B1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B2") {
                            U3B2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B3") {
                            U3B3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R1") {
                            U3R1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R2") {
                            U3R2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R3") {
                            U3R3();
                            kk = kk + 1;
                        } else U3();
                    } else U3();
                } else if (solutionOperation[kk].equals("D1")) {
                    if (kk < (solutionOperation.length - 1)) {

                    }
                    D1();
                } else if (solutionOperation[kk].equals("D2")) {

                    D2();
                } else if (solutionOperation[kk].equals("D3")) {

                    D3();
                } else if (solutionOperation[kk].equals("F1")) {
                    if (kk < (solutionOperation.length - 1)) {
                        if (solutionOperation[kk + 1] == "F1") {
                            F1U1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F2") {
                            F1U2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F3") {
                            F1U3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B1") {
                            F1B1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B2") {
                            F1B2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B3") {
                            F1B3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R1") {
                            F1R1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R2") {
                            F1R2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R3") {
                            F1R3();
                            kk = kk + 1;
                        } else F1();
                    } else F1();
                } else if (solutionOperation[kk].equals("F2")) {
                    if (kk < (solutionOperation.length - 1)) {
                        if (solutionOperation[kk + 1] == "F2") {
                            F2U1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F2") {
                            F2U2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F3") {
                            F2U3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B1") {
                            F2B1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B2") {
                            F2B2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B3") {
                            F2B3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R1") {
                            F2R1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R2") {
                            F2R2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R3") {
                            F2R3();
                            kk = kk + 1;
                        } else F2();
                    } else F2();

                } else if (solutionOperation[kk].equals("F3")) {
                    if (kk < (solutionOperation.length - 1)) {
                        if (solutionOperation[kk + 1] == "F3") {
                            F3U1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F2") {
                            F3U2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F3") {
                            F3U3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B1") {
                            F3B1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B2") {
                            F3B2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B3") {
                            F3B3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R1") {
                            F3R1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R2") {
                            F3R2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R3") {
                            F3R3();
                            kk = kk + 1;
                        } else F3();
                    } else F3();

                } else if (solutionOperation[kk].equals("B1")) {
                    if (kk < (solutionOperation.length - 1)) {
                        if (solutionOperation[kk + 1] == "B1") {
                            B1U1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B1") {
                            B1U2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F3") {
                            B1U3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R1") {
                            B1F1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R2") {
                            B1F2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R3") {
                            B1F3();
                            kk = kk + 1;
                        } else B1();
                    } else B1();
                } else if (solutionOperation[kk].equals("B2")) {

                    if (kk < (solutionOperation.length - 1)) {
                        if (solutionOperation[kk + 1] == "B2") {
                            B2U1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B2") {
                            B2U2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F3") {
                            B2U3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R1") {
                            B2F1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R2") {
                            B2F2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R3") {
                            B2F3();
                            kk = kk + 1;
                        } else B2();
                    } else B2();
                } else if (solutionOperation[kk].equals("B3")) {
                    if (kk < (solutionOperation.length - 1)) {
                        if (solutionOperation[kk + 1] == "B3") {
                            B3U1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B3") {
                            B3U2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F3") {
                            B3U3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R1") {
                            B3F1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R2") {
                            B3F2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R3") {
                            B3F3();
                            kk = kk + 1;
                        } else B3();
                    } else B3();
                } else if (solutionOperation[kk].equals("R1")) {
                    R1();
                } else if (solutionOperation[kk].equals("R2")) {
                    R2();
                } else if (solutionOperation[kk].equals("R3")) {
                    R3();
                } else if (solutionOperation[kk].equals("L1")) {
                    if (kk < (solutionOperation.length - 1)) {
                        if (solutionOperation[kk + 1] == "F1") {
                            L1F1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F2") {
                            L1F2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F3") {
                            L1F3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B1") {
                            L1B1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B2") {
                            L1B2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B3") {
                            L1B3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R1") {
                            L1R1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R2") {
                            L1R2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R3") {
                            L1R3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "L1") {
                            L1U1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "L2") {
                            L1U2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "L3") {
                            L1U3();
                            kk = kk + 1;
                        } else L1();
                    } else L1();
                } else if (solutionOperation[kk].equals("L2")) {
                    if (kk < (solutionOperation.length - 1)) {
                        if (solutionOperation[kk + 1] == "F1") {
                            L2F1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F2") {
                            L2F2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F3") {
                            L2F3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B1") {
                            L2B1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B2") {
                            L2B2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B3") {
                            L2B3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R1") {
                            L2R1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R2") {
                            L2R2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R3") {
                            L2R3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "L1") {
                            L2U1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "L2") {
                            L2U2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "L3") {
                            L2U3();
                            kk = kk + 1;
                        } else L2();
                    } else L2();
                } else if (solutionOperation[kk].equals("L3")) {
                    if (kk < (solutionOperation.length - 1)) {
                        if (solutionOperation[kk + 1] == "F1") {
                            L3F1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F2") {
                            L3F2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "F3") {
                            L3F3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B1") {
                            L3B1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B2") {
                            L3B2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "B3") {
                            L3B3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R1") {
                            L3R1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R2") {
                            L3R2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "R3") {
                            L3R3();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "L1") {
                            L3U1();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "L2") {
                            L3U2();
                            kk = kk + 1;
                        } else if (solutionOperation[kk + 1] == "L3") {
                            L3U3();
                            kk = kk + 1;
                        } else L3();
                    } else L3();
                }
            }
        }
    }

    public void U3F1() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//F1

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void U3F3() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//F3

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void U3F2() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }


    public void U3R3() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();
    }

    public void U3R2() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();
    }

    public void U3R1() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();
    }

    public void U3B3() {

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();//u1,2,3

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//b1,2,3

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();//go to back
        motorCPull();
    }

    public void U3B2() {

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();//u1,2,3

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//b1,2,3
        motorBClockwise90();//b1,2,3

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();//go to back
        motorCPull();
    }

    public void U3B1() {

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();//u1,2,3

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//b1,2,3

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();//go to back
        motorCPull();

    }

    public void U2R3() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();
    }

    public void U2R2() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();
    }

    public void U2R1() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();
    }

    public void U2B3() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//u1,2,3
        motorBCounterClockwise90();//u1,2,3

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//b1,2,3

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();//go to back
        motorCPull();
    }

    public void U2B2() {

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//u1,2,3
        motorBCounterClockwise90();//u1,2,3

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//b1,2,3
        motorBClockwise90();//b1,2,3

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();//go to back
        motorCPull();

    }

    public void U2B1() {

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//u1,2,3
        motorBCounterClockwise90();//u1,2,3

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//b1,2,3

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();//go to back
        motorCPull();
    }

    public void U2F3() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//F3

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void U2F2() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//F1
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void U2F1() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//F1

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }


    public void U1R3() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();
    }

    public void U1R2() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();
    }

    public void U1R1() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();
    }

    public void U1B3() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//u1,2,3

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//b1,2,3

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();//go to back
        motorCPull();
    }

    public void U1B2() {

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//u1,2,3

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//b1,2,3
        motorBCounterClockwise90();//b1,2,3

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();//go to back
        motorCPull();
    }

    public void U1B1() {

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//u1,2,3

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//b1,2,3

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();//go to back
        motorCPull();
    }

    public void U1F3() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//F3

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void U1F2() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//F1
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void U1F1() {
        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//F1

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void F1R3() {
        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//f1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//r3

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();
    }

    public void F1R2() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//f1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//r1
        motorBCounterClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();
    }

    public void F1R1() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//f1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//r1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();
    }

    public void F1B3() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F1B2() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F1B1() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F1U2() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F1U1() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F3R1() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//f3

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//r1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();
    }

    public void F3R3() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//f3

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//r3

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();
    }

    public void F3R2() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//f3

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//r1
        motorBCounterClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();
    }

    public void F3B3() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F3B2() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F3B1() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F3U3() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

    }

    public void F3U2() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F3U1() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F2R3() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//f1
        motorBCounterClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//r3

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();
    }

    public void F2R2() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//f1
        motorBCounterClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//r1
        motorBClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();
    }

    public void F2R1() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//f1
        motorBCounterClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//r1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();
    }

    public void F2B3() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F2B2() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F2B1() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F2U3() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F2U2() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F2U1() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void F1U3() {

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();
    }

    public void B3F3() {

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B3F2() {

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B3F1() {

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B3U3() {
        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void B3U2() {
        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B3U1() {

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void B2U1() {

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B2U2() {
        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void B2U3() {

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void B2F2() {

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B2F1() {

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B2F3() {

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B1U1() {
        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B1U3() {
        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B1F1() {

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B1F2() {
        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B1F3() {
        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();
    }

    public void B1U2() {
        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }


    public void L3U3() {

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();
    }

    public void L3U2() {

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();
    }

    public void L3U1() {

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();
    }

    public void L3R3() {

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();
    }

    public void L3R2() {

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();
    }

    public void L3R1() {

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();
    }

    public void L3B3() {
        motorCPull();
        motorCPush();

        motorBClockwise90();//l1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//b1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

    }

    public void L3B2() {
        motorCPull();
        motorCPush();

        motorBClockwise90();//l1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//b1
        motorBClockwise90();//b1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

    }

    public void L3B1() {
        motorCPull();
        motorCPush();

        motorBClockwise90();//l1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//b1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

    }

    public void L3F3() {
        motorCPull();
        motorCPush();

        motorBClockwise90();//l1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//f1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void L3F2() {
        motorCPull();
        motorCPush();

        motorBClockwise90();//l1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//f1
        motorBCounterClockwise90();//f1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void L3F1() {
        motorCPull();
        motorCPush();

        motorBClockwise90();//l1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//f1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void L2U3() {

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();
    }

    public void L2U2() {

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();
    }

    public void L2U1() {

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();
    }

    public void L2R3() {

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();
    }

    public void L2R2() {

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();
        motorCPush();
    }

    public void L2R1() {

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();
    }

    public void L2B3() {
        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//l1
        motorBCounterClockwise90();//l1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//b1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

    }

    public void L2B2() {
        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//l1
        motorBCounterClockwise90();//l1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//b1
        motorBClockwise90();//l1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

    }

    public void L2B1() {
        motorCPull();
        motorCPush();

        motorBClockwise90();//l1
        motorBClockwise90();//l1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//b1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

    }

    public void L2F3() {
        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//l1
        motorBCounterClockwise90();//l1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//f1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void L2F2() {
        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//l1
        motorBCounterClockwise90();//l1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//f1
        motorBCounterClockwise90();//l1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void L2F1() {
        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//l1
        motorBCounterClockwise90();//l1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//f1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void L1U3() {

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();
    }

    public void L1U2() {

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();
    }

    public void L1U1() {

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();
    }

    public void L1R3() {

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();

        motorCPull();
        motorCPush();
    }

    public void L1R2() {

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBClockwise90();
        motorBClockwise90();

        motorCPull();
        motorCPush();
    }

    public void L1R1() {

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
        motorCPush();
    }

    public void L1B3() {
        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//l1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//b1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

    }

    public void L1B2() {
        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//l1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//b1
        motorBClockwise90();//b1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

    }

    public void L1B1() {
        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//l1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//b1

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBCounterClockwise90();
        motorCPull();

    }

    public void L1F3() {
        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//l1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBClockwise90();//f1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void L1F2() {
        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//l1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//f1
        motorBCounterClockwise90();

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void L1F1() {
        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//l1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorBCounterClockwise90();//f1

        motorCPush();
        motorBClockwise90();
        motorCPull();

        motorCPull();
        motorCPush();

        motorCPush();
        motorBClockwise90();
        motorCPull();

    }

    public void U1() {
        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorBCounterClockwise90();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();
    }

    public void D1() {

        motorBCounterClockwise90();
    }

    public void F1() {
        motorCPush();// not enough

        motorBClockwise90();

        motorCPull();

        motorCPull();

        motorCPush();

        motorBCounterClockwise90();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
    }

    public void B1() {
        motorCPush();

        motorBClockwise90();

        motorCPull();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorBCounterClockwise90();

        motorCPull();

        motorCPush();

        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
    }

    public void R1() {
        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorBCounterClockwise90();

        motorCPull();

        motorCPush();
    }

    public void L1() {
        motorCPull();

        motorCPush();

        motorBCounterClockwise90();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();
    }

    public void U2() {
        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();
    }

    public void D2() {
        motorBCounterClockwise90();
        motorBCounterClockwise90();
    }

    public void F2() {
        motorCPush();// not enough

        motorBClockwise90();

        motorCPull();

        motorCPull();

        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
    }

    public void B2() {
        motorCPush();

        motorBClockwise90();

        motorCPull();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();

        motorCPush();

        motorCPush();

        motorBCounterClockwise90();

        motorCPull();
    }

    public void L2() {

        motorCPull();

        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();
    }

    public void R2() {
        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorBCounterClockwise90();
        motorBCounterClockwise90();

        motorCPull();

        motorCPush();
    }

    public void U3() {
        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorBClockwise90();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();
    }

    public void D3() {
        motorBClockwise90();
    }

    public void F3() {

        motorCPush();// not enough

        motorBClockwise90();

        motorCPull();

        motorCPull();

        motorCPush();

        motorBClockwise90();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPush();// not enough

        motorBCounterClockwise90();

        motorCPull();
    }

    public void B3() {

        motorCPush();

        motorBCounterClockwise90();

        motorCPull();

        motorCPull();

        motorCPush();

        motorBClockwise90();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPush();

        motorBClockwise90();

        motorCPull();
    }

    public void R3() {

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorBClockwise90();

        motorCPull();

        motorCPush();

    }

    public void L3() {
        motorCPull();

        motorCPush();

        motorBClockwise90();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();

        motorCPull();

        motorCPush();
    }



    public synchronized int getState() {
        return mState;
    }

    public synchronized void setHandler(Handler handler) {
        mHandler = handler;
    }

    private void toast(String text) {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(MainActivity.MESSAGE_TOAST);
            Bundle bundle = new Bundle();
            bundle.putString(MainActivity.TOAST, text);
            msg.setData(bundle);
            mHandler.sendMessage(msg);
        } else {
            //XXX
        }
    }


    public synchronized void stop() {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_NONE);
    }

    private void connectionFailed() {
        setState(STATE_NONE);
        //toast("Connection failed");
    }

    private void connectionLost() {
        setState(STATE_NONE);
        //toast("Connection lost");
    }

    public void motors(byte l, byte r, boolean speedReg, boolean motorSync) {
        byte[] data = {0x0c, 0x00, (byte) 0x80, 0x04, 0x02, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00,
                0x0c, 0x00, (byte) 0x80, 0x04, 0x01, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00};

        //Log.i("NXT", "motors: " + Byte.toString(l) + ", " + Byte.toString(r));

        data[5] = l;
        data[19] = r;
        if (speedReg) {
            data[7] |= 0x01;
            data[21] |= 0x01;
        }
        if (motorSync) {
            data[7] |= 0x02;
            data[21] |= 0x02;
        }
        write(data);
    }

    public void motor(int motor, byte power, boolean speedReg, boolean motorSync) {
        byte[] data = {0x0c, 0x00, (byte) 0x80, 0x04, 0x02, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00};

        //Log.i("NXT", "motor: " + Integer.toString(motor) + ", " + Byte.toString(power));

        if (motor == 0) {
            data[4] = 0x02;
        } else {
            data[4] = 0x01;
        }
        data[5] = power;
        if (speedReg) {
            data[7] |= 0x01;
        }
        if (motorSync) {
            data[7] |= 0x02;
        }
        write(data);
    }

    public void motors3(byte l, byte r, byte action, boolean speedReg, boolean motorSync) {
        byte[] data = {0x0c, 0x00, (byte) 0x80, 0x04, 0x02, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00,
                0x0c, 0x00, (byte) 0x80, 0x04, 0x01, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00,
                0x0c, 0x00, (byte) 0x80, 0x04, 0x00, 0x32, 0x07, 0x00, 0x00, 0x20, 0x00, 0x00, 0x00, 0x00};

        //Log.i("NXT", "motors3: " + Byte.toString(l) + ", " + Byte.toString(r) + ", " + Byte.toString(action));

        data[5] = l;
        data[19] = r;
        data[33] = action;
        if (speedReg) {
            data[7] |= 0x01;
            data[21] |= 0x01;
        }
        if (motorSync) {
            data[7] |= 0x02;
            data[21] |= 0x02;
        }
        write(data);
    }


    public void turnFrontUp() {
        // TODO Auto-generated method stub

        motorCPullPlus();

        motorCPull();

        motorCPush();

        motorCPushPlus();

    }


    public void returnReadyPosition() {
        // TODO Auto-generated method stub
        motorBClockwise90();
        motorBClockwise90();
        motorCPullPlus();

        motorCPull();

        motorCPush();


    }


    public void turnBottomUp() {
        // TODO Auto-generated method stub
        motorCPullPlus();

        motorCPull();

        motorCPush();

        motorCPushPlus();
    }


    public void turnRightUp() {
        // TODO Auto-generated method stub
        motorBClockwise90();
        motorCPullPlus();

        motorCPull();

        motorCPush();

        motorCPushPlus();
        motorBClockwise90();
        motorBClockwise90();

        motorCPullPlus();    //in order to put Rubik at the same place
        motorCPushPlus();
    }


    public void turnBackUp() {
        // TODO Auto-generated method stub
        motorBClockwise90();
        motorCPullPlus();

        motorCPull();

        motorCPush();

        motorCPushPlus();
        motorBCounterClockwise90();

        motorCPullPlus();
        motorCPushPlus();
    }


    public void turnLeftUp() {
        // TODO Auto-generated method stub
        motorBClockwise90();
        motorCPullPlus();

        motorCPull();

        motorCPush();

        motorCPushPlus();
        motorBCounterClockwise90();

        motorCPullPlus();
        motorCPushPlus();
    }


    public void openTop() {
        // TODO Auto-generated method stub
        motorCPushPlus();
    }


    public void motorBClockwiseCorrection() { //let the rubik corrects error of motor B by turnning more 18 degrees
        write(myCommandsToNXT.motorBClockwiseCorrectionMessage);
        trySleepThread();
    }


}
