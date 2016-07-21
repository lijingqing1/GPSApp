package com.example.king.gpsapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity {
    private final  int TIME = 2500;
    private EditText editText;
    private TextView tv_gps;
    private Button button1;//红色保存
    private Button button2;//绿色保存
    private Button auto_button1;//红色自动保存
    private Button auto_button2;
    private Boolean auto_button1_flag=false;//红色自动保存标准
    private Boolean auto_button2_flag=false;

    String filePath = Environment.getExternalStorageDirectory() + "/save/";
    String fileName = "save.txt";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        tv_gps = (TextView) findViewById(R.id.textView1);
        button1 = (Button) findViewById(R.id.button1);
        button2 = (Button) findViewById(R.id.button2);
        auto_button1 = (Button) findViewById(R.id.auto_button1);
        auto_button2 = (Button) findViewById(R.id.auto_button2);
        registerGPS();
        handler.postDelayed(runnable, TIME);
    }


    public LocationManager lm;
    private static final String TAG = "GPS Services";
    private void registerGPS() {

        button1.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                writeTxtToFile("red,"+editText.getText().toString().trim(), filePath, fileName);
                Toast.makeText(getApplicationContext(), "红点保存成功",
                        Toast.LENGTH_SHORT).show();
            }
        });
        button2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                writeTxtToFile("green,"+editText.getText().toString().trim(), filePath, fileName);
                Toast.makeText(getApplicationContext(), "绿点保存成功",
                        Toast.LENGTH_SHORT).show();
            }
        });
        auto_button1.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (auto_button1_flag==false) {
                    auto_button1_flag=true;
                    button1.setClickable(false);
                    button1.setText("自动保存红点中...");
                    auto_button1.setText("停止自动保存红点");
                } else {
                    auto_button1_flag=false;
                    button1.setClickable(true);
                    button1.setText("手动保存红点");
                    auto_button1.setText("自动保存红点");
                }

            }
        });
        auto_button2.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (auto_button2_flag==false) {
                    auto_button2_flag=true;
                    button2.setClickable(false);
                    button2.setText("自动保存绿点中...");
                    auto_button2.setText("停止自动保存绿点");
                } else {
                    auto_button2_flag=false;
                    button2.setClickable(true);
                    button2.setText("手动保存绿点");
                    auto_button2.setText("自动保存绿点");

                }
            }
        });
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //判断GPS是否正常启动
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "请开启GPS导航...", Toast.LENGTH_SHORT).show();
            //返回开启GPS导航设置界面
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 0);
            return;
        }

        //为获取地理位置信息时设置查询条件
        String bestProvider = lm.getBestProvider(getCriteria(), true);


//        lm.setTestProviderEnabled(bestProvider,true);
        //获取位置信息
        //如果不设置查询要求，getLastKnownLocation方法传人的参数为LocationManager.GPS_PROVIDER
        Location location = lm.getLastKnownLocation(bestProvider);
//        Location location= lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        updateView(location);
        //监听状态
        lm.addGpsStatusListener(listener);
        //绑定监听，有4个参数
        //参数1，设备：有GPS_PROVIDER和NETWORK_PROVIDER两种
        //参数2，位置信息更新周期，单位毫秒
        //参数3，位置变化最小距离：当位置距离变化超过此值时，将更新位置信息
        //参数4，监听
        //备注：参数2和3，如果参数3不为0，则以参数3为准；参数3为0，则通过时间来定时更新；两者为0，则随时刷新

        // 3秒更新一次，或最小位移变化超过1米更新一次；
        //注意：此处更新准确度非常低，推荐在service里面启动一个Thread，在run中sleep(10000);然后执行handler.sendMessage(),更新位置
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME, 1, locationListener);
//        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
    }

    //位置监听
    private LocationListener locationListener = new LocationListener() {

        /**
         * 位置信息变化时触发
         */
        public void onLocationChanged(Location location) {
            updateView(location);
            Log.i(TAG, "时间：" + location.getTime());
            Log.i(TAG, "经度：" + location.getLongitude());
            Log.i(TAG, "纬度：" + location.getLatitude());
            Log.i(TAG, "海拔：" + location.getAltitude());
        }

        /**
         * GPS状态变化时触发
         */
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch (status) {
                //GPS状态为可见时
                case LocationProvider.AVAILABLE:
                    Log.i(TAG, "当前GPS状态为可见状态");
                    break;
                //GPS状态为服务区外时
                case LocationProvider.OUT_OF_SERVICE:
                    Log.i(TAG, "当前GPS状态为服务区外状态");
                    break;
                //GPS状态为暂停服务时
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.i(TAG, "当前GPS状态为暂停服务状态");
                    break;
            }
        }

        /**
         * GPS开启时触发
         */
        public void onProviderEnabled(String provider) {
            Location location = lm.getLastKnownLocation(provider);
            updateView(location);
        }

        /**
         * GPS禁用时触发
         */
        public void onProviderDisabled(String provider) {
            updateView(null);
        }
    };

    //状态监听
    GpsStatus.Listener listener = new GpsStatus.Listener() {
        public void onGpsStatusChanged(int event) {
            switch (event) {
                //第一次定位
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    Log.i(TAG, "第一次定位");
                    break;
                //卫星状态改变
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    Log.i(TAG, "卫星状态改变");
                    //获取当前状态
                    GpsStatus gpsStatus = lm.getGpsStatus(null);
                    //获取卫星颗数的默认最大值
                    int maxSatellites = gpsStatus.getMaxSatellites();
                    //创建一个迭代器保存所有卫星
                    Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                    int count = 0;
                    while (iters.hasNext() && count <= maxSatellites) {
                        GpsSatellite s = iters.next();
                        count++;
                    }
//                    System.out.println("搜索到：" + count + "颗卫星");
                    tv_gps.setText("搜索到：" + count + "颗卫星");
                    break;
                //定位启动
                case GpsStatus.GPS_EVENT_STARTED:
                    Log.i(TAG, "定位启动");
                    break;
                //定位结束
                case GpsStatus.GPS_EVENT_STOPPED:
                    Log.i(TAG, "定位结束");
                    break;
            }
        }

        ;
    };

    /**
     * 实时更新文本内容
     *
     * @param location
     */
    private void updateView(Location location) {
        if (location != null) {
            editText.setText("lon,");
            editText.append(String.valueOf(location.getLongitude()));
            editText.append(",lat,");
            editText.append(String.valueOf(location.getLatitude()));
        } else {
            //清空EditText对象
            editText.setText("无法获取位置,请检查网络或GPS");

        }
    }

    /**
     * 返回查询条件
     *
     * @return
     */
    private Criteria getCriteria() {
        Criteria criteria = new Criteria();
        //设置定位精确度 Criteria.ACCURACY_COARSE比较粗略，Criteria.ACCURACY_FINE则比较精细
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        //设置是否要求速度
        criteria.setSpeedRequired(false);
        // 设置是否允许运营商收费
        criteria.setCostAllowed(false);
        //设置是否需要方位信息
        criteria.setBearingRequired(false);
        //设置是否需要海拔信息
        criteria.setAltitudeRequired(false);
        // 设置对电源的需求
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        return criteria;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        lm.removeUpdates(locationListener);
    }
/*
* string转成文件
* */
    // 将字符串写入到文本文件中
    public void writeTxtToFile(String strcontent, String filePath, String fileName) {
        // 生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成文件
    public File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


/*
* 定时器：
*
* */

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {

            // handler自带方法实现定时器
            try {
                handler.postDelayed(this, TIME);
               //需要触发的事件为：模拟点击按钮

                if (auto_button1_flag==true) {
                    button1.performClick();
                    Toast.makeText(getApplicationContext(), "红点保存成功",
                            Toast.LENGTH_SHORT).show();
                }
                if (auto_button2_flag==true) {
                    button2.performClick();
                    Toast.makeText(getApplicationContext(), "绿点保存成功",
                            Toast.LENGTH_SHORT).show();
                }


            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("exception...");
            }


        }
    };



    /**
     *
     * 显示toast，自己定义显示长短。
     * param1:activity  传入context
     * param2:word   我们需要显示的toast的内容
     * param3:time length  long类型，我们传入的时间长度（如500）
     */
   /* public static void showToast(final Activity activity, final String word, final long time){
        activity.runOnUiThread(new Runnable() {
            public void run() {
                final Toast toast = Toast.makeText(activity, word, Toast.LENGTH_LONG);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        toast.cancel();
                    }
                }, time);
            }
        });
    }*/


}