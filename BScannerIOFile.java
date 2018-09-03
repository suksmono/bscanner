package abscanner.bscanner5;

import android.os.Environment;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;


// --
public class BScannerIOFile {
    //move all I/I routines here ...
    // - get project name
    // - get nRow, nCol
    // - write outfile
    float[] aFloat=new float[3*100];
    String tRoot="Magdat";
    //--
    // --
    public void createFile(String sFileName, String sBody){
        // ----- WRITE DATA INTO FILE -----
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(), tRoot);
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
         } catch(IOException e) { e.printStackTrace(); }
    }
    //--
    // --
    public int[] getGridSize(){
        String spName = "project_name.txt";
        String tRoot = "Magdat";
        int[] aInt=new int[2]; // nRow, nCol
        //
        BScannerIOFile myBSIOFile=new BScannerIOFile();
        String pjfString=myBSIOFile.getProjectFile(tRoot, spName);
        String sFileName=pjfString+"_gridsize.txt";
        //
        try {
            String storageState1 = Environment.getExternalStorageState();
            if (storageState1.equals(Environment.MEDIA_MOUNTED)) {
                // connect to root directory
                File root1 = new File(Environment.getExternalStorageDirectory(), tRoot);
                File file1 = new File(root1,sFileName);
                //**
                BufferedReader inputReader21 = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file1)));
                String inputString21;
                StringBuffer stringBuffer2 = new StringBuffer();
                // read string, row-by-row, convert to array-float
                String[] tabOfIntegerString;
                int m = 0;
                while ((inputString21 = inputReader21.readLine()) != null) {
                    tabOfIntegerString=inputString21.split(",");
                    for(String s : tabOfIntegerString){
                        aInt[m] = Integer.parseInt(s);
                        m=m+1;
                    }
                }

            }

        }   catch (IOException e) {
            e.printStackTrace();
            // display error message
        }
        return aInt;
    }
    // --

    // --
    public float[] readRCBa(String sFileName, int NData){
        // Read recording file
        // input: file name, NData
        // output: [Row,Col, Ba], where Ba=|B(x,y,z)|

        float[] myBxyz=new float[5*NData];
        float[] myRCBa=new float[3*NData];
        try {
            String storageState1 = Environment.getExternalStorageState();
            if (storageState1.equals(Environment.MEDIA_MOUNTED)) {
                // connect to root directory
                File root1 = new File(Environment.getExternalStorageDirectory(), tRoot);
                File file1 = new File(root1,sFileName);
                //**
                BufferedReader inputReader21 = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file1)));
                String inputString21;
                StringBuffer stringBuffer2 = new StringBuffer();
                // read string, row-by-row, convert to array-float
                String[] tabOfFloatString;
                int m = 0;
                while ((inputString21 = inputReader21.readLine()) != null) {
                    tabOfFloatString=inputString21.split(",");
                    for(String s : tabOfFloatString){
                        myBxyz[m] = Float.parseFloat(s);
                        m=m+1;
                    }
                }
                // display last read data
                //tv1.setText(String.valueOf(aFloat[m-1]));
            }

        }   catch (IOException e) {
            e.printStackTrace();
            // display error message
        }
        //--
        //Toast.makeText(this, "Read Project File OK!", Toast.LENGTH_SHORT).show();
        // calculate Ba
        int m=0;
        for(m=0;m<NData;m++) {
            myRCBa[3*m]= myBxyz[5*m];
            myRCBa[3*m+1]= myBxyz[5*m+1];
            double tBx=(double) myBxyz[5*m+2];
            double tBy=(double) myBxyz[5*m+3];
            double tBz=(double) myBxyz[5*m+4];
            float tBa=(float) Math.sqrt(tBx*tBx + tBy*tBy + tBz*tBz);
            myRCBa[3*m+2]= tBa;
        }

        return myRCBa;
    } // end read readRCBa
    // --
    // --
    public String getProjectFile(String tRoot, String spName) {
        // get the name of current project file
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
        return pjfString;
        // project name in pjfString
    }
    // --

    // ============ DEPRECATED METHODS =========================
    // --
    public float[] readFile(String sFileName){
        // possibly deprecated
        //
        try {
            String storageState = Environment.getExternalStorageState();
            if (storageState.equals(Environment.MEDIA_MOUNTED)) {
                // connect to root directory
                File root = new File(Environment.getExternalStorageDirectory(), tRoot);
                File file = new File(root,sFileName);
                //**
                BufferedReader inputReader2 = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file)));
                String inputString2;
                StringBuffer stringBuffer2 = new StringBuffer();
                // read string, row-by-row, convert to array-float
                String[] tabOfFloatString;
                int m = 0;
                while ((inputString2 = inputReader2.readLine()) != null) {
                    tabOfFloatString=inputString2.split(",");
                    for(String s : tabOfFloatString){
                        aFloat[m] = Float.parseFloat(s);
                        m=m+1;
                    }
                }
                // display last read data
            }

        }   catch (IOException e) {
            e.printStackTrace();
            // display error message
        }
        return aFloat;
    }
    // --
    // --
    public float[] readProjectFile(String sFileName){
        // preparation, read project name, gridsize, and get data

        String spName = "project_name.txt";
        String tRoot = "Magdat";
        String pjfString=getProjectFile(tRoot, spName);

        //Toast.makeText(this, "Project name:" + pjfString, Toast.LENGTH_SHORT).show()

        sFileName=pjfString+".txt";
        //
        try {
            String storageState1 = Environment.getExternalStorageState();
            if (storageState1.equals(Environment.MEDIA_MOUNTED)) {
                // connect to root directory
                File root1 = new File(Environment.getExternalStorageDirectory(), tRoot);
                File file1 = new File(root1,sFileName);
                //**
                BufferedReader inputReader21 = new BufferedReader(
                        new InputStreamReader(new FileInputStream(file1)));
                String inputString21;
                StringBuffer stringBuffer2 = new StringBuffer();
                // read string, row-by-row, convert to array-float
                String[] tabOfFloatString;
                int m = 0;
                while ((inputString21 = inputReader21.readLine()) != null) {
                    tabOfFloatString=inputString21.split(",");
                    for(String s : tabOfFloatString){
                        aFloat[m] = Float.parseFloat(s);
                        m=m+1;
                    }
                }
                // display last read data
            }

        }   catch (IOException e) {
            e.printStackTrace();
            // display error message
        }
        //--
        return aFloat;
        //Toast.makeText(this, "Read Project File OK!", Toast.LENGTH_SHORT).show();
    }
    // --
}
