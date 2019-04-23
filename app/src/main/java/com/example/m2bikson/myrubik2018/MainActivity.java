package com.example.m2bikson.myrubik2018;
/**
 * Sept.25 expand the ranges of color values
 * Sept 24: new steps:
 * check buttom
 * if no error , go to operationlist buttom and then solve button
  * Aug 25. If the color is recognized correctly, this program will solve the Rubik Cube.
 * may add some error check to recognize color.
 * <p>
 * on Aug. 3, pulling  and pushing  degree is 0x60(96 degree), may add 2-4 degrees. now choose 0x63 degree
 * motor B is OK , but power is 0x32 (50%), degree 0x147 (327degree), adjust degree is 0x38(56degree)
 * almost solve the cube. 5 times error in pulling and pushing.
 * <p>
 * on Aug. 3, before step 3, can click display to check all six sides are right.
 * on July 29, plugging Lejos into Eclipse
 * On July 28, 2014
 * now this is copy can recognize the color in 10am-5pm fine day. and return, and start solve the cube.
 * <p>
 * step 1: click ToStart button, and take 6 pictures of cube, following the order : top , right , bottom , left , back and front.
 * step 2: just before save the picture of front, turn on NXT and put the arm of NXT in right position
 * step 3: click solveRubik button, the NXT will start to solve the cube by itself.
 * <p>
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener {

    final static int cameraData = 0;

    public static final int MESSAGE_TOAST = 1;
    public static final int MESSAGE_STATE_CHANGE = 2;
    public static final String TOAST = "toast";

    private int mState = NXTTalker.STATE_NONE;
    private int mySavedState = NXTTalker.STATE_NONE;
    private static final int REQUEST_CONNECT_DEVICE = 2;

    private boolean NO_BT = false;
    private boolean myNewLaunch = true;
    private boolean myRegulateSpeed = true;
    private boolean mySynchronizeMotors = true;
    private String myDeviceAddress = null;

    private BluetoothAdapter myBluetoothAdapter;
    private NXTTalker myNXTTalker;

    static int faceNumber = 0;
    static int TOP = 0;
    static int FRONT = 1;
    static int BOTTOM = 2;
    static int RIGHT = 3;
    static int BACK = 4;
    static int LEFT = 5;

    static String RubikTempString = "";        //used to keep the color result in format of string. //static means we do not need to instance it before we can use it.

    String solutionStringMain;

    static boolean haveTakenPicture = false;

    RubikCube myRubikCube = new RubikCube();

    char[] top = {'D', 'L', 'D', 'B', 'U', 'B', 'B', 'R', 'B'};
    char[] bottom = {'F', 'L', 'F', 'F', 'D', 'F', 'D', 'R', 'D'};
    char[] front = {'R', 'F', 'L', 'U', 'F', 'D', 'R', 'B', 'L'};
    char[] back = {'R', 'D', 'R', 'F', 'B', 'B', 'L', 'U', 'L'};
    char[] right = {'U', 'D', 'B', 'R', 'R', 'L', 'U', 'U', 'B'};
    char[] left = {'F', 'U', 'U', 'R', 'L', 'L', 'F', 'D', 'U'};

    char[] temptop = {'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U', 'U'}; // temptop and corrected are arrays which are used as variables in procedure
    char[] tempfront = {'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F', 'F'}; // declare array
    char[] tempback = {'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B', 'B'}; // declare array
    char[] tempright = {'R', 'R', 'R', 'R', 'R', 'R', 'R', 'R', 'R'}; // declare array
    char[] templeft = {'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L', 'L'}; // declare array
    char[] tempbottom = {'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D'}; // declare array

    //try to solve the issue which can not keep the return values after recognize colors
    //!!!!!!!!!!!!!!!!!!
    //!!!!!!!!!!!!!!!!!!   correct the color arrays if there is wrong after click display buttom.
    //following the below figure with orientational letters: U(up) D(down) B(back) R(right) L(left) F(front)
    char[] correctedtop = {'D', 'R', 'R', 'D', 'U', 'U', 'F', 'R', 'R'}; // temptop and corrected are arrays which are used as variables in procedure
    char[] correctedfront = {'L', 'B', 'U', 'L', 'F', 'L', 'U', 'F', 'F'}; // declare array
    char[] correctedbottom = {'B', 'D', 'D', 'R', 'D', 'L', 'L', 'F', 'L'}; // declare array
    char[] correctedright = {'F', 'R', 'D', 'B', 'R', 'U', 'R', 'D', 'F'}; // declare array
    char[] correctedback = {'B', 'D', 'L', 'F', 'B', 'B', 'D', 'L', 'B'}; // declare array
    char[] correctedleft = {'B', 'B', 'U', 'U', 'L', 'U', 'U', 'F', 'R'}; // declare array


    char[] messageInCharArray = {'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',  //
            'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',
            'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',
            'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',
            'E', 'E', 'E', 'E', 'E', 'E'};

    char[] messageInCharArrayx = {'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',
            'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',
            'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',
            'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E', 'E',
            'E', 'E', 'E', 'E', 'E', 'E'};

    /**
     |****U=4*****|
     |*U0**U1**U2*|
     |************|
     |*U3**U4**U5*|
     |************|
     |*U6**U7**U8*|
     |************|
     |****L=0******|****F=2*****|****R=0*****|****B=3*****|
     |*L0**L1**L2**|*F0**F1**F2*|*R0**R1**F2*|*B0**B1**B2*|
     |*************|************|************|************|
     |*L3**L4**L5**|*F3**F4**F5*|*R3**R4**R5*|*B3**B4**B5*|
     |*************|************|************|************|
     |*L6**L7**L8**|*F6**F7**F8*|*R6**R7**R8*|*B6**B7**B8*|
     |*************|************|************|************|
     |****D=5*****|
     |*D0**D1**D2*|
     |************|
     |*D3**D4**D5*|
     |************|
     |*D6**D7**D8*|
     |************|
     */

    /**
     * first take six pictures
     */
    Button bttnTop;
    Button bttnFront;
    Button bttnBottom;
    Button bttnRight;
    Button bttnBack;
    Button bttnLeft;
    /**
     * after take six pictures,
     */
    Button CheckCubeButton;            // step 1: to check the result after recognizing color, if there is mistake , it will display
    Button bttnreadyToCorrect;        // step 2: button to update the color if there is mistake:  taking pictures // button, ImageView, TextView are put the front of var in cativity_main.xlm
    Button bttnOperationList;        // step 3: find the solution of the Rubik. Solution will display.
    Button bttnreadyToSolve;        // step 4: solve and roll the cube

    TextView checkingResultTextView;

    EditText displayTop;
    EditText displayBottom;
    EditText displayFront;
    EditText displayBack;
    EditText displayRight;
    EditText displayLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        displayTop      = (EditText) findViewById(R.id.editTextTop);
        displayBottom   = (EditText) findViewById(R.id.editTextBottom);
        displayFront    = (EditText) findViewById(R.id.editTextFront);
        displayBack     = (EditText) findViewById(R.id.editTextBack);
        displayRight    = (EditText) findViewById(R.id.editTextRight);
        displayLeft     = (EditText) findViewById(R.id.editTextLeft);

        checkingResultTextView = (TextView) findViewById(R.id.textViewOperationResult);

        CheckCubeButton     = (Button) findViewById(R.id.bttnCheckRubikcube);
        bttnreadyToCorrect  = (Button) findViewById(R.id.bttnCorrect);
        bttnreadyToSolve    = (Button) findViewById(R.id.bttnReadySlv);
        bttnOperationList   = (Button) findViewById(R.id.bttnPreSolve);        //prepare to solve: operation list

        bttnTop     = (Button) findViewById(R.id.buttonTop);
        bttnFront   = (Button) findViewById(R.id.buttonFront);
        bttnBottom  = (Button) findViewById(R.id.buttonBottom);
        bttnRight   = (Button) findViewById(R.id.buttonRight);
        bttnBack    = (Button) findViewById(R.id.buttonBack);
        bttnLeft    = (Button) findViewById(R.id.buttonLeft);

        CheckCubeButton.setOnClickListener(this);
        bttnreadyToCorrect.setOnClickListener(this);
        bttnreadyToSolve.setOnClickListener(this);
        bttnOperationList.setOnClickListener(this);

        bttnTop.setOnClickListener(this);
        bttnFront.setOnClickListener(this);
        bttnBottom.setOnClickListener(this);
        bttnRight.setOnClickListener(this);
        bttnBack.setOnClickListener(this);
        bttnLeft.setOnClickListener(this);

        //below is about Bluetooth
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();            // ?
        myNXTTalker = new NXTTalker(mHandler);
        Toast.makeText(getApplicationContext(), "connecting motors...", Toast.LENGTH_SHORT).show();

        if (savedInstanceState != null) {
            haveTakenPicture = true;
        }

        if (haveTakenPicture == true) {
            switch (faceNumber) {
                case 0:        //TOP
                    Bundle gotMessageTop = getIntent().getExtras();                                // has bug
                    String messageFromReadSurfaceColorTop = gotMessageTop.getString("allsixfaces");    // get the string of top side from ReadSurfaceColor.class
                    checkingResultTextView.setText(messageFromReadSurfaceColorTop);
                    //messageInCharArray = messageFromReadSurfaceColorTop	.toCharArray(); 		// To convert a String to char array
                    RubikTempString = messageFromReadSurfaceColorTop;                            //each calling, will return sides and keep color result of one side of rubik; this like sum = sum +newNumber
                    break;

                case 1:            //FRONT:
                    Bundle gotMessageFront = getIntent().getExtras();                                    //has bug
                    String messageFromReadSurfaceColorFront = gotMessageFront.getString("allsixfaces");    //get the string of front side from ReadSurfaceColor.class
                    checkingResultTextView.setText(messageFromReadSurfaceColorFront);
                    //messageInCharArray = messageFromReadSurfaceColorFront.toCharArray(); 				//To convert a String to char array
                    RubikTempString = messageFromReadSurfaceColorFront;
                    break;
                case 2:            //BOTTOM:
                    Bundle gotMessageBottom = getIntent().getExtras();                                        //has bug
                    String messageFromReadSurfaceColorBottom = gotMessageBottom.getString("allsixfaces");    //get the string from ReadSurfaceColor.class
                    checkingResultTextView.setText(messageFromReadSurfaceColorBottom);
                    //messageInCharArray = messageFromReadSurfaceColorBottom.toCharArray(); 				//To convert a String to char array
                    RubikTempString = messageFromReadSurfaceColorBottom;
                    break;
                case 3:            //RIGHT:
                    Bundle gotMessageRight = getIntent().getExtras();                                    //has bug
                    String messageFromReadSurfaceColorRight = gotMessageRight.getString("allsixfaces");    //get the string from ReadSurfaceColor.class
                    checkingResultTextView.setText(messageFromReadSurfaceColorRight);
                    //messageInCharArray = messageFromReadSurfaceColorRight.toCharArray(); 				//To convert a String to char array
                    RubikTempString = messageFromReadSurfaceColorRight;
                    break;
                case 4:            //BACK:
                    Bundle gotMessageBack = getIntent().getExtras();                                    //has bug
                    String messageFromReadSurfaceColorBack = gotMessageBack.getString("allsixfaces");    //get the string from ReadSurfaceColor.class
                    checkingResultTextView.setText(messageFromReadSurfaceColorBack);
                    //messageInCharArray = messageFromReadSurfaceColorBack.toCharArray(); 				//To convert a String to char array
                    RubikTempString = messageFromReadSurfaceColorBack;
                    break;
                case 5:            //LEFT:
                    Bundle gotMessageLeft = getIntent().getExtras();                                    //has bug
                    String messageFromReadSurfaceColorLeft = gotMessageLeft.getString("allsixfaces");    //get the string from ReadSurfaceColor.class
                    checkingResultTextView.setText(messageFromReadSurfaceColorLeft);
                    messageInCharArray = messageFromReadSurfaceColorLeft.toCharArray();                //To convert a String to char array
                    RubikTempString = messageFromReadSurfaceColorLeft;
                    for (int mn = 0; mn < 9; mn++) {
                        temptop[mn]     = messageInCharArray[mn];
                        tempfront[mn]   = messageInCharArray[mn + 9];
                        tempbottom[mn]  = messageInCharArray[mn + 18];
                        tempright[mn]   = messageInCharArray[mn + 27];
                        tempback[mn]    = messageInCharArray[mn + 36];
                        templeft[mn]    = messageInCharArray[mn + 45];
                    }
                    break;
                default:
                    break;
            }
        }
    }

    //this bring copies of MainActivity, and the copies will be slow to find the solution of Rubik(giving the Operations List).
    //the real reason is the recognition has mistakes. when I check the number of color, it will be okay when the number is okay.
    //working in debug mode, it will take minutes(3-30 minutes) to find solution to a Rubik.

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
        haveTakenPicture = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.imgReturn:
                break;
            case R.id.buttonTop:

                /**
                 * in order to send commands to NXT, we need to maintain the BT connection: step 1:connecting, step 2:using, step 3: disconnecting.
                 */
                if (!NO_BT) {
                    findBrick();
                } else {
                    mState = NXTTalker.STATE_CONNECTED;
                    displayState();
                }
                myNXTTalker.openTop();            //move the bar away and take the picture of top side

                Intent startReadSurfaceColorActivity = new Intent(); // this has the same result
                startReadSurfaceColorActivity.setClass(getApplicationContext(), ReadSurfaceColor.class);
                startActivity(startReadSurfaceColorActivity);
                faceNumber = 0;        //faceNumber is used to control and choose pictures which are handled by program.
                myNXTTalker.stop();
                haveTakenPicture = true;

                break;
            case R.id.buttonFront:
                if (!NO_BT) {
                    findBrick();
                } else {
                    mState = NXTTalker.STATE_CONNECTED;
                    displayState();
                }
                myNXTTalker.turnFrontUp();
                Intent startReadSurfaceColorActivityFront = new Intent(); // this has the same result
                // to start intent
                startReadSurfaceColorActivityFront.setClass(getApplicationContext(), ReadSurfaceColor.class);
                startActivity(startReadSurfaceColorActivityFront);
                faceNumber = FRONT;
                myNXTTalker.stop();
                haveTakenPicture = true;
            
                break;
            case R.id.buttonBottom:
                if (!NO_BT) {
                    findBrick();
                } else {
                    mState = NXTTalker.STATE_CONNECTED;
                    displayState();
                }
                myNXTTalker.turnBottomUp();
                Intent startReadSurfaceColorActivityBottom = new Intent(); // this has the same result
                // to start intent
                startReadSurfaceColorActivityBottom.setClass(getApplicationContext(), ReadSurfaceColor.class);
                startActivity(startReadSurfaceColorActivityBottom);
                faceNumber = BOTTOM;
                myNXTTalker.stop();
                haveTakenPicture = true;

                break;
            case R.id.buttonRight:
                if (!NO_BT) {
                    findBrick();
                } else {
                    mState = NXTTalker.STATE_CONNECTED;
                    displayState();
                }
                myNXTTalker.turnRightUp();
                Intent startReadSurfaceColorActivityRight = new Intent(); // this has the same result
                // to start intent
                startReadSurfaceColorActivityRight.setClass(getApplicationContext(), ReadSurfaceColor.class);
                startActivity(startReadSurfaceColorActivityRight);
                faceNumber = RIGHT;
                myNXTTalker.stop();
                haveTakenPicture = true;

                break;
            case R.id.buttonBack:
                if (!NO_BT) {
                    findBrick();
                } else {
                    mState = NXTTalker.STATE_CONNECTED;
                    displayState();
                }
                myNXTTalker.turnBackUp();
                Intent startReadSurfaceColorActivityBack = new Intent(); // this has the same result
                // to start intent
                startReadSurfaceColorActivityBack.setClass(getApplicationContext(), ReadSurfaceColor.class);
                startActivity(startReadSurfaceColorActivityBack);
                faceNumber = BACK;
                myNXTTalker.stop();
                haveTakenPicture = true;

                break;
            case R.id.buttonLeft:
                if (!NO_BT) {
                    findBrick();
                } else {
                    mState = NXTTalker.STATE_CONNECTED;
                    displayState();
                }
                myNXTTalker.turnLeftUp();
                Intent startReadSurfaceColorActivityLeft = new Intent(); // this has the same result
                // to start intent
                startReadSurfaceColorActivityLeft.setClass(getApplicationContext(), ReadSurfaceColor.class);
                startActivity(startReadSurfaceColorActivityLeft);
                faceNumber = LEFT;
                myNXTTalker.stop();
                haveTakenPicture = true;

                break;

            case R.id.bttnCheckRubikcube: {
                // randomCube rubikCube = new randomCube();
                {
                    ReadSurfaceColor myReadSurfaceColorA = new ReadSurfaceColor();
            
                    top     = temptop;
                    front   = tempfront;
                    back    = tempback;
                    right   = tempright;
                    left    = templeft;
                    bottom  = tempbottom;

                    String inputPictureStringTop = new String(top);
                    String inputPictureStringBottom = new String(bottom);
                    String inputPictureStringFront = new String(front);
                    String inputPictureStringBack = new String(back);
                    String inputPictureStringRight = new String(right);
                    String inputPictureStringLeft = new String(left);

                    displayTop.setText(inputPictureStringTop);
                    displayBottom.setText(inputPictureStringBottom);
                    displayFront.setText(inputPictureStringFront);
                    displayBack.setText(inputPictureStringBack);
                    displayRight.setText(inputPictureStringRight);
                    displayLeft.setText(inputPictureStringLeft);

                    String tempTeststringA = myReadSurfaceColorA.checkColorNumber(top, bottom, front, back, right, left);
                    checkingResultTextView.setText(tempTeststringA);
                }
            }
            break;
            case R.id.bttnCorrect:

                String TopString = displayTop.getText().toString();
                String FrontString = displayFront.getText().toString();
                String BottomString = displayBottom.getText().toString();
                String RightString = displayRight.getText().toString();
                String BackString = displayBack.getText().toString();
                String LeftString = displayLeft.getText().toString();

                TopString = TopString.toUpperCase();
                correctedtop = TopString.toCharArray();

                FrontString = FrontString.toUpperCase();
                correctedfront = FrontString.toCharArray();

                BottomString = BottomString.toUpperCase();
                correctedbottom = BottomString.toCharArray();

                RightString = RightString.toUpperCase();
                correctedright = RightString.toCharArray();

                BackString = BackString.toUpperCase();
                correctedback = BackString.toCharArray();

                LeftString = LeftString.toUpperCase();
                correctedleft = LeftString.toCharArray();

                temptop = correctedtop;        //because switch case of R.id.bttnPreSolve will assign top = temptop; and do not need click here if no mistake
                tempfront = correctedfront;
                tempback = correctedback;
                tempright = correctedright;
                templeft = correctedleft;
                tempbottom = correctedbottom;

                //colorConvert();
                ReadSurfaceColor myReadSurfaceColorB = new ReadSurfaceColor();
                String tempTeststringB = myReadSurfaceColorB.checkColorNumber(correctedtop, correctedbottom, correctedfront, correctedback, correctedright, correctedleft);
                checkingResultTextView.setText(tempTeststringB);
                break;
            case R.id.bttnPreSolve:                //get all values of six faces from ReadSurfaceColor activity

                top = temptop;
                front = tempfront;
                back = tempback;
                right = tempright;
                left = templeft;
                bottom = tempbottom;

                String inputStringTop = new String(top);
                String inputStringBottom = new String(bottom);
                String inputStringFront = new String(front);
                String inputStringBack = new String(back);
                String inputStringRight = new String(right);
                String inputStringLeft = new String(left);

                displayTop.setText(inputStringTop);
                displayBottom.setText(inputStringBottom);
                displayFront.setText(inputStringFront);
                displayBack.setText(inputStringBack);
                displayRight.setText(inputStringRight);
                displayLeft.setText(inputStringLeft);

                solutionStringMain = myRubikCube.getRubikValueAndSolve(top, bottom, front, back, right, left);
                Toast.makeText(getApplicationContext(), "operations: " + solutionStringMain, Toast.LENGTH_SHORT).show();

                break;
            case R.id.bttnReadySlv:        //start rolling the rubik cube.

                myNXTTalker.returnReadyPosition();
                myNXTTalker.getRubikRoll(solutionStringMain);
                myNXTTalker.playTone();

                break;
            default:
                break;
        }
    }

    /**
     * colorConvert are not called in this program. It may be called if we want to have color(ROGBYW) instead of position(UBFRLD)
     */
    private void colorConvert() {
        // TODO Auto-generated method stub
        for (int i = 0; i < 9; i++) {
            if (correctedtop[i] == 'R') {
                top[i] = 'U';
            } else if (correctedtop[i] == 'B') {
                top[i] = 'F';
            } else if (correctedtop[i] == 'G') {
                top[i] = 'B';
            } else if (correctedtop[i] == 'O') {
                top[i] = 'D';
            } else if (correctedtop[i] == 'W') {
                top[i] = 'R';
            } else if (correctedtop[i] == 'Y') {
                top[i] = 'L';
            }

            if (correctedfront[i] == 'R') {
                front[i] = 'U';
            } else if (correctedfront[i] == 'B') {
                front[i] = 'F';
            } else if (correctedfront[i] == 'G') {
                front[i] = 'B';
            } else if (correctedfront[i] == 'O') {
                front[i] = 'D';
            } else if (correctedfront[i] == 'W') {
                front[i] = 'R';
            } else if (correctedfront[i] == 'Y') {
                front[i] = 'L';
            }

            if (correctedleft[i] == 'R') {
                left[i] = 'U';
            } else if (correctedleft[i] == 'B') {
                left[i] = 'F';
            } else if (correctedleft[i] == 'G') {
                left[i] = 'B';
            } else if (correctedleft[i] == 'O') {
                left[i] = 'D';
            } else if (correctedleft[i] == 'W') {
                left[i] = 'R';
            } else if (correctedleft[i] == 'Y') {
                left[i] = 'L';
            }

            if (correctedback[i] == 'R') {
                back[i] = 'U';
            } else if (correctedback[i] == 'B') {
                back[i] = 'F';
            } else if (correctedback[i] == 'G') {
                back[i] = 'B';
            } else if (correctedback[i] == 'O') {
                back[i] = 'D';
            } else if (correctedback[i] == 'W') {
                back[i] = 'R';
            } else if (correctedback[i] == 'Y') {
                back[i] = 'L';
            }

            if (correctedright[i] == 'R') {
                right[i] = 'U';
            } else if (correctedright[i] == 'B') {
                right[i] = 'F';
            } else if (correctedright[i] == 'G') {
                right[i] = 'B';
            } else if (correctedright[i] == 'O') {
                right[i] = 'D';
            } else if (correctedright[i] == 'W') {
                right[i] = 'R';
            } else if (correctedright[i] == 'Y') {
                right[i] = 'L';
            }

            if (correctedbottom[i] == 'R') {
                bottom[i] = 'U';
            } else if (correctedbottom[i] == 'B') {
                bottom[i] = 'F';
            } else if (correctedbottom[i] == 'G') {
                bottom[i] = 'B';
            } else if (correctedbottom[i] == 'O') {
                bottom[i] = 'D';
            } else if (correctedbottom[i] == 'W') {
                bottom[i] = 'R';
            } else if (correctedbottom[i] == 'Y') {
                bottom[i] = 'L';
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!NO_BT) {
            myDeviceAddress = "00:16:53:15:BC:A6";// added//break6
            BluetoothDevice mDevice = myBluetoothAdapter.getRemoteDevice(myDeviceAddress);
            myNXTTalker.connect(mDevice);//break7
        }
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_STATE_CHANGE:
                    mState = msg.arg1;
                    displayState();
                    break;
            }
        }
    };


    //above is added for bluetooth after testing read color of faces

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    private void findBrick() {
//        Intent intent = new Intent(this, ChooseDeviceActivity.class);
//        startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
    }

    private void displayState() {
        String stateText = null;
        int color = 0;
        switch (mState) {
            case NXTTalker.STATE_NONE:
                stateText = "Not connected";
                break;
            case NXTTalker.STATE_CONNECTING:
                stateText = "Connecting...";
                color = 0xffffff00;
                break;
            case NXTTalker.STATE_CONNECTED:
                stateText = "Connected";
                color = 0xff00ff00;
                break;
        }
    }
}
