package com.victor_xiao.countdownapp;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

public class CountDownActivity extends AppCompatActivity {

    ProgressBar bar = null;

    private String[] PLANETS_FOR_DAY;
    private String[] PLANETS_FOR_HOUR;
    private String[] PLANETS_FOR_MIN;
    private String[] PLANETS_FOR_SEC;

    TextView tv_day;
    TextView tv_hour;
    TextView tv_min;
    TextView tv_sec;

    Button bt_cus;
    Button bt_pause;
    Button bt_cancel;
    Button bt_music;

    Uri ringUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    MediaPlayer mp;

    private boolean pauseFlag = false;
    private boolean stopFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_count_down);

        bar = (ProgressBar) findViewById(R.id.bar);
        bar.setMax(100);

        PLANETS_FOR_DAY = setNum(31);
        PLANETS_FOR_HOUR = setNum(24);
        PLANETS_FOR_MIN = setNum(60);
        PLANETS_FOR_SEC = setNum(60);

        tv_day = (TextView) findViewById(R.id.tv_day);
        tv_hour = (TextView) findViewById(R.id.tv_hour);
        tv_min = (TextView) findViewById(R.id.tv_min);
        tv_sec = (TextView) findViewById(R.id.tv_sec);

        bt_cus = (Button) findViewById(R.id.time_set);
        bt_pause = (Button) findViewById(R.id.bt_pause);
        bt_cancel = (Button) findViewById(R.id.bt_cancel);
        bt_music = (Button) findViewById(R.id.bt_music);

        setFormattedText(tv_day, 0);
        setFormattedText(tv_hour, 0);
        setFormattedText(tv_min, 0);
        setFormattedText(tv_sec, 0);

        bt_cus.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                LinearLayout outerView = (LinearLayout) getLayoutInflater().inflate(R.layout.wheelview_dialog, null);
                final WheelView wv_day = (WheelView) outerView.findViewById(R.id.wv_day);
                final WheelView wv_hour = (WheelView) outerView.findViewById(R.id.wv_hour);
                final WheelView wv_min = (WheelView) outerView.findViewById(R.id.wv_min);
                final WheelView wv_sec = (WheelView) outerView.findViewById(R.id.wv_sec);

                wv_day.setOffset(1);
                wv_day.setItems(Arrays.asList(PLANETS_FOR_DAY));
                wv_day.setSeletion(0);

                wv_hour.setOffset(1);
                wv_hour.setItems(Arrays.asList(PLANETS_FOR_HOUR));
                wv_hour.setSeletion(0);

                wv_min.setOffset(1);
                wv_min.setItems(Arrays.asList(PLANETS_FOR_MIN));
                wv_min.setSeletion(0);

                wv_sec.setOffset(1);
                wv_sec.setItems(Arrays.asList(PLANETS_FOR_SEC));
                wv_sec.setSeletion(0);

                new android.app.AlertDialog.Builder(CountDownActivity.this)
                        .setTitle("Customized")
                        .setView(outerView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int day = wv_day.getSeletedIndex();
                                int hour = wv_hour.getSeletedIndex();
                                int min = wv_min.getSeletedIndex();
                                int sec = wv_sec.getSeletedIndex();

                                setFormattedText(tv_day, day);
                                setFormattedText(tv_hour, hour);
                                setFormattedText(tv_min, min);
                                setFormattedText(tv_sec, sec);

                                int sum1 = hour * 60 * 60 + min * 60 + sec;          //将时分秒加为一个总数

                                stopFlag = false;
                                final MyThread thread = new MyThread(sum1, day);
                                thread.start();
                            }
                        })
                        .show();
            }
        });

        bt_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!pauseFlag) {
                    pauseFlag = true;
                    bt_pause.setText("continue");
                } else {
                    pauseFlag = false;
                    bt_pause.setText("pause");
                }
            }
        });

        bt_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("tag", "1");

                Log.d("tag", "4");
                stopFlag = true;
                bar.setProgress(0);
            }
        });

        bt_music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(RingtoneManager.ACTION_RINGTONE_PICKER);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, false);
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "设置闹玲铃声");
                intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
                Uri pickedUri = RingtoneManager.getActualDefaultRingtoneUri(CountDownActivity.this, RingtoneManager.TYPE_ALARM);
                if (pickedUri != null) {
                    intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, pickedUri);
                    ringUri = pickedUri;
                }
                startActivityForResult(intent, 1);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case 1:
                //获取选中的铃声的URI
                Uri pickedURI = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                ringUri = pickedURI;
                break;

            default:
                break;
        }
    }


    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            setFormattedText(tv_day, msg.arg2);
            setFormattedText(tv_hour, msg.arg1 / 3600);
            setFormattedText(tv_min, msg.arg1 % 3600 / 60);
            setFormattedText(tv_sec, msg.arg1 % 3600 % 60);
        }
    };

    public class MyThread extends Thread {              //定义内部类，由于用匿名内部类传递数据比较麻烦，所以改用内部类
        private int num1, num2;

        MyThread(int num1, int num2) {                  //num1为时分秒换算的总秒数，num2为天数
            this.num1 = num1;
            this.num2 = num2;
        }


        @Override
        public void run() {
            double i = 0.0, j = 0.0, sec = num1, day = num2;
            while (num1 > -1 || num2 > 0) {

                try {
                    while (pauseFlag) {                      //判断是否暂停
                        sleep(1000);
                    }
                    if (stopFlag) {
                        num1 = 0;
                        num2 = 0;

                    }

                    Message msg = new Message();
                    msg.arg1 = num1;
                    msg.arg2 = num2;
                    CountDownActivity.this.handler.sendMessage(msg);

                    if (num1 == 0 && num2 == 0) {
                        if (!stopFlag) {
                            try {
                                if (ringUri != null) {
                                    mp = MediaPlayer.create(CountDownActivity.this, ringUri);
                                    if (mp != null) {
                                        mp.stop();
                                    }
                                    mp.prepare();
                                    Log.d("tag", String.valueOf(mp));
                                    mp.start();


                                    bar.setProgress(100);

                                    Looper.prepare();
                                    CreateDialog(mp);
                                    Looper.loop();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    }

                    if (!stopFlag) {
                        if (num1 == 0) {
                            num2--;
                            num1 = 24 * 60 * 60;
                        }
                        num1 -= 1;


                        i = (j / ((day * 24 * 60 * 60) + sec)) * 100;
                        bar.setProgress((int) i);
                        j += 1;

                        Thread.sleep(1000);
                    } else stopFlag = false;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void CreateDialog(final MediaPlayer mp) {
        AlertDialog.Builder builder = new AlertDialog.Builder(CountDownActivity.this);
        builder.setMessage("倒计时完成！")//显示的消息内容
                .setTitle("倒数");//对话框标题

        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(CountDownActivity.this, "倒计时已完成", Toast.LENGTH_LONG).show();
                if (mp != null)
                    if (mp.isPlaying()) {
                        mp.stop();
                        mp.release();
                    }
                bar.setProgress(0);
                stopFlag = false;
            }
        });
        builder.show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {        //点击返回键不会退出程序，程序后台运行
        PackageManager pm = getPackageManager();
        ResolveInfo homeInfo =
                pm.resolveActivity(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME), 0);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ActivityInfo ai = homeInfo.activityInfo;
            Intent startIntent = new Intent(Intent.ACTION_MAIN);
            startIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            startIntent.setComponent(new ComponentName(ai.packageName, ai.name));
            startActivitySafely(startIntent);
            return true;
        } else
            return super.onKeyDown(keyCode, event);
    }

    private void startActivitySafely(Intent intent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(this, "null",
                    Toast.LENGTH_SHORT).show();
        }
    }

    //设置格式化显示数字
    public void setFormattedText(TextView bt, int num) {
        if (num >= 0 && num < 10)
            bt.setText("0" + num);
        else
            bt.setText(num + "");
    }

    public String[] setNum(int n) {
        String[] s = new String[n];
        for (int i = 0; i < n; i++) {
            s[i] = i + "";
        }
        return s;
    }

}
