package com.example.aoge.skillexchange;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class LaunchActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_launch);


//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                listhistory(LaunchActivity.this);
//            }
//        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                readFromFile(LaunchActivity.this);
            }
        }).start();

    }

    public void readFromFile(Context context) {
        String path = context.getFilesDir()+"/userinfo.txt";
        UserInformation.ph = context.getFilesDir()+"/";
        File dir = new File(path);
        FileReader reader;
        try {
            if(dir.exists()) {
                reader = new FileReader(path);
                BufferedReader breader = new BufferedReader(reader);
                String line = breader.readLine();
                if (line.equals(null)) {
                    Intent intent = new Intent(context, LoginActivity.class);
                    startActivity(intent);
                    breader.close();

                    finish();
                } else {
                    UserInformation.userinformation = line;
                    listhistory(LaunchActivity.this);
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                    breader.close();

                    finish();
                }
            }else{

                dir.createNewFile();

                Intent intent = new Intent(context, LoginActivity.class);
                startActivity(intent);
                finish();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void listhistory(Context context){

        File dir = new File(UserInformation.ph+"history.txt");
        FileReader reader;
        try {
            if(dir.exists()) {
                reader = new FileReader(UserInformation.ph+"history.txt");
                BufferedReader breader = new BufferedReader(reader);

                String line = "";
                String[] sp = null;

                while ((line = breader.readLine()) != null) {
                    sp = line.split(",,,,");
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("talkto",sp[0]);
                    map.put("image",sp[1]);
                    map.put("content",sp[2]);
                    map.put("time",sp[3]);
                    map.put("username",sp[4]);
                    UserInformation.historyList.add(map);
                }
                breader.close();


            }else{

                dir.createNewFile();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
