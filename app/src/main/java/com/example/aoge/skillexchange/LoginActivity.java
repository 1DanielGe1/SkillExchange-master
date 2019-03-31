package com.example.aoge.skillexchange;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends CheckPermissionsActivity {

    private EditText inputEmail, inputPassword;
    private Button loginButton;
    private UserInformation userinformation;
    private String location="";
    AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
//        if(mark == CheckPermissionsActivity.Onr){
//            loginButton.setClickable(false);
//        }
        userinformation = new UserInformation();
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        inputPassword.setOnClickListener(new View.OnClickListener() {
                                             @Override
                                             public void onClick(View v) {
                                                 getLocation();
                                             }
        });


        loginButton = findViewById(R.id.btnLogin);
//        System.out.println(this.getFilesDir());

    }

    /**
     * Press the button Login, go to Login form
     *
     * @param view from the activity_login.xml
     */
    public void btnLogin(View view) {


        String email = inputEmail.getText().toString().trim();
        String password = inputPassword.getText().toString().trim();

        // Check for empty data and email format in the form
        if(email.isEmpty() || password.isEmpty()){
            Toast.makeText(getApplicationContext(),
                    R.string.enter_credentials, Toast.LENGTH_LONG)
                    .show();
        }else if(!emailFormat(email)){
            Toast.makeText(getApplicationContext(),
                    R.string.enter_emailcredentials, Toast.LENGTH_LONG)
                    .show();
        }else if(UserInformation.permission == -1){
            Toast.makeText(getApplicationContext(),
                    "Sorry, this App can't work if you don't allow the required permission.", Toast.LENGTH_LONG)
                    .show();
        }else if(location.equals("")) {
            Toast.makeText(getApplicationContext(),
                    "Can't get your location, please click this button later.", Toast.LENGTH_LONG)
                    .show();
            getLocation();
        }else{
            // Avoid multiple clicks on the button
            loginButton.setClickable(false);
            //Check the login request.
            LoginRequest(email,password,location);
        }
    }

    /**
     * Press the button register, go to Registration form
     *
     * @param view from the activity_login.xml
     */
    public void btntoRegister(View view) {
        startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
        finish();
    }

    /**
     * link to server to depend whether the username and password are right.
     * @param email
     * @param password
     */
    public void LoginRequest(final String email, final String password, final String location) {
        //request url
        String url = "http://106.14.117.91:8080/SkillsExchangeServer/LoginServlet";    //注①
        String tag = "Login";    //注②

        //get the request queue
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());

        //cancel the request queue that marked by "Register" in order to not request again.
        requestQueue.cancelAll(tag);

        //build StringRequest and set the request method "POST"(default "GET")
        final StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = (JSONObject) new JSONObject(response).get("params");  //注③
                            String result = jsonObject.getString("Result");
                            if (result.equals("successF")) {
                                saveToFile(LoginActivity.this,email);
                                Intent intent = new Intent(getApplicationContext(), FirstLoginActivity.class);
                                intent.putExtra("email",email);
                                startActivity(intent);
                                finish();
                            } else if(result.equals("success")){
                                saveToFile(LoginActivity.this,email);
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                Toast.makeText(getApplicationContext(),
                                        "Email or password is wrong!", Toast.LENGTH_LONG)
                                        .show();
                                loginButton.setClickable(true);
                            }
                        } catch (JSONException e) {
                            Log.e("TAG", e.getMessage(), e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        "No internet!", Toast.LENGTH_LONG)
                        .show();
                Log.e("TAG", error.getMessage(), error);
                loginButton.setClickable(true);
            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("AccountNumber", email);  //set the parameter.
                params.put("Password", password);
                params.put("Location", location);
                return params;
            }
        };

        //set the tag.
        request.setTag(tag);

        //add the request to queue.
        requestQueue.add(request);
    }

    public void saveToFile(Context context, String email) {
        UserInformation.userinformation = email;
        String path = context.getFilesDir() + "/userinfo.txt";

//        File dir = context.getFilesDir(); //查找这个应用下的所有文件所在的目录
        FileWriter writer;
        try {
            writer = new FileWriter(path);
            writer.append(email);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




    /**
     * Check the email format.
     * @param email
     */
    public boolean emailFormat(String email)
    {
        boolean tag = true;
        String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(pattern1);
        Matcher mat = pattern.matcher(email);
        if (!mat.find()) {
            tag = false;
        }
        return tag;
    }

    public void getLocation(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                //初始化定位
                mLocationClient = new AMapLocationClient(getApplicationContext());
                //设置定位回调监听
                mLocationClient.setLocationListener(mLocationListener);
                //初始化AMapLocationClientOption对象
                mLocationOption = new AMapLocationClientOption();

 /* //设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景） 设置了场景就不用配置定位模式等
    option.setLocationPurpose(AMapLocationClientOption.AMapLocationPurpose.SignIn);
    if(null != locationClient){
        locationClient.setLocationOption(option);
        //设置场景模式后最好调用一次stop，再调用start以保证场景模式生效
        locationClient.stopLocation();
        locationClient.startLocation();
    }*/
                // 同时使用网络定位和GPS定位,优先返回最高精度的定位结果,以及对应的地址描述信息
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                //只会使用网络定位
   /* mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Battery_Saving);*/
                //只使用GPS进行定位
    /*mLocationOption.setLocationMode(AMapLocationMode.Device_Sensors);*/
                // 设置为单次定位 默认为false
    /*mLocationOption.setOnceLocation(true);*/
                //设置定位间隔,单位毫秒,默认为2000ms，最低1000ms。默认连续定位 切最低时间间隔为1000ms
                mLocationOption.setInterval(1000);
                //设置是否返回地址信息（默认返回地址信息）
    /*mLocationOption.setNeedAddress(true);*/
                //关闭缓存机制 默认开启 ，在高精度模式和低功耗模式下进行的网络定位结果均会生成本地缓存,不区分单次定位还是连续定位。GPS定位结果不会被缓存。
    /*mLocationOption.setLocationCacheEnable(false);*/
                //给定位客户端对象设置定位参数
                mLocationClient.setLocationOption(mLocationOption);
                //启动定位
                mLocationClient.startLocation();
            }
        }).start();
    }

    /**
     * 定位回调监听器
     */
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation amapLocation) {

            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    amapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表
                    double currentLat = amapLocation.getLatitude();//获取纬度
                    double currentLon = amapLocation.getLongitude();//获取经度
//                        latLonPoint = new LatLonPoint(currentLat, currentLon);  // latlng形式的
                /*currentLatLng = new LatLng(currentLat, currentLon);*/   //latlng形式的
                    Log.i("currentLocation", "currentLat : " + currentLat + " currentLon : " + currentLon);
                    Log.i("currentLocation", "currentLat : " + amapLocation.getCity()+amapLocation.getAddress());
//                    System.out.println(amapLocation.getCity());
//                    Toast.makeText(getApplicationContext(),
//                            location, Toast.LENGTH_LONG)
//                            .show();
                    location = amapLocation.getCity();
                    amapLocation.getAccuracy();//获取精度信息
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLocationClient!=null) {
            mLocationClient.onDestroy();//销毁定位客户端。
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(mLocationClient!=null) {
            mLocationClient.startLocation(); // 启动定位
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        if(mLocationClient!=null) {
            mLocationClient.stopLocation();//停止定位
        }
    }

}

