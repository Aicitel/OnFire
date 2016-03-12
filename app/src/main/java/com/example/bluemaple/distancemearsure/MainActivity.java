package com.example.bluemaple.distancemearsure;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
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

public class MainActivity extends Activity implements SensorEventListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    //cache to draw
    private  Cache datax=new Cache();
    private  Cache dataxa=new Cache();
    private  Cache dataxb=new Cache();
    //Wave wv=null;

    private WifiManager mWifiManager = null;
    private WifiInfo mWifiInfo = null;
    private List<ScanResult>mWifiList;
    //private Handler handler;
    private int level;

    //Socket Client
    Socket client = null;

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
            Bundle data = msg.getData();
            String val = data.getString("value");
            Toast.makeText(MainActivity.this,"handler", Toast.LENGTH_SHORT).show();
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

    /*
    Handler loopHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                Thread thread = new NetworkThread("looping", handler);
                thread.start();
                thread.join();
                //String value = ((NetworkThread) thread).getSendString;
               // String res[] = value.split("\\|");

                if (res[0].equals("start")) {
                    runStory(res[1]);
                    return;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            //new WaitThread(1000, loopHandler).start();
        }
    };*/

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //wv = new Wave(this);
        setContentView(R.layout.activity_main);
        //MyView myView = new MyView(this);
        //myView.setX(0);
        //myView.setY(0);
        //setContentView( new MyView(this));
        /*
        textview = (TextView) findViewById(R.id.textViewDot);
        textviewX = (TextView) findViewById(R.id.textView1);
        textviewY = (TextView) findViewById(R.id.textView2);
        */
        //textviewY.setText("1234567789");
        /*
        textviewZ = (TextView) findViewById(R.id.textView3);
        textviewM = (TextView) findViewById(R.id.textView4);
        textviewD = (TextView) findViewById(R.id.textView5);
        textviewS = (TextView) findViewById(R.id.textViewAccLock);
        iv = (ImageView)findViewById(R.id.imageView);
        */
        //iv.setl
        /*
        iv.setImageResource(R.drawable.ic_launcher);
        sendButton = (Button)findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new ButtonSend());

        accLockButton = (Button)findViewById(R.id.accButton);
        accLockButton.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        if(ACC_LOCK==0) {
                            ACC_LOCK = 1;
                            textviewS.setText("1");
                        }
                        displayments[0]=0;
                        displayments[1]=0;
                        displayments[2]=0;
                        speeds[0]=0;
                        speeds[1]=0;
                        speeds[2]=0;

                    }
                }
        );

        Button resetButton = (Button)findViewById(R.id.resetButton);
        resetButton.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        displayments[0]=0;
                        displayments[1]=0;
                        displayments[2]=0;
                        speeds[0]=0;
                        speeds[1]=0;
                        speeds[2]=0;

                    }
                }
        );
        Button sensorButton = (Button)findViewById(R.id.sensorButton);
        sensorButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
                        String sensorList = "";
                        for (Sensor sensor : sensors)
                            sensorList += (sensor.getName() + "\n");

                        Toast.makeText(MainActivity.this, sensorList, Toast.LENGTH_SHORT).show();
                    }
                }
        );*/

        mc = new MyCount(10000, 1000);

        Button startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(
                new View.OnClickListener(){
                    public void onClick(View v){

                        Intent intent = new Intent(MainActivity.this,GameActivity.class);
                        startActivityForResult(intent,1);
                    }
                }
        );

        /*
        Button button = (Button)findViewById(R.id.verificationButton);
        button.setOnClickListener(
            new View.OnClickListener() {
                public void onClick(View v)
                {
                    verificationFlag = 0;
                    sensorDot = 0;
                    verification[0]=0;
                    verification[1]=0;
                    verification[2]=0;
                    mc.start();

                }
        });

        mWifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        Timer signCheckerTimer = new Timer();
        signCheckerTimer.scheduleAtFixedRate(new SignChecker(),1000,1000);
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg){
                Bundle data = msg.getData();
            }
        };*/

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        aSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);// TYPE_EXCEPT_GRAVITY
        gSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);// TYPE_GRAVITY
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);//MAGNETIC_FIELD
        /*
        if (null == mSensorManager) {
            Log.d(TAG, "deveice not support SensorManager");
        }*/
        // 参数三，检测的精准度
        mSensorManager.registerListener(this, aSensor, SensorManager.SENSOR_DELAY_GAME);// SENSOR_DELAY_GAME
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);// SENSOR_DELAY_GAME
        mSensorManager.registerListener(this, gSensor, SensorManager.SENSOR_DELAY_GAME);// SENSOR_DELAY_GAME

        values = new float[3];
        valuesR = new float[9];

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        //Log.d("timing", now.getTime() + " " + now);

        if (event.sensor == null) {
            return;
        }
        int type = 0;
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
                System.out.println("MOVE "+linearmeterValues[0]+" "+linearmeterValues[1]+" "+linearmeterValues[2]);
                //warning
                //filterAcc[0] = (float)(linearmeterValues[0]-resAcc[0]);
                //filterAcc[1] = (float)(linearmeterValues[1]-resAcc[1]);
                //filterAcc[2] = (float)(linearmeterValues[2]-resAcc[2]);
                linearmeterValues[0] = 0;
                linearmeterValues[1] = 0;
                linearmeterValues[2] = 0;
                if(lastInterval<now.getTime()-2000) {
                    fequency = 0;
                    lastInterval = now.getTime();
                    feqReverse *= -1;
                }
                if(fequency<130){
                        if(fequency==0 || fequency==65){
                            linearmeterValues[0] = 0;
                            linearmeterValues[1] = 0;
                        }
                        if(fequency<65){
                            linearmeterValues[0] = feqReverse;
                            linearmeterValues[1] = feqReverse;
                        }
                        if(fequency>65){
                            linearmeterValues[0] = -feqReverse;
                            linearmeterValues[1] = -feqReverse;
                        }
                    fequency++;
                }

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
                datax.append((float)weightedAcc[0]);
                dataxa.append((float)weightedAcc[1]);
                dataxb.append((float) weightedAcc[2]);
                //wv.Set(200f, datax.data, dataxa.data, dataxb.data);

                iv.setX((int) (displayments[0] * 10000.0+500.0));
                iv.setY((int) (-displayments[1] * 10000.0+500.0));
                //setDisplayment(calDisplayment(linearmeterValues, deltaTime), values);

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
