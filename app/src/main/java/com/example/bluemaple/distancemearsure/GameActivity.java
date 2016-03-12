package com.example.bluemaple.distancemearsure;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by bluemaple on 2015/11/16.
 */
public class GameActivity extends Activity  implements SensorEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private int lifePoint = 100;
    private int lastPoint = 100;
    private Vibrator vibrator;
    long [] pattern = {100,400,100,400};

    private WifiManager mWifiManager = null;
    private WifiInfo mWifiInfo = null;
    private List<ScanResult>mWifiList;
    //private Handler handler;
    private int level;

    //Socket Client
    Socket client = null;
    int interval = 1;
    int timeCount = 0;

    private int verificationFlag = 0;        // 0 means start work, 2 means finish
    private int ACC_LOCK = 0;
    private double[] lockedAcc = {1,1,1};
    private SensorManager mSensorManager;
    private Sensor aSensor;
    private Sensor mSensor;
    private Sensor gSensor;
    private TextView textview;
    private TextView textviewX;
    private TextView textviewY;
    private TextView textviewZ;
    private TextView textviewD;
    private TextView textviewF;
    private TextView textviewM;
    private TextView textviewS;
    Button accLockButton;
    Button sendButton;
    private ImageView iv;

    int fequency = 0;
    int feqReverse = 1;
    long lastInterval = 0;

    private int size = 7;
    private double[] gaussCache = {1,6,15,20,15,6,1};

    private double[][] accCache = {{0,0,0,0,0,0,0},{0,0,0,0,0,0,0},{0,0,0,0,0,0,0}};

    private MyCount mc;
    private double MaxX = 0;
    private double FMaxX = 0;
    private double sensorDot = 0;
    private double verification[] = {0.0,0.0,0.0};
    private double resAcc[] = {0.0,0.0,0.0};

    private double speeds[] = {0.0,0.0,0.0};
    private double displayments[] ={0.0,0.0,0.0};
    private float rotations[] = {0,0,0};
    private int moveLock[] = {0,0,0};
    float[] values;
    float[] valuesR;
    float[] linearmeterValues = new float[3];
    float[] accelerometerValues = new float[3];
    float[] magneticFieldValues = new float[3];

    private long lasttimestamp = 0;

    class MyCount extends CountDownTimer{
        public MyCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture,countDownInterval);
        }
        @Override
        public void onFinish(){
            resAcc[0] = verification[0]/sensorDot;
            resAcc[1] = verification[1]/sensorDot;
            resAcc[2] = verification[2]/sensorDot;
            //textview.setText("Finish "+resAcc[0]+" "+resAcc[1]+" "+resAcc[2]);
            verificationFlag = 2;
            MaxX = 0;
            FMaxX = 0;
            this.cancel();
        }
        @Override
        public void onTick(long millisUntilFinished){
            //textview.setText("请等待10秒(" + millisUntilFinished / 1000 + ")...");
        }

    }
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String data = msg.getData().toString();
            lifePoint-=Integer.parseInt(data);
        }
    };
    public class NetworkThread extends Thread {
        public String sendString;
        public String getSendString;
        Handler handler;

        public NetworkThread(String send,Handler h) {
            sendString = send;
            handler = h;
        }

        @Override
        public void run() {
            Message msg = new Message();
            Bundle data = new Bundle();
            String site="192.168.43.33";
            int  port = 39998;
            try {
                if(client==null)
                    client = new Socket(site, port);
                System.out.println("after new");
                //Toast.makeText(MainActivity.this, "ok", Toast.LENGTH_SHORT).show();
                writeStr2Stream((new Timestamp(System.currentTimeMillis())).getTime() + "", client.getOutputStream());
                System.out.println("after w2s");
                //client.close();
                Looper.prepare();
            } catch (Exception e) {
                Looper.prepare();
                System.out.println("after BOOOOOOM");
                //Toast.makeText(MainActivity.this,e.toString(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
            /*
            SocketClient client = new SocketClient();
            String val = client.sendMsg(sendString);
            client.s;

            data.putString("value", val);
            getSendString = val;
            msg.setData(data);
            handler.sendMessage(msg);*/
        }
    }

    class ButtonSend implements View.OnClickListener{
        @Override
        public void onClick(View v){
            try {
                NetworkThread thread = new NetworkThread("dingdingding",handler);
                thread.start();
                //thread.join();
            }catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static void writeStr2Stream(String str, OutputStream out) throws IOException {
        try {
            // add buffered writer
            BufferedOutputStream writer = new BufferedOutputStream(out);

            // write
            writer.write(str.getBytes());

            writer.flush();
        } catch (IOException ex) {
            System.out.println(ex);
            throw ex;
        }
    }

    int fireMusic;
    ImageView weaponImage;
    ImageView ringImage;
    int coldDown = 10;
    int weaponType = 0;
    int ammoAmount = 0;
    int boomFlag = 1;

    static int lastX;
    static int lastY;
    static int PosX;
    static int PosY;
    private SoundPool sound;

    int rifleMusic,shotMusic,aceMusic;
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game_main);

        weaponImage = (ImageView)findViewById(R.id.weaponImage);
        //ringImage = (ImageView)findViewById(R.id.weaponRing);
        weaponImage.setOnTouchListener(new MyTouchListener());

        sound = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);
        rifleMusic=sound.load(this, R.raw.rifle, 1);
        shotMusic=sound.load(this, R.raw.beshot, 1);
        aceMusic=sound.load(this, R.raw.real_ace, 1);
        fireMusic=sound.load(this, R.raw.fireinthehore, 1);


        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);// TYPE_EXCEPT_GRAVITY
        gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);//MAGNETIC_FIELD

        mSensorManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_UI);// SENSOR_DELAY_GAME
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);// SENSOR_DELAY_GAME
        mSensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_UI);// SENSOR_DELAY_GAME

        values = new float[3];
        valuesR = new float[9];

        vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

    }

    protected class MyTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            /*
            if(interval==1){
                interval=0;
                weaponImage.setImageResource(R.drawable.blood);
            }
            else{
                interval=1;
                weaponImage.setImageResource(R.drawable.rifle47);
            }*/
            int action = event.getAction();
            float downX = event.getRawX();
            float downY = event.getRawY();

            switch (action) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getRawX();
                    downY = event.getRawY();
                    Toast.makeText(GameActivity.this, "down" + downX + " " + downY, Toast.LENGTH_SHORT).show();

                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();

                    if(downX<350 && downY<600) {
                        if(weaponType>0)
                            weaponType--;
                        if(weaponType==1) {
                            weaponImage.setImageResource(R.drawable.handgun);
                        }
                        if(weaponType==0) {
                            weaponImage.setImageResource(R.drawable.rifle47);
                        }
                    }
                    if(downX>600 && downY<600) {
                        if(weaponType<2)
                            weaponType++;
                        if(weaponType==1)
                            weaponImage.setImageResource(R.drawable.handgun);
                        if(weaponType==2 && boomFlag!=0) {
                            weaponImage.setImageResource(R.drawable.ring);
                        }
                        if(weaponType==2 && boomFlag==0) {
                            weaponImage.setImageResource(R.drawable.white);
                        }
                        //Toast.makeText(GameActivity.this, "shiftH " + downX + " " + downY, Toast.LENGTH_SHORT).show();
                    }

                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    if(boomFlag!=0) {
                        int dx = (int) event.getRawX() - lastX;
                        int dy = (int) event.getRawY() - lastY;
                        PosX += dx;
                        PosY += dy;
                        if (weaponType == 2) {
                            weaponImage.setX(PosX);
                            weaponImage.setY(PosY);
                        }
                        lastX = (int) event.getRawX();
                        lastY = (int) event.getRawY();
                    }

                    break;
                }
                case MotionEvent.ACTION_UP: {
                    if(weaponType==2) {
                        if(PosX>300 && PosY<-350) {
                            weaponImage.setImageResource(R.drawable.white);
                            sound.play(fireMusic, 1, 1, 0, 0, 1);
                            boomFlag = 0;
                            weaponImage.setX(0);
                            weaponImage.setY(0);
                        }
                        else {
                            PosX = 0;
                            PosY = 0;
                            weaponImage.setX(0);
                            weaponImage.setY(0);
                        }
                    }
                    lastX = (int) event.getRawX();
                    lastY = (int) event.getRawY();
                    break;
                }
            }
            if(downY>1000 && weaponType!=2) {
                try {
                    NetworkThread thread = new NetworkThread
                            ("{'ID':0,'data':{" +"'X':"+displayments[0]+",'Y':"+displayments[1]+",'EventType':'ATK',"+
                                    "'rotation':"+"["+values[0]+","+values[1]+","+values[2]+"],"+"'WeaponType':"+weaponType+
                                    "}}",
                                    handler);
                    thread.start();
                }catch (Exception e) {
                    e.printStackTrace();
                }
                sound.play(1, 1, 1, 0, 0, 1);
            }

            return false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {


        Timestamp now = new Timestamp(System.currentTimeMillis());
        //Log.d("timing", now.getTime() + " " + now);
        if(lifePoint<=0)
            Toast.makeText(GameActivity.this, "YOU LOSE!", Toast.LENGTH_LONG).show();
        if(lifePoint<lastPoint) {
            vibrator.vibrate(pattern, 2);
            lastPoint = lifePoint;
        }
        if (event.sensor == null) {
            return;
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = event.values;
        }

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            //if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            linearmeterValues = event.values;
        }
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = event.values;
        }

        SensorManager.getRotationMatrix(valuesR, null, accelerometerValues, magneticFieldValues);
        SensorManager.getOrientation(valuesR, values);
        rotations = values;

        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            //reset flag
            final int ZERO_SET = 0;
            final int ZUNI_SET = 1;
            int resetFlag = ZUNI_SET;

            //Verify Mode
            //if(verificationFlag==0)
            //    setVeriDif(event.values);

            //Display Mode
            if(lasttimestamp!=0 && verificationFlag==2) {
                long deltaTime = now.getTime() - lasttimestamp;
                lasttimestamp = now.getTime();

                float[] filterAcc = new float[3];
                //System.out.println("MOVE "+linearmeterValues[0]+" "+linearmeterValues[1]+" "+linearmeterValues[2]);
                //warning
                //filterAcc[0] = (float)(linearmeterValues[0]-resAcc[0]);
                //filterAcc[1] = (float)(linearmeterValues[1]-resAcc[1]);
                //filterAcc[2] = (float)(linearmeterValues[2]-resAcc[2]);

                for(int i =0;i<3;i++) {
                    for(int j=1;j<size;j++) {
                        accCache[i][j-1] = accCache[i][j];
                    }
                    accCache[i][0] = linearmeterValues[i];
                }

                double cntGaussX = 0;
                double cntGaussY = 0;
                double cntGaussZ = 0;
                for(int i = 0 ;i<size;i++) {
                    cntGaussX+=accCache[0][i]*gaussCache[i];
                    cntGaussY+=accCache[1][i]*gaussCache[i];
                    cntGaussZ+=accCache[2][i]*gaussCache[i];
                }
                cntGaussX/=(double)size;
                cntGaussY/=(double)size;
                cntGaussZ/=(double)size;

                if(Math.abs(cntGaussX)<0.005)
                    filterAcc[0] = 0;
                else
                    filterAcc[0] =(float)cntGaussX;

                if(Math.abs(cntGaussY)<0.005)
                    filterAcc[1] = 0;
                else
                    filterAcc[1] =(float)cntGaussY;

                if(Math.abs(cntGaussZ)<0.005)
                    filterAcc[2] = 0;
                else
                    filterAcc[2] =(float)cntGaussZ;

                if(filterAcc[0]>MaxX)
                    MaxX = filterAcc[0];
                if(filterAcc[0]<FMaxX)
                    FMaxX =filterAcc[0];

                textviewX.setText("X " + filterAcc[0]+'\n'+MaxX+'\n'+FMaxX);
                textviewY.setText("Y " + filterAcc[1]);
                textviewZ.setText("Z " + filterAcc[2]);

                double[] weightedAcc = {0,0,0};
                if(ACC_LOCK == 1) {
                    filterAcc[0] = 1;
                    filterAcc[1] = 1;
                    filterAcc[2] = 1;
                }
                //for(int i=0;i<3;i++)
                //    weightedAcc[i] = (double)linearmeterValues[i];
                //weightedAcc = simpleCalDisplayments(filterAcc,(double)deltaTime);
                //displayments[0]+=weightedAcc[0];
                weightedAcc = getWeigthedAcc(filterAcc, valuesR);
                for(int i = 0;i<3;i++){
                    speeds[i] += weightedAcc[i]*(double)deltaTime/1000;
                    if(resetFlag == resetFlag) {
                        if (Math.abs(filterAcc[i]) < 0.01)
                            speeds[i] /= 1.5;
                    }
                    displayments[i] += (speeds[i]*(double)deltaTime/1000);
                }
                try {
                    NetworkThread thread = new NetworkThread
                            ("{'ID':0,'data':{" +"'X':"+displayments[0]+",'Y':"+displayments[1]+",'EventType':'MOV',"+
                                    "'rotation':"+"["+valuesR[1]+","+valuesR[4]+","+valuesR[7]+"],"+"'WeaponType':"+weaponType+
                                    "}}",
                                    handler);
                    thread.start();
                }catch (Exception e) {
                    e.printStackTrace();
                }

            }
            else
                lasttimestamp = now.getTime();
        }
    }
    public double[] getWeigthedAcc(float[] sourceAcc,float[] values) {
        double[] xAxisArray = new double[3];
        xAxisArray[0] = values[0];
        xAxisArray[1] = values[3];
        xAxisArray[2] = values[6];

        double[] yAxisArray = new double[3];
        yAxisArray[0] = values[1];
        yAxisArray[1] = values[4];
        yAxisArray[2] = values[7];

        double[] zAxisArray = new double[3];
        zAxisArray[0] = values[2];
        zAxisArray[1] = values[5];
        zAxisArray[2] = values[8];

        double[] temp = new double[3];
        temp[0] = -sourceAcc[2]*zAxisArray[0];
        temp[1] = -sourceAcc[2]*zAxisArray[1];
        temp[2] = -sourceAcc[2]*zAxisArray[2];

        temp[0] += -sourceAcc[0]*xAxisArray[0];
        temp[1] += -sourceAcc[0]*xAxisArray[1];
        temp[2] += -sourceAcc[0]*xAxisArray[2];

        temp[0] += sourceAcc[1]*yAxisArray[0];
        temp[1] += sourceAcc[1]*yAxisArray[1];
        temp[2] += sourceAcc[1]*yAxisArray[2];

        return temp;

    }
}
