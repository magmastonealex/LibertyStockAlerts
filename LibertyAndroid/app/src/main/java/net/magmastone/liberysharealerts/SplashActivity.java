package net.magmastone.liberysharealerts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by Alex on 14-07-29.
 */
public class SplashActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        //finish();
    }
}
