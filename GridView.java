package abscanner.bscanner5;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

public class GridView extends View {
    // ---
    // parameters throughout program
    int MAX_GRow=500;
    int MAX_GCol=500;
    //
    public int[] myFCell=new int[MAX_GRow*MAX_GCol];
    public int[] myCurr= new int[2];
    public int gblNData=0;
    int color=0xfff00000;
    public int[] gParams={200,200,2,2};
    // ---
    private Bitmap mBitmap;
    private Canvas mCanvas;

    public GridView(Context c, AttributeSet attrs) {
        super(c, attrs);
    }
    // additionals
    protected void drawGrid(Canvas canvas,int[] gParams, int[] gCurr, int [] myFCell){
        int lnWidth = 2; // line width
        //
        int gWidth=gParams[0];
        int gHeight=gParams[1];
        int nRow=gParams[2];
        int nCol=gParams[3];
        int dRow=gHeight/nRow;
        int dCol=gWidth/nCol;
        int m,n;

        Paint paintBK = new Paint();
        Paint paintGY = new Paint();
        Paint paintYL = new Paint();
        Paint paintGR = new Paint();
        paintBK.setColor(Color.BLACK);
        paintGY.setColor(Color.GRAY);
        paintYL.setColor(Color.YELLOW);
        paintGR.setColor(Color.GREEN);

        //
        //int idxGCell=0;
        int x0=0,y0=0,x1=0,y1=0,x01=0,y01=0,x11=0,y11=0;
        int tIdx=0;
        for(m=0;m<nRow;m++)
            for(n=0;n<nCol;n++) {
            //for(n=nCol-1;n>=0;n--) {
                x0 = n*dCol;
                y0 = m*dRow;
                x1 = (n+1)* dCol;
                y1 = (m+1) * dRow;

                //int lnWidth = 2; // line width
                x01 = x0+lnWidth;
                y01 = y0+lnWidth;
                x11 = x1-lnWidth;
                y11 = y1-lnWidth;

                canvas.drawRect(x0, y0, x1, y1, paintBK);

                if (myFCell[tIdx]>0) {
                    canvas.drawRect(x01, y01, x11, y11, paintGY);
                }
                else
                    canvas.drawRect(x01, y01, x11, y11, paintGR);
                if ((n==gCurr[0]) && (m==gCurr[1])) {
                    canvas.drawRect(x01, y01, x11, y11, paintYL);
                }
                tIdx++;
            }
    }
    //protected void drawGridZZ(Canvas canvas,int[] gParams, int[] gCurr, int [] myFCell){
    protected void drawGridZZ(Canvas canvas,int[] gParams, int[] gCurr, int NData){
        // zig-zag scan
        int lnWidth = 2; // line width
        //
        int gWidth=gParams[0];
        int gHeight=gParams[1];
        int nRow=gParams[2];
        int nCol=gParams[3];
        int dRow=gHeight/nRow;
        int dCol=gWidth/nCol;
        int m,n;

        //TextView tv1;
        //tv1 = (TextView)findViewById(R.id.textViewCrow);

        Paint paintBK = new Paint();
        Paint paintGY = new Paint();
        Paint paintYL = new Paint();
        Paint paintGR = new Paint();
        paintBK.setColor(Color.BLACK);
        paintGY.setColor(Color.GRAY);
        paintYL.setColor(Color.YELLOW);
        paintGR.setColor(Color.GREEN);

        //
        //int idxGCell=0;
        int x0=0,y0=0,x1=0,y1=0,x01=0,y01=0,x11=0,y11=0;
        int tIdx=0;
        for(m=0;m<nRow;m++) {
            // flag even-odd rows
            boolean P_EVEN = false, F_CELL = false;
            if ((m % 2) == 0) P_EVEN = true; // even cell, idx: 0,2, ...
            // --
            if(P_EVEN){ //scan left-to right
                for (n =0; n<nCol ; n++) {
                    if (tIdx < NData) F_CELL = true;
                        else F_CELL=false;
                    // calculate row-parity
                    x0 = n * dCol;
                    y0 = m * dRow;
                    x1 = (n + 1) * dCol;
                    y1 = (m + 1) * dRow;

                    //int lnWidth = 2; // line width
                    x01 = x0 + lnWidth;
                    y01 = y0 + lnWidth;
                    x11 = x1 - lnWidth;
                    y11 = y1 - lnWidth;

                    canvas.drawRect(x0, y0, x1, y1, paintBK);

                    //if (myFCell[tIdx]>0) {
                    if (F_CELL) {
                        canvas.drawRect(x01, y01, x11, y11, paintGY);
                    } else
                        canvas.drawRect(x01, y01, x11, y11, paintGR);
                    // current "cursor"
                    if ((n == gCurr[0]) && (m == gCurr[1])) {
                        canvas.drawRect(x01, y01, x11, y11, paintYL);
                    }
                    tIdx++;
                }
            }// for even-odd
            else {
                for (n = nCol-1; n >= 0; n--) {
                //for (n =0; n<nCol ; n++) {
                    if (tIdx < NData) F_CELL = true;
                        else F_CELL=false;

                    // calculate row-parity
                    x0 = n * dCol;
                    y0 = m * dRow;
                    x1 = (n + 1) * dCol;
                    y1 = (m + 1) * dRow;

                    //int lnWidth = 2; // line width
                    x01 = x0 + lnWidth;
                    y01 = y0 + lnWidth;
                    x11 = x1 - lnWidth;
                    y11 = y1 - lnWidth;

                    canvas.drawRect(x0, y0, x1, y1, paintBK);

                    //if (myFCell[tIdx]>0) {
                    if (F_CELL) {
                        canvas.drawRect(x01, y01, x11, y11, paintGY);
                    } else
                        canvas.drawRect(x01, y01, x11, y11, paintGR);
                    // current "cursor"
                    if ((n == gCurr[0]) && (m == gCurr[1])) {
                        canvas.drawRect(x01, y01, x11, y11, paintYL);
                    }
                    tIdx++;
                }

            }// else
        } // for-m
    }
    //

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // your Canvas will draw onto the defined Bitmap
        //mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //mCanvas = new Canvas(mBitmap);
    }
    // override onDraw
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw the mPath with the mPaint on the canvas when onDraw
        //canvas.drawPath(mPath, mPaint);


        Paint paint = new Paint();
        //paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawPaint(paint);

        // define grid parameters
        int gWidth = getWidth();
        int gHeight=getHeight();

        gParams[0]=gWidth;
        gParams[1]=gHeight;

        int nRow=gParams[2];
        int nCol=gParams[3];
        // dimension and size of the grid

        // define: which has been measured, and current to be measured point
        int m=0,n=0;
        // mark done cells

        int tDone=gblNData;// nRow*nCol/2+3;
        int tRow=0, tCol=0;
        int tCurr=0;

        // first clear all flag
        tCurr=0;
        for(m=0;m<nRow;m++)
            for(n=0;n<nCol;n++) { // NON-ZIGZAG
            //for(n=nCol-1;n>=0;n--) { // ZIGZAG
                myFCell[tCurr]=0;
                tCurr++;
            }
        // than flag appropriately
        tCurr=0;
        int tcurRow=0;

        while (tCurr<tDone) {
            myFCell[tCurr]=1;
            tCurr++;
        }
        // Cursor: yellow mark current cell-to-be-measured
        myCurr[1]=tCurr/nCol; //Y-pos: row
        if ((myCurr[1] % 2)<1) // even, ref=0-> normal scan left->right
            myCurr[0]=tCurr % nCol; // XY scan
        else // odd scan: right->left
            myCurr[0]=nCol-(tCurr % nCol)-1; // ZIGZAG Scan // X-Pos: col
        //
        drawGridZZ(canvas,gParams, myCurr, gblNData); //myCurr, myFCell);
    }


    public void clearCanvas() {
        invalidate();
    }
    //--tes
    //public void dispCanvas(Canvas canvas) {
    //    super.draw(canvas);
    public void dispCanvas() {
       // super.draw(mCanvas);
        //
        //Toast.makeText(this, "Display |B|-map Again", Toast.LENGTH_SHORT).show();
        int w=100,h=100;
        mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);

        /// ----------------- ******* ----------------
        int bmW= mBitmap.getWidth(), bmH=mBitmap.getHeight();
        //operation = Bitmap.createBitmap(bmW,bmH, mBitmap.getConfig());
        int vPix=0;
        int alpha=255; //0 transparent - 255 opaque

        // test modify bmp
        for (int i = 0; i < mBitmap.getWidth(); i++) {
            for (int j = 0; j < mBitmap.getHeight(); j++) {
                vPix=i+j;
                mBitmap.setPixel(i, j, Color.argb(alpha, vPix, vPix, vPix));
            }
        }
        //try draw canvas
        Paint tPaint=new Paint();
        //
        mCanvas = new Canvas(mBitmap);
    }
    //--test
 //
}
