package com.example.m2bikson.myrubik2018;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class ReadSurfaceColor extends Activity implements OnClickListener {

    Button takePicBttnRC; //button, ImageView, TextView are put back of var in readsurfacecolor.xml
    Button readySlvBttnRC;
    Bitmap bmpPhoto;
    ImageView returnImageRC;
    TextView remindDoneTV;
    final static int cameraData = 0;

    boolean hoursIsDaytime = true;

    //may get different RGB value at different hours of day
    //different days in seasons also have different RGB
    //the light will change the RGB value
    //
    int RedRange_redMax = 255;        // top
    int RedRange_redMin = 150;        // expand from 180
    int RedRange_blueMax = 160;        // expand from 122
    int RedRange_blueMin = 0;
    int RedRange_greenMax = 94;        //can higher? with Orange
    int RedRange_greenMin = 0;        // expand from 3

    int OrangeRange_redMax = 255;    // color in bottom's enter
    int OrangeRange_redMin = 150;    // expand from 205
    int OrangeRange_blueMax = 144;    // expand from 101
    int OrangeRange_blueMin = 0;
    int OrangeRange_greenMax = 167;    //can higher? with yellow
    int OrangeRange_greenMin = 95;    //can lower?	with red

    int BlueRange_redMax = 149;        // expand from 59// front
    int BlueRange_redMin = 0;
    int BlueRange_blueMax = 255;        //changed again from 231
    int BlueRange_blueMin = 80;        // expand from 103
    int BlueRange_greenMax = 190;        // expand from 153
    int BlueRange_greenMin = 0;        // expand from 36

    int GreenRange_redMax = 149;    // expand from 99// back
    int GreenRange_redMin = 0;
    int GreenRange_blueMax = 160;    // expand from 137
    int GreenRange_blueMin = 0;    // expand from 16 lower(161) may needed
    int GreenRange_greenMax = 255;    // expand from 210
    int GreenRange_greenMin = 30;    // expand from 84

    int WhiteRange_redMax = 255;    // right face
    int WhiteRange_redMin = 150;    // expand from 186
    int WhiteRange_blueMax = 255;
    int WhiteRange_blueMin = 145;    // expand from 170
    int WhiteRange_greenMax = 255;
    int WhiteRange_greenMin = 100;    // expand from 185

    int YellowRange_redMax = 255;    // left
    int YellowRange_redMin = 150;    // expand from 198
    int YellowRange_blueMax = 144;    // expand from 99
    int YellowRange_blueMin = 0;
    int YellowRange_greenMax = 255;
    int YellowRange_greenMin = 168;    //can lower? with Orange


    int x, y;                        // position of a pixel (x,y)
    int faceNumberMainActivity;        //the value comes from MainActivity
    int faceNumber = 0;            // number of face
    int numberface = 0;
    int i = 0;            //used in for loop
    int j = 0;
    int kk;
    int index = 0;            // index of arrays
    int pixel;                        // RGB color of a pixel

    int[] red = new int[100];    // declare red array of RGB value at a pixel
    int[] green = new int[100];    // declare green array of RGB value at a pixel
    int[] blue = new int[100];    // declare blue array of RGB value at a pixel

    String RubikTempStringMainActivity;
    String tempstring;
    private static final String TAG = "MyActivity";
    public static final int MEDIA_TYPE_IMAGE = 1;

    char[] toptemp = {'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E'};  //declare array
    char[] fronttemp = {'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E'};  //declare array
    char[] backtemp = {'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E'};  //declare array
    char[] righttemp = {'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E'};  //declare array
    char[] lefttemp = {'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E'};  //declare array
    char[] bottomtemp = {'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E'};  //declare array

    char[] temptop = {'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U'}; // declare array
    char[] tempfront = {'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F'}; // declare array
    char[] tempback = {'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B'}; // declare array
    char[] tempright = {'R', 'R', 'R', 'R', 'R', 'R', 'R', 'R', 'R'}; // declare array
    char[] templeft = {'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L'}; // declare array
    char[] tempbottom = {'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D'}; // declare array
    char colorOfCell = 'E';

    private Camera mCamera;
    private CameraSurfacePreview mPreview;
    SurfaceHolder holder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.readsurfacecolor);

        // Initiation of following variables
        takePicBttnRC = (Button) findViewById(R.id.button_capture);

        takePicBttnRC.setOnClickListener(this);

        RubikTempStringMainActivity = MainActivity.RubikTempString;
        faceNumberMainActivity = MainActivity.faceNumber;

        mCamera = getCameraInstance();            //step 1: Obtain an instance of Camera from open(int).
        mCamera.setDisplayOrientation(90);

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraSurfacePreview(this, mCamera); //step 6: Important: Call startPreview() to start updating the preview surface. Preview must be started before you can take a picture.
        preview.addView(mPreview);

        Camera.Parameters params = mCamera.getParameters();

        int zoomValue;
        zoomValue = params.getMaxZoom() - 2;//or 4 //params.getMaxZoom() =30
        params.setZoom(zoomValue);
        mCamera.setParameters(params);

        /**
         * add the condition of hours of day, then change the rgb values
         */
        String timeStamp = new SimpleDateFormat("HH").format(new Date());
        if (timeStamp.equals("19") || timeStamp.equals("20") || timeStamp.equals("21") || timeStamp.equals("22") || timeStamp.equals("23") || timeStamp.equals("24")) {
            hoursIsDaytime = false;
        }
    }

    @Override
    public void onBackPressed() { //this will have bug: Out of memory on a 19660816-byte allocation. So it will not called in PictureCallback ()
        // TODO Auto-generated method stub
        super.onBackPressed();
        finish();
        try {
            Thread.sleep(1000);//changed from 1000
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        onDestroy();
        try {
            Thread.sleep(1000);//changed from 1000
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

	/*	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}*/

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.button_capture:
                mCamera.takePicture(null, null, mPicture);////break  again//
                //startActivity(takePictureintent); //this is not needed, startActivityForResult has made the activity start, don't need to use startActivity
                break;
            default:
                break;
        }
    }

    private char whatColorIsIt(int k, int l, int m) {
        //copied from com.example.rubik3x3x3cameraRandomColor

        if (hoursIsDaytime == false) {
            //night in Sept-Oct. reference of file "RandomCubeRGB09-24 1117 B -90"
            //with this value group, only green and blue have overlay,
            //which means do not need other conditions to decide color
            RedRange_redMax = 255;        // top
            RedRange_redMin = 180;
            RedRange_blueMax = 142;
            RedRange_blueMin = 0;
            RedRange_greenMax = 136;        //can higher? with Orange
            RedRange_greenMin = 3;

            OrangeRange_redMax = 255;    // color in bottom's enter
            OrangeRange_redMin = 205;
            OrangeRange_blueMax = 140;
            OrangeRange_blueMin = 0;
            OrangeRange_greenMax = 197;    //can higher? with yellow
            OrangeRange_greenMin = 137;    //can lower?	with red

            BlueRange_redMax = 99;        // front
            BlueRange_redMin = 0;
            BlueRange_blueMax = 255;
            BlueRange_blueMin = 103;
            BlueRange_greenMax = 153;
            BlueRange_greenMin = 36;

            GreenRange_redMax = 99;    // back
            GreenRange_redMin = 0;
            GreenRange_blueMax = 137;
            GreenRange_blueMin = 16;    //lower(161) may needed
            GreenRange_greenMax = 210;
            GreenRange_greenMin = 84;

            WhiteRange_redMax = 255;    // right face
            WhiteRange_redMin = 186;
            WhiteRange_blueMax = 255;
            WhiteRange_blueMin = 170;
            WhiteRange_greenMax = 255;
            WhiteRange_greenMin = 185;

            YellowRange_redMax = 255;    // left
            YellowRange_redMin = 198;
            YellowRange_blueMax = 157;
            YellowRange_blueMin = 0;
            YellowRange_greenMax = 255;
            YellowRange_greenMin = 198;    //can lower? with Orange
        }

        char colorOfCell = 'B';        //it is danger to change to G from E,

        if (red[index] <= RedRange_redMax && red[index] >= RedRange_redMin) {
            if (green[index] <= RedRange_greenMax && green[index] >= RedRange_greenMin) {
                if (blue[index] <= RedRange_blueMax && blue[index] >= RedRange_blueMin) {
                    colorOfCell = 'U';
                }
            }
        }

        if (red[index] <= OrangeRange_redMax && red[index] >= OrangeRange_redMin) {
            if (green[index] <= OrangeRange_greenMax && green[index] >= OrangeRange_greenMin) {
                if (blue[index] <= OrangeRange_blueMax && blue[index] >= OrangeRange_blueMin) {
                    colorOfCell = 'D';//this is color in bottom's center
                }
            }
        }

        if (red[index] <= BlueRange_redMax && red[index] >= BlueRange_redMin) {
            if (green[index] <= BlueRange_greenMax && green[index] >= BlueRange_greenMin) {
                if (blue[index] <= BlueRange_blueMax && blue[index] >= BlueRange_blueMin) {
                    if (blue[index] >= (green[index] + 5)) {
                        colorOfCell = 'F';
                    }
                }
            }
        }

        if (red[index] <= GreenRange_redMax && red[index] >= GreenRange_redMin) {
            if (green[index] <= GreenRange_greenMax && green[index] >= GreenRange_greenMin) {
                if (blue[index] <= GreenRange_blueMax && blue[index] >= GreenRange_blueMin) {
                    if ((green[index] - blue[index]) >= 4) {
                        colorOfCell = 'B';
                    }
                }
            }
        }

        if (red[index] <= YellowRange_redMax && red[index] >= YellowRange_redMin) {
            if (green[index] <= YellowRange_greenMax && green[index] >= YellowRange_greenMin) {
                if (blue[index] <= YellowRange_blueMax && blue[index] >= YellowRange_blueMin) {
                    colorOfCell = 'L';
                }
            }
        }

        if (red[index] <= WhiteRange_redMax && red[index] >= WhiteRange_redMin) {
            if (green[index] <= WhiteRange_greenMax && green[index] >= WhiteRange_greenMin) {
                if (blue[index] <= WhiteRange_blueMax && blue[index] >= WhiteRange_blueMin) {
                    colorOfCell = 'R';//color gray
                }
            }
        }
        return colorOfCell;
    }

    private void read_top_surface() {
        // TODO Auto-generated method stub
        char[] temp = {'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U'}; // declare array
        int high = bmpPhoto.getHeight();
        int width = bmpPhoto.getWidth();
        int unitX = high / 6;
        int unitY = width / 6;
        int unit = 0;
        if (unitX > unitY)
            unit = unitY;
        else
            unit = unitX;
        pixel = bmpPhoto.getPixel(12, 12);// need this to delete min and
        index = 0;

        tempstring = "";

        for (i = 0; i <= 2; i++) {
            y = unit + unit * 2 * i;
            for (j = 0; j <= 2; j++) {
                x = unit + unit * 2 * j;
                pixel = bmpPhoto.getPixel(x, y);
                red[index] = Color.red(pixel);
                green[index] = Color.green(pixel);
                blue[index] = Color.blue(pixel);
                int tempRed = 0;
                int tempBlue = 0;
                int tempGreen = 0;
                for (int jj = -2; jj <= 2; jj++) {
                    y = y + jj;
                    for (int kk = -2; kk <= 2; kk++) {
                        x = x + kk;
                        pixel = bmpPhoto.getPixel(x, y);
                        tempRed = tempRed + Color.red(pixel);
                        tempGreen = tempGreen + Color.green(pixel);
                        tempBlue = tempBlue + Color.blue(pixel);
                    }
                }

                red[index] = tempRed / 25;
                green[index] = tempGreen / 25;
                blue[index] = tempBlue / 25;

                toptemp[index] = whatColorIsIt(red[index], green[index], blue[index]);    // find out what color it is

                index = index + 1;
            }
        }
        temp[0] = toptemp[6];
        temp[1] = toptemp[3];
        temp[2] = toptemp[0];
        temp[3] = toptemp[7];
        temp[4] = toptemp[4];
        temp[5] = toptemp[1];
        temp[6] = toptemp[8];
        temp[7] = toptemp[5];
        temp[8] = toptemp[2];
        toptemp = temp;
    }


    public void read_front_surface() {

        char[] temp = {'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U'}; // declare array
        int high = bmpPhoto.getHeight();
        int width = bmpPhoto.getWidth();
        int unitX = high / 6;
        int unitY = width / 6;
        int unit = 0;
        if (unitX > unitY)
            unit = unitY;
        else
            unit = unitX;
        pixel = bmpPhoto.getPixel(12, 12);// need this to delete min and
        index = 0;

        tempstring = "";

        for (i = 0; i <= 2; i++) {
            y = unit + unit * 2 * i;
            for (j = 0; j <= 2; j++) {
                x = unit + unit * 2 * j;
                pixel = bmpPhoto.getPixel(x, y);
                red[index] = Color.red(pixel);
                green[index] = Color.green(pixel);
                blue[index] = Color.blue(pixel);
                int tempRed = 0;
                int tempBlue = 0;
                int tempGreen = 0;
                for (int jj = -2; jj <= 2; jj++) {
                    y = y + jj;
                    for (int kk = -2; kk <= 2; kk++) {
                        x = x + kk;
                        pixel = bmpPhoto.getPixel(x, y);
                        tempRed = tempRed + Color.red(pixel);
                        tempGreen = tempGreen + Color.green(pixel);
                        tempBlue = tempBlue + Color.blue(pixel);
                    }
                }

                red[index] = tempRed / 25;
                green[index] = tempGreen / 25;
                blue[index] = tempBlue / 25;
                fronttemp[index] = whatColorIsIt(red[index], green[index], blue[index]);    // find out what color it is

                index = index + 1;
                // bmpPhoto.setPixel(x, y, -256); // -256 yellow , cannot do. because will crash the program.
            }
        }
        temp[0] = fronttemp[6];
        temp[1] = fronttemp[3];
        temp[2] = fronttemp[0];
        temp[3] = fronttemp[7];
        temp[4] = fronttemp[4];
        temp[5] = fronttemp[1];
        temp[6] = fronttemp[8];
        temp[7] = fronttemp[5];
        temp[8] = fronttemp[2];

        fronttemp = temp;
//		faceNumber = faceNumber + 1;
    }

    public void read_right_surface() {
        char[] temp = {'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U'}; // declare array
        int high = bmpPhoto.getHeight();
        int width = bmpPhoto.getWidth();
        int unitX = high / 6;
        int unitY = width / 6;
        int unit = 0;
        if (unitX > unitY)
            unit = unitY;
        else
            unit = unitX;
        pixel = bmpPhoto.getPixel(12, 12);// need this to delete min and
        index = 0;

        tempstring = "";

        for (i = 0; i <= 2; i++) {
            y = unit + unit * 2 * i;
            for (j = 0; j <= 2; j++) {
                x = unit + unit * 2 * j;
                pixel = bmpPhoto.getPixel(x, y);
                red[index] = Color.red(pixel);
                green[index] = Color.green(pixel);
                blue[index] = Color.blue(pixel);
                int tempRed = 0;
                int tempBlue = 0;
                int tempGreen = 0;
                for (int jj = -2; jj <= 2; jj++) {
                    y = y + jj;
                    for (int kk = -2; kk <= 2; kk++) {
                        x = x + kk;
                        pixel = bmpPhoto.getPixel(x, y);
                        tempRed = tempRed + Color.red(pixel);
                        tempGreen = tempGreen + Color.green(pixel);
                        tempBlue = tempBlue + Color.blue(pixel);
                    }
                }

                red[index] = tempRed / 25;
                green[index] = tempGreen / 25;
                blue[index] = tempBlue / 25;
                righttemp[index] = whatColorIsIt(red[index], green[index], blue[index]);    // find out what color it is

                index = index + 1;
            }
        }
        temp[0] = righttemp[6];
        temp[1] = righttemp[3];
        temp[2] = righttemp[0];
        temp[3] = righttemp[7];
        temp[4] = righttemp[4];
        temp[5] = righttemp[1];
        temp[6] = righttemp[8];
        temp[7] = righttemp[5];
        temp[8] = righttemp[2];

        righttemp = temp;
//		faceNumber = faceNumber + 1;
    }

    public void read_back_surface() {
        // TODO Auto-generated method stub
        char[] temp = {'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U'}; // declare array
        int high = bmpPhoto.getHeight();
        int width = bmpPhoto.getWidth();
        int unitX = high / 6;
        int unitY = width / 6;
        int unit = 0;
        if (unitX > unitY)
            unit = unitY;
        else
            unit = unitX;
        pixel = bmpPhoto.getPixel(12, 12);// need this to delete min and
        index = 0;

        tempstring = "";

        for (i = 0; i <= 2; i++) {
            y = unit + unit * 2 * i;
            for (j = 0; j <= 2; j++) {
                x = unit + unit * 2 * j;
                pixel = bmpPhoto.getPixel(x, y);
                red[index] = Color.red(pixel);
                green[index] = Color.green(pixel);
                blue[index] = Color.blue(pixel);
                int tempRed = 0;
                int tempBlue = 0;
                int tempGreen = 0;
                for (int jj = -2; jj <= 2; jj++) {
                    y = y + jj;
                    for (int kk = -2; kk <= 2; kk++) {
                        x = x + kk;
                        pixel = bmpPhoto.getPixel(x, y);
                        tempRed = tempRed + Color.red(pixel);
                        tempGreen = tempGreen + Color.green(pixel);
                        tempBlue = tempBlue + Color.blue(pixel);
                    }
                }

                red[index] = tempRed / 25;
                green[index] = tempGreen / 25;
                blue[index] = tempBlue / 25;
                backtemp[index] = whatColorIsIt(red[index], green[index], blue[index]);    // find out what color it is

                index = index + 1;
            }
        }
        temp[0] = backtemp[6];
        temp[1] = backtemp[3];
        temp[2] = backtemp[0];
        temp[3] = backtemp[7];
        temp[4] = backtemp[4];
        temp[5] = backtemp[1];
        temp[6] = backtemp[8];
        temp[7] = backtemp[5];
        temp[8] = backtemp[2];

        backtemp = temp;
        //	faceNumber = faceNumber + 1;
    }

    public void read_left_suface() {
        // TODO Auto-generated method stub
        char[] temp = {'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U'}; // declare array
        int high = bmpPhoto.getHeight();
        int width = bmpPhoto.getWidth();
        int unitX = high / 6;
        int unitY = width / 6;
        int unit = 0;
        if (unitX > unitY)
            unit = unitY;
        else
            unit = unitX;
        pixel = bmpPhoto.getPixel(12, 12);// need this to delete min and
        index = 0;
        tempstring = "";
        for (i = 0; i <= 2; i++) {
            y = unit + unit * 2 * i;
            for (j = 0; j <= 2; j++) {
                x = unit + unit * 2 * j;
                pixel = bmpPhoto.getPixel(x, y);
                red[index] = Color.red(pixel);
                green[index] = Color.green(pixel);
                blue[index] = Color.blue(pixel);
                int tempRed = 0;
                int tempBlue = 0;
                int tempGreen = 0;
                for (int jj = -2; jj <= 2; jj++) {
                    y = y + jj;
                    for (int kk = -2; kk <= 2; kk++) {
                        x = x + kk;
                        pixel = bmpPhoto.getPixel(x, y);
                        tempRed = tempRed + Color.red(pixel);
                        tempGreen = tempGreen + Color.green(pixel);
                        tempBlue = tempBlue + Color.blue(pixel);
                    }
                }

                red[index] = tempRed / 25;
                green[index] = tempGreen / 25;
                blue[index] = tempBlue / 25;
                lefttemp[index] = whatColorIsIt(red[index], green[index], blue[index]);    // find out what color it is

                index = index + 1;
            }
        }
        temp[0] = lefttemp[6];
        temp[1] = lefttemp[3];
        temp[2] = lefttemp[0];
        temp[3] = lefttemp[7];
        temp[4] = lefttemp[4];
        temp[5] = lefttemp[1];
        temp[6] = lefttemp[8];
        temp[7] = lefttemp[5];
        temp[8] = lefttemp[2];

        lefttemp = temp;
        //	faceNumber = faceNumber + 1;
    }

    public void read_bottom_surface() {
        // TODO Auto-generated method stub
        char[] temp = {'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U'}; // declare array
        int high = bmpPhoto.getHeight();
        int width = bmpPhoto.getWidth();
        int unitX = high / 6;
        int unitY = width / 6;
        int unit = 0;
        if (unitX > unitY)
            unit = unitY;
        else
            unit = unitX;
        pixel = bmpPhoto.getPixel(12, 12);// need this to delete min and
        index = 0;

        tempstring = "";

        for (i = 0; i <= 2; i++) {
            y = unit + unit * 2 * i;
            for (j = 0; j <= 2; j++) {
                x = unit + unit * 2 * j;
                pixel = bmpPhoto.getPixel(x, y);
                red[index] = Color.red(pixel);
                green[index] = Color.green(pixel);
                blue[index] = Color.blue(pixel);
                int tempRed = 0;
                int tempBlue = 0;
                int tempGreen = 0;
                for (int jj = -2; jj <= 2; jj++) {
                    y = y + jj;
                    for (int kk = -2; kk <= 2; kk++) {
                        x = x + kk;
                        pixel = bmpPhoto.getPixel(x, y);
                        tempRed = tempRed + Color.red(pixel);
                        tempGreen = tempGreen + Color.green(pixel);
                        tempBlue = tempBlue + Color.blue(pixel);
                    }
                }

                red[index] = tempRed / 25;
                green[index] = tempGreen / 25;
                blue[index] = tempBlue / 25;
                bottomtemp[index] = whatColorIsIt(red[index], green[index], blue[index]);    // find out what color it is

                index = index + 1;
            }
        }
        temp[0] = bottomtemp[6];
        temp[1] = bottomtemp[3];
        temp[2] = bottomtemp[0];
        temp[3] = bottomtemp[7];
        temp[4] = bottomtemp[4];
        temp[5] = bottomtemp[1];
        temp[6] = bottomtemp[8];
        temp[7] = bottomtemp[5];
        temp[8] = bottomtemp[2];

        bottomtemp = temp;
        //	faceNumber = faceNumber + 1;
    }

    public String checkColorNumber(char[] top, char[] bottom, char[] front, char[] back, char[] right, char[] left) {
        // TODO Auto-generated method stub
        int numberRed = 0;
        int numberOrange = 0;
        int numberBlue = 0;
        int numberGreen = 0;
        int numberYellow = 0;
        int numberWhite = 0;
        int numberError = 0;

        temptop = top;
        tempfront = front;
        tempback = back;
        tempbottom = bottom;
        tempright = right;
        templeft = left;
        for (i = 0; i < 9; i++) {
            if (temptop[i] == 'U') {
                numberRed = numberRed + 1;
            } else if (temptop[i] == 'F') {
                numberBlue = numberBlue + 1;
            } else if (temptop[i] == 'B') {
                numberGreen = numberGreen + 1;
            } else if (temptop[i] == 'D') {
                numberOrange = numberOrange + 1;
            } else if (temptop[i] == 'R') {
                numberWhite = numberWhite + 1;
            } else if (temptop[i] == 'L') {
                numberYellow = numberYellow + 1;
            } else {
                numberError = numberError + 1;
            }

            if (tempfront[i] == 'U') {
                numberRed = numberRed + 1;
            } else if (tempfront[i] == 'F') {
                numberBlue = numberBlue + 1;
            } else if (tempfront[i] == 'B') {
                numberGreen = numberGreen + 1;
            } else if (tempfront[i] == 'D') {
                numberOrange = numberOrange + 1;
            } else if (tempfront[i] == 'R') {
                numberWhite = numberWhite + 1;
            } else if (tempfront[i] == 'L') {
                numberYellow = numberYellow + 1;
            } else {
                numberError = numberError + 1;
            }

            if (templeft[i] == 'U') {
                numberRed = numberRed + 1;
            } else if (templeft[i] == 'F') {
                numberBlue = numberBlue + 1;
            } else if (templeft[i] == 'B') {
                numberGreen = numberGreen + 1;
            } else if (templeft[i] == 'D') {
                numberOrange = numberOrange + 1;
            } else if (templeft[i] == 'R') {
                numberWhite = numberWhite + 1;
            } else if (templeft[i] == 'L') {
                numberYellow = numberYellow + 1;
            } else {
                numberError = numberError + 1;
            }

            if (tempback[i] == 'U') {
                numberRed = numberRed + 1;
            } else if (tempback[i] == 'F') {
                numberBlue = numberBlue + 1;
            } else if (tempback[i] == 'B') {
                numberGreen = numberGreen + 1;
            } else if (tempback[i] == 'D') {
                numberOrange = numberOrange + 1;
            } else if (tempback[i] == 'R') {
                numberWhite = numberWhite + 1;
            } else if (tempback[i] == 'L') {
                numberYellow = numberYellow + 1;
            } else {
                numberError = numberError + 1;
            }

            if (tempright[i] == 'U') {
                numberRed = numberRed + 1;
            } else if (tempright[i] == 'F') {
                numberBlue = numberBlue + 1;
            } else if (tempright[i] == 'B') {
                numberGreen = numberGreen + 1;
            } else if (tempright[i] == 'D') {
                numberOrange = numberOrange + 1;
            } else if (tempright[i] == 'R') {
                numberWhite = numberWhite + 1;
            } else if (tempright[i] == 'L') {
                numberYellow = numberYellow + 1;
            } else {
                numberError = numberError + 1;
            }

            if (tempbottom[i] == 'U') {
                numberRed = numberRed + 1;
            } else if (tempbottom[i] == 'F') {
                numberBlue = numberBlue + 1;
            } else if (tempbottom[i] == 'B') {
                numberGreen = numberGreen + 1;
            } else if (tempbottom[i] == 'D') {
                numberOrange = numberOrange + 1;
            } else if (tempbottom[i] == 'R') {
                numberWhite = numberWhite + 1;
            } else if (tempbottom[i] == 'L') {
                numberYellow = numberYellow + 1;
            } else {
                numberError = numberError + 1;
            }
        }


        if (numberYellow == 9 && numberBlue == 9 && numberGreen == 9
                && numberRed == 9 && numberOrange == 9 && numberWhite == 9) {
            return "no error";
        }
		/*if (numberError==1) {
			return true;
		}*/
        String resultofrubik = "";
        if (numberRed != 9) {
            resultofrubik = resultofrubik + "U: " + (numberRed - 9);
        }
        if (numberBlue != 9) {
            resultofrubik = resultofrubik + " F: " + (numberBlue - 9);
        }
        if (numberOrange != 9) {
            resultofrubik = resultofrubik + " D: " + (numberOrange - 9);
        }
        if (numberWhite != 9) {
            resultofrubik = resultofrubik + " R: " + (numberWhite - 9);
        }
        if (numberGreen != 9) {
            resultofrubik = resultofrubik + " B: " + (numberGreen - 9);
        }
        if (numberYellow != 9) {
            resultofrubik = resultofrubik + " L: " + (numberYellow - 9);
        }
        return resultofrubik;
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release(); // release the camera for other applications
            mCamera = null;
        }
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private PictureCallback mPicture = new PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {


            faceNumber = faceNumberMainActivity;
            ////////////////////////////
            switch (faceNumber) {

                case 0:    // top FACE:
                    bmpPhoto = BitmapFactory.decodeByteArray(data, 0, data.length);        //may place this statement before switch
                    read_top_surface();
                {
                    // after taking six pictures, then start to send the color values back to main activity intent.
                    // get rubik cube positions' color
                    temptop = toptemp;
                    String stringTop = String.valueOf(temptop);        // convert char array to a String

                    Bundle basket = new Bundle();
                    basket.putString("allsixfaces", RubikTempStringMainActivity + stringTop); // let basket holds msg of string
                    Intent sendMessageIntent = new Intent(ReadSurfaceColor.this, MainActivity.class);//choose an activity to send this message
                    sendMessageIntent.putExtras(basket);                //message is loaded into the basket, and ready to send to MainActivity.class
                    startActivity(sendMessageIntent);                    //message is sent
					
/*!!!!!!!!*/
                    bmpPhoto.recycle();                                    //may put it in onDestroy();
                    finish();
                    onStop();
                }
                break;
                case 1:    // front FACE:
                    bmpPhoto = BitmapFactory.decodeByteArray(data, 0, data.length);
                    read_front_surface();
                {
                    tempfront = fronttemp;
                    String stringFront = String.valueOf(tempfront);        // convert char array to a String
                    Bundle basket = new Bundle();
                    basket.putString("allsixfaces", RubikTempStringMainActivity + stringFront); // let basket holds msg of string
                    Intent sendMessageIntent = new Intent(ReadSurfaceColor.this, MainActivity.class);//choose an activity to send this message
                    sendMessageIntent.putExtras(basket);                //message is loaded into the basket, and ready to send to MainActivity.class
                    startActivity(sendMessageIntent);                    //message is sent

                    bmpPhoto.recycle();
                    finish();
                    onStop();
                }
                break;

                case 2:    // bottom FACE:
                    bmpPhoto = BitmapFactory.decodeByteArray(data, 0, data.length);
                    read_bottom_surface();
                {
                    tempbottom = bottomtemp;
                    String stringBottom = String.valueOf(tempbottom);
                    Bundle basket = new Bundle();
                    basket.putString("allsixfaces", RubikTempStringMainActivity + stringBottom); // let basket holds msg of string
                    Intent sendMessageIntent = new Intent(ReadSurfaceColor.this, MainActivity.class);//choose an activity to send this message
                    sendMessageIntent.putExtras(basket);                //message is loaded into the basket, and ready to send to MainActivity.class
                    startActivity(sendMessageIntent);                    //message is sent

                    bmpPhoto.recycle();
                    finish();
                    onStop();
                }
                break;
                case 3:    // RIGHTFACE:
                    bmpPhoto = BitmapFactory.decodeByteArray(data, 0, data.length);
                    read_right_surface();
                {

                    tempright = righttemp;
                    String stringrRight = String.valueOf(tempright);
                    Bundle basket = new Bundle();
                    basket.putString("allsixfaces", RubikTempStringMainActivity + stringrRight); // let basket holds msg of string
                    Intent sendMessageIntent = new Intent(ReadSurfaceColor.this, MainActivity.class);//choose an activity to send this message
                    sendMessageIntent.putExtras(basket);                //message is loaded into the basket, and ready to send to MainActivity.class
                    startActivity(sendMessageIntent);                    //message is sent

                    bmpPhoto.recycle();
                    finish();
                    onStop();
                }
                break;

                case 4:    // back FACE:
                    bmpPhoto = BitmapFactory.decodeByteArray(data, 0, data.length);
                    read_back_surface();
                {
                    tempback = backtemp;
                    String stringBack = String.valueOf(tempback);
                    Bundle basket = new Bundle();
                    basket.putString("allsixfaces", RubikTempStringMainActivity + stringBack); // let basket holds msg of string
                    Intent sendMessageIntent = new Intent(ReadSurfaceColor.this, MainActivity.class);//choose an activity to send this message
                    sendMessageIntent.putExtras(basket);        //message is loaded into the basket, and ready to send to MainActivity.class
                    startActivity(sendMessageIntent);            //message is sent
                    bmpPhoto.recycle();
                    finish();
                    onStop();
                }
                break;
                case 5:    // LEFTFACE:
                    bmpPhoto = BitmapFactory.decodeByteArray(data, 0, data.length);
                    read_left_suface();
                {
                    templeft = lefttemp;
                    String stringLeft = String.valueOf(templeft);
                    Bundle basket = new Bundle();
                    basket.putString("allsixfaces", RubikTempStringMainActivity + stringLeft); // let basket holds msg of string
                    Intent sendMessageIntent = new Intent(ReadSurfaceColor.this, MainActivity.class);//choose an activity to send this message
                    sendMessageIntent.putExtras(basket);        //message is loaded into the basket, and ready to send to MainActivity.class
                    startActivity(sendMessageIntent);            //message is sent
                    bmpPhoto.recycle();
                    finish();
                    onStop();
                }
                break;
                default:
                    break;
            }

            camera.startPreview();// added statement
            Log.d("AndroidCameraActivity", "" + pixel);

//			onBackPressed();
//			finish();
        }
    };

    // Create a File for saving an image or video
    private static File getOutputMediaFile(int type) {

        File mediaStorageDir = new File(
                Environment.getExternalStoragePublicDirectory(// //////
                        Environment.DIRECTORY_PICTURES), "MyCameraApp");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d("AndroidCameraActivity", "failed to create directory");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera(); // release the camera immediately on pause event
    }
	

}