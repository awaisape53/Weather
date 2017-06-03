package top.maweihao.weather.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import top.maweihao.weather.R;
import top.maweihao.weather.activity.WeatherActivity;
import top.maweihao.weather.bean.temperature;
import top.maweihao.weather.util.Utility;

/**
 * 后台刷新service， 每晚提示第二天温差
 * 还有问题
 */

public class SyncService extends Service {

    static final String TAG = "SyncService";
    List<temperature> temList;
    Boolean isChinese = false;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate: SyncService created");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            isChinese = getResources().getConfiguration().getLocales().get(0).getDisplayLanguage().equals("中文");
        } else {
            isChinese = getResources().getConfiguration().locale.getDisplayLanguage().equals("zh-CN");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        fetchData();
        startAgain();
        return START_NOT_STICKY;
    }

    private void fetchData() {
        final OkHttpClient client = new OkHttpClient();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                String fUrl = prefs.getString("furl", null);
                if (fUrl != null) {
                    try {
                        Request request = new Request.Builder()
                                .url(fUrl).build();
                        Response response = client.newCall(request).execute();
                        String responseData = response.body().string();
                        parseJSON(responseData);
                        temperature temp = temList.get(0);
                        temperature temp2 = temList.get(1);
                        calTemDiff(Utility.intRoundString(temp.getMax()), Utility.intRoundString(temp.getMin()),
                                Utility.intRoundString(temp2.getMax()), Utility.intRoundString(temp2.getMin()));
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "onStartCommand: okhttp error");
                    } catch (JSONException e) {
                        Log.e(TAG, "onStartCommand: Gson error");
                        e.printStackTrace();
                    }
                } else {
                    Log.e(TAG, "onStartCommand: furl == null");
                }
            }
        }).start();
    }

    private void startAgain() {
        //     每天18:00启动
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Calendar calendar = new GregorianCalendar();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        if (hour > 18) {
            calendar.set(Calendar.DAY_OF_MONTH, day + 1);
        }
        calendar.set(Calendar.HOUR_OF_DAY, 18);
        calendar.set(Calendar.MINUTE, 0);
        Intent intent = new Intent(this, SyncService.class);
        Log.d(TAG, "startAgain: calendar == " + calendar.getTime());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
    }

    private void sendNotification(String title, String text) {
        Intent intent = new Intent(getApplicationContext(), WeatherActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new NotificationCompat.Builder(getApplicationContext())
                .setContentTitle(title)
                .setContentText(text)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.ic_cloud_queue_black_24dp)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.weather_few_clouds))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        manager.notify(1, notification);
    }

    private void sendAlarmNotification(int id, String text) {
        //to finish
    }

    private void parseJSON(String responseData) throws JSONException {
        Gson gson = new Gson();
        JSONObject all = new JSONObject(responseData);
        JSONObject result = all.getJSONObject("result");
        JSONObject daily = result.getJSONObject("daily");
        JSONArray tem = daily.getJSONArray("temperature");
        temList = gson.fromJson(tem.toString(), new TypeToken<List<temperature>>() {
        }.getType());
    }

    /**
     * 计算温差，发送通知
     */
    private void calTemDiff(int todayMax, int todayMin, int tomMax, int tomMin) {
        int maxDiff = tomMax - todayMax;
        int minDiff = tomMin - todayMin;
        if (maxDiff * minDiff >= 0) {
            int a = Math.abs(maxDiff);
            int b = Math.abs(minDiff);
            if (Math.max(a, b) >= 3) {
                Calendar calendar = new GregorianCalendar();
                int nextDay = calendar.get(Calendar.DAY_OF_WEEK) + 1;
                String dayOfWeek = getResources().getStringArray(R.array.weekend)[nextDay == 7 ? 0 : nextDay];
                String tem = (maxDiff > 0 || minDiff > 0) ? getResources().getString(R.string.warmer) : getResources().getString(R.string.colder);
                if (isChinese) {
                    sendNotification(dayOfWeek + "将" + tem + ' ' + Math.max(a, b) + "° ",
                            todayMin + "° - " + todayMax + "° -> " + tomMin + "° - " + tomMax + "° ");
                } else {
                    sendNotification(Math.max(a, b) + "° " + tem + " than " + dayOfWeek,
                            todayMin + "° - " + todayMax + "° -> " + tomMin + "° - " + tomMax + "° ");
                }
            }
        } else {
            Log.d(TAG, "calTemDiff: diff: " + maxDiff * minDiff);
            sendNotification("Temperature", todayMin + "° - " + todayMax + "° -> " + tomMin + "° - " + tomMax + "° ");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: SyncService destroyed");
    }
}