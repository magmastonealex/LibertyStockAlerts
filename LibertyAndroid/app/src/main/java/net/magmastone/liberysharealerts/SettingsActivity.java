package net.magmastone.liberysharealerts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.format.Time;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import net.magmastone.liberysharealerts.R;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import info.hoang8f.android.segmented.SegmentedGroup;

public class SettingsActivity extends Activity {
    public static String doubleToStringNoDecimal_web(double d) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("###");
        return formatter.format(d);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        String userName = sharedPref.getString("userName", "xxx");
         if(!userName.equals("xxx")){
            TextView nameView = (TextView)findViewById(R.id.ownerName);
            nameView.setText(userName);
        }
        SegmentedGroup segmented3 = (SegmentedGroup)findViewById(R.id.segmented2);
        RadioButton b21 = (RadioButton)findViewById(R.id.button21);
        RadioButton b22 = (RadioButton)findViewById(R.id.button22);
        int sel=sharedPref.getInt("tradePlat",0);
        if(sel==0){
            b21.setChecked(true);
            b22.setChecked(false);
        }else{
            b21.setChecked(false);
            b22.setChecked(true);
        }
        final Context context = this;
        TextView emailField = (TextView)findViewById(R.id.emailField);
        emailField.setText(sharedPref.getString("user_email",""));
        emailField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String data=v.getText().toString();
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                    sharedPref.edit().putString("user_email", data).commit();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        segmented3.setTintColor(Color.parseColor("#0C368C"));
        doUpdate();


        Button get = (Button)findViewById(R.id.getApp);
        get.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.mobileappcity.com"));
                    startActivity(browserIntent);

                    return true;
                }

                return false;
            }
        });

       //Spinner spin = (Spinner)findViewById(R.id.tradeSpinner);
       //int stockOp = sharedPref.getInt("stockOption", -10);
       //if(stockOp != -10) {
       //    spin.setSelection(stockOp);
       //}
       //String[] mains = {"Your Name","Your Shares are Worth","Last Price", "Current Price", "Percent Change", "Trade Platform"};
       //String[] seconds = {"None set!","100000","$1.04 per share", "$1.05 per share", "1%", "MoreShares"};
       //settingsArrayAdapter simpleAdpt = new settingsArrayAdapter(this,mains,seconds);
        //ListView lv = (ListView) findViewById(R.id.setList);
        //lv.setAdapter(simpleAdpt);
        /*final Context c = getApplicationContext();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parentAdapter, View view, int position,long id) {
                //Intent intent = new Intent(c, chatActivity.class);
                //intent.putExtra("phoneNum",cons[position]);
                if(position==0){
                    AlertDialog.Builder builder = new AlertDialog.Builder(c);
                    builder.setTitle("Adding contact").setMessage("You will receive a notification when negotiation completes").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else if(position==5){

                }
                //setTitle(intent.getStringExtra("chatID"));
                //cid = intent.getStringExtra("contactID");
                //phoneNum = intent.getStringExtra("phoneNum");
                //String photo = intent.getStringExtra("photoURI");
               // startActivity(intent);
            }
        });
        */
        getActionBar().setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }
    public static String doubleToStringNoDecimal(double d) {
        DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        formatter.applyPattern("#,###.##");
        return formatter.format(d);
    }
    private void doUpdate(){
    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    float lastPrice = sharedPref.getFloat("lastPrice", 0);
    float currentPrice = sharedPref.getFloat("currentPrice", 0);
    long numShares = sharedPref.getLong("numShares", 0);
    ((TextView)findViewById(R.id.lastPrice)).setText("$ "+doubleToStringNoDecimal(lastPrice));
    ((TextView)findViewById(R.id.currentPrice)).setText("$ "+doubleToStringNoDecimal(currentPrice));
    ((TextView)findViewById(R.id.shareValue)).setText("$ "+doubleToStringNoDecimal(numShares*currentPrice));
    System.out.println("(("+String.valueOf(currentPrice)+"-"+String.valueOf(lastPrice)+")/"+String.valueOf("lastPrice")+")*100");
    ((TextView)findViewById(R.id.percentChange)).setText(doubleToStringNoDecimal(((currentPrice-lastPrice)/lastPrice)*100.0)+" %");
    }

    private BroadcastReceiver onNotice= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("Received new data");
            doUpdate();
        }
    };
    @Override
    public void onResume(){
        doUpdate();
        registerReceiver(onNotice, new IntentFilter("newData"));
        super.onResume();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }
    @Override
    public void onPause(){
        // Create object of SharedPreferences.
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        ////now get Editor
        SharedPreferences.Editor editor = sharedPref.edit();
        ////put your value
        TextView nameText = (TextView)findViewById(R.id.ownerName);
        RadioButton b21 = (RadioButton)findViewById(R.id.button21);
        RadioButton b22 = (RadioButton)findViewById(R.id.button22);
        int sel=0;
        if(b21.isChecked()){
            sel=0;
        }else{
            sel=1;
        }
        editor.putInt("tradePlat", sel);
        editor.putString("userName",nameText.getText().toString());
        System.out.println(nameText.getText().toString());
        //Spinner spin = (Spinner)findViewById(R.id.tradeSpinner);
        //editor.putInt("stockOption",spin.getSelectedItemPosition());
        ////commits your edits
        editor.commit();
        unregisterReceiver(onNotice);
        super.onPause();
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        final Context context = this;
        // as you specify a parent activity in AndroidManifest.xml.
        if(item.getItemId()==R.id.action_save){
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
                        nameValuePairs.add(new BasicNameValuePair("tradinglink", String.valueOf(-1)));
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
        }
        return super.onOptionsItemSelected(item);
    }
}
