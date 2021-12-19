package com.defenderstudio.geeksjob;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Objects;

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    TextView companyNameTextView;
    Animation logoAnimation, signInAnimation;
    // [START declare_auth]
    private FirebaseAuth firebaseAuth;
    private ProgressBar progressBar;
    private GoogleSignInClient googleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_sign_in);


        CardView signInButton = findViewById(R.id.sign_in_button);
        ImageView appLogo = findViewById(R.id.appLogo);
        companyNameTextView = findViewById(R.id.company_name_sign_in);
        TextView SignInButtonText = findViewById(R.id.sign_in_button_text);
        ImageView googleImage = findViewById(R.id.googleImage);

        Animation right_animation = AnimationUtils.loadAnimation(this, R.anim.company_name_anim);
        progressBar = findViewById(R.id.progressbar_sign_in);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1096631451357-nm97s6b45hllm30r8ij6935bdg40seii.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleSignInClient = GoogleSignIn.getClient(this, gso);


        logoAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.fade_in);
        appLogo.startAnimation(logoAnimation);


        logoAnimation.setAnimationListener((new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                animatorMethod(appLogo, (int) convertFromDp(-100));
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                appLogo.clearAnimation();

                signInButton.setVisibility(View.VISIBLE);
                companyNameTextView.setVisibility(View.VISIBLE);
                SignInButtonText.setTextSize(convertFromDp(40));
                googleImage.getLayoutParams().height = (int) convertFromDp(600);
                googleImage.getLayoutParams().width = (int) convertFromDp(600);
                signInAnimation = AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.fade_in_fast);
                signInButton.startAnimation(signInAnimation);

                signInAnimation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                companyNameTextView.setAnimation(right_animation);

            }
        }));

        firebaseAuth = FirebaseAuth.getInstance();

        signInButton.setOnClickListener(v -> {
            signIn();
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    private void animatorMethod(View view, float value) {
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(view, "TranslationY", value);
        objectAnimator.setDuration(2000);
        objectAnimator.start();
    }


    @Override
    public void onStart() {
        super.onStart();
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {

                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {

                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),
                        "Sign in failed. Please be sure to turn on internet", Toast.LENGTH_LONG).show();

            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getApplicationContext(),
                                "Signed in successfully", Toast.LENGTH_SHORT).show();

                        Intent mainActivityIntent = new Intent(SignInActivity.this,
                                MainActivity.class);
                        startActivity(mainActivityIntent);
                        finish();


                    } else {
                        progressBar.setVisibility(View.GONE);
                        try {
                            Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
                            Toast.makeText(getApplicationContext(),
                                    "Sign in failed. Be sure to turn on internet connection", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            if (isOnline()) {
                                Toast.makeText(getApplicationContext(),
                                        "Sorry. You've been banned for misusing Geeks Job", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        "Sign in failed. Be sure to turn on internet connection", Toast.LENGTH_LONG).show();
                            }
                        }

                    }
                });
    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }


    private void signIn() {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> SignInActivity.super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
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

