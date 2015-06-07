package net.magmastone.liberysharealerts;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by Alex on 14-07-28.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    public static final String TAG="GCM Intent Service";
    private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        // The getMessageType() intent parameter must be the intent you received
        // in your BroadcastReceiver.
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {  // has effect of unparcelling Bundle
            /*
             * Filter messages based on message type. Since it is likely that GCM
             * will be extended in the future with new message types, just ignore
             * any message types you're not interested in, or that you don't
             * recognize.
             */
            if (GoogleCloudMessaging.
                    MESSAGE_TYPE_SEND_ERROR.equals(messageType)) {
                sendNotification("Send error: " + extras.toString());
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_DELETED.equals(messageType)) {
                sendNotification("Deleted messages on server: " +
                        extras.toString());
                // If it's a regular GCM message, do some work.
            } else if (GoogleCloudMessaging.
                    MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor ouredit = sharedPref.edit();
                ouredit.putFloat("lastPrice",Float.parseFloat(extras.getString("last_price")));
                ouredit.putFloat("currentPrice",Float.parseFloat(extras.getString("current_price")));
                ouredit.commit();
                Intent broadcastIntent = new Intent("newData");
                this.sendBroadcast(broadcastIntent);


                float last = Float.parseFloat(extras.getString("last_price"));
                float current = Float.parseFloat(extras.getString("current_price"));
                if((last-current)>0){
                    sendNotification("Movement in Liberty Shares");
                    Intent broadcastIntent2 = new Intent("sendAlert");
                    broadcastIntent2.putExtra("type","down");
                    this.sendBroadcast(broadcastIntent2);
                    Log.i(TAG, "Down");
                }else{
                    sendNotification("Price goes up!");
                    Intent broadcastIntent2 = new Intent("sendAlert");
                    broadcastIntent2.putExtra("type","up");
                    this.sendBroadcast(broadcastIntent2);
                    Log.i(TAG, "Up");
                }
                // Post notification of received message.

                Log.i(TAG, "Received: " + String.valueOf(last)+" Current:" + String.valueOf(current));
            }
        }
        // Release the wake lock provided by the WakefulBroadcastReceiver.
        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancel(1);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, MainActivity.class), 0);
        int px =(int) Math.floor(64.0f * this.getResources().getDisplayMetrics().density);
        Bitmap b = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        Uri sound = null;
        if(msg.equals("Price goes up!")){
            sound = Uri.parse("android.resource://net.magmastone.liberysharealerts/raw/cash");
        }else{
            sound = Uri.parse("android.resource://net.magmastone.liberysharealerts/raw/lowdong");
        }
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_smallicon)
                        .setLargeIcon(Bitmap.createScaledBitmap(b, px, px, false))
                        .setContentTitle("Liberty Share Alerts")
                        .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(msg))
                        .setTicker(msg)
                        .setSound(sound)
                        .setContentText(msg);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());
    }
}