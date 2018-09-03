package abscanner.bscanner5;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class Tab2Activity extends AppCompatActivity implements SensorEventListener {
    // Input: -  col-size,row-size input by user
    //        - name of project retrieved from "Magdat/project_name.txt"
    // Output: measured array saved in "Magdat/***.txt"; ***; project_name
    //         gridsize: saved in "Magdat/***_gridsize.txt"; project_name
    // --
    // parameters throughout program
    int MAX_GRow=500;
    int MAX_GCol=500;
    int gRow=2, gCol=2;
    float azimut;
    int FMSZ_MODE=0;
    //
    int[] myFCell=new int[MAX_GRow*MAX_GCol];
    int[] myCurr= new int[2];
    int gblNData=25;
    int color=0xfff00000;
    int[] gParams={1,1,1,1};

    //EditText etnCol,etnRow;
    TextView tvStatus;
    //---------------------------------------
    float valX=0,valY=0,valZ=0;
    //--
    int idxMag=0;
    //int idx=0;
    int nData=10000; //anArray = new int[10];
    // measured data
    float[] vMag=new float[3*nData];
    //float[] xyFMag=new float[5*
    int ctr=0;
    // -------------------------
    // sensor
    private SensorManager mSensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    //
    Button buttonMeasure;//ZZ;
    //Button buttonMeasureZZAuto;

    //Tambahan Save Data
    //public String pName="pName"; //project name
    public EditText etPName;
    public String sfpjName="project_name.txt";

    //Tambahan Setting
//    private CheckBox checkBoxRememberMe;

    private GridView customCanvas;
    //--

    //--
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grid0);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buttonMeasure = (Button) findViewById(R.id.btnMeasure);
        // --
        buttonMeasure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmdMeasureZZ(v);
            }
        });
        // --
        customCanvas = (GridView) findViewById(R.id.signature_canvas);

//        //Tambahan Setting
//        checkBoxRememberMe = (CheckBox) findViewById(R.id.checkBoxRememberMe);
//
//        //Validasi Grid Size
//        if(!new PrefManager(this).isDeleteGrid()){
//            //kolom dan baris sudah disimpan
//            //start grid window//
//            cmdGenGrid();
//        }

        // init sensor
        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }
    // --

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_saveproject:
                //get project name
                showNameProjectDialog();
                break;
            case R.id.action_defgrid:
                // define grid size
                showInputDialog();
                //cmdGenGrid();
                break;
            case R.id.action_mszmode:
                Toast.makeText(getApplicationContext(), "Set measurement mode", Toast.LENGTH_SHORT).show();
                break;
                // manual/auto
            case R.id.group_mszmode_manual:
                buttonMeasure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cmdMeasureZZ(v);
                    }
                });

                FMSZ_MODE=0;
                Toast.makeText(this, "Manual mode", Toast.LENGTH_SHORT).show();                //cmdGenGrid();
                if (item.isChecked()) item.setChecked(false);
                    else item.setChecked(true);
                break;
            case R.id.group_mszmode_auto:
                //cmdGenGrid();
                buttonMeasure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cmdMeasureZZAuto(v);
                    }
                });

                if (item.isChecked()) item.setChecked(false);
                    else item.setChecked(true);
                FMSZ_MODE=1;
                Toast.makeText(this, "Automatic mode", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_setdelay:
                Toast.makeText(getApplicationContext(), "Set delay", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_refreshgrid:
                // regenerate grid
                cmdGenGrid();
                Toast.makeText(getApplicationContext(), "Refresh grid", Toast.LENGTH_SHORT).show();
                break;
            }

        return super.onOptionsItemSelected(item);
    }
    // --
    protected void showInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(Tab2Activity.this);
        View promptView = layoutInflater.inflate(R.layout.dialog_gridsize, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Tab2Activity.this);
        alertDialogBuilder.setView(promptView);

        // init
        final EditText etRow = (EditText) promptView.findViewById(R.id.etDlgRowNumber);
        final EditText etCol = (EditText) promptView.findViewById(R.id.etDlgColNumber);
        // update with previous values
        etRow.setText(Integer.toString(gRow));
        etCol.setText(Integer.toString(gCol));

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //EditText etRow= (EditText) findViewById(R.id.etDlgRowNumber);
                        //EditText etCol= (EditText) findViewById(R.id.etDlgColNumber);
                        gRow=Integer.valueOf(etRow.getText().toString()).intValue();
                        gCol=Integer.valueOf(etCol.getText().toString()).intValue();
                        //
                        customCanvas.gParams[2]=gRow;
                        customCanvas.gParams[3]=gCol;
                        // init cursor
                        customCanvas.gblNData=0;
                        idxMag=0;
                        // redraw canvas
                        customCanvas.clearCanvas();
                        Button msrButton=(Button) findViewById(R.id.btnMeasure);
                        msrButton.setEnabled(true);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    //Tambahan Save
    //--
    protected void showNameProjectDialog(){
        // get prompts.xml view
        LayoutInflater layoutInflater2 = LayoutInflater.from(Tab2Activity.this);
        View promptView2 = layoutInflater2.inflate(R.layout.dialog_nameproject, null);
        AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(Tab2Activity.this);
        alertDialogBuilder2.setView(promptView2);

        // init
        final EditText etPName = (EditText) promptView2.findViewById(R.id.etPName);

        // update with previous values
        //etPName.setText( );

        //setup a dialog window
        alertDialogBuilder2.setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        BScannerIOFile myBSIOFile=new BScannerIOFile();
                        String sBody= etPName.getText().toString();
                        myBSIOFile.createFile(sfpjName, sBody);

                        Button msrButton=(Button) findViewById(R.id.btnMeasure);
                        msrButton.setEnabled(true);
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert2 = alertDialogBuilder2.create();
        alert2.show();
    }
    //--

    // --
    public void onAccuracyChanged(Sensor sensor, int accuracy) {  }

    float[] mGravity;
    float[] mGeomagnetic;

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = event.values;
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = event.values;
        //tvStatus.setText("sensor->valX="+Float.toString(valX));
        if (mGravity != null && mGeomagnetic != null) {
            float R[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic);
            if (success) {
                float orientation[] = new float[3];
                SensorManager.getOrientation(R, orientation);
                azimut = orientation[0]; // orientation contains: azimut, pitch and roll
                valX = mGeomagnetic[0];
                valY = mGeomagnetic[1];
                valZ = mGeomagnetic[2];

            }
        }
    }
    // --

    public void cmdGenGrid() {
            // set parameters
        //
        //etnCol = (EditText) findViewById(R.id.etnCol);
        //etnRow = (EditText) findViewById(R.id.etnRow);
        int nRow=1, nCol=1,gH, gW;
        nRow=gRow; nCol=gCol;

        //nRow=Integer.valueOf(etnRow.getText().toString()).intValue();
        //nCol=Integer.valueOf(etnCol.getText().toString()).intValue();

        customCanvas.gParams[2]=nRow;
        customCanvas.gParams[3]=nCol;
        // init cursor
        customCanvas.gblNData=0;
        idxMag=0;
        // redraw canvas
        customCanvas.clearCanvas();


        // set measured button on
        Button msrButton=(Button) findViewById(R.id.btnMeasure);
        msrButton.setEnabled(true);

        // set button measure enabled
        //
        buttonMeasure.setEnabled(true);
        buttonMeasure.setClickable(true);

        //tvStatus = (TextView) findViewById(R.id.tvStatus);
        //tvStatus.setText("Grid" + Float.toString(99) + ", row=" + Float.toString(nRow));
    }
    //---

    //--
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }
    // --
    public void cmdMeasureZZ(View v) {
        // set parameters
        int nRow=1, nCol=1,gH, gW;

        nRow=gRow;//Integer.valueOf(etnRow.getText().toString()).intValue();
        nCol=gCol;//Integer.valueOf(etnCol.getText().toString()).intValue();

        // perform measurement
        vMag[3 * idxMag] = valX;
        vMag[3 * idxMag + 1] = valY;
        vMag[3 * idxMag + 2] = valZ;
        // also fill second version

        //
        if (idxMag<gRow*gCol) {
            idxMag++;
            customCanvas.gblNData++;
        } else{
            buttonMeasure.setEnabled(false);
            buttonMeasure.setClickable(false);
        }
        //

        customCanvas.gParams[2]=nRow;
        customCanvas. gParams[3]=nCol;


        if (customCanvas.gblNData<nRow*nCol){
            //customCanvas.gblNData++;
            // -- debug --
         }
        else {
            // -- debug --
             //tvStatus.setText("Measurement done !");
            // save file
            cmdSave(v);
            // -- debug --
        }
        // redraw
        customCanvas.clearCanvas();
    }
    // --
    // --
    public void cmdMeasureZZAuto(View v) {
        // set parameters
        // perform measurement
        // get size from screen
        //
        //etnCol = (EditText) findViewById(R.id.etnCol);
        //etnRow = (EditText) findViewById(R.id.etnRow);
        int nRow=1, nCol=1,gH, gW;

        nRow=gRow;//Integer.valueOf(etnRow.getText().toString()).intValue();
        nCol=gCol;//Integer.valueOf(etnCol.getText().toString()).intValue();
        //
        //
        idxMag=0;
        customCanvas.gblNData=0;
        //tvStatus = (TextView) findViewById(R.id.tvStatus);
        //int ctr=0;
        int NDataTimed=nRow*nCol+1;
        int tdMs=500; // delay in millisecond
        final CountDownTimer myTimer=new CountDownTimer(NDataTimed*tdMs,tdMs) {
            //int ctr=0;
            @Override
            public void onTick(long millisUntilFinished) {
                vMag[3 * idxMag] = valX;
                vMag[3 * idxMag + 1] = valY;
                vMag[3 * idxMag + 2] = valZ;
                // also fill second version
                idxMag++;
                customCanvas.gblNData++;
                // redraw
                customCanvas.clearCanvas();

            }
            @Override
            public void onFinish() {
                //tvStatus.setText(Integer.toString(idxMag+100));
                cmdSaveOnThread();
                //
                buttonMeasure.setEnabled(false);
                buttonMeasure.setClickable(false);


            }
        };
        myTimer.start();
    }
    //--

    //---
    public void cmdSave(View v) {
        // Save data
        // read project name
        String spName = "project_name.txt";
        String tRoot = "Magdat";
        String storageState = Environment.getExternalStorageState();
        // connect to root directory
        File root = new File(Environment.getExternalStorageDirectory(), tRoot);
        File file = new File(root, spName);
        //**
        BufferedReader inputReader2 = null;
        try {
            inputReader2 = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String inputString2;
        String pjfString = null;
        try {
            if ((inputString2 = inputReader2.readLine()) != null) {
                pjfString = inputString2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create file for dimension of grid
        String rowcolFName = pjfString + "_gridsize.txt";
        String stRow=Integer.toString(gRow);//etnRow.getText().toString();
        String stCol=Integer.toString(gRow);//etnCol.getText().toString();
        String srcBody = stRow+"," +stCol;

        BScannerIOFile myBSIOFile=new BScannerIOFile();

        myBSIOFile.createFile(rowcolFName, srcBody);
        //
        String sFileName = pjfString + ".txt";
        String sBody = "";
        int m = 0;
        for (m = 0; m < idxMag; m++) {
            sBody = sBody + Float.toString(vMag[3 * m]) + ", " + Float.toString(vMag[3 * m + 1]) + ", " + Float.toString(vMag[3 * m + 2]) + "\n";
        }
        myBSIOFile.createFile(sFileName, sBody);
        //
        Toast.makeText(this, "Data Saved:" + sFileName, Toast.LENGTH_SHORT).show();

        // Create second kind of file
        // [x,y, value]
        //
        String sFileName2 = pjfString + "zz" + ".txt";
        sBody = "";
        BScannerMath myBSMath =new BScannerMath();
        int[] myRowCol=new int[2];
        int nRow=Integer.valueOf(stRow);
        int nCol=Integer.valueOf(stCol);
        //m = 0;
        for (m = 0; m < idxMag; m++) {
            // get coordinate
            myRowCol=myBSMath.getRowColZZ(m,nRow,nCol);

            sBody = sBody + String.valueOf(myRowCol[0])+", " + String.valueOf(myRowCol[1]) +
                    ", " +Float.toString(vMag[3 * m]) + ", " + Float.toString(vMag[3 * m + 1]) + ", " + Float.toString(vMag[3 * m + 2]) + "\n";
        }
        myBSIOFile.createFile(sFileName2, sBody);
        //
        Toast.makeText(this, "Data Saved:" + sFileName2, Toast.LENGTH_SHORT).show();


    }
    // --

    //---
    public void cmdSaveOnThread() {
        // Save data
        // read project name
        String spName = "project_name.txt";
        String tRoot = "Magdat";
        String storageState = Environment.getExternalStorageState();
        // connect to root directory
        File root = new File(Environment.getExternalStorageDirectory(), tRoot);
        File file = new File(root, spName);
        //**
        BufferedReader inputReader2 = null;
        try {
            inputReader2 = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String inputString2;
        String pjfString = null;
        try {
            if ((inputString2 = inputReader2.readLine()) != null) {
                pjfString = inputString2;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // create file for dimension of grid
        String rowcolFName = pjfString + "_gridsize.txt";
        String stRow=Integer.toString(gRow);//etnRow.getText().toString();
        String stCol=Integer.toString(gCol);//etnCol.getText().toString();
        String srcBody = stRow+"," +stCol;

        BScannerIOFile myBSIOFile=new BScannerIOFile();

        myBSIOFile.createFile(rowcolFName, srcBody);
        //
        String sFileName = pjfString + ".txt";
        String sBody = "";
        int m = 0;
        for (m = 0; m < idxMag; m++) {
            sBody = sBody + Float.toString(vMag[3 * m]) + ", " + Float.toString(vMag[3 * m + 1]) + ", " + Float.toString(vMag[3 * m + 2]) + "\n";
        }
        myBSIOFile.createFile(sFileName, sBody);
        //
       // Toast.makeText(this, "Data Saved:" + sFileName, Toast.LENGTH_SHORT).show();

        // Create second kind of file
        // [x,y, value]
        //
        String sFileName2 = pjfString + "zz" + ".txt";
        sBody = "";
        BScannerMath myBSMath =new BScannerMath();
        int[] myRowCol=new int[2];
        int nRow=Integer.valueOf(stRow);
        int nCol=Integer.valueOf(stCol);
        //m = 0;
        for (m = 0; m < idxMag; m++) {
            // get coordinate
            myRowCol=myBSMath.getRowColZZ(m,nRow,nCol);

            sBody = sBody + String.valueOf(myRowCol[0])+", " + String.valueOf(myRowCol[1]) +
                    ", " +Float.toString(vMag[3 * m]) + ", " + Float.toString(vMag[3 * m + 1]) + ", " + Float.toString(vMag[3 * m + 2]) + "\n";
        }
        myBSIOFile.createFile(sFileName2, sBody);
        //
        //Toast.makeText(this, "Data Saved:" + sFileName2, Toast.LENGTH_SHORT).show();


    }
    // --


//    //Tambahan Pengaturan Grid Size
//
//    private void saveGridDetails(){
//        new PrefManager(this).saveGridDetails(gCol,gRow);
//    }
//
//    //Validasi Kolom
//    private boolean isColumnValid(int gCol){
//        //ketika nilainya tidak sama dengan 0 atau lebih dari 0
//        return gCol.value()>0;
//    }
//
//    //Validasi Baris
//    private boolean isRowValid(){
//        //ketika nilainya tidak sama dengan 0 atau lebih dari 0
//    }



    //---------------------------------------
}