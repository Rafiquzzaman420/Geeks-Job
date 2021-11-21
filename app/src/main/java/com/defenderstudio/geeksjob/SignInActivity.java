package com.defenderstudio.geeksjob;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

public class SignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 100;
    TextView appNameTextView, companyNameTextView;
    // [START declare_auth]
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private GoogleSignInClient mGoogleSignInClient;
    private ImageView googleIcon;
    private String valueFromServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // [START config_signin]
        // Configure Google Sign In
        overridePendingTransition(R.anim.left_in_anim, R.anim.left_out_anim);


        CardView signInButton = findViewById(R.id.sign_in_button);

        appNameTextView = findViewById(R.id.app_name_sign_in_window);
        companyNameTextView = findViewById(R.id.company_name_sign_in);

        Animation right_animation = AnimationUtils.loadAnimation(this, R.anim.company_name_anim);
        appNameTextView.setAnimation(right_animation);

        companyNameTextView.setAnimation(right_animation);

        googleIcon = findViewById(R.id.google_icon);

        progressBar = findViewById(R.id.progressbar_sign_in);
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("1096631451357-nm97s6b45hllm30r8ij6935bdg40seii.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        // [END config_signin]

        // [START initialize_auth]
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]
        signInButton.setOnClickListener(v -> {
            googleIconSpinner googleIconSpinner = new googleIconSpinner();
            googleIconSpinner.googleIconSpinnerStart();
            signIn();
            progressBar.setVisibility(View.VISIBLE);
        });
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
    }

    // [END on_start_check_user]

    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                progressBar.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(),
                        "Sign in failed.Please be sure to turn on internet", Toast.LENGTH_LONG).show();

            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText(getApplicationContext(),
                                "Signed in successfully", Toast.LENGTH_SHORT).show();

                        Intent mainActivityIntent = new Intent(SignInActivity.this,
                                MainActivity.class);
                        startActivity(mainActivityIntent);


                    } else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(getApplicationContext(),
                                "Sign in failed. Be sure to turn on internet connection", Toast.LENGTH_LONG).show();
                    }
                });
    }

    // [END auth_with_google]

    // [START sign in]

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);

    }
    // [END signing]

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> SignInActivity.super.onBackPressed())
                .setNegativeButton("No", null)
                .show();
    }

    private class googleIconSpinner {
        @SuppressLint("Recycle")
        ObjectAnimator rotateAnimation = ObjectAnimator.ofFloat(googleIcon, "rotation", 0, 360);

        private void googleIconSpinnerStart() {
            this.rotateAnimation.start();
            this.rotateAnimation.setDuration(1000);
            this.rotateAnimation.setRepeatCount(15);
        }

    }

}

