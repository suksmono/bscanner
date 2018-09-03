package abscanner.bscanner5;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Tab3Activity  extends AppCompatActivity
{   // variables
    int cSCR=256;// 512
    String tRoot="Magdat";
    String svFName="Ha2D.txt";
    int XMAX=2*cSCR, YMAX=2*cSCR;

    // redef XMAX, YMAX as screen WIDTH and HEIGHT
    int scWIDTH=2*cSCR, scHEIGHT=2*cSCR;
    float[] Ha2D=new float[4*XMAX*YMAX];// |H| array
    int NX, NY;

    private Bitmap bmp;
    private Bitmap operation;
    ImageView im;
    // --

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_field1);

        im = (ImageView) findViewById(R.id.imageView);

        BitmapDrawable abmp = (BitmapDrawable) im.getDrawable();
        bmp = abmp.getBitmap();
    }
    // --


    // --
//    public void cmdContour(View view){
//        String spName = "project_name.txt";
//        String tRoot = "Magdat";
//        //BScannerIOFile myBSIOFile=new BScannerIOFile();
//        //String pjfString=myBSIOFile.getProjectFile(tRoot, spName);
//        //getGridSize();
//        Toast.makeText(this, "Will display contour ... soon!", Toast.LENGTH_SHORT).show();
//        //Toast.makeText(this, "Project name:" + pjfString, Toast.LENGTH_SHORT).show();
//
//    }
    // --
    // --
    public void btnSave(View view){
        Toast.makeText(this, "Will save data ... soon!", Toast.LENGTH_SHORT).show();
    }
    // --

    // --
    public void btnSave_ORG(View view){
        // Assume, idw has been run, so that NX, NY, and Ha2D filled
        //tv1 = (TextView) findViewById(R.id.textView);
        int m=0,n=0;
        try
        {
            File root = new File(Environment.getExternalStorageDirectory(),tRoot);
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, svFName);
            FileWriter writer = new FileWriter(gpxfile);
            String tStr;
            for(m=0;m<NY;m++) {
                //String sBody = "";
                for (n = 0; n < NX; n++) {
                    //sBody = sBody + Float.toString(Ha2D[m * NY + n]) + " ";
                    tStr = Float.toString(Ha2D[m * NX + n]) + " ";
                    writer.append(tStr);
                    writer.flush();
                }
            }
            writer.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    } // end-btnSave_ORG
    // ========
    // --
    public void cmdDisplayZZ(View view){
        // ---
        // layout: get_file_info->get_data -> display
        // Using Bilinear interpolation
        // Toast.makeText(this, "Be patient, I am calculating ... !", Toast.LENGTH_SHORT).show();
        //

        int bmW= bmp.getWidth(), bmH=bmp.getHeight();
        operation = Bitmap.createBitmap(bmW,bmH, bmp.getConfig());
        //int vPix=0;
        int alpha=255; //0 transparent - 255 opaque

        // read data from file: magnetic field
        String spName = "project_name.txt";
        String tRoot = "Magdat";

        BScannerIOFile myBSIOFile=new BScannerIOFile();

        String pjfString=myBSIOFile.getProjectFile(tRoot, spName); //project_file
        String sFileName=pjfString+"zz"+".txt";

        // processing the array, display into screen
        int[] taRC=new int[2];
        taRC=myBSIOFile.getGridSize();// update global var gridNROW, gridNCOL to actual values
        int nRow=taRC[0];//tgridNROW;
        int nCol=taRC[1]; //gridNCOL;
        float[] Ba2Data=new float[3*nCol*nRow]; //3x since x,y, Ba
        Toast.makeText(this, "nRow: "+Integer.toString(nRow)+"nCol: "+
                Integer.toString(nCol), Toast.LENGTH_SHORT).show();


        int NData=nRow*nCol;
        Ba2Data=myBSIOFile.readRCBa(sFileName, NData); // result in float of array aFloat

        // define boundary coordinate
        float XMin=0, XMax= (float) nCol-1;
        float YMin=0, YMax= (float) nRow-1;
        //*****************
        float dX=(XMax-XMin)/XMAX, dY=(YMax-YMin)/YMAX;
        NX=Math.round((XMax-XMin)/dX);
        NY=Math.round((YMax-YMin)/dY);

        // interpolate
        float[] Ba2DInterp=new float[NX*NY];
        BScannerMath myBSMath = new BScannerMath();
        // use bilinear interpolation
        Ba2DInterp = myBSMath.itpBilinear(Ba2Data, nRow, nCol, NData, NX, NY);

        //
        int m,n;
        // determine datarange to improve contrast
        //
        float maxB= -100000,minB=100000;
        int cntTh=0;
        for(m=0;m<NY;m++) {
            for (n = 0; n < NX; n++) {
                if (Ba2DInterp[m * NX + n] > maxB) maxB = Ba2DInterp[m * NX + n];
                if (Ba2DInterp[m * NX + n] < minB) minB = Ba2DInterp[m * NX + n];
                if (Ba2DInterp[m * NX + n] > maxB/2) cntTh++;

            }
        }
        float BRange=maxB-minB;
        int rInt=0,gInt=0,bInt=0;
        Toast.makeText(this, "NX: "+Integer.toString(NX)+
                "NY: "+ Integer.toString(NY) +"Rang: "+ Float.toString(BRange)
                , Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "maxB: "+Float.toString(maxB)+
                ", minB: "+ Float.toString(minB)+", cnt: "+ Integer.toString(cntTh), Toast.LENGTH_SHORT).show();

        for(m=0;m<NY;m++) {
            for (n = 0; n < NX; n++) {
                // create heat-map
                float tVal=Ba2DInterp[m * NX + n];
                float ratio=2*(tVal-minB)/BRange;
                bInt=(int) (Math.max(0,255*(1-ratio)));
                rInt=(int) (Math.max(0,255*(ratio-1)));
                gInt=255-bInt-rInt;

                operation.setPixel(n, m, Color.argb(alpha, rInt, gInt, bInt));
                //operation.setPixel(m, n, Color.argb(alpha, rInt, gInt, bInt));
            }
        }
        im.setImageBitmap(operation); //optional?

    }
    // --
    // --
    public void cmdDisplayBcb2D(View view){
        // ---
        // layout: get_file_info->get_data -> display
        // Using Bicubic interpolation
        // Toast.makeText(this, "Be patient, I am calculating ... !", Toast.LENGTH_SHORT).show();
        //

        int bmW= bmp.getWidth(), bmH=bmp.getHeight();
        operation = Bitmap.createBitmap(bmW,bmH, bmp.getConfig());
        //int vPix=0;
        int alpha=255; //0 transparent - 255 opaque

        // read data from file: magnetic field
        String spName = "project_name.txt";
        String tRoot = "Magdat";

        BScannerIOFile myBSIOFile=new BScannerIOFile();

        String pjfString=myBSIOFile.getProjectFile(tRoot, spName); //project_file
        String sFileName=pjfString+"zz"+".txt";

        // processing the array, display into screen
        int[] taRC=new int[2];
        taRC=myBSIOFile.getGridSize();// update global var gridNROW, gridNCOL to actual values
        int nRow=taRC[0];//tgridNROW;
        int nCol=taRC[1]; //gridNCOL;
        float[] Ba2Data=new float[3*nCol*nRow]; //3x since x,y, Ba
        Toast.makeText(this, "nRow: "+Integer.toString(nRow)+"nCol: "+
                Integer.toString(nCol), Toast.LENGTH_SHORT).show();


        int NData=nRow*nCol;
        Ba2Data=myBSIOFile.readRCBa(sFileName, NData); // result in float of array aFloat

        // define boundary coordinate
        float XMin=0, XMax= (float) nCol-1;
        float YMin=0, YMax= (float) nRow-1;
        //*****************
        float dX=(XMax-XMin)/XMAX, dY=(YMax-YMin)/YMAX;
        NX=Math.round((XMax-XMin)/dX);
        NY=Math.round((YMax - YMin) / dY);

        // interpolate
        //float[] Ba2DInterp=new float[NX*NY];
        float[][] Ba2DItp=new float[NY][NX];
        BScannerMath myBSMath = new BScannerMath();
        // use biCubic interpolation
        Ba2DItp = myBSMath.itpBiCubic2D(Ba2Data, nRow, nCol, NData, NX, NY);

        //
        int mx,ny;
        // determine datarange to improve contrast
        float maxB= -100000,minB=100000;
        for(mx=0;mx<NX;mx++) {
            for (ny = 0; ny < NY; ny++) {
                if (Ba2DItp[mx][ny] > maxB) maxB = Ba2DItp[mx][ny];
                if (Ba2DItp[mx][ny] < minB) minB = Ba2DItp[mx][ny];
            }
        }
        float BRange=maxB-minB;
        int rInt=0,gInt=0,bInt=0;
        Toast.makeText(this, "NX: "+Integer.toString(NX)+
                "NY: "+ Integer.toString(NY) +"Rang: "+ Float.toString(BRange)
                , Toast.LENGTH_SHORT).show();

       // Toast.makeText(this, "maxB: "+Float.toString(maxB)+
       //         ", minB: "+ Float.toString(minB)+", cnt: "+ Integer.toString(cntTh), Toast.LENGTH_SHORT).show();


        for(mx=0;mx<NX;mx++) {
            for (ny = 0; ny < NY; ny++) {
                // create heat-map
                //float tVal=Ba2DItp[m][n];;// * NX + n];
                float tVal=Ba2DItp[mx][ny];;// * NX + n];
                float ratio=2*(tVal-minB)/BRange;
                bInt=(int) (Math.max(0,255*(1-ratio)));
                rInt=(int) (Math.max(0,255*(ratio-1)));
                gInt=255-bInt-rInt;

                operation.setPixel(mx, ny, Color.argb(alpha, rInt, gInt, bInt));
            }
        }
        im.setImageBitmap(operation); //optional?

    }
    // --

    // --
    public void cmdDisplayBcb(View view){
        // ---
        // layout: get_file_info->get_data -> display
        // Using Bicubic interpolation
        // Toast.makeText(this, "Be patient, I am calculating ... !", Toast.LENGTH_SHORT).show();
        //

        int bmW= bmp.getWidth(), bmH=bmp.getHeight();
        operation = Bitmap.createBitmap(bmW,bmH, bmp.getConfig());
        //int vPix=0;
        int alpha=255; //0 transparent - 255 opaque

        // read data from file: magnetic field
        String spName = "project_name.txt";
        String tRoot = "Magdat";

        BScannerIOFile myBSIOFile=new BScannerIOFile();

        String pjfString=myBSIOFile.getProjectFile(tRoot, spName); //project_file
        String sFileName=pjfString+"zz"+".txt";

        // processing the array, display into screen
        int[] taRC=new int[2];
        taRC=myBSIOFile.getGridSize();// update global var gridNROW, gridNCOL to actual values
        int nRow=taRC[0];//tgridNROW;
        int nCol=taRC[1]; //gridNCOL;
        float[] Ba2Data=new float[3*nCol*nRow]; //3x since x,y, Ba
        Toast.makeText(this, "nRow: "+Integer.toString(nRow)+"nCol: "+
                Integer.toString(nCol), Toast.LENGTH_SHORT).show();


        int NData=nRow*nCol;
        Ba2Data=myBSIOFile.readRCBa(sFileName, NData); // result in float of array aFloat

        // define boundary coordinate
        float XMin=0, XMax= (float) nCol-1;
        float YMin=0, YMax= (float) nRow-1;
        //*****************
        float dX=(XMax-XMin)/XMAX, dY=(YMax-YMin)/YMAX;
        NX=Math.round((XMax-XMin)/dX);
        NY=Math.round((YMax - YMin) / dY);

        // interpolate
        float[] Ba2DInterp=new float[NX*NY];
        //float[][] Ba2DItp=new float[NY][NX];
        BScannerMath myBSMath = new BScannerMath();
        // use biCubic interpolation
        Ba2DInterp = myBSMath.itpBiCubic(Ba2Data, nRow, nCol, NData, NX, NY);

        //
        int m,n;
        // determine datarange to improve contrast
        //
        float maxB= -100000,minB=100000;
        int cntTh=0;
        for(m=0;m<NY;m++) {
            for (n = 0; n < NX; n++) {
                if (Ba2DInterp[m * NX + n] > maxB) maxB = Ba2DInterp[m * NX + n];
                if (Ba2DInterp[m * NX + n] < minB) minB = Ba2DInterp[m * NX + n];
                if (Ba2DInterp[m * NX + n] > maxB/2) cntTh++;

            }
        }
        float BRange=maxB-minB;
        int rInt=0,gInt=0,bInt=0;
        Toast.makeText(this, "NX: "+Integer.toString(NX)+
                "NY: "+ Integer.toString(NY) +"Rang: "+ Float.toString(BRange)
                , Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "maxB: "+Float.toString(maxB)+
                ", minB: "+ Float.toString(minB)+", cnt: "+ Integer.toString(cntTh), Toast.LENGTH_SHORT).show();

        for(m=0;m<NY;m++) {
            for (n = 0; n < NX; n++) {
                // create heat-map
                float tVal=Ba2DInterp[m * NX + n];
                float ratio=2*(tVal-minB)/BRange;
                bInt=(int) (Math.max(0,255*(1-ratio)));
                rInt=(int) (Math.max(0,255*(ratio-1)));
                gInt=255-bInt-rInt;

                operation.setPixel(n, m, Color.argb(alpha, rInt, gInt, bInt));
                //operation.setPixel(m, n, Color.argb(alpha, rInt, gInt, bInt));
            }
        }
        im.setImageBitmap(operation); //optional?

    }
    // --
    // ======== end of Tab3 ========
}