package com.example.bluemaple.distancemearsure;

/**
 * Created by Chenyx on 2015/11/20.
 */
public class Cache {
    public  float data[]=new float[1000];
    public Cache(){
        for(int i=0;i<1000;i++)
        {
            data[i]=0.0f;
        }
    }
    public void append(float a)
    {
        for(int i=0;i<1000-1;i++)
        {
            data[i]=data[i+1];
        }
        data[1000-1]=a;
    }
}
