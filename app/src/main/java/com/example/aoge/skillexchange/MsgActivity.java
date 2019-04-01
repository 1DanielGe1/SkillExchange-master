package com.example.aoge.skillexchange;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Set;


public class MsgActivity extends BaseActivity {
    private UserInformation user;
    private ListView msgListView;
    private EditText inputText;
    private Button send;
    private MsgAdapter adapter;
    private TextView Uname;
    private String uname;
    private String email;
    private String talkto = UserInformation.userinformation;
    private String Mark = "";
    private String image = null;

//    public  String HOST = "169.254.26.233";//服务器地址
//    public  String HOST = "172.19.101.46";//服务器地址106.14.117.91
    public  String HOST = "106.14.117.91";
    public  int PORT = 8800;//连接端口号
    public  Socket socket = null;
    public  BufferedReader in = null;
    public  PrintWriter out = null;



    private List<Msg> msgList = new ArrayList<Msg>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_msg);

        Uname = (TextView)findViewById(R.id.txt_msg_username);
        //get the parameter from last activity.
        Intent intent = getIntent();
        talkto = (String) intent.getStringExtra("talktoemail");
        uname = (String)intent.getStringExtra("talktoname");
        Uname.setText(uname);
        image = (String)intent.getStringExtra("talktoimage");

        //the message adapter of message list.
        adapter = new MsgAdapter(MsgActivity.this, R.layout.item_msg_show, msgList);
        inputText = (EditText) findViewById(R.id.input_text);
        send = (Button) findViewById(R.id.send);
        msgListView = (ListView) findViewById(R.id.msg_list_view);
        msgListView.setAdapter(adapter);
        //get the chat history from txt file in this device.
        initMsgs();

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = inputText.getText().toString();
                if (!"".equals(content)) {
                    if (socket.isConnected()) {//if connect to server
                        if (!socket.isOutputShutdown()) {//if the stream is not closed.
                            Msg msg = new Msg(content, Msg.TYPE_SENT,String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))+
                                    String.valueOf(Calendar.getInstance().get(Calendar.MINUTE)),"Yes");
                            msgList.add(msg);

                            adapter.notifyDataSetChanged(); // when get the message,refresh the list.
                            msgListView.setSelection(msgList.size()); // Position it to the last row of listView.

                            new Thread(runnableout).start();
                        }
                    }
                }
            }
        });

        new Thread(runnable).start();
    }


    private void initMsgs() {
        File file = new File(UserInformation.ph+talkto.replace("@",""));

        FileInputStream in = null;
        BufferedReader reader = null;
        StringBuilder content = new StringBuilder();
        try{
            if(!file.exists()){
                file.createNewFile();
            }else {
                in = openFileInput(talkto.replace("@","").replace(".",""));//file name
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                String[] sp = null;

                while ((line = reader.readLine()) != null) {
                    sp = line.split(",,,,,");
                    Msg msg = new Msg(sp[0], Integer.parseInt(sp[1]), sp[2], sp[3]);
                    msgList.add(msg);
                    Mark = sp[0]+",,,,"+sp[2];
                }
                adapter.notifyDataSetChanged();
                msgListView.setSelection(msgList.size());
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            if (reader !=null){
                try{
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }


    Runnable runnableout = new Runnable(){
        @Override
        public void run() {

            out.println(UserInformation.userinformation+",,,,,"+talkto+",,,,,"+inputText.getText().toString());
            Mark = inputText.getText().toString()+",,,,"+String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))+":"+
                    String.valueOf(Calendar.getInstance().get(Calendar.MINUTE));
            inputText.setText(""); // 清空输入框中的内容
        }
    };



    /**
     * get the message from server, and send it to UI thread by Handler.
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            connection();// connect to server
            try {
                while (true) {//get the message from server
                    if (!socket.isClosed()) {//if the server is opened.
                        if (socket.isConnected()) {//connect.
                            if (!socket.isInputShutdown()) {//if the inout stream is not closed.
                                String getLine;
                                if ((getLine = in.readLine()) != null) {//get the message.
                                    Message message = new Message();
                                    message.obj = getLine;
                                    mHandler.sendMessage(message);//UI refresh.
                                } else {

                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * get the message from thread.
     */
    public Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String m = (String)msg.obj;
            String[]str = m.split(",,,,,");
            if(str[0].equals(UserInformation.userinformation)){
                Toast.makeText(getApplicationContext(),
                        "The user is not online, message sent failed!", Toast.LENGTH_LONG)
                        .show();
            }else{
                Msg msgs = new Msg(str[2], Msg.TYPE_RECEIVED,String.valueOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY))+":"+
                        String.valueOf(Calendar.getInstance().get(Calendar.MINUTE)),"Yes");
                msgList.add(msgs);
                Mark = str[2]+",,,,"+msgs.getTheTime();
                adapter.notifyDataSetChanged();
                msgListView.setSelection(msgList.size());
            }

        }
    };

    /**
      * connect to server
      */
    private void connection() {
        try {
            socket = new Socket(HOST, PORT);
            in = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream())), true);
            String information = UserInformation.userinformation+",,,,,"+talkto+",,,,,"+"My first message.";
            out.println(information);
            System.out.println("Success");
        } catch (IOException ex) {
            ex.printStackTrace();
            ShowDialog("Failed to connect to server：" + ex.getMessage());
        }
    }

    /**
     * if something wrong, AlertDialog！
     */
    public void ShowDialog(String msg) {
        new AlertDialog.Builder(this).setTitle("Notice").setMessage(msg)
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
    }

    /**
     * click the back button.
     */
    public void btnmsgBack(View view){
        SaveToFile();
        finish();
    }

    /**
     * click the back button of the device.
     */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            SaveToFile();
            finish();
        }
        return false;
    }

    /**
     * save the chat history to the device.
     */
    public void SaveToFile() {
        String[]str = Mark.split(",,,,");

        int mk=0;
        for(int i=0;i<UserInformation.historyList.size();i++){
            if(UserInformation.historyList.get(i).get("talkto").equals(talkto)){
                UserInformation.historyList.get(i).put("username",uname);
                UserInformation.historyList.get(i).put("image",image);
                UserInformation.historyList.get(i).put("content",str[0]);
                UserInformation.historyList.get(i).put("time",str[1]);
                mk = 1;
            }
        }

        if(mk==0){
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("username",uname);
            map.put("talkto",talkto);
            map.put("image",image);
            map.put("content",str[0]);
            map.put("time",str[1]);
            UserInformation.historyList.add(map);
        }

        File file = new File(UserInformation.ph+"history.txt");
        if(file.exists()){
            file.delete();
        }else{
            try{
            FileOutputStream fos=new FileOutputStream(file);
            OutputStreamWriter osw=new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter  bw=new BufferedWriter(osw);
            String all="";
            for(int i=0;i<UserInformation.historyList.size();i++){
                all = UserInformation.historyList.get(i).get("talkto")+",,,,"+UserInformation.historyList.get(i).get("image")+",,,,"+UserInformation.historyList.get(i).get("content")+",,,,"+UserInformation.historyList.get(i).get("time")+",,,,"+UserInformation.historyList.get(i).get("username");

                bw.write(all);
                bw.newLine();
            }

                bw.close();
                osw.close();
                fos.close();
            }catch (IOException e){
                e.printStackTrace();
            }

        }

        FileOutputStream outt = null;
        BufferedWriter writer = null;
        file = new File(talkto.replace("@","").replace(".",""));
        if(file.exists()){
            file.delete();
        }
        try{
            outt = openFileOutput(talkto.replace("@","").replace(".",""), Context.MODE_PRIVATE);
            writer = new BufferedWriter(new OutputStreamWriter(outt));

            String all="";
            for(int i=0;i<msgList.size();i++){
                all = msgList.get(i).getContent()+",,,,,"+msgList.get(i).getType()+",,,,,"+msgList.get(i).getTheTime()+",,,,,"+msgList.get(i).getRorn();

                writer.write(all);
                writer.newLine();
            }
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            try{
                if (writer !=null){
                    writer.close();
                }
            }catch (IOException e){
                e.printStackTrace();
            }
        }


    }
}