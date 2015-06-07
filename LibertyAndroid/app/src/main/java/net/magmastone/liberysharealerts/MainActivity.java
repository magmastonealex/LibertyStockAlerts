package net.magmastone.liberysharealerts;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.text.format.Time;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import android.os.Build;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class MainActivity extends Activity {
    GoogleCloudMessaging gcm;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    String regid;
    final String SENDER_ID = "587946435019";
    Context context;
    private int dialogShowing=0;
    final Handler h = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        long shares=sharedPref.getLong("numShares", 0);
        String text=null;
        if(shares<1){
            text="";
        }else {
            text = doubleToStringNoDecimal(shares) + " Shares";
        }
        System.out.println(text);
        context=this;
        try {
            NotificationManager mNotificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(1);
        }catch(Exception e){

        }



        TextView tV = (TextView)findViewById(R.id.editText);
        tV.setText(text);
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment())
                    .commit();
        }
        if(!isNetworkAvailable()){
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("No network connection").setMessage("You need a network connection to use this app!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
        final Context c = this;
       Button stock = (Button)findViewById(R.id.buyButton);
        stock.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    AsyncTask poster = new AsyncTask() {
                        @Override
                        protected String doInBackground(Object[] objects) {
                            HttpClient httpclient = new DefaultHttpClient();
                            HttpPost httppost = new HttpPost("http://sharealertapp.com/trade/api/madetrades.php");

                            try {
                                String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                                float lastPrice = sharedPref.getFloat("lastPrice", 0);
                                float currentPrice = sharedPref.getFloat("currentPrice", 0);
                                long numShares = sharedPref.getLong("numShares", 0);
                                int tradePlat = sharedPref.getInt("tradePlat",0);
                                float diff=(currentPrice-lastPrice);
                                System.out.println("diff:" + String.valueOf(diff));

                                String email = sharedPref.getString("user_email","");

                                String belongsto = sharedPref.getString("userName", "xxx");

                                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

                                System.out.println("Test_web:"+doubleToStringNoDecimal((((currentPrice-lastPrice)/lastPrice)*100.0)) +" %");

                                nameValuePairs.add(new BasicNameValuePair("device_token", android_id));
                                nameValuePairs.add(new BasicNameValuePair("home_shares", String.valueOf(numShares)));
                                nameValuePairs.add(new BasicNameValuePair("home_made", String.valueOf(diff*numShares)));
                                nameValuePairs.add(new BasicNameValuePair("belongsto", belongsto));
                                nameValuePairs.add(new BasicNameValuePair("email_address", email));
                                nameValuePairs.add(new BasicNameValuePair("worthnow", doubleToStringNoDecimal_web(numShares*currentPrice)));
                                nameValuePairs.add(new BasicNameValuePair("lastprice", String.valueOf(lastPrice)));
                                nameValuePairs.add(new BasicNameValuePair("currentprice", String.valueOf(currentPrice)));
                                nameValuePairs.add(new BasicNameValuePair("percentchange", doubleToStringNoDecimal((((currentPrice-lastPrice)/lastPrice)*100.0)) +" %"));
                                nameValuePairs.add(new BasicNameValuePair("tradinglink", String.valueOf(tradePlat)));
                                Time now = new Time();
                                now.setToNow();
                                nameValuePairs.add(new BasicNameValuePair("date_time", now.format("%d-%m-%Y %H.%M.%S")));

                                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                                // Execute HTTP Post Request
                                HttpResponse response = httpclient.execute(httppost);
                                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                                String json = reader.readLine();
                                System.out.println("Json:" + json);
                            } catch (Exception e){
                                e.printStackTrace();
                            }
                            return "";
                        }
                    };
                    poster.execute(null);
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    int tradePlat = sharedPref.getInt("tradePlat",0);
                    String url;
                    if(tradePlat==0){
                        url="https://us.etrade.com/home";
                    }else{
                        url="https://www.commsec.com.au/";
                    }
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(browserIntent);



                    return true;
                }

                return false;
            }
        });



        ((TextView)findViewById(R.id.editText)).setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String data=v.getText().toString().split("Shares")[0].replace(" ", "").replace(",", "");
                    long value;
                    if(data.equals("")){
                        value=0;
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle("Please enter correct shares").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();

                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }else{
                        value = Long.parseLong(data);
                    }
                    if(value!=0) {
                        v.setText(doubleToStringNoDecimal(value) + " Shares");
                    }else{
                        v.setText("");
                    }
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(c);
                    sharedPref.edit().putLong("numShares", value).commit();
                    Intent broadcastIntent = new Intent("newData");
                    c.sendBroadcast(broadcastIntent);
                    System.out.println("Have shares:" + String.valueOf(value));
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        if (checkPlayServices()) {

            gcm = GoogleCloudMessaging.getInstance(this);
            regid = getRegistrationId(this);
            registerInBackground();
            //if (regid.isEmpty()) {
            //    registerInBackground();
            //}else{
            //    System.out.println("regid:" + regid);
            //}
        }
        final int delay = 30000; //milliseconds
        h.removeCallbacksAndMessages(null);
        h.postDelayed(new Runnable() {
            public void run() {
                AsyncTask getData = new AsyncTask() {
                    @Override
                    protected String doInBackground(Object[] objects) {
                        HttpClient httpclient = new DefaultHttpClient();
                        HttpPost httppost = new HttpPost("http://sharealertapp.com/trade/api/currentprice.php");

                        try {
                            // Execute HTTP Post Request
                            HttpResponse response = httpclient.execute(httppost);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                            String json = reader.readLine();
                            //System.out.println(json);
                            JSONTokener tokener = new JSONTokener(json);
                            JSONObject finalResult = new JSONObject(tokener);
                            float price = Float.parseFloat(finalResult.getString("current_price"));
                            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                            float current = sharedPref.getFloat("currentPrice", 0);
                            if (current != price) {
                                SharedPreferences.Editor edit = sharedPref.edit();
                                edit.putFloat("currentPrice", price);
                                System.out.println("Last price:"+finalResult.getString("last_price"));
                                edit.putFloat("lastPrice",Float.parseFloat(finalResult.getString("last_price")));
                                edit.commit();
                                System.out.println("Updated price!");
                                Intent broadcastIntent = new Intent("newData");
                                context.sendBroadcast(broadcastIntent);
                                Intent broadcastIntent2 = new Intent("sendAlert");
                                context.sendBroadcast(broadcastIntent2);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return "";
                    }
                };
                getData.execute(null);
                h.postDelayed(this, delay);
            }
        }, delay);
    }
    public static String doubleToStringNoDecimal(double d) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###.##");
        return formatter.format(d);
    }
    public static String doubleToStringNoDecimal_web(double d) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("###");
        return formatter.format(d);
    }
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.commit();
    }
    private void registerInBackground() {
        AsyncTask reg = new AsyncTask() {
            @Override
            protected String doInBackground(Object[] objects) {
                String msg = "";
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(context);
                    }
                    regid = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID=" + regid;
                    System.out.println(msg);
                    // You should send the registration ID to your server over HTTP,
                    // so it can use GCM/HTTP or CCS to send messages to your app.
                    // The request to your server should be authenticated if your app
                    // is using accounts.
                    //sendRegistrationIdToBackend();
                    HttpClient httpclient = new DefaultHttpClient();
                    HttpPost httppost = new HttpPost("http://sharealertapp.com/trade/api/registerdevice.php");

                    try {
                        // Add your data
                        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                        nameValuePairs.add(new BasicNameValuePair("token", regid));
                        String android_id = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                        nameValuePairs.add(new BasicNameValuePair("deviceid", android_id));
                        nameValuePairs.add(new BasicNameValuePair("is_android", "1"));
                        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                        // Execute HTTP Post Request
                        HttpResponse response = httpclient.execute(httppost);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                        String json = reader.readLine();
                        System.out.println(json);
                        JSONTokener tokener = new JSONTokener(json);
                        JSONArray finalResult = new JSONArray(tokener);

                    } catch (Exception e){
                        System.out.println("Error Posting: "+e.getLocalizedMessage());
                    }
                    // For this demo: we don't need to send it because the device
                    // will send upstream messages to a server that echo back the
                    // message using the 'from' address in the message.

                    // Persist the regID - no need to register again.
                    storeRegistrationId(context, regid);

                } catch (IOException ex) {
                    msg = "Error :";
                    System.out.println(ex.getMessage());
                    // If there is an error, don't just keep trying to register.
                    // Require the user to click a button again, or perform
                    // exponential back-off.
                }
                return msg;
            }


        };
        reg.execute(null, null, null);
    }
    //private void
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("MainActivity", "Registration not found.");
            return "";
        }
        return registrationId;
    }

    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences("GCM_MainActivity_Liberty", Context.MODE_PRIVATE);
    }

    private void doUpdate(){
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        float lastPrice = sharedPref.getFloat("lastPrice", 0);
        float currentPrice = sharedPref.getFloat("currentPrice", 15);
        long numShares = sharedPref.getLong("numShares", 0);
        float diff=currentPrice-lastPrice;
        double profit=diff*numShares;
        if(profit<1) {
            ((TextView) findViewById(R.id.textView4)).setText("");
            ((TextView) findViewById(R.id.youmade)).setText("");
        }else{
            ((TextView) findViewById(R.id.youmade)).setText("You have made");
            ((TextView) findViewById(R.id.textView4)).setText("$ " + doubleToStringNoDecimal(profit) + " !");
        }
       }
    private BroadcastReceiver onNotice= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Received new data");
            doUpdate();
        }
    };
    private BroadcastReceiver onPostAlert= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Received new data");
            String type=intent.getStringExtra("type");
            if(dialogShowing==0 && type.equals("up")) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);

                builder.setTitle("$Cha-Ching$").setMessage("Price goes up!").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialogShowing = 0;
                        dialog.dismiss();

                    }
                });
                AlertDialog dialog = builder.create();
                dialogShowing=1;
                dialog.show();
            }
            if(type.equals("up")) {
                MediaPlayer mp = MediaPlayer.create(context, R.raw.cash);
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {

                        mp.release();
                    }

                });
                mp.start();
            }else{
                MediaPlayer mp = MediaPlayer.create(context, R.raw.lowdong);
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.release();
                    }

                });
                mp.start();
            }
            doUpdate();
        }
    };
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("MainActivity", "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }
    @Override
    public void onResume(){
        doUpdate();
        registerReceiver(onNotice, new IntentFilter("newData"));
        registerReceiver(onPostAlert, new IntentFilter("sendAlert"));
        super.onResume();
    }
    @Override
    public void onPause() {
        unregisterReceiver(onNotice);
        unregisterReceiver(onPostAlert);
        super.onPause();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus){
            //Get name of the person!
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String userName = sharedPref.getString("userName", "xxx");
            TextView text = (TextView)findViewById(R.id.nameField);
            if(userName.equals("xxx") || userName.equals("")){
                text.setText("You have");
            }else{
                text.setText(userName+", you have");
            }
            System.out.println(userName);
        }else{

        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_adv:
                System.out.println("Pressed Settings");
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
}
