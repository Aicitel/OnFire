package com.example.bluemaple.distancemearsure;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by DELL on 2015/11/6.
 */
public class SocketClient {

    static Socket client = null;
    static String site = "10.209.203.107";
    static int port = 8888;
    public SocketClient() {
        try {
            client = new Socket(site, port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String sendMsg(String msg){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream());
            out.println(msg);
            out.flush();
            return in.readLine();
        }catch(IOException e){
            e.printStackTrace();
        } finally {
            closeSocket();
        }
        return "";
    }

    private void closeSocket(){
        try{
            client.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
}
