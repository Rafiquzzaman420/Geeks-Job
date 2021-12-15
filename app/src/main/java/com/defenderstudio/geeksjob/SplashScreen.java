package com.defenderstudio.geeksjob;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;


public class SplashScreen extends AppCompatActivity {

    Animation mainAnim;

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
                    R.anim.fade_in);

            ImageView appLogo = findViewById(R.id.appLogo);
            appLogo.startAnimation(mainAnim);

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

    public float convertFromDp(int input) {
        final float scale = getResources().getDisplayMetrics().density;
        return ((input - 0.8f) / scale);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
//++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
