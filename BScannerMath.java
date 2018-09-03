package abscanner.bscanner5;

public class BScannerMath {
    // ------------------------------------------------------
    // math functions related to BScanner processing
    // 1. interpolation
    // 2. contour computation
    // ------------------------------------------------------

    // --
    public int[] getRowColZZ(int myCursor,int nRow, int nCol){
        // given current counter, return zigzag coordinate
        int tRow, tCol;
        boolean P_EVEN = false;
        tRow=(int) myCursor/nCol;
        if ((tRow % 2) == 0) P_EVEN = true; //
        if(P_EVEN){
            tCol=myCursor % nCol;
        }
        else {
            tCol=nCol-(myCursor % nCol)-1;
        }
        int[] myRowCol= new int[2];
        myRowCol[0]=tRow;
        myRowCol[1]=tCol;
        return myRowCol;
    }
    // --

    // --
    public float[] itpBilinear(float[] rcFKnown, int ndRow, int ndCol,
                               int NData, int solNX, int solNY){
        // ---------------------------------------
        // input:,
        // -known quantity at particular coordinates: [x, y, f(x,y)] ->
        // -grid dimension: ndRow, ndCol, in case of incomplete sampling
        // -coordinates where the quantity should be computed: [X,Y]: solNX,solNY
        // output:
        // -interpolated quantity: [X,Y,f(X,Y)]
        // ---------------------------------------

        float[] F2DData=new float[ndRow*ndCol]; //2x since x, and y
        int tRow=0, tCol=0, cntr=0;
        float tHx=0, tHy=0, tHz=0, tHa=0;
        //
        cntr=0;
        while(cntr<NData) {
            tRow=(int) rcFKnown[3*cntr];
            tCol=(int) rcFKnown[3*cntr+1];
            F2DData[tRow*ndCol+tCol]=rcFKnown[3*cntr+2];
        cntr++;
        }

        int m=0,n=0;

        // create XY grid
        // define boundary coordinate
        float XMin=0, XMax= (float) ndCol-1;
        float YMin=0, YMax= (float) ndRow-1;
        //*****************
        float dX=(XMax-XMin)/solNX, dY=(YMax-YMin)/solNY;

        float[] XGrid=new float[solNX*solNY];
        float[] YGrid=new float[solNX*solNY];
        float[] F2DInterp=new float[solNX*solNY];

        // create XY-grid: gridX, gridY
        for(m=0;m<solNY;m++)
            for(n=0;n<solNX;n++) {
                XGrid[m*solNX+n]=XMin+n*dX;
                YGrid[m*solNX+n]=YMin+m*dY;
                F2DInterp[m*solNX+n]=0;
            }

        // main of Bilinear
        int cx0=1, cy0=1,cx=1,cy=1;
        for(m=0;m<solNY;m++) {
            for (n = 0; n < solNX; n++) {
                double currX=XGrid[m*solNX+n];
                double currY=YGrid[m*solNX+n]; //yes, its the same index as currX
                // perform bilinear interpolation
                double x0=Math.floor(currX);
                double y0=Math.floor(currY);
                double wt1=currY-y0;
                double wt2=currX-x0;
                double wt1Cj=1-wt1;
                double wt2Cj=1-wt2;

                cy0= (int) Math.ceil(y0);
                cx0= (int) Math.ceil(x0);
                cx= (int) Math.ceil(currX);
                cy= (int) Math.ceil(currY);

                double itTop=wt2Cj*F2DData[cy0*ndCol+cx0] + wt2*F2DData[cy0*ndCol+cx];
                double itBot=wt2Cj*F2DData[cy*ndCol+cx0] + wt2*F2DData[cy*ndCol+cx];
                double itVal=wt1Cj*itTop + wt1*itBot;
                // assign to current value of the field
                F2DInterp[m*solNX+n]=(float) itVal;
                //F2DInterp[m*solNX+n]=(float) (m+n);

            }
        }
        //
        return F2DInterp; // return (pointer to) array of the results

    } // ----- end of bilinear ---------
    //

    // --
    public float[] itpBiCubic(float[] rcFKnown, int ndRow, int ndCol,
                               int NData, int solNX, int solNY){
        // ---------------------------------------
        // input:,
        // -known quantity at particular coordinates: [x, y, f(x,y)] ->
        // -grid dimension: ndRow, ndCol, in case of incomplete sampling
        // -coordinates where the quantity should be computed: [X,Y]: solNX,solNY
        // output:
        // -interpolated quantity: [X,Y,f(X,Y)]
        // ---------------------------------------

        float[][] p=new float[ndRow][ndCol]; //2x since x, and y
        int tRow=0, tCol=0, cntr=0;
        float tHx=0, tHy=0, tHz=0, tHa=0;
        //
        cntr=0;
        while(cntr<NData) {
            tRow=(int) rcFKnown[3*cntr];
            tCol=(int) rcFKnown[3*cntr+1];
            p[tRow][tCol]=rcFKnown[3*cntr+2];
            cntr++;
        }

        int m=0,n=0;
        // Create a template of (ndRow+4)x (ndCol+4)
        float[][] xp= new float[ndRow+4][ndCol+4];
        // init with zero
        for(m=0;m<ndRow+4;m++)
            for(n=0;n<ndCol+4;n++) {
                xp[m][n]=0;
            }
        // fill with p
        for(m=0;m<ndRow;m++)
            for(n=0;n<ndCol;n++) {
                xp[m+2][n+2]=p[m][n];
            }

        // fill boundary: col1=col2; col ndCol+2+1=ndCol+2
        for(m=0;m<ndRow+4;m++) {
            xp[m][1]=xp[m][2];
            xp[m][ndCol+2]=xp[m][ndCol+1];
        }
        // fill boundary:row1=row2; row ndRow+2+1=ndRow+2
        for(m=0;m<ndCol+4;m++) {
            xp[1][m]=xp[2][m];
            xp[ndRow+2][m]=xp[ndRow+1][m];
        }

        // create grid on domain [0,1][0,1]
        // screen size-xy:solNX x solNY
        // per-domain size: solNX/ndCol x solNY/ndRow
        int subNX=solNX/ndCol, subNY=solNY/ndRow;
        float dx,dy;
        dx=1/((float)subNX);
        dy=1/((float)subNY);
        // create subGridX, subGridY
        float[][] subXGrid=new float[subNX][subNY];
        float[][] subYGrid=new float[subNX][subNY];
        for(m=0;m<subNY;m++)
            for(n=0;n<subNX;n++) {
                subXGrid[m][n]=n*dx;
                subYGrid[m][n]=m*dy;
                //subF2DInterp[m*solNX+n]=0;
            }
        // define extended solution template
        float[][] tF=new float[(ndRow+4)*subNY][(ndCol+4)*subNX];

        // start interpolation, patch by patch
        BSBiCubic myBSBC=new BSBiCubic();

        int mm=0,nn=0;
        for(m=0;m<ndRow+1;m++)
            for(n=0;n<ndCol+1;n++) {
                int i0=m, j0=n;
                // get current 4x4 block
                double[][] currP=new double[4][4];
                for(mm=0;mm<4;mm++)
                    for(nn=0;nn<4;nn++) {
                        currP[mm][nn] = (double) xp[i0 + mm][j0 + nn];
                        //currP[mm][nn] = (double) xp[j0 + nn][i0 + mm];
                    }
                // calculate Coeff
                //double[][] currCoeff =new double[4][4];
                myBSBC.bcbUpdCoeff(currP);
                int oRow=m*subNY,oCol=n*subNX;
                for(mm=0;mm<subNY;mm++)
                    for(nn=0;nn<subNX;nn++) {
                        tF[oRow + mm][oCol + nn] =
                                (float) myBSBC.bcbGetValue(subXGrid[nn][mm], subYGrid[nn][mm]);
                        //(float) myBSBC.bcbGetValue(subXGrid[mm][nn], subYGrid[mm][nn]);
                    }
            }
        // crop appropriately as the result
        //float[][] F_crop=new float[ndRow*subNY][ndCol*subNX];
        float[] F_crop1=new float[ndRow*subNY*ndCol*subNX];
        int offsRow=subNY/2,offsCol=subNX/2;
        for(m=0;m<ndRow*subNY;m++)
            for(n=0;n<ndCol*subNX;n++) {
                        //F_crop[m][n] = tF[offsRow+m][offsCol+n];
                F_crop1[m*ndCol*subNX+n] = tF[offsRow+m][offsCol+n];
                //dummy
            }
        //
        //return F2DInterp; // return (pointer to) array of the results
        return F_crop1;
    } // ----- end of bicubic ---------
    // --

    // --
    public float[][] itpBiCubic2D(float[] rcFKnown, int ndRow, int ndCol,
                              int NData, int solNX, int solNY){
        // ---------------------------------------
        // input:,
        // -known quantity at particular coordinates: [x, y, f(x,y)] ->
        // -grid dimension: ndRow, ndCol, in case of incomplete sampling
        // -coordinates where the quantity should be computed: [X,Y]: solNX,solNY
        // output:
        // -interpolated quantity: [X,Y,f(X,Y)]
        // ---------------------------------------

        float[][] p=new float[ndRow][ndCol]; //2x since x, and y
        int tRow=0, tCol=0, cntr=0;
        float tHx=0, tHy=0, tHz=0, tHa=0;
        //
        cntr=0;
        while(cntr<NData) {
            tRow=(int) rcFKnown[3*cntr];
            tCol=(int) rcFKnown[3*cntr+1];
            p[tRow][tCol]=rcFKnown[3*cntr+2];
            cntr++;
        }

        int m=0,n=0;
        // Create extended template of (ndRow+4)x (ndCol+4)
        float[][] xp= new float[ndRow+4][ndCol+4];
        // init with zero
        for(m=0;m<ndRow+4;m++)
            for(n=0;n<ndCol+4;n++) {
                xp[m][n]=0;
                //xpXy[n][m]=0;
            }
        // fill with p
        for(m=0;m<ndRow;m++)
            for(n=0;n<ndCol;n++) {
                xp[m+2][n+2]=p[m][n];
                //xpXy[n+2][m+2]=p[m][n];
            }

        // fill boundary: col1=col2; col ndCol+2+1=ndCol+2
        for(m=0;m<ndRow+4;m++) {
            xp[m][1]=xp[m][2];
            xp[m][ndCol+2]=xp[m][ndCol+1];
        }
        // fill boundary:row1=row2; row ndRow+2+1=ndRow+2
        for(m=0;m<ndCol+4;m++) {
            xp[1][m]=xp[2][m];
            xp[ndRow+2][m]=xp[ndRow+1][m];
        }
        // change xp to cartesian
        // ---- prepare-in cartesian --
        float[][] xpXy= new float[ndCol+4][ndRow+4];
        for(m=0;m<ndCol+4;m++)
            for(n=0;n<ndRow+4;n++)
                xpXy[m][n]=xp[n][m];

        // create grid on domain [0,1][0,1]
        // screen size-xy:solNX x solNY
        // per-domain size: solNX/ndCol x solNY/ndRow
        int subNX=solNX/ndCol, subNY=solNY/ndRow;
        float dx,dy;
        dx=1/((float)subNX);
        dy=1/((float)subNY);
        // create subGridX, subGridY
        float[][] subXGrid=new float[subNX][subNY];
        float[][] subYGrid=new float[subNX][subNY];
        for(m=0;m<subNX;m++)
            for(n=0;n<subNY;n++) {
                subXGrid[m][n]=m*dx;
                subYGrid[m][n]=n*dy;
            }
        // define extended solution template
        //float[][] tF=new float[(ndRow+4)*subNY][(ndCol+4)*subNX];
        float[][] tF=new float[(ndCol+4)*subNX][(ndRow+4)*subNY];

        // start interpolation, patch by patch
        BSBiCubic myBSBC=new BSBiCubic();

        int mm=0,nn=0;
        int mx,ny;

        for(mx=0;mx<ndCol+1;mx++)
            for(ny=0;ny<ndRow+1;ny++) {
                int i0=mx, j0=ny;
                // get current 4x4 block
                double[][] currP=new double[4][4];
                for(mm=0;mm<4;mm++)
                    for(nn=0;nn<4;nn++) {
                        currP[mm][nn] = (double) xpXy[i0 + mm][j0 + nn];
                        //currP[mm][nn] = (double) xp[j0 + nn][i0 + mm];
                    }
                // calculate Coeff
                //double[][] currCoeff =new double[4][4];
                myBSBC.bcbUpdCoeff(currP);
                //int oRow=m*subNY,oCol=n*subNX;
                int oX=mx*subNX,oY=ny*subNY;
                for(mm=0;mm<subNX;mm++)
                    for(nn=0;nn<subNY;nn++) {
                        tF[oX + mm][oY + nn] =
                             //(float) myBSBC.bcbGetValue(subYGrid[nn][mm], subXGrid[nn][mm]);
                            (float) myBSBC.bcbGetValue(subXGrid[mm][nn], subYGrid[mm][nn]);
                            //(float) myBSBC.bcbGetValue(subXGrid[mm][nn], subYGrid[mm][nn]);
                    }
            }
        // crop appropriately as the result
        float[][] F_crop=new float[ndCol*subNX][ndRow*subNY];
        //float[][] F_crop=new float[ndRow*subNY][ndCol*subNX];
        //float[] F_crop1=new float[ndRow*subNY*ndCol*subNX];
        int offsRow=subNY/2,offsCol=subNX/2;
        for(mm=0;mm<ndCol*subNX;mm++)
            for(nn=0;nn<ndRow*subNY;nn++) {
                F_crop[mm][nn] = tF[offsRow+mm][offsCol+nn];
                //F_crop[m][n] = tF[offsRow+m][offsCol+n];
                //F_crop1[m*ndCol*subNX+n] = tF[offsRow+m][offsCol+n];
                //dummy
            }
        //
        //return F2DInterp; // return (pointer to) array of the results
        return F_crop;
    } // ----- end of bicubic -------
    // --
   }
