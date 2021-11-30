package com.defenderstudio.geeksjob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class SplashScreen extends AppCompatActivity {

    Animation mainAnim, bottomAnim;

    SharedPreferences sharedpreferences;
    boolean splash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        sharedpreferences = getSharedPreferences("MyPrefs", 0);
        splash = sharedpreferences.getBoolean("Splash", false);
        if (splash) {
            Intent signInIntent = new Intent(SplashScreen.this, SignInActivity.class);
            startActivity(signInIntent);
            finish();
        } else {
            setContentView(R.layout.splash_screen);

            mainAnim = AnimationUtils.loadAnimation(getApplicationContext(),
                    R.anim.main_animation);

            bottomAnim = AnimationUtils.loadAnimation(this, R.anim.company_name_anim);

            TextView appName = findViewById(R.id.app_name);
            TextView company_name = findViewById(R.id.company_name);

            appName.startAnimation(mainAnim);
            company_name.setAnimation(bottomAnim);

            mainAnim.setAnimationListener((new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    // Auto-generated method stub
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                    // Auto-generated method stub
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    // Start activity here.
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean("Splash", true);
                    editor.apply();
                    Intent mainIntent = new Intent(SplashScreen.this, SignInActivity.class);
                    SplashScreen.this.startActivity(mainIntent);
                    SplashScreen.this.finish();

                }
            }));
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
