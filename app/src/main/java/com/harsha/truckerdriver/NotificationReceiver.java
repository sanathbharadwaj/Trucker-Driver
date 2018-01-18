package com.harsha.truckerdriver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.parse.ParsePushBroadcastReceiver;

import org.json.JSONObject;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Admin on 1/16/2018.
 */

public class NotificationReceiver extends ParsePushBroadcastReceiver {

    Context context;
    static int notificationId=0;

    @Override
    protected void onPushReceive(Context mContext, Intent intent) {
        //enter your custom here generateNotification();
        //super.onPushReceive(mContext, intent);
        context = mContext;
        Log.i("Push", "Message Received");
        try {
            JSONObject json = new JSONObject(intent.getExtras().getString("com.parse.Data"));
            int title = json.getInt("title");
            String requestId = json.getString("alert");
            Intent newRequestIntent = new Intent("NEW_REQUEST");
            newRequestIntent.putExtra("requestId", requestId);
            mContext.sendBroadcast(newRequestIntent);
            notifyUser(requestId);
            }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    void notifyUser(String requestId)
    {
        Notification notification;

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("requestId", requestId);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        if(Build.VERSION.SDK_INT < 16) {
            notification = new Notification.Builder(context)
                    .setSound(uri)
                    .setContentTitle("New Request found")
                    .setContentText("Take the request before someone takes before")
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher).getNotification();
        }
        else
        {
            notification = new Notification.Builder(context)
                    .setSound(uri)
                    .setContentTitle("New Request found")
                    .setContentText("Take the request before someone takes before")
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher).build();
        }
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(notificationId,notification);
        notificationId++;
    }
}
