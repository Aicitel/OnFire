package com.example.bluemaple.distancemearsure;
/**
 * Created by Chenyx on 2015/11/20.
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class Wave extends View implements Runnable{
    private  Paint mPaint = null;
    private  static float amplifier = 100.0f;
    //private  static float frequency = 2.0f;    //2Hz
    //private  static float phase = 45.0f;         //相位
    private  int height = 0;
    private  int width = 0;
    private  static float px=-1,py=-1;
    private  boolean sp=false;
    private  static float data[]=new float[1000];
    private  static float dataa[]=new float[1000];
    private  static float datab[]=new float[1000];
    public Wave(Context context){
        super(context);
        mPaint = new Paint();
        new Thread(this).start();
    }

    //如果不写下面的构造函数，则会报错：custom view SineWave is not using the 2- or 3-argument View constructors
    public Wave(Context context, AttributeSet attrs){
        super(context,attrs);
        mPaint = new Paint();
        new Thread(this).start();
    }
    /*
    public Wave(Context context,float amplifier,float frequency,float phase){
        super(context);
        this.frequency = frequency;
        this.amplifier = amplifier;
        this.phase     = phase;
        mPaint = new Paint();
        new Thread(this).start();
    }
    */
    public float GetAmplifier(){
        return amplifier;
    }
    /*
    public float GetFrequency(){
        return frequency;
    }

    public float GetPhase(){
        return phase;
    }
    /*
    public void Set(float amplifier,float frequency,float phase){
        this.frequency = frequency;
        this.amplifier = amplifier;
        this.phase     = phase;

    }*/

    public void Set(float amplifier,float[] datat,float[] dataat,float[] databt)
    {
        this.amplifier = amplifier;
        for(int i=0;i<1000;i++) {
            this.data[i] = datat[i];
            this.dataa[i]=dataat[i];
            this.datab[i]=databt[i];
        }
    }

    public void SetXY(float px,float py)
    {
        this.px = px;
        this.py = py;
    }

    public void onDraw(Canvas canvas){
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        height = this.getHeight();
        width  = this.getWidth();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GREEN);
        //amplifier = (amplifier*2>height)?(height/2):amplifier;
        mPaint.setAlpha(200);
        mPaint.setStrokeWidth(1);
        float cy0 = height/5;
        float cy1 = height/5*2.5f;
        float cy2 = height/5*4;
        //float py=this.py-this.getTop();
        for(int i=0;i<width-1;i++)
        {
            mPaint.setColor(Color.GREEN);
            canvas.drawLine((float) i, cy0 - amplifier * data[i], (float) (i + 1), cy0 - amplifier * data[i + 1], mPaint);
            mPaint.setColor(Color.BLUE);
            canvas.drawLine((float) i, cy1 - amplifier * dataa[i], (float) (i + 1), cy1 - amplifier * dataa[i + 1], mPaint);
            mPaint.setColor(Color.RED);
            canvas.drawLine((float) i, cy2 - amplifier * datab[i], (float) (i + 1), cy2 - amplifier * datab[i+1], mPaint);
        }
        //System.out.print("\n");

        /*if(sp)
        {
            mPaint.setColor(Color.RED);
            mPaint.setTextSize(20);
            canvas.drawText("(x="+Float.toString(px)+",y="+Float.toString(py)+")", 20, 20, mPaint);
            sp = false;
        }*/
        mPaint.setColor(Color.BLUE);
        mPaint.setTextSize(20);
        canvas.drawText("(x="+Float.toString(px)+",y="+Float.toString(py)+")", 20, this.getHeight()-20, mPaint);
    }



    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        float px = event.getX();
        float py = event.getY();
        this.SetXY(px, py);
        return super.onTouchEvent(event);
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        while(!Thread.currentThread().isInterrupted())
        {
            try{
                Thread.sleep(10);
            }catch(InterruptedException e)
            {
                Thread.currentThread().interrupt();
            }
            postInvalidate();
        }
    }
}

