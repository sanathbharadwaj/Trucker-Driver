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
            if(title == 1) {
                Intent newRequestIntent = new Intent("NEW_REQUEST");
                newRequestIntent.putExtra("requestId", requestId);
                mContext.sendBroadcast(newRequestIntent);
                notifyUser(requestId, "New Request Found", "Take the request before someone takes before");
            }
            if(title == 2)
            {
                Intent newRequestIntent = new Intent("RIDE_CANCELLED");
                mContext.sendBroadcast(newRequestIntent);
                notifyUser("cancelled", "Ride Cancelled", "Ride has been cancelled by the customer");
            }
            }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    void notifyUser(String extra, String title, String alert)
    {
        Notification notification;

        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Intent resultIntent = new Intent(context, MainActivity.class);
        resultIntent.putExtra("extra", extra);
        resultIntent.putExtra("id", notificationId);
        resultIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        if(Build.VERSION.SDK_INT < 16) {
            notification = new Notification.Builder(context)
                    .setSound(uri)
                    .setContentTitle(title)
                    .setContentText(alert)
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher).getNotification();
        }
        else
        {
            notification = new Notification.Builder(context)
                    .setSound(uri)
                    .setContentTitle(title)
                    .setContentText(alert)
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher).build();
        }
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        assert notificationManager != null;
        notificationManager.notify(notificationId,notification);
        notificationId++;
    }
}
